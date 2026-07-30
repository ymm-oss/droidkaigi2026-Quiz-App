#!/usr/bin/env python3
"""Summarize `android layout` JSON, or resolve an element to adb tap coordinates.

Usage:
    layout.py <layout.json>                # list elements with text / clickability
    layout.py <layout.json> --find 回答する  # print "x y" for `adb shell input tap`
    layout.py <layout.json> --all          # include elements without text
"""

import argparse
import json
import sys

TAPPABLE = ("clickable", "checkable", "long-clickable")


def load(path):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    if isinstance(data, dict):
        for key in ("elements", "nodes", "children"):
            if isinstance(data.get(key), list):
                return data[key]
        return [data]
    return data


def center(element):
    """Return (x, y) from a `"[x,y]"` center value, or None."""
    raw = element.get("center")
    if isinstance(raw, str):
        parts = raw.strip("[]").split(",")
        if len(parts) == 2:
            return int(parts[0]), int(parts[1])
    elif isinstance(raw, (list, tuple)) and len(raw) == 2:
        return int(raw[0]), int(raw[1])
    return None


def label(element):
    return element.get("text") or element.get("contentDesc") or element.get("content-desc")


def find(elements, needle):
    """Exact text match wins over substring so short queries stay predictable."""
    for match in (lambda t: t == needle, lambda t: needle in t):
        for element in elements:
            text = label(element)
            if text and match(text) and center(element):
                return element
    return None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("path")
    parser.add_argument("--find", help="print 'x y' of the element with this text")
    parser.add_argument("--all", action="store_true", help="include elements without text")
    args = parser.parse_args()

    elements = load(args.path)

    if args.find:
        element = find(elements, args.find)
        if not element:
            sys.exit(f"not found on screen: {args.find!r} (it may be scrolled out of view)")
        x, y = center(element)
        print(f"{x} {y}")
        return

    for element in elements:
        text = label(element)
        interactions = element.get("interactions") or []
        if not text and not (args.all or any(i in interactions for i in TAPPABLE)):
            continue
        parts = [f"{text!r}" if text else "<no text>"]
        point = center(element)
        if point:
            parts.append(f"center={point[0]},{point[1]}")
        if interactions:
            parts.append("+".join(interactions))
        if element.get("state"):
            parts.append("state=" + "+".join(element["state"]))
        if element.get("off-screen"):
            parts.append("OFF-SCREEN")
        print("  ".join(parts))


if __name__ == "__main__":
    main()
