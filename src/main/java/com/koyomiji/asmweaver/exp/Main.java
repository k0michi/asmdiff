package com.koyomiji.asmweaver.exp;

import aspa.core.Stream;
import aspa.jvm.ClassFile;
import com.koyomiji.asmweaver.ClassDiff;
import com.koyomiji.asmweaver.ClassDiffUtils;
import com.koyomiji.asmweaver.exp.log.Logger;
import com.koyomiji.asmweaver.io.BinaryReader;
import com.koyomiji.asmweaver.io.BinaryWriter;
import com.koyomiji.asmweaver.io.TextWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
  // =========================================================================
  // 【開発者用設定領域】 実験に関する設定はすべてここに記述します
  // =========================================================================
  private static final String EVACUATE_ROOT_DIR = "./compiled";

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
    File repoDir = new File(CONFIG.getRepoPath());
    if (!repoDir.exists() || !repoDir.isDirectory()) {
      Logger.error("エラー: 設定された REPO_PATH が存在しないか、ディレクトリではありません。: " + CONFIG.getRepoPath());
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
      originalHead = executeCommand(repoDir, "git", "rev-parse", "--abbrev-ref", "HEAD");
      if ("HEAD".equals(originalHead)) {
        originalHead = executeCommand(repoDir, "git", "rev-parse", "HEAD");
      }
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
        // 💡 【修正】CSVヘッダーに SrcSizeBytesBefore と SrcSizeBytesAfter を追加
        csvWriter.println("CommitHash,Describe,ClassPath,MethodName,SrcSizeBytesBefore,SrcSizeBytesAfter,DurationNs,DiffSizeBytes,AsmTreeMatch,Status");
      }

      List<String> commitHistory = getCommitHistory(repoDir, CONFIG.getStartCommit(), CONFIG.getMaxHistory());
      Logger.info(commitHistory.size() + " 件のコミットを解析対象として抽出しました。");

      for (int i = 0; i < commitHistory.size(); i++) {
        String commitX = commitHistory.get(i);

        String commitDesc = getCommitDescribe(repoDir, commitX);
        String commitMsg = getCommitMessage(repoDir, commitX);

        Logger.infof("[%d/%d] ----------------------------------------", i + 1, commitHistory.size());
        Logger.infof("  コミット: %s (%s)", commitX, commitDesc);
        Logger.infof("  概要    : %s", commitMsg);

        try {
          String commitY = executeCommand(repoDir, "git", "rev-parse", commitX + "~1");

          if (CONFIG.isFilterBySrcDir() && !hasChangesInDirectory(repoDir, commitY, commitX, CONFIG.getSourceDir())) {
            Logger.info("  -> [SKIP] 指定されたソースディレクトリ (" + CONFIG.getSourceDir() + ") 内に変更がないためスキップします。");
            continue;
          }

          Path tempDirX = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName(), commitX);
          if (!Files.exists(tempDirX)) {
            Logger.info("  -> コミット X_i (" + commitX + ") をビルド中...");
            executeCommand(repoDir, "git", "checkout", "-f", commitX);
            clearOutputDirectory(repoDir, CONFIG.getClassDir());
            executeCommand(repoDir, CONFIG.getBuildCommand());
            tempDirX = evacuateClassFiles(repoDir, commitX);
          }

          Path tempDirY = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName(), commitY);
          if (!Files.exists(tempDirY)) {
            Logger.info("  -> コミット X_i-1 (" + commitY + ") をビルド中...");
            executeCommand(repoDir, "git", "checkout", "-f", commitY);
            clearOutputDirectory(repoDir, CONFIG.getClassDir());
            executeCommand(repoDir, CONFIG.getBuildCommand());
            tempDirY = evacuateClassFiles(repoDir, commitY);
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
            byte[] bytesBefore = Files.readAllBytes(classFileY); // 変更前（X_{i-1}）のバイト配列
            byte[] bytesAfter = Files.readAllBytes(classFileX);   // 変更後（X_i）のバイト配列

            for (BytecodeDiffMethod method : DIFF_METHODS) {
              try {
                MeasurementResult result = measurePerformance(method, bytesBefore, bytesAfter);

                // 💡 【修正】CSVに元のファイルの容量（前、後）を書き出し
                csvWriter.printf("%s,\"%s\",%s,%s,%d,%d,%d,%d,%b,%s\n",
                        commitX, commitDesc, relativeClassPath, method.getName(),
                        bytesBefore.length, bytesAfter.length,
                        result.medianDurationNs, result.diffSizeBytes, result.isAsmMatched, "SUCCESS");

                // 💡 【修正】コンソール出力ログに元のサイズ情報を視覚的に追加
                Logger.infof("     [%s] 元サイズ(前/後): %d/%d bytes, 時間(中央値): %d ns, パッチ: %d bytes, ASM一致: %b",
                        method.getName(), bytesBefore.length, bytesAfter.length, result.medianDurationNs, result.diffSizeBytes, result.isAsmMatched);

              } catch (Exception e) {
                // 💡 【修正】メソッド実行エラー時もCSVの列数がズレないようにダミーサイズ(Before/Afterの長さ)を保持
                csvWriter.printf("%s,\"%s\",%s,%s,%d,%d,0,0,false,ERROR_%s\n",
                        commitX, commitDesc, relativeClassPath, method.getName(),
                        bytesBefore.length, bytesAfter.length, e.getClass().getSimpleName());
                e.printStackTrace();
              }
            }
          }

        } catch (Exception e) {
          Logger.errorf("  -> コミット %s の解析中にエラーが発生しました: %s", commitX, e.getMessage());
          // 💡 【修正】コミット全体のエラー時もCSVの列数（計10列）を維持するためダミーの0を2つ追加
          csvWriter.printf("%s,\"%s\",NONE,NONE,0,0,0,0,false,ERROR_COMMIT_%s\n",
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
        executeCommand(repoDir, "git", "checkout", "-f", originalHead);
      } catch (Exception e) {
        Logger.error("警告: 元のブランチの復元に失敗しました: " + e.getMessage());
      }
    }
  }

  private static MeasurementResult measurePerformance(BytecodeDiffMethod method, byte[] bytesBefore, byte[] bytesAfter) throws Exception {
    for (int i = 0; i < WARMUP_ITERATIONS; i++) {
      method.computeDiff(bytesBefore, bytesAfter);
    }

    List<Long> durations = new ArrayList<>(MEASUREMENT_ITERATIONS);
    byte[] finalPatch = null;

    for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
      long startTime = System.nanoTime();
      byte[] patch = method.computeDiff(bytesBefore, bytesAfter);
      long endTime = System.nanoTime();

      durations.add(endTime - startTime);

      if (i == MEASUREMENT_ITERATIONS - 1) {
        finalPatch = patch;
      }
    }

    Collections.sort(durations);
    long medianDurationNs = durations.get(durations.size() / 2);
    long diffSizeBytes = (finalPatch != null) ? finalPatch.length : 0;

    byte[] restoredBytes = method.applyPatch(bytesBefore, finalPatch);
    boolean isAsmMatched = compareAsmTree(bytesAfter, restoredBytes);

    return new MeasurementResult(medianDurationNs, diffSizeBytes, isAsmMatched);
  }

  private static class MeasurementResult {
    final long medianDurationNs;
    final long diffSizeBytes;
    final boolean isAsmMatched;

    MeasurementResult(long medianDurationNs, long diffSizeBytes, boolean isAsmMatched) {
      this.medianDurationNs = medianDurationNs;
      this.diffSizeBytes = diffSizeBytes;
      this.isAsmMatched = isAsmMatched;
    }
  }

  private static boolean hasChangesInDirectory(File repoDir, String commitY, String commitX, String targetDir) throws IOException {
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

  private static void clearOutputDirectory(File repoDir, String relativeClassDir) {
    clearOutputDirectory(repoDir.toPath().resolve(relativeClassDir));
  }

  private static Path evacuateClassFiles(File repoDir, String commitHash) throws IOException {
    Path targetDir = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName(), commitHash);
    if (Files.exists(targetDir)) {
      return targetDir;
    }
    Files.createDirectories(targetDir);

    Path sourceDir = repoDir.toPath().resolve(CONFIG.getClassDir());
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
    // 依存関係（org.objectweb.asm.tree.ClassNode / Sort）を想定した既存ロジック
    ClassNode classNode = new ClassNode();
    ClassReader cr = new ClassReader(classBytes);
    cr.accept(classNode, 0);
    Sort.sortMembers(classNode);

    StringWriter sw = new StringWriter();
    TraceClassVisitor tcv = new TraceClassVisitor(null, new Textifier(), new PrintWriter(sw));
    classNode.accept(tcv);
    return sw.toString();
  }

  private static String executeCommand(File directory, String... command) throws Exception {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(directory);
    pb.redirectErrorStream(true);
    pb.environment().putAll(CONFIG.getEnv());
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

  private static List<String> getCommitHistory(File repoDir, String startCommit, int maxCount) throws IOException {
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

  private static String getCommitDescribe(File repoDir, String commitHash) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("git", "describe", "--tags", "--always", commitHash);
    pb.directory(repoDir);
    Process p = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line = reader.readLine();
      return (line != null) ? line.trim() : "unknown";
    }
  }

  private static String getCommitMessage(File repoDir, String commitHash) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("git", "log", "-1", "--format=%s", commitHash);
    pb.directory(repoDir);
    Process p = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line = reader.readLine();
      return (line != null) ? line.trim() : "No message";
    }
  }
}

