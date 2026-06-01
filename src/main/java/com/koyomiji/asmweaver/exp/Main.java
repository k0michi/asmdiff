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
import java.nio.file.FileSystem;
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
      originalHead = repo.resolveHead();

      List<String> commitHistory = repo.getCommitHistory(CONFIG.getStartCommit(), CONFIG.getMaxHistory());
      Logger.info(commitHistory.size() + " 件のコミットを解析対象として抽出しました。");

      // 💡 1つ目の実験：Diff/Patchベンチマーク実験の実行
      Logger.info("----------------------------------------");
      Logger.info(">>> Diff/Patchのベンチマーク実験を開始します");
      runDiffPatchExperiment(repo, commitHistory);

      // 💡 2つ目の実験：マージを伴うシナリオ実験の実行
//      Logger.info("----------------------------------------");
//      Logger.info(">>> マージシナリオの検証実験を開始します");
//      runMergeScenarioExperiment(repo, commitHistory);

      Logger.info("----------------------------------------");
      Logger.info("実験ベンチマークがすべて正常に完了しました。");

    } catch (Exception e) {
      Logger.error("致命的なエラーが発生しました: " + e.getMessage());
      e.printStackTrace();
    } finally {
      try {
        Logger.info("リポジトリのコミット状態を元に戻しています...");
        repo.checkout(originalHead);
      } catch (Exception e) {
        Logger.error("警告: 元のブランチの復元に失敗しました: " + e.getMessage());
      }
    }
  }

  private static void runDiffPatchExperiment(GitRepository repo, List<String> commitHistory) {
    // 💡 関数内部で独自のタイムスタンプとCSVパスを動的に算出
    String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
    String csvPath = "./benchmark_diffpatch_" + CONFIG.getRepoName() + "_" + timestamp + ".csv";
    Logger.info("Diff/Patch CSV出力先: " + csvPath);

    try (
            FileWriter fw = new FileWriter(csvPath, StandardCharsets.UTF_8, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter csvWriter = new PrintWriter(bw, true)
    ) {
      if (new File(csvPath).length() == 0) {
        csvWriter.println("CommitHash,Describe,ClassPath,MethodName,SrcSizeBytesBefore,SrcSizeBytesAfter,DiffDurationNs,PatchDurationNs,DiffSizeBytes,AsmTreeMatch,Status");
      }

      for (int i = 0; i < commitHistory.size(); i++) {
        String commitX = commitHistory.get(i);
        String commitDesc = repo.getCommitDescribe(commitX);
        String commitMsg = repo.getCommitMessage(commitX);

        Logger.infof("[%d/%d] ----------------------------------------", i + 1, commitHistory.size());
        Logger.infof("  コミット: %s (%s)", commitX, commitDesc);
        Logger.infof("  概要    : %s", commitMsg);

        try {
          String commitY = repo.resolveRev(commitX + "~1");

          if (CONFIG.isFilterBySrcDir() && !repo.hasChangesInDirectory(commitY, commitX, CONFIG.getSourceDir())) {
            Logger.info("  -> [SKIP] 指定されたソースディレクトリ (" + CONFIG.getSourceDir() + ") 内に変更がないためスキップします。");
            continue;
          }

          Path tempDirX = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName(), commitX);
          if (!Files.exists(tempDirX)) {
            Logger.info("  -> コミット X_i (" + commitX + ") をビルド中...");
            repo.checkout(commitX);
            clearOutputDirectory(Paths.get(CONFIG.getRepoPath()).resolve(CONFIG.getClassDir()));
            repo.build();
            tempDirX = repo.evacuateClassFiles(commitX, EVACUATE_ROOT_DIR);
          }

          Path tempDirY = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName(), commitY);
          if (!Files.exists(tempDirY)) {
            Logger.info("  -> コミット X_i-1 (" + commitY + ") をビルド中...");
            repo.checkout(commitY);
            clearOutputDirectory(Paths.get(CONFIG.getRepoPath()).resolve(CONFIG.getClassDir()));
            repo.build();
            tempDirY = repo.evacuateClassFiles(commitY, EVACUATE_ROOT_DIR);
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
//          csvWriter.printf("%s,\"%s\",NONE,NONE,0,0,0,0,0,false,ERROR_COMMIT_%s\n",
//                  commitX, commitDesc, e.getClass().getSimpleName());
        }
      }
    } catch (IOException e) {
      Logger.error("CSVファイルの制御で致命的なエラーが発生しました: " + e.getMessage());
    }
  }

  /**
   * 💡 【新規拡張】 マージ実験専用のCSVを関数内で算出して出力する機構を追加
   */
  private static void runMergeScenarioExperiment(GitRepository repo, List<String> commitHistory) {
    // 💡 関数内部でマージ実験専用のCSVパスを動的に算出
    String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
    String csvPath = "./benchmark_merge_" + CONFIG.getRepoName() + "_" + timestamp + ".csv";
    Logger.info("Merge CSV出力先: " + csvPath);

    try (
            FileWriter fw = new FileWriter(csvPath, StandardCharsets.UTF_8, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter csvWriter = new PrintWriter(bw, true)
    ) {
      if (new File(csvPath).length() == 0) {
        csvWriter.println("CommitHash,ClassPath,MethodName,AsmTreeMatch,Status");
      }

      for (int i = 0; i < commitHistory.size(); i++) {
        String commitXi = commitHistory.get(i);

        try {
          String commitXi_1 = repo.resolveRev(commitXi + "~1");

          if (CONFIG.isFilterBySrcDir() && !repo.hasChangesInDirectory(commitXi_1, commitXi, CONFIG.getSourceDir())) {
            continue;
          }

          Path tempDirXi = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName(), commitXi);
          Path tempDirXi_1 = Paths.get(EVACUATE_ROOT_DIR, CONFIG.getRepoName(), commitXi_1);

          List<String> modifiedClassPaths = getModifiedClassFiles(tempDirXi, tempDirXi_1);
          if (modifiedClassPaths.isEmpty()) continue;

          for (String relativeClassPath : modifiedClassPaths) {
            String classId = relativeClassPath.replace(".class", "").replace("/", "_");
            String relativeJavaPath = CONFIG.getSourceDir() + relativeClassPath.replace(".class", ".java");

            String commitXk = repo.findLastCommitModifyingFile(commitXi_1 + "~1", relativeJavaPath);
            if (commitXk == null) {
              Logger.infof("    -> [SKIP] %s の過去の変更コミット(Xk)が見つかりません。", relativeClassPath);
              continue;
            }

            repo.checkout(commitXi_1);
            boolean revertSuccess = repo.applyDiffBetweenCommits(commitXk, commitXk + "~1", relativeJavaPath);
            if (!revertSuccess) {
              Logger.infof("    -> [SKIP] %s に対する Xk (%s) の Revert パッチ適用に失敗しました。", relativeClassPath, commitXk);
              continue;
            }

            clearOutputDirectory(Paths.get(CONFIG.getRepoPath()).resolve(CONFIG.getClassDir()));
            repo.build();
            Path tempDirY = repo.evacuateClassFilesTo(commitXi + "_" + commitXk + "_" + classId + "_Y", Paths.get(EVACUATE_ROOT_DIR));

            boolean cpSuccess = repo.applyDiffBetweenCommits(commitXi_1, commitXi, relativeJavaPath);
            if (!cpSuccess) {
              Logger.infof("    -> [SKIP] %s に対する Xi (%s) の Cherry-pick パッチ適用に失敗しました。", relativeClassPath, commitXi);
              continue;
            }

            clearOutputDirectory(Paths.get(CONFIG.getRepoPath()).resolve(CONFIG.getClassDir()));
            repo.build();
            Path tempDirZ = repo.evacuateClassFilesTo(commitXi + "_" + commitXk + "_" + classId + "_Z", Paths.get(EVACUATE_ROOT_DIR));

            byte[] bytesY = Files.readAllBytes(tempDirY.resolve(relativeClassPath));
            byte[] bytesXi_1 = Files.readAllBytes(tempDirXi_1.resolve(relativeClassPath));
            byte[] bytesZ = Files.readAllBytes(tempDirZ.resolve(relativeClassPath));
            byte[] bytesXi = Files.readAllBytes(tempDirXi.resolve(relativeClassPath));

            for (BytecodeDiffMethod method : DIFF_METHODS) {
              try {
                clearRamDisk();
                byte[] p = method.computeMerge(bytesY, bytesXi_1, bytesZ);

                byte[] restoredBytes = method.applyPatch(bytesY, p);
                boolean isAsmMatched = compareAsmTree(bytesXi, restoredBytes);

                // 💡 実験結果をマージ専用のCSVに記録
                csvWriter.printf("%s,%s,%s,%b,%s\n", commitXi, relativeClassPath, method.getName(), isAsmMatched, "SUCCESS");

                Logger.infof("       [%s - マージテスト骨組み] クラス: %s, 一致検証: %b",
                        method.getName(), relativeClassPath, isAsmMatched);

              } catch (Exception e) {
                // 💡 エラー時の例外レコード
                csvWriter.printf("%s,%s,%s,false,ERROR_%s\n", commitXi, relativeClassPath, method.getName(), e.getClass().getSimpleName());
                Logger.errorf("       [%s - マージテスト] エラーを記録しました: %s", method.getName(), e.getMessage());
                e.printStackTrace();
              }
            }
          }
        } catch (Exception e) {
          Logger.errorf("  -> マージシナリオ %s の処理中に例外が発生しました: %s", commitXi, e.getMessage());
        }
      }
    } catch (IOException e) {
      Logger.error("CSVファイルの制御で致命的なエラーが発生しました: " + e.getMessage());
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
//      if (!orgText.equals(resText)) {
//        Logger.info("--- Original ASM Structure ---");
//        Logger.info(orgText);
//        Logger.info("--- Restored ASM Structure ---");
//        Logger.info(resText);
//        Logger.info("--- End of ASM Structures ---");
//      }
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

// =========================================================================
// 💡 Git 操作をカプセル化するドメインクラス（マージ用の新機能をプレースホルダとして追加）
// =========================================================================
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
        if (line.trim().startsWith(targetDir)) return true;
      }
    }
    return false;
  }

  /**
   * 💡 【新規追加】 指定されたコミット(Xi-1)より過去に、特定のファイル(C_j)を修正した直近のコミットハッシュ(Xk)を引く
   */
  public String findLastCommitModifyingFile(String startCommitExpression, String relativeFilePath) throws Exception {
    String hash = executeCommand("git", "log", "-1", "--format=%H", startCommitExpression, "--", relativeFilePath);
    return hash.isEmpty() ? null : hash;
  }

  /**
   * 💡 【新規追加】 2つのコミット間の「特定のファイルのみ」の差分を作業コピーに無理やり適用する。
   * Revert（Xk -> Xk~1）や Cherry-pick（Xi-1 -> Xi）のファイル限定エミュレート用。
   */
  public boolean applyDiffBetweenCommits(String fromCommit, String toCommit, String relativeFilePath) {
    try {
      // パイプラインを安全に模擬するため、git diff の内容を一度文字列としてキャプチャ
      ProcessBuilder diffPb = new ProcessBuilder("git", "diff", "--binary", fromCommit, toCommit, "--", relativeFilePath);
      diffPb.directory(repoDir);
      Process diffProcess = diffPb.start();

      byte[] diffBytes;
      try (InputStream is = diffProcess.getInputStream()) {
        diffBytes = is.readAllBytes();
      }
      diffProcess.waitFor();

      if (diffBytes.length == 0) return true; // 差分がなければ成功とみなす

      // キャプチャしたパッチデータを git apply に標準入力経由で流し込む
      ProcessBuilder applyPb = new ProcessBuilder("git", "apply", "-");
      applyPb.directory(repoDir);
      Process applyProcess = applyPb.start();

      try (OutputStream os = applyProcess.getOutputStream()) {
        os.write(diffBytes);
        os.flush();
      }
      int exitCode = applyProcess.waitFor();
      return exitCode == 0; // コンフリクトせず適用できたら true
    } catch (Exception e) {
      return false;
    }
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
        if (!hash.isEmpty()) commits.add(hash);
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
    return evacuateClassFilesTo(commitHash, Paths.get(evacuateRootDir));
  }

  /**
   * 💡 【新規拡張】 退避先の親ディレクトリ（RAMディスク等）を外部から動的に指定できる形式
   */
  public Path evacuateClassFilesTo(String uniqueDirName, Path targetBaseDir) throws IOException {
    Path targetDir = targetBaseDir.resolve(config.getRepoName()).resolve(uniqueDirName);
    if (Files.exists(targetDir)) return targetDir;
    Files.createDirectories(targetDir);

    Path sourceDir = repoDir.toPath().resolve(config.getClassDir());
    if (!Files.exists(sourceDir)) return targetDir;

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
      while ((line = reader.readLine()) != null) output.append(line).append("\n");
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

  // 💡 【新規拡張】 3-way merge のためのプレースホルダメソッドを新設（既存の手法を壊さないため default 実装）
  default byte[] computeMerge(byte[] bytesBase, byte[] bytesLocal, byte[] bytesRemote) throws Exception {
    return new byte[0]; // 次フェーズの本実装のためのダミープラスホルダー
  }
}

