import argparse
import sys
from pathlib import Path
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


def get_target_filepath(input_path: str) -> Path:
    """引数のパスを評価し、適切なファイルのパスを返す関数"""
    path = Path(input_path)

    if path.is_file():
        print(f"Loading specified file: {path}")
        return path

    elif path.is_dir():
        csv_files = [f for f in path.glob("*.csv") if f.is_file()]
        if not csv_files:
            print(
                f"Error: No CSV files found in directory '{path}'",
                file=sys.stderr,
            )
            sys.exit(1)

        latest_file = max(csv_files, key=lambda f: f.stat().st_mtime)
        print(f"Directory detected. Automatically found latest file: {latest_file}")
        return latest_file

    else:
        print(
            f"Error: The path '{path}' does not exist or is invalid.",
            file=sys.stderr,
        )
        sys.exit(1)


def main():
    # --- CLI 引数の設定 ---
    parser = argparse.ArgumentParser(
        description="Plot comprehensive benchmark results into separate visualization files."
    )
    parser.add_argument(
        "path",
        nargs="?",
        default=".",
        help="Path to the CSV file or directory containing CSVs (default: current directory)",
    )
    args = parser.parse_args()

    csv_path = get_target_filepath(args.path)

    # --- 1. Load Data from File ---
    try:
        df = pd.read_csv(csv_path)
    except Exception as e:
        print(f"Error reading CSV file: {e}", file=sys.stderr)
        sys.exit(1)

    required_cols = [
        "MethodName",
        "DiffDurationNs",
        "PatchDurationNs",
        "DiffSizeBytes",
        "SrcSizeBytesBefore",
        "SrcSizeBytesAfter",
        "AsmTreeMatch",
        "Status",
    ]
    if not all(col in df.columns for col in required_cols):
        print(
            f"Error: CSV must contain columns: {required_cols}", file=sys.stderr
        )
        sys.exit(1)

    # --- 2. Data Processing ---
    # 単位変換
    df["DiffDurationMs"] = df["DiffDurationNs"] / 1_000_000
    df["PatchDurationMs"] = df["PatchDurationNs"] / 1_000_000
    df["TotalDurationMs"] = df["DiffDurationMs"] + df["PatchDurationMs"]

    # 💡 パッチサイズ比率（%）の計算（変更後クラスサイズに対するパッチの割合）
    df["PatchSizeRatio%"] = (df["DiffSizeBytes"] / df["SrcSizeBytesAfter"]) * 100

    # 総処理時間（中央値）の短い順に手法の並び順を固定
    method_order = (
        df.groupby("MethodName")["TotalDurationMs"]
        .median()
        .sort_values()
        .index.tolist()
    )

    # 共通のスタイリング設定
    flier_props = dict(
        marker="o", markersize=4, linestyle="none", markeredgecolor="gray"
    )
    median_props = dict(color="black", linewidth=1.5)
    positions = np.arange(len(method_order))
    markers = ["o", "s", "^", "D", "v", "<", ">", "p", "*", "h"]

    # 厳密な成功判定基準の定義
    df["IsSuccess"] = (df["Status"].astype(str).str.upper() == "SUCCESS") & (
        df["AsmTreeMatch"].astype(str).str.lower() == "true"
    )

    # =========================================================================
    # FILE 1: 処理時間の箱髭図（Duration）
    # =========================================================================
    fig1, ax1 = plt.subplots(figsize=(8, 6))
    width = 0.35

    diff_data = [df[df["MethodName"] == m]["DiffDurationMs"] for m in method_order]
    patch_data = [df[df["MethodName"] == m]["PatchDurationMs"] for m in method_order]

    box1 = ax1.boxplot(diff_data, positions=positions - width / 2, widths=width, patch_artist=True, flierprops=flier_props, medianprops=median_props)
    box2 = ax1.boxplot(patch_data, positions=positions + width / 2, widths=width, patch_artist=True, flierprops=flier_props, medianprops=median_props)

    for patch in box1["boxes"]: patch.set_facecolor("#1f77b4")
    for patch in box2["boxes"]: patch.set_facecolor("#aec7e8")

    ax1.legend(handles=[mpatches.Patch(color="#1f77b4", label="Diff Duration"), mpatches.Patch(color="#aec7e8", label="Patch Duration")], fontsize=11)
    ax1.set_ylabel("Duration (ms) [Log Scale]", fontsize=12)
    ax1.set_yscale("log")
    ax1.set_xlabel("Method Name", fontsize=12)
    ax1.set_title("Execution Time Distribution", fontsize=14, pad=15)
    ax1.set_xticks(positions)
    ax1.set_xticklabels(method_order, fontsize=11)
    ax1.grid(axis="y", linestyle="--", alpha=0.5, which="both")

    fig1.tight_layout()
    plt.savefig(f"boxplot_duration_{csv_path.stem}.png", dpi=300)
    plt.close(fig1)

    # =========================================================================
    # FILE 2: パッチサイズの箱髭図（Patch Size）
    # =========================================================================
    fig2, ax2 = plt.subplots(figsize=(8, 6))
    size_data = [df[df["MethodName"] == m]["DiffSizeBytes"] for m in method_order]
    box3 = ax2.boxplot(size_data, positions=positions, widths=0.5, patch_artist=True, flierprops=flier_props, medianprops=median_props)

    for patch in box3["boxes"]: patch.set_facecolor("#2ca02c")
    ax2.set_ylabel("Size (Bytes) [Log Scale]", fontsize=12)
    ax2.set_yscale("log")
    ax2.set_xlabel("Method Name", fontsize=12)
    ax2.set_title("Patch Size Distribution", fontsize=14, pad=15)
    ax2.set_xticks(positions)
    ax2.set_xticklabels(method_order, fontsize=11)
    ax2.grid(axis="y", linestyle="--", alpha=0.5, which="both")

    fig2.tight_layout()
    plt.savefig(f"boxplot_patch_size_{csv_path.stem}.png", dpi=300)
    plt.close(fig2)

    # =========================================================================
    # FILE 3: 元のクラスファイルサイズの箱髭図（Class File Size）
    # =========================================================================
    fig3, ax3 = plt.subplots(figsize=(6, 6))
    df_unique_classes = df.drop_duplicates(subset=["CommitHash", "ClassPath"])
    class_data = [df_unique_classes["SrcSizeBytesBefore"], df_unique_classes["SrcSizeBytesAfter"]]
    box4 = ax3.boxplot(class_data, positions=[0, 1], widths=0.4, patch_artist=True, flierprops=flier_props, medianprops=median_props)

    box4["boxes"][0].set_facecolor("#7f7f7f")
    box4["boxes"][1].set_facecolor("#ff7f0e")
    ax3.set_ylabel("Size (Bytes) [Log Scale]", fontsize=12)
    ax3.set_yscale("log")
    ax3.set_title("Original Class File Size Distribution", fontsize=14, pad=15)
    ax3.set_xticks([0, 1])
    ax3.set_xticklabels(["Before (X_i-1)", "After (X_i)"], fontsize=11)
    ax3.grid(axis="y", linestyle="--", alpha=0.5, which="both")

    fig3.tight_layout()
    plt.savefig(f"boxplot_class_size_{csv_path.stem}.png", dpi=300)
    plt.close(fig3)

    # =========================================================================
    # FILE 4: 成功率の比較プロット（Success Rate Bar Chart）
    # =========================================================================
    fig4, ax4 = plt.subplots(figsize=(8, 6))
    success_rates = (df.groupby("MethodName")["IsSuccess"].mean() * 100).reindex(method_order).fillna(0)
    bars = ax4.bar(positions, success_rates, width=0.5, edgecolor="black")

    for bar in bars:
        height = bar.get_height()
        ax4.annotate(f"{height:.1f}%", xy=(bar.get_x() + bar.get_width() / 2, height), xytext=(0, 3), textcoords="offset points", ha="center", va="bottom", fontsize=10, weight="bold")

    ax4.set_ylabel("Success Rate (%)", fontsize=12)
    ax4.set_xlabel("Method Name", fontsize=12)
    ax4.set_title("Benchmark Success Rate Comparison", fontsize=14, pad=15)
    ax4.set_xticks(positions)
    ax4.set_xticklabels(method_order, fontsize=11)
    ax4.set_ylim(0, 110)
    ax4.grid(axis="y", linestyle="--", alpha=0.5)

    fig4.tight_layout()
    plt.savefig(f"bar_success_rate_{csv_path.stem}.png", dpi=300)
    plt.close(fig4)

    # =========================================================================
    # FILE 5: 実行時間とパッチサイズの相関図（Correlation Scatter Plot）
    # =========================================================================
    fig5, ax5 = plt.subplots(figsize=(9, 6))

    for idx, m in enumerate(method_order):
        success_method_df = df[(df["MethodName"] == m) & (df["IsSuccess"] == True)]
        if not success_method_df.empty:
            ax5.scatter(success_method_df["TotalDurationMs"], success_method_df["DiffSizeBytes"], label=m, alpha=0.7, s=50, marker=markers[idx % len(markers)])

    ax5.set_xlabel("Total Duration (ms) [Log Scale]", fontsize=12)
    ax5.set_ylabel("Patch Size (Bytes) [Log Scale]", fontsize=12)
    ax5.set_xscale("log")
    ax5.set_yscale("log")
    ax5.set_title("Correlation between Total Duration and Patch Size", fontsize=14, pad=15)
    ax5.legend(fontsize=11, loc="best")
    ax5.grid(True, linestyle="--", alpha=0.5, which="both")

    fig5.tight_layout()
    plt.savefig(f"scatter_correlation_{csv_path.stem}.png", dpi=300)
    plt.close(fig5)

    # =========================================================================
    # 💡 FILE 6: 【提案】パッチサイズ比率の箱髭図（Patch Size Ratio %）
    # =========================================================================
    fig6, ax6 = plt.subplots(figsize=(8, 6))

    # 成功データのみの比率分布をプロット（100%を大幅に超える外れ値対策でLog推奨）
    ratio_data = [df[(df["MethodName"] == m) & (df["IsSuccess"] == True)]["PatchSizeRatio%"] for m in method_order]
    box6 = ax6.boxplot(ratio_data, positions=positions, widths=0.5, patch_artist=True, flierprops=flier_props, medianprops=median_props)

    for patch in box6["boxes"]: patch.set_facecolor("#e377c2") # ピンク系
    ax6.set_ylabel("Patch / Class Size Ratio (%) [Log Scale]", fontsize=12)
    ax6.set_yscale("log")
    ax6.set_xlabel("Method Name", fontsize=12)
    ax6.set_title("Patch-to-Class Size Ratio Efficiency", fontsize=14, pad=15)
    ax6.set_xticks(positions)
    ax6.set_xticklabels(method_order, fontsize=11)
    ax6.grid(axis="y", linestyle="--", alpha=0.5, which="both")

    fig6.tight_layout()
    output_ratio = f"boxplot_patch_ratio_{csv_path.stem}.png"
    plt.savefig(output_ratio, dpi=300)
    plt.close(fig6)
    print(f"Saved: {output_ratio}")

    # =========================================================================
    # 💡 FILE 7: 【提案】クラスファイルサイズ vs 実行時間のスケーラビリティ相関図
    # =========================================================================
    fig7, ax7 = plt.subplots(figsize=(9, 6))

    for idx, m in enumerate(method_order):
        success_method_df = df[(df["MethodName"] == m) & (df["IsSuccess"] == True)]
        if not success_method_df.empty:
            ax7.scatter(
                success_method_df["SrcSizeBytesAfter"], # 横軸：元のファイルサイズ
                success_method_df["TotalDurationMs"],   # 縦軸：総実行時間
                label=m, alpha=0.6, s=40, marker=markers[idx % len(markers)]
            )

    ax7.set_xlabel("Original Class Size (Bytes) [Log Scale]", fontsize=12)
    ax7.set_ylabel("Total Duration (ms) [Log Scale]", fontsize=12)
    ax7.set_xscale("log")
    ax7.set_yscale("log")
    ax7.set_title("Scalability: Class Size vs. Total Duration", fontsize=14, pad=15)
    ax7.legend(fontsize=11, loc="best")
    ax7.grid(True, linestyle="--", alpha=0.5, which="both")

    fig7.tight_layout()
    output_scalability = f"scatter_scalability_{csv_path.stem}.png"
    plt.savefig(output_scalability, dpi=300)
    plt.close(fig7)
    print(f"Saved: {output_scalability}")


if __name__ == "__main__":
    main()