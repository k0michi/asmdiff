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
        description="Plot benchmark results into separate log-scale boxplots."
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

    # 💡 必須カラムにクラスファイルサイズを追加
    required_cols = [
        "MethodName",
        "DiffDurationNs",
        "PatchDurationNs",
        "DiffSizeBytes",
        "SrcSizeBytesBefore",
        "SrcSizeBytesAfter",
    ]
    if not all(col in df.columns for col in required_cols):
        print(
            f"Error: CSV must contain columns: {required_cols}", file=sys.stderr
        )
        sys.exit(1)

    # --- 2. Data Processing ---
    # Convert Nanoseconds (Ns) to Milliseconds (Ms)
    df["DiffDurationMs"] = df["DiffDurationNs"] / 1_000_000
    df["PatchDurationMs"] = df["PatchDurationNs"] / 1_000_000
    df["TotalDurationMs"] = df["DiffDurationMs"] + df["PatchDurationMs"]

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

    # =========================================================================
    # 💡 FILE 1: 処理時間の箱髭図（Duration）
    # =========================================================================
    fig1, ax1 = plt.subplots(figsize=(8, 6))
    width = 0.35

    diff_data = [
        df[df["MethodName"] == m]["DiffDurationMs"] for m in method_order
    ]
    patch_data = [
        df[df["MethodName"] == m]["PatchDurationMs"] for m in method_order
    ]

    box1 = ax1.boxplot(
        diff_data,
        positions=positions - width / 2,
        widths=width,
        patch_artist=True,
        flierprops=flier_props,
        medianprops=median_props,
    )
    box2 = ax1.boxplot(
        patch_data,
        positions=positions + width / 2,
        widths=width,
        patch_artist=True,
        flierprops=flier_props,
        medianprops=median_props,
    )

    for patch in box1["boxes"]:
        patch.set_facecolor("#1f77b4")
    for patch in box2["boxes"]:
        patch.set_facecolor("#aec7e8")

    diff_legend = mpatches.Patch(color="#1f77b4", label="Diff Duration")
    patch_legend = mpatches.Patch(color="#aec7e8", label="Patch Duration")
    ax1.legend(handles=[diff_legend, patch_legend], fontsize=11)

    ax1.set_ylabel("Duration (ms) [Log Scale]", fontsize=12)
    ax1.set_yscale("log")
    ax1.set_xlabel("Method Name", fontsize=12)
    ax1.set_title("Execution Time Distribution", fontsize=14, pad=15)
    ax1.set_xticks(positions)
    ax1.set_xticklabels(method_order, fontsize=11)
    ax1.grid(axis="y", linestyle="--", alpha=0.5, which="both")

    fig1.tight_layout()
    output_duration = f"boxplot_duration_{csv_path.stem}.png"
    plt.savefig(output_duration, dpi=300)
    plt.close(fig1)
    print(f"Saved: {output_duration}")

    # =========================================================================
    # 💡 FILE 2: パッチサイズの箱髭図（Patch Size）
    # =========================================================================
    fig2, ax2 = plt.subplots(figsize=(8, 6))

    size_data = [df[df["MethodName"] == m]["DiffSizeBytes"] for m in method_order]
    box3 = ax2.boxplot(
        size_data,
        positions=positions,
        widths=0.5,
        patch_artist=True,
        flierprops=flier_props,
        medianprops=median_props,
    )

    for patch in box3["boxes"]:
        patch.set_facecolor("#2ca02c")

    ax2.set_ylabel("Size (Bytes) [Log Scale]", fontsize=12)
    ax2.set_yscale("log")
    ax2.set_xlabel("Method Name", fontsize=12)
    ax2.set_title("Patch Size Distribution", fontsize=14, pad=15)
    ax2.set_xticks(positions)
    ax2.set_xticklabels(method_order, fontsize=11)
    ax2.grid(axis="y", linestyle="--", alpha=0.5, which="both")

    fig2.tight_layout()
    output_patch = f"boxplot_patch_size_{csv_path.stem}.png"
    plt.savefig(output_patch, dpi=300)
    plt.close(fig2)
    print(f"Saved: {output_patch}")

    # =========================================================================
    # 💡 FILE 3: 元のクラスファイルサイズの箱髭図（Class File Size）
    # =========================================================================
    fig3, ax3 = plt.subplots(figsize=(6, 6))

    # 手法ごとに重複している同一クラスファイルのサイズをデデュプ（クレンジング）
    df_unique_classes = df.drop_duplicates(subset=["CommitHash", "ClassPath"])
    class_data = [
        df_unique_classes["SrcSizeBytesBefore"],
        df_unique_classes["SrcSizeBytesAfter"],
    ]

    box4 = ax3.boxplot(
        class_data,
        positions=[0, 1],
        widths=0.4,
        patch_artist=True,
        flierprops=flier_props,
        medianprops=median_props,
    )

    # 前後で色分け（Before: グレー、After: オレンジ）
    box4["boxes"][0].set_facecolor("#7f7f7f")
    box4["boxes"][1].set_facecolor("#ff7f0e")

    ax3.set_ylabel("Size (Bytes) [Log Scale]", fontsize=12)
    ax3.set_yscale("log")
    ax3.set_title("Original Class File Size Distribution", fontsize=14, pad=15)
    ax3.set_xticks([0, 1])
    ax3.set_xticklabels(["Before (X_i-1)", "After (X_i)"], fontsize=11)
    ax3.grid(axis="y", linestyle="--", alpha=0.5, which="both")

    fig3.tight_layout()
    output_class = f"boxplot_class_size_{csv_path.stem}.png"
    plt.savefig(output_class, dpi=300)
    plt.close(fig3)
    print(f"Saved: {output_class}")


if __name__ == "__main__":
    main()