interface BytecodeDiffMethod {
  String getName();

  byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception;

  byte[] applyPatch(byte[] classBefore, byte[] patch) throws Exception;
}

class DiffMethodASMWeaver implements BytecodeDiffMethod {
  @Override
  public String getName() {
    return "ASMWeaver";
  }

  @Override
  public byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception {
    ClassNode classNodeBefore = new ClassNode();
    ClassNode classNodeAfter = new ClassNode();
    ClassReader classReaderBefore = new ClassReader(classBefore);
    classReaderBefore.accept(classNodeBefore, 0);
    ClassReader classReaderAfter = new ClassReader(classAfter);
    classReaderAfter.accept(classNodeAfter, 0);

    ClassDiff diff = ClassDiffUtils.diff(classNodeBefore, classNodeAfter);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ClassDiffUtils.write(diff, new BinaryWriter(baos));
    return baos.toByteArray();
  }

  @Override
  public byte[] applyPatch(byte[] classBefore, byte[] patch) throws Exception {
    ClassNode classNodeBefore = new ClassNode();
    ClassReader classReader = new ClassReader(classBefore);
    classReader.accept(classNodeBefore, 0);

    ByteArrayInputStream bais = new ByteArrayInputStream(patch);
    ClassDiff diff = ClassDiffUtils.read(new BinaryReader(bais));
    ClassNode patchedNode = ClassDiffUtils.patch(classNodeBefore, diff);
    ClassWriter classWriter = new ClassWriter(0);
    patchedNode.accept(classWriter);
    return classWriter.toByteArray();
  }
}