class DiffMethodASMWeaver implements BytecodeDiffMethod {
  @Override
  public String getName() {
    return "ASMWeaver";
  }

  @Override
  public byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    Path beforeFile = Files.createTempFile(ramPath, "asmw_b_", ".class");
    Path afterFile = Files.createTempFile(ramPath, "asmw_a_", ".class");
    Path patchFile = Files.createTempFile(ramPath, "asmw_p_", ".bin");

    // 💡 削除を伴う try-finally 構造を全廃
    Files.write(beforeFile, classBefore);
    Files.write(afterFile, classAfter);

    byte[] readBefore = Files.readAllBytes(beforeFile);
    byte[] readAfter = Files.readAllBytes(afterFile);

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
    Path patchFile = Files.createTempFile(ramPath, "asmw_patch_p_", ".bin");
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

  @Override
  public byte[] computeMerge(byte[] bytesBase, byte[] bytesLocal, byte[] bytesRemote) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    Path baseFile = Files.createTempFile(ramPath, "asmw_base_", ".class");
    Path localFile = Files.createTempFile(ramPath, "asmw_local_", ".class");
    Path remoteFile = Files.createTempFile(ramPath, "asmw_remote_", ".class");
    Files.write(baseFile, bytesBase);
    Files.write(localFile, bytesLocal);
    Files.write(remoteFile, bytesRemote);

