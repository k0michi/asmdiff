package com.koyomiji.asmweaver.exp;

import aspa.core.Stream;
import aspa.jvm.ClassFile;
import com.koyomiji.asmweaver.ClassDiff;
import com.koyomiji.asmweaver.ClassDiffUtils;
import com.koyomiji.asmweaver.exp.log.Logger;
import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
  // =========================================================================
  // 【開発者用設定領域】 実験に関する設定はすべてここに記述します
  // =========================================================================
  private static final String EVACUATE_ROOT_DIR = "./compiled";

  public static final String RAM_DISK_DIR = "/Volumes/RAMDisk";

  private static final int WARMUP_ITERATIONS = 10;
  private static final int MEASUREMENT_ITERATIONS = 11;

  private static final RepositoryConfig CONFIG = new RepositoryConfig(
          "/Users/k0michi/Local/DevelopmentResearch/asmweaver-exp/gson",
          "gson-parent-2.11.0",
          100,
          true,
          new String[]{"mvn", "compile"},
          "gson/src/main/java/",
          "gson/target/classes/",
          Map.of(
                  "JAVA_HOME", "/Users/k0michi/Local/DevelopmentResearch/asmweaver-exp/jdk-17.0.19+10/Contents/Home"
          ),
          new String[]{
                  "**/com/google/gson/**/*.class"
          },
          new String[]{}
  );

  private static final List<BytecodeDiffMethod> DIFF_METHODS = Arrays.asList(
          new DiffMethodASMWeaver(),
          new DiffMethodAspa(),
          new BsdiffMethod()
  );
  // =========================================================================

  public static void main(String[] args) {
    // 💡 Git操作を担当するリポジトリコンテキストをインスタンス化
    GitRepository repo = new GitRepository(CONFIG);

    if (!repo.exists()) {
      Logger.error("エラー: 設定された REPO_PATH が存在しないか、ディレクトリではありません。: " + CONFIG.getRepoPath());
      return;
    }

    try {
      Files.createDirectories(Paths.get(RAM_DISK_DIR));
    } catch (IOException e) {
      Logger.error("RAMディスク用フォルダの作成に失敗しました: " + e.getMessage());
      return;
    }

    Path finalEvacuateLogPath = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName());

    Logger.info("=== 実験ベンチマークランナー ===");
    Logger.info("対象リポジトリ  : " + CONFIG.getRepoPath());
    Logger.info("解析開始の始点  : " + CONFIG.getStartCommit());
    Logger.info("自動生成されたCSV: " + CONFIG.getCsvPath());
    Logger.info("クラスファイル退避先: " + finalEvacuateLogPath);
    Logger.info("--------------------------------");

    String originalHead = null;
    try {
      originalHead = repo.resolveHead(); // 💡 引っ越し先から呼び出し
    } catch (Exception e) {
      Logger.error("初期状態の取得に失敗しました。実験を中断します: " + e.getMessage());
      return;
    }

    try (
            FileWriter fw = new FileWriter(CONFIG.getCsvPath(), StandardCharsets.UTF_8, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter csvWriter = new PrintWriter(bw, true)
    ) {
      if (new File(CONFIG.getCsvPath()).length() == 0) {
        csvWriter.println("CommitHash,Describe,ClassPath,MethodName,SrcSizeBytesBefore,SrcSizeBytesAfter,DiffDurationNs,PatchDurationNs,DiffSizeBytes,AsmTreeMatch,Status");
      }

      List<String> commitHistory = repo.getCommitHistory(CONFIG.getStartCommit(), CONFIG.getMaxHistory()); // 💡 委譲
      Logger.info(commitHistory.size() + " 件のコミットを解析対象として抽出しました。");

      for (int i = 0; i < commitHistory.size(); i++) {
        String commitX = commitHistory.get(i);

        String commitDesc = repo.getCommitDescribe(commitX); // 💡 委譲
        String commitMsg = repo.getCommitMessage(commitX);   // 💡 委譲

        Logger.infof("[%d/%d] ----------------------------------------", i + 1, commitHistory.size());
        Logger.infof("  コミット: %s (%s)", commitX, commitDesc);
        Logger.infof("  概要    : %s", commitMsg);

        try {
          String commitY = repo.resolveRev(commitX + "~1"); // 💡 委譲

          if (CONFIG.isFilterBySrcDir() && !repo.hasChangesInDirectory(commitY, commitX, CONFIG.getSourceDir())) { // 💡 委譲
            Logger.info("  -> [SKIP] 指定されたソースディレクトリ (" + CONFIG.getSourceDir() + ") 内に変更がないためスキップします。");
            continue;
          }

          Path tempDirX = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName(), commitX);
          if (!Files.exists(tempDirX)) {
            Logger.info("  -> コミット X_i (" + commitX + ") をビルド中...");
            repo.checkout(commitX); // 💡 委譲
            clearOutputDirectory(Paths.get(CONFIG.getRepoPath()).resolve(CONFIG.getClassDir()));
            repo.build();           // 💡 委譲
            tempDirX = repo.evacuateClassFiles(commitX, EVACUATE_ROOT_DIR); // 💡 委譲
          }

          Path tempDirY = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName(), commitY);
          if (!Files.exists(tempDirY)) {
            Logger.info("  -> コミット X_i-1 (" + commitY + ") をビルド中...");
            repo.checkout(commitY); // 💡 委譲
            clearOutputDirectory(Paths.get(CONFIG.getRepoPath()).resolve(CONFIG.getClassDir()));
            repo.build();           // 💡 委譲
            tempDirY = repo.evacuateClassFiles(commitY, EVACUATE_ROOT_DIR); // 💡 委譲
          }

          List<String> modifiedClassPaths = getModifiedClassFiles(tempDirX, tempDirY);

          if (modifiedClassPaths.isEmpty()) {
            Logger.info("  -> [SKIP] ビルド成果物に対象となるクラスの変更（修正）がありません。");
            continue;
          }

          for (String relativeClassPath : modifiedClassPaths) {
            Path classFileX = tempDirX.resolve(relativeClassPath);
            Path classFileY = tempDirY.resolve(relativeClassPath);

            Logger.info("  -> クラス解析中: " + relativeClassPath);
            byte[] bytesBefore = Files.readAllBytes(classFileY);
            byte[] bytesAfter = Files.readAllBytes(classFileX);

            for (BytecodeDiffMethod method : DIFF_METHODS) {
              try {
                MeasurementResult result = measurePerformance(method, bytesBefore, bytesAfter);

                csvWriter.printf("%s,\"%s\",%s,%s,%d,%d,%d,%d,%d,%b,%s\n",
                        commitX, commitDesc, relativeClassPath, method.getName(),
                        bytesBefore.length, bytesAfter.length,
                        result.medianDiffDurationNs, result.medianPatchDurationNs, result.diffSizeBytes, result.isAsmMatched, "SUCCESS");

                Logger.infof("     [%s] 元サイズ(前/後): %d/%d bytes, Diff時間(中央値): %d ns, Patch時間(中央値): %d ns, パッチ: %d bytes, ASM一致: %b",
                        method.getName(), bytesBefore.length, bytesAfter.length, result.medianDiffDurationNs, result.medianPatchDurationNs, result.diffSizeBytes, result.isAsmMatched);

              } catch (Exception e) {
                csvWriter.printf("%s,\"%s\",%s,%s,%d,%d,0,0,0,false,ERROR_%s\n",
                        commitX, commitDesc, relativeClassPath, method.getName(),
                        bytesBefore.length, bytesAfter.length, e.getClass().getSimpleName());
                e.printStackTrace();
              }
            }
          }

        } catch (Exception e) {
          Logger.errorf("  -> コミット %s の解析中にエラーが発生しました: %s", commitX, e.getMessage());
          csvWriter.printf("%s,\"%s\",NONE,NONE,0,0,0,0,0,false,ERROR_COMMIT_%s\n",
                  commitX, commitDesc, e.getClass().getSimpleName());
        }
      }

      Logger.info("----------------------------------------");
      Logger.info("実験ベンチマークがすべて正常に完了しました。");

    } catch (IOException e) {
      Logger.error("CSVファイルの制御で致命的なエラーが発生しました: " + e.getMessage());
    } finally {
      try {
        Logger.info("リポジトリのコミット状態を元に戻しています...");
        repo.checkout(originalHead); // 💡 委譲
      } catch (Exception e) {
        Logger.error("警告: 元のブランチの復元に失敗しました: " + e.getMessage());
      }
    }
  }

  private static void clearRamDisk() {
    Path ramDiskPath = Paths.get(RAM_DISK_DIR);
    if (!Files.exists(ramDiskPath)) return;
    try {
      try (var stream = Files.walk(ramDiskPath)) {
        stream.sorted(Comparator.reverseOrder())
                .filter(p -> !p.equals(ramDiskPath))
                .map(Path::toFile)
                .forEach(File::delete);
      }
    } catch (IOException e) {
      Logger.error("警告: RAMディスクのクレンジングに失敗しました: " + e.getMessage());
    }
  }

  private static MeasurementResult measurePerformance(BytecodeDiffMethod method, byte[] bytesBefore, byte[] bytesAfter) throws Exception {
    for (int i = 0; i < WARMUP_ITERATIONS; i++) {
      clearRamDisk();
      byte[] patch = method.computeDiff(bytesBefore, bytesAfter);
      clearRamDisk();
      method.applyPatch(bytesBefore, patch);
    }

    List<Long> diffDurations = new ArrayList<>(MEASUREMENT_ITERATIONS);
    List<Long> patchDurations = new ArrayList<>(MEASUREMENT_ITERATIONS);
    byte[] finalPatch = null;
    byte[] finalRestoredBytes = null;

    for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
      clearRamDisk();
      long startDiff = System.nanoTime();
      byte[] patch = method.computeDiff(bytesBefore, bytesAfter);
      long endDiff = System.nanoTime();
      diffDurations.add(endDiff - startDiff);

      clearRamDisk();
      long startPatch = System.nanoTime();
      byte[] restored = method.applyPatch(bytesBefore, patch);
      long endPatch = System.nanoTime();
      patchDurations.add(endPatch - startPatch);

      if (i == MEASUREMENT_ITERATIONS - 1) {
        finalPatch = patch;
        finalRestoredBytes = restored;
      }
    }

    Collections.sort(diffDurations);
    Collections.sort(patchDurations);
    long medianDiffDurationNs = diffDurations.get(diffDurations.size() / 2);
    long medianPatchDurationNs = patchDurations.get(patchDurations.size() / 2);

    long diffSizeBytes = (finalPatch != null) ? finalPatch.length : 0;
    boolean isAsmMatched = compareAsmTree(bytesAfter, finalRestoredBytes);

    return new MeasurementResult(medianDiffDurationNs, medianPatchDurationNs, diffSizeBytes, isAsmMatched);
  }

  private static class MeasurementResult {
    final long medianDiffDurationNs;
    final long medianPatchDurationNs;
    final long diffSizeBytes;
    final boolean isAsmMatched;

    MeasurementResult(long medianDiffDurationNs, long medianPatchDurationNs, long diffSizeBytes, boolean isAsmMatched) {
      this.medianDiffDurationNs = medianDiffDurationNs;
      this.medianPatchDurationNs = medianPatchDurationNs;
      this.diffSizeBytes = diffSizeBytes;
      this.isAsmMatched = isAsmMatched;
    }
  }

  private static List<String> getModifiedClassFiles(Path dirX, Path dirY) throws IOException {
    List<String> modifiedClasses = new ArrayList<>();
    if (!Files.exists(dirX) || !Files.exists(dirY)) return modifiedClasses;

    try (var stream = Files.walk(dirX)) {
      List<Path> allFilesX = stream.filter(Files::isRegularFile).toList();
      for (Path fileX : allFilesX) {
        Path relativePath = dirX.relativize(fileX);
        String relPathStr = relativePath.toString().replace("\\", "/");

        if (!relPathStr.endsWith(".class")) continue;
        if (!CONFIG.isTargetFile(fileX)) continue;

        Path fileY = dirY.resolve(relativePath);
        if (Files.exists(fileY)) {
          byte[] bytesX = Files.readAllBytes(fileX);
          byte[] bytesY = Files.readAllBytes(fileY);

          if (!Arrays.equals(bytesX, bytesY)) {
            modifiedClasses.add(relPathStr);
          }
        }
      }
    }
    return modifiedClasses;
  }

  private static void clearOutputDirectory(Path targetPath) {
    if (!Files.exists(targetPath)) return;
    try {
      try (var stream = Files.walk(targetPath)) {
        stream.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
      }
      Files.createDirectories(targetPath);
    } catch (IOException e) {
      Logger.error("警告: ビルド前ディレクトリのクレンジングに失敗しました: " + e.getMessage());
    }
  }

  private static boolean compareAsmTree(byte[] original, byte[] restored) {
    if (original == null || restored == null) return false;
    try {
      String orgText = getAsmStructureText(original);
      String resText = getAsmStructureText(restored);
      if (!orgText.equals(resText)) {
        Logger.info("--- Original ASM Structure ---");
        Logger.info(orgText);
        Logger.info("--- Restored ASM Structure ---");
        Logger.info(resText);
        Logger.info("--- End of ASM Structures ---");
      }
      return orgText.equals(resText);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String getAsmStructureText(byte[] classBytes) {
    ClassNode classNode = new ClassNode();
    ClassReader cr = new ClassReader(classBytes);
    cr.accept(classNode, 0);
    Sort.sortMembers(classNode);

    StringWriter sw = new StringWriter();
    TraceClassVisitor tcv = new TraceClassVisitor(null, new Textifier(), new PrintWriter(sw));
    classNode.accept(tcv);
    return sw.toString();
  }
}

class GitRepository {
  private final File repoDir;
  private final RepositoryConfig config;

  public GitRepository(RepositoryConfig config) {
    this.config = config;
    this.repoDir = new File(config.getRepoPath());
  }

  public boolean exists() {
    return repoDir.exists() && repoDir.isDirectory();
  }

  public String resolveHead() throws Exception {
    String originalHead = executeCommand("git", "rev-parse", "--abbrev-ref", "HEAD");
    if ("HEAD".equals(originalHead)) {
      originalHead = executeCommand("git", "rev-parse", "HEAD");
    }
    return originalHead;
  }

  public String resolveRev(String revisionExpression) throws Exception {
    return executeCommand("git", "rev-parse", revisionExpression);
  }

  public void checkout(String commitOrBranch) throws Exception {
    executeCommand("git", "checkout", "-f", commitOrBranch);
  }

  public void build() throws Exception {
    executeCommand(config.getBuildCommand());
  }

  public boolean hasChangesInDirectory(String commitY, String commitX, String targetDir) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("git", "diff", "--name-only", commitY, commitX);
    pb.directory(repoDir);
    Process p = pb.start();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().startsWith(targetDir)) {
          return true;
        }
      }
    }
    return false;
  }

  public List<String> getCommitHistory(String startCommit, int maxCount) throws IOException {
    List<String> commits = new ArrayList<>();
    ProcessBuilder pb = new ProcessBuilder("git", "log", "--format=%H", "--first-parent", "-n", String.valueOf(maxCount), startCommit);
    pb.directory(repoDir);
    Process p = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String hash = line.trim();
        if (!hash.isEmpty()) {
          commits.add(hash);
        }
      }
    }
    return commits;
  }

  public String getCommitDescribe(String commitHash) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("git", "describe", "--tags", "--always", commitHash);
    pb.directory(repoDir);
    Process p = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line = reader.readLine();
      return (line != null) ? line.trim() : "unknown";
    }
  }

  public String getCommitMessage(String commitHash) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("git", "log", "-1", "--format=%s", commitHash);
    pb.directory(repoDir);
    Process p = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line = reader.readLine();
      return (line != null) ? line.trim() : "No message";
    }
  }

  public Path evacuateClassFiles(String commitHash, String evacuateRootDir) throws IOException {
    Path targetDir = Paths.get(evacuateRootDir, config.getRepoName(), commitHash);
    if (Files.exists(targetDir)) {
      return targetDir;
    }
    Files.createDirectories(targetDir);

    Path sourceDir = repoDir.toPath().resolve(config.getClassDir());
    if (!Files.exists(sourceDir)) {
      return targetDir;
    }

    try (var stream = Files.walk(sourceDir)) {
      List<Path> sourcePaths = stream.toList();
      for (Path source : sourcePaths) {
        Path relative = sourceDir.relativize(source);
        Path dest = targetDir.resolve(relative);

        if (Files.isDirectory(source)) {
          Files.createDirectories(dest);
        } else {
          Files.createDirectories(dest.getParent());
          Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
    return targetDir;
  }

  private String executeCommand(String... command) throws Exception {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(repoDir);
    pb.redirectErrorStream(true);
    pb.environment().putAll(config.getEnv());
    Process p = pb.start();

    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    }
    int exitCode = p.waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("コマンドが異常終了しました (ExitCode: " + exitCode + "). 出力:\n" + output);
    }
    return output.toString().trim();
  }
}