class DiffMethodAspa implements BytecodeDiffMethod {
  @Override
  public String getName() {
    return "Aspa";
  }

  @Override
  public byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception {
    {
      // From aspa.jvm.ClassFile

      // FIXME: IO
      String fSrc = Files.createTempFile("aspa_src_", ".class").toString();
      String fTgt = Files.createTempFile("aspa_tgt_", ".class").toString();
      String fDiff = Files.createTempFile("aspa_diff_", ".bin").toString();
      Files.write(Paths.get(fSrc), classBefore);
      Files.write(Paths.get(fTgt), classAfter);
      ClassFile cSrc = new ClassFile(fSrc);
      ClassFile cTgt = new ClassFile(fTgt);
      Stream diff = new Stream(fDiff, "rw");
      cTgt.diff(cSrc, diff, cTgt.getPool());
      diff.close();
      File fObj = new File(fDiff);
      long patchLength = fObj.length();
      if (patchLength == 0L) {
        fObj.delete();
//        System.err.printf("No differences found. Patch file '%s' not generated.\n", fDiff);
      } else {
//        System.err.printf("Patch '%s' generated (%s bytes).\n", fDiff, patchLength);
      }

      return Files.readAllBytes(Paths.get(fDiff));
    }
  }

  @Override
  public byte[] applyPatch(byte[] classBefore, byte[] patch) throws Exception {
    // From aspa.jvm.ClassFile

    String fSrc = Files.createTempFile("aspa_patch_src_", ".class").toString();
    Files.write(Paths.get(fSrc), classBefore);
    String fDiff = Files.createTempFile("aspa_patch_diff_", ".bin").toString();
    Files.write(Paths.get(fDiff), patch);
    String fTgt = Files.createTempFile("aspa_patch_tgt_", ".class").toString();
    ClassFile c = new ClassFile(fSrc);
    Stream diff = new Stream(fDiff, "r");
    c.patch(diff, c.getPool());
    Stream out = new Stream(fTgt, "rw");
    c.write(out, null);
    diff.close();
    out.close();
    System.err.printf("JVM class file '%s' generated (%s bytes)\n", fTgt, new File(fTgt).length());
    return Files.readAllBytes(Paths.get(fTgt));
  }
}

