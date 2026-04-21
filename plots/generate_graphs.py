from __future__ import annotations

import csv
from pathlib import Path

import matplotlib.pyplot as plt


PROJECT_ROOT = Path(__file__).resolve().parent.parent
OUTPUT_DIR = PROJECT_ROOT / "output"
PLOTS_DIR = OUTPUT_DIR / "plots"

TIME_VS_K_CSV = OUTPUT_DIR / "time_vs_k.csv"
SPACE_VS_K_CSV = OUTPUT_DIR / "space_vs_k.csv"
COMPARISON_CSV = OUTPUT_DIR / "comparison_all_algorithms.csv"
COMPARISON_LARGE_K_CSV = OUTPUT_DIR / "comparison_all_algorithms_large_k.csv"

PREFERRED_TARGET_N = 100_000


def read_csv_rows(path: Path) -> list[dict[str, str]]:
    with path.open("r", newline="", encoding="utf-8") as file:
        return list(csv.DictReader(file))


def rows_for_n(rows: list[dict[str, str]], n_value: int) -> list[dict[str, str]]:
    return [row for row in rows if int(row["n"]) == n_value]


def resolve_target_n(rows: list[dict[str, str]], preferred_n: int) -> int:
    available = sorted({int(row["n"]) for row in rows})
    if not available:
        raise ValueError("No rows available to resolve target n.")
    if preferred_n in available:
        return preferred_n
    return max(available)


def group_xy_by_algorithm(rows: list[dict[str, str]], y_key: str) -> dict[str, tuple[list[int], list[float]]]:
    grouped: dict[str, list[tuple[int, float]]] = {}
    for row in rows:
        algorithm = row["algorithm"]
        k = int(row["k"])
        y = float(row[y_key])
        grouped.setdefault(algorithm, []).append((k, y))

    result: dict[str, tuple[list[int], list[float]]] = {}
    for algorithm, points in grouped.items():
        points.sort(key=lambda p: p[0])
        result[algorithm] = ([p[0] for p in points], [p[1] for p in points])
    return result


def resolve_metric_key(rows: list[dict[str, str]], preferred_keys: list[str]) -> str:
    if not rows:
        raise ValueError("No rows available to resolve metric key.")
    available = set(rows[0].keys())
    for key in preferred_keys:
        if key in available:
            return key
    raise ValueError(f"None of the expected keys found. Expected one of: {preferred_keys}. Found: {sorted(available)}")


def plot_lines(
    data: dict[str, tuple[list[int], list[float]]],
    title: str,
    y_label: str,
    output_path: Path,
) -> None:
    plt.figure(figsize=(9, 5))

    for algorithm, (x_values, y_values) in data.items():
        plt.plot(x_values, y_values, marker="o", linewidth=2, label=algorithm)

    plt.title(title)
    plt.xlabel("k (maximum value in array)")
    plt.ylabel(y_label)
    plt.xscale("log")
    plt.grid(True, linestyle="--", linewidth=0.5, alpha=0.6)
    plt.legend()
    plt.tight_layout()
    plt.savefig(output_path, dpi=180)
    plt.close()


def main() -> None:
    PLOTS_DIR.mkdir(parents=True, exist_ok=True)

    all_time_rows = read_csv_rows(TIME_VS_K_CSV)
    all_space_rows = read_csv_rows(SPACE_VS_K_CSV)
    all_comparison_rows = read_csv_rows(COMPARISON_CSV)

    target_n = resolve_target_n(all_comparison_rows, PREFERRED_TARGET_N)

    time_rows = rows_for_n(all_time_rows, target_n)
    space_rows = rows_for_n(all_space_rows, target_n)
    comparison_rows = rows_for_n(all_comparison_rows, target_n)

    if not time_rows or not space_rows or not comparison_rows:
        raise ValueError(
            "Missing rows for TARGET_N in one or more CSV files. "
            "Run experiments first or adjust the target n."
        )

    time_key = resolve_metric_key(time_rows, ["avg_time_ms", "time"])
    memory_key = resolve_metric_key(space_rows, ["avg_memory_bytes", "memory"])

    plot_lines(
        data=group_xy_by_algorithm(time_rows, time_key),
        title=f"Time vs k (Count Sort vs ARU Count Sort) at n={target_n}",
        y_label="Time (milliseconds)" if time_key == "avg_time_ms" else "Time (nanoseconds)",
        output_path=PLOTS_DIR / "time_vs_k_counting_vs_aru.png",
    )

    plot_lines(
        data=group_xy_by_algorithm(space_rows, memory_key),
        title=f"Space vs k (Count Sort vs ARU Count Sort) at n={target_n}",
        y_label="Memory (bytes)",
        output_path=PLOTS_DIR / "space_vs_k_counting_vs_aru.png",
    )

    plot_lines(
        data=group_xy_by_algorithm(comparison_rows, time_key),
        title=f"Time vs k (All Algorithms) at n={target_n}",
        y_label="Time (milliseconds)" if time_key == "avg_time_ms" else "Time (nanoseconds)",
        output_path=PLOTS_DIR / "time_vs_k_all_algorithms.png",
    )

    if COMPARISON_LARGE_K_CSV.exists():
        comparison_large_rows = rows_for_n(read_csv_rows(COMPARISON_LARGE_K_CSV), target_n)
        if comparison_large_rows:
            large_time_key = resolve_metric_key(comparison_large_rows, ["avg_time_ms", "time"])
            plot_lines(
                data=group_xy_by_algorithm(comparison_large_rows, large_time_key),
                title=f"Time vs k (All Algorithms, Large k range) at n={target_n}",
                y_label="Time (milliseconds)" if large_time_key == "avg_time_ms" else "Time (nanoseconds)",
                output_path=PLOTS_DIR / "time_vs_k_all_algorithms_large_k.png",
            )

    print("Graphs generated:")
    print("-", PLOTS_DIR / "time_vs_k_counting_vs_aru.png")
    print("-", PLOTS_DIR / "space_vs_k_counting_vs_aru.png")
    print("-", PLOTS_DIR / "time_vs_k_all_algorithms.png")
    if COMPARISON_LARGE_K_CSV.exists():
        print("-", PLOTS_DIR / "time_vs_k_all_algorithms_large_k.png")


if __name__ == "__main__":
    main()