    ClassNode classNodeBase = new ClassNode();
    new ClassReader(Files.readAllBytes(baseFile)).accept(classNodeBase, 0);
    ClassNode classNodeLocal = new ClassNode();
    new ClassReader(Files.readAllBytes(localFile)).accept(classNodeLocal, 0);
    ClassNode classNodeRemote = new ClassNode();
    new ClassReader(Files.readAllBytes(remoteFile)).accept(classNodeRemote, 0);

    ClassDiff diff12 = ClassDiffUtils.diff(classNodeBase, classNodeLocal);
    ClassDiff diff13 = ClassDiffUtils.diff(classNodeBase, classNodeRemote);

    ClassDiff merged = ClassDiffUtils.merge(diff12, diff13);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ClassDiffUtils.write(merged, new BinaryWriter(baos));

    Path mergedFile = Files.createTempFile(ramPath, "asmw_merged_", ".class");
    Files.write(mergedFile, baos.toByteArray());
    return Files.readAllBytes(mergedFile);
  }
}

/**
 * 💡 2. Aspa方式（測定区間内からのdelete処理を完全排除）
 */
class DiffMethodAspa implements BytecodeDiffMethod {
  @Override
  public String getName() {
    return "Aspa";
  }

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
  @Override
  public String getName() {
    return "Bsdiff";
  }

  @Override
  public byte[] computeDiff(byte[] classBefore, byte[] classAfter) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    Path before = Files.createTempFile(ramPath, "bsdiff_b_", ".class");
    Path after = Files.createTempFile(ramPath, "bsdiff_a_", ".class");
    Path patch = Files.createTempFile(ramPath, "bsdiff_p_", ".bin");

    Files.write(before, classBefore);
    Files.write(after, classAfter);

    ProcessUtils.runProcess(before.getParent().toFile(), "bsdiff", before.toString(), after.toString(), patch.toString());
    return Files.readAllBytes(patch);
  }

  @Override
  public byte[] applyPatch(byte[] classBefore, byte[] patchData) throws Exception {
    Path ramPath = Paths.get(Main.RAM_DISK_DIR);
    Path before = Files.createTempFile(ramPath, "bsdiff_b_", ".class");
    Path patch = Files.createTempFile(ramPath, "bsdiff_p_", ".bin");
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
  }

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