interface BytecodeDiffMethod {
  String getName();
  byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception;
  byte[] applyPatch(byte[] classBefore, byte[] patch) throws Exception;
}

class DiffMethodASMWeaver implements BytecodeDiffMethod {
  @Override public String getName() { return "ASMWeaver"; }

  @Override
  public byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    Path beforeFile = Files.createTempFile(ramPath, "asmw_b_", ".class");
    Path afterFile  = Files.createTempFile(ramPath, "asmw_a_", ".class");
    Path patchFile  = Files.createTempFile(ramPath, "asmw_p_", ".bin");

    // 💡 削除を伴う try-finally 構造を全廃
    Files.write(beforeFile, classBefore);
    Files.write(afterFile, classAfter);

    byte[] readBefore = Files.readAllBytes(beforeFile);
    byte[] readAfter  = Files.readAllBytes(afterFile);

    ClassNode classNodeBefore = new ClassNode();
    ClassNode classNodeAfter = new ClassNode();
    new ClassReader(readBefore).accept(classNodeBefore, 0);
    new ClassReader(readAfter).accept(classNodeAfter, 0);

    ClassDiff diff = ClassDiffUtils.diff(classNodeBefore, classNodeAfter);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ClassDiffUtils.write(diff, new BinaryWriter(baos));