class BsdiffMethod implements BytecodeDiffMethod {
  @Override
  public String getName() {
    return "Bsdiff";
  }

  @Override
  public byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception {
    Path before = Files.createTempFile("bsdiff_b_", ".class");
    Path after = Files.createTempFile("bsdiff_a_", ".class");
    Path patch = Files.createTempFile("bsdiff_p_", ".bin");
    try {
      Files.write(before, classBefore);
      Files.write(after, classAfter);

      // bsdiff <before> <after> <patch>
      ProcessUtils.runProcess(before.getParent().toFile(), "bsdiff", before.toString(), after.toString(), patch.toString());
      return Files.readAllBytes(patch);
    } finally {
      Files.deleteIfExists(before);
      Files.deleteIfExists(after);
      Files.deleteIfExists(patch);
    }
  }

  @Override
  public byte[] applyPatch(byte[] classBefore, byte[] patchData) throws Exception {
    Path before = Files.createTempFile("bsdiff_b_", ".class");
    Path patch = Files.createTempFile("bsdiff_p_", ".bin");
    Path restored = Files.createTempFile("bsdiff_r_", ".class");
    try {
      Files.write(before, classBefore);
      Files.write(patch, patchData);

      // bspatch <before> <restored> <patch>
      ProcessUtils.runProcess(before.getParent().toFile(), "bspatch", before.toString(), restored.toString(), patch.toString());
      return Files.readAllBytes(restored);
    } finally {
      Files.deleteIfExists(before);
      Files.deleteIfExists(patch);
      Files.deleteIfExists(restored);
    }
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
  private final boolean filterBySrcDir; // 💡 追加

  private final String[] buildCommand;
  private final String sourceDir;
  private final String classDir;

  private final Map<String, String> env;

  private final List<PathMatcher> includeMatchers = new ArrayList<>();
  private final List<PathMatcher> excludeMatchers = new ArrayList<>();

  public RepositoryConfig(String repoPath, String startCommit, int maxHistory, boolean filterBySrcDir, // 💡 引数に追加
                          String[] buildCommand, String sourceDir, String classDir,
                          Map<String, String> env,
                          String[] includes, String[] excludes) {
    this.repoPath = repoPath;
    this.startCommit = startCommit;
    this.maxHistory = maxHistory;
    this.filterBySrcDir = filterBySrcDir; // 💡 保持
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

  // ゲッター群
  public String getRepoPath() {
    return repoPath;
  }

  public String getRepoName() {
    return repoName;
  }

  public String getStartCommit() {
    return startCommit;
  }

  public String getCsvPath() {
    return csvPath;
  }

  public int getMaxHistory() {
    return maxHistory;
  }

  public boolean isFilterBySrcDir() {
    return filterBySrcDir;
  } // 💡 追加

  public String[] getBuildCommand() {
    return buildCommand;
  }

  public String getSourceDir() {
    return sourceDir;
  }

  public String getClassDir() {
    return classDir;
  }

  public Map<String, String> getEnv() {
    return env;
  }
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