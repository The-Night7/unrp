#!/usr/bin/env python3
"""Recolor cool violet GUI pixels to gold without changing atlas geometry or alpha.

Usage: python3 tools/recolor_gui_gold.py
Also run by make_gui_textures.py after generating the shared interface textures.
Red depletion indicators, cyan highlights, neutral pixels and existing gold stay intact.
"""
import colorsys
from pathlib import Path
from PIL import Image

GUI = Path(__file__).resolve().parent.parent / 'src/main/resources/assets/unrp/textures/gui'
GOLD_HUE = 42 / 360


def gold_pixel(pixel):
    red, green, blue, alpha = pixel
    if alpha == 0:
        return pixel
    hue, saturation, value = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)
    if not (0.60 <= hue <= 0.94 and saturation > 0.04):
        return pixel
    # Preserve brightness and shading; tint pale lavender towards cream gold.
    saturation = min(0.85, saturation * 0.85 + 0.18)
    rgb = colorsys.hsv_to_rgb(GOLD_HUE, saturation, value)
    return tuple(round(channel * 255) for channel in rgb) + (alpha,)


def recolor_gui(root=GUI):
    changed = []
    for path in sorted(root.rglob('*.png')):
        with Image.open(path) as source:
            image = source.convert('RGBA')
        palette = {pixel: gold_pixel(pixel) for pixel in set(image.get_flattened_data())}
        if all(before == after for before, after in palette.items()):
            continue
        image.putdata([palette[pixel] for pixel in image.get_flattened_data()])
        image.save(path)
        changed.append(path)
    return changed


if __name__ == '__main__':
    changed = recolor_gui()
    print(f'GUI textures recolored: {len(changed)}')
    for path in changed:
        print(path.relative_to(GUI))
