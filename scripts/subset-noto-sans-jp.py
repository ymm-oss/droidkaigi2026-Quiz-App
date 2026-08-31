#!/usr/bin/env python3
"""Subsets Noto Sans JP for the Wasm build.

The web build has to ship its own CJK font because Skia on Wasm has no Japanese fallback, and the
full variable font is ~9.6 MB — large enough that text stays unrendered for seconds on venue Wi-Fi.
Keeping JIS X 0208 (level 1 + 2) plus the CP932 vendor extensions covers every character the quiz
UI, the questions and Japanese nicknames need, at roughly half the size.

Source font: Noto Sans JP variable (wght), https://fonts.google.com/noto/specimen/Noto+Sans+JP

Usage:
    pip install fonttools
    python3 scripts/subset-noto-sans-jp.py \
        --input NotoSansJP[wght].ttf \
        --output core/ui/src/wasmJsMain/composeResources/font/noto_sans_jp.ttf
"""

from __future__ import annotations

import argparse
import subprocess
import sys
import tempfile
from pathlib import Path

# Non-CJK blocks worth keeping whole: they are a rounding error in file size next to the kanji, and
# dropping one would show tofu for a stray symbol in a question or a nickname.
SYMBOL_RANGES = (
    (0x0000, 0x00FF),  # Basic Latin + Latin-1 Supplement
    (0x0100, 0x017F),  # Latin Extended-A
    (0x0370, 0x03FF),  # Greek
    (0x0400, 0x04FF),  # Cyrillic
    (0x2000, 0x206F),  # General Punctuation (en/em dash, quotes, ellipsis)
    (0x20A0, 0x20BF),  # Currency symbols
    (0x2100, 0x214F),  # Letterlike symbols (No., ™, ℃)
    (0x2190, 0x21FF),  # Arrows
    (0x2200, 0x22FF),  # Mathematical operators
    (0x2460, 0x24FF),  # Enclosed alphanumerics (①②③)
    (0x2500, 0x257F),  # Box drawing
    (0x25A0, 0x25FF),  # Geometric shapes (■▲)
    (0x2600, 0x26FF),  # Miscellaneous symbols (★♪)
    (0x2700, 0x27BF),  # Dingbats (✓✔)
    (0x3000, 0x303F),  # CJK symbols and punctuation
    (0x3040, 0x309F),  # Hiragana
    (0x30A0, 0x30FF),  # Katakana
    (0x31F0, 0x31FF),  # Katakana phonetic extensions
    (0x3200, 0x33FF),  # Enclosed CJK letters and CJK compatibility (㈱㎡)
    (0xFE30, 0xFE4F),  # CJK compatibility forms
    (0xFF00, 0xFFEF),  # Halfwidth and fullwidth forms
)


def codepoints_to_keep(font_path: Path) -> list[int]:
    from fontTools.ttLib import TTFont

    with TTFont(font_path, lazy=True) as font:
        available = set(font.getBestCmap())

    keep = {cp for cp in available for start, end in SYMBOL_RANGES if start <= cp <= end}
    for cp in available:
        try:
            chr(cp).encode("cp932")
        except UnicodeEncodeError:
            continue
        keep.add(cp)
    return sorted(keep)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", required=True, type=Path, help="full Noto Sans JP variable font")
    parser.add_argument("--output", required=True, type=Path, help="subset font to write")
    args = parser.parse_args()

    keep = codepoints_to_keep(args.input)
    with tempfile.NamedTemporaryFile("w", suffix=".txt", delete=False) as unicodes_file:
        unicodes_file.write(",".join(f"U+{cp:04X}" for cp in keep))
        unicodes_path = Path(unicodes_file.name)

    try:
        subprocess.run(
            [
                sys.executable,
                "-m",
                "fontTools.subset",
                str(args.input),
                f"--output-file={args.output}",
                f"--unicodes-file={unicodes_path}",
                "--layout-features=*",  # keep vertical/alternate forms used by Japanese shaping
                "--drop-tables+=BASE",
                "--recalc-bounds",
                "--name-IDs=*",
            ],
            check=True,
        )
    finally:
        unicodes_path.unlink(missing_ok=True)

    print(f"kept {len(keep)} codepoints, {args.output.stat().st_size / 1024 / 1024:.2f} MB")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