    Files.write(patchFile, baos.toByteArray());
    return Files.readAllBytes(patchFile);
  }

  @Override
  public byte[] applyPatch(byte[] classBefore, byte[] patch) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    Path beforeFile = Files.createTempFile(ramPath, "asmw_patch_b_", ".class");
    Path patchFile  = Files.createTempFile(ramPath, "asmw_patch_p_", ".bin");
    Path restoredFile = Files.createTempFile(ramPath, "asmw_patch_r_", ".class");

    Files.write(beforeFile, classBefore);
    Files.write(patchFile, patch);

    ClassNode classNodeBefore = new ClassNode();
    new ClassReader(Files.readAllBytes(beforeFile)).accept(classNodeBefore, 0);

    ByteArrayInputStream bais = new ByteArrayInputStream(Files.readAllBytes(patchFile));
    ClassDiff diff = ClassDiffUtils.read(new BinaryReader(bais));
    ClassNode patchedNode = ClassDiffUtils.patch(classNodeBefore, diff);
    ClassWriter classWriter = new ClassWriter(0);
    patchedNode.accept(classWriter);

    Files.write(restoredFile, classWriter.toByteArray());
    return Files.readAllBytes(restoredFile);
  }
}

/**
 * 💡 2. Aspa方式（測定区間内からのdelete処理を完全排除）
 */
class DiffMethodAspa implements BytecodeDiffMethod {
  @Override public String getName() { return "Aspa"; }

  @Override
  public byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    String fSrc = Files.createTempFile(ramPath, "aspa_src_", ".class").toString();
    String fTgt = Files.createTempFile(ramPath, "aspa_tgt_", ".class").toString();
    String fDiff = Files.createTempFile(ramPath, "aspa_diff_", ".bin").toString();

    Files.write(Paths.get(fSrc), classBefore);
    Files.write(Paths.get(fTgt), classAfter);

    ClassFile cSrc = new ClassFile(fSrc);
    ClassFile cTgt = new ClassFile(fTgt);
    Stream diff = new Stream(fDiff, "rw");
    cTgt.diff(cSrc, diff, cTgt.getPool());
    diff.close();

    return Files.readAllBytes(Paths.get(fDiff));
  }

  @Override
  public byte[] applyPatch(byte[] classBefore, byte[] patch) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    String fSrc = Files.createTempFile(ramPath, "aspa_patch_src_", ".class").toString();
    String fDiff = Files.createTempFile(ramPath, "aspa_patch_diff_", ".bin").toString();
    String fTgt = Files.createTempFile(ramPath, "aspa_patch_tgt_", ".class").toString();

    Files.write(Paths.get(fSrc), classBefore);
    Files.write(Paths.get(fDiff), patch);

    ClassFile c = new ClassFile(fSrc);
    Stream diff = new Stream(fDiff, "r");
    c.patch(diff, c.getPool());
    Stream out = new Stream(fTgt, "rw");
    c.write(out, null);
    diff.close();
    out.close();

    return Files.readAllBytes(Paths.get(fTgt));
  }
}

/**
 * 💡 3. Bsdiff方式（測定区間内からのdelete処理を完全排除）
 */
class BsdiffMethod implements BytecodeDiffMethod {
  @Override public String getName() { return "Bsdiff"; }

  @Override
  public byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    Path before = Files.createTempFile(ramPath, "bsdiff_b_", ".class");
    Path after  = Files.createTempFile(ramPath, "bsdiff_a_", ".class");
    Path patch  = Files.createTempFile(ramPath, "bsdiff_p_", ".bin");

    Files.write(before, classBefore);
    Files.write(after, classAfter);

    ProcessUtils.runProcess(before.getParent().toFile(), "bsdiff", before.toString(), after.toString(), patch.toString());
    return Files.readAllBytes(patch);
  }

  @Override
  public byte[] applyPatch(byte[] classBefore, byte[] patchData) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    Path before   = Files.createTempFile(ramPath, "bsdiff_b_", ".class");
    Path patch    = Files.createTempFile(ramPath, "bsdiff_p_", ".bin");
    Path restored = Files.createTempFile(ramPath, "bsdiff_r_", ".class");

    Files.write(before, classBefore);
    Files.write(patch, patchData);

    ProcessUtils.runProcess(before.getParent().toFile(), "bspatch", before.toString(), restored.toString(), patch.toString());
    return Files.readAllBytes(restored);
  }
}

/**
 * リポジトリ固有の設定情報をカプセル化するクラス
 */
class RepositoryConfig {
  private final String repoPath;
  private final String repoName;
  private final String startCommit;
  private final String csvPath;
  private final int maxHistory;
  private final boolean filterBySrcDir;

  private final String[] buildCommand;
  private final String sourceDir;
  private final String classDir;

  private final Map<String, String> env;

  private final List<PathMatcher> includeMatchers = new ArrayList<>();
  private final List<PathMatcher> excludeMatchers = new ArrayList<>();

  public RepositoryConfig(String repoPath, String startCommit, int maxHistory, boolean filterBySrcDir,
                          String[] buildCommand, String sourceDir, String classDir,
                          Map<String, String> env,
                          String[] includes, String[] excludes) {
    this.repoPath = repoPath;
    this.startCommit = startCommit;
    this.maxHistory = maxHistory;
    this.filterBySrcDir = filterBySrcDir;
    this.buildCommand = buildCommand;
    this.sourceDir = sourceDir;
    this.classDir = classDir;

    this.env = env != null ? Collections.unmodifiableMap(new HashMap<>(env)) : Collections.emptyMap();
    this.repoName = Paths.get(repoPath).getFileName().toString();

    String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
    this.csvPath = "./benchmark_" + repoName + "_" + timestamp + ".csv";

    FileSystem fs = FileSystems.getDefault();
    for (String pattern : includes) {
      String prefix = pattern.startsWith("glob:") ? "" : "glob:";
      this.includeMatchers.add(fs.getPathMatcher(prefix + pattern));
    }
    for (String pattern : excludes) {
      String prefix = pattern.startsWith("glob:") ? "" : "glob:";
      this.excludeMatchers.add(fs.getPathMatcher(prefix + pattern));
    }
  }

  public boolean isTargetFile(Path absolutePath) {
    if (!includeMatchers.isEmpty()) {
      boolean matchInclude = false;
      for (PathMatcher matcher : includeMatchers) {
        if (matcher.matches(absolutePath)) {
          matchInclude = true;
          break;
        }
      }
      if (!matchInclude) return false;
    }

    for (PathMatcher matcher : excludeMatchers) {
      if (matcher.matches(absolutePath)) return false;
    }
    return true;
  }

  public String getRepoPath() { return repoPath; }
  public String getRepoName() { return repoName; }
  public String getStartCommit() { return startCommit; }
  public String getCsvPath() { return csvPath; }
  public int getMaxHistory() { return maxHistory; }
  public boolean isFilterBySrcDir() { return filterBySrcDir; }
  public String[] getBuildCommand() { return buildCommand; }
  public String getSourceDir() { return sourceDir; }
  public String getClassDir() { return classDir; }
  public Map<String, String> getEnv() { return env; }
}

class ProcessUtils {
  public static void runProcess(File workingDir, String... command) throws Exception {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(workingDir);
    Process p = pb.start();
    int exitCode = p.waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("Command failed: " + Arrays.toString(command) + " (Exit: " + exitCode + ")");
    }
  }
}