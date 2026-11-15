#!/usr/bin/env python3
"""Native 16x16 pixel glyphs for Covenant's existing ability-icon system."""
from pathlib import Path
from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parent.parent / 'src/main/resources/assets/unrp/textures/icons'
GOLD, LIGHT, SHADE = '#F0C846', '#FFE6A1', '#886023'
NAMES = ['posture', 'enact', 'terms', 'fine', 'foreclose', 'pact', 'endow', 'proxy', 'appropriate',
         'immunity', 'indemnity', 'escrow', 'arbitrate', 'constitution', 'retroactive', 'forfeit',
         'class_action', 'jurisdiction', 'sovereign_pact', 'due', 'message', 'ledger', 'return', 'spend', 'summon']


def make(name):
    image = Image.new('RGBA', (16, 16)); draw = ImageDraw.Draw(image)
    def line(points, color=GOLD, width=1): draw.line(points, fill=color, width=width)
    def box(bounds, color=GOLD): draw.rectangle(bounds, outline=color)
    def crown():
        line([(3, 8), (3, 3), (6, 6), (8, 2), (10, 6), (13, 3), (13, 8), (3, 8)], LIGHT)
        line([(4, 10), (12, 10)]); line([(8, 10), (8, 14)])
    if name in ['posture', 'sovereign_pact']:
        crown()
        if name == 'sovereign_pact': line([(1, 11), (5, 13), (1, 14)])
    elif name in ['enact', 'fine', 'constitution', 'ledger', 'terms', 'message']:
        box((3, 2, 12, 13), SHADE); line([(3, 2), (12, 2), (12, 13), (3, 13), (3, 2)])
        for y in [5, 8, 11]: line([(5, y), (10, y)], LIGHT)
        if name == 'enact': line([(7, 12), (14, 3)], LIGHT, 2)
        if name == 'fine': line([(2, 14), (14, 1)], SHADE, 2)
        if name == 'constitution': line([(8, 4), (8, 11)], LIGHT, 2)
        if name == 'terms': line([(0, 7), (6, 7), (4, 5)]); line([(6, 7), (4, 9)])
        if name == 'message': line([(9, 11), (14, 6), (14, 10)])
    elif name in ['immunity', 'indemnity', 'escrow', 'arbitrate']:
        line([(8, 1), (13, 3), (12, 9), (8, 14), (4, 9), (3, 3), (8, 1)])
        if name == 'immunity': line([(6, 7), (8, 9), (11, 5)], LIGHT)
        elif name == 'indemnity': line([(8, 4), (8, 10)], LIGHT); line([(5, 7), (11, 7)], LIGHT)
        elif name == 'escrow': box((6, 6, 10, 10), LIGHT); line([(7, 6), (7, 4), (9, 4), (9, 6)], LIGHT)
        else: line([(10, 4), (6, 8), (10, 8), (6, 12)], LIGHT)
    elif name in ['pact', 'proxy', 'endow']:
        draw.ellipse((1, 5, 7, 11), outline=GOLD); draw.ellipse((8, 5, 14, 11), outline=LIGHT)
        line([(5, 8), (10, 8)])
        if name == 'proxy': line([(8, 1), (8, 5)], LIGHT); line([(6, 3), (8, 5), (10, 3)], LIGHT)
        if name == 'endow': line([(7, 3), (11, 3), (9, 1)], LIGHT); line([(11, 3), (9, 5)], LIGHT)
    elif name in ['foreclose', 'forfeit']:
        line([(3, 12), (10, 5)], LIGHT, 2)
        line([(5, 3), (7, 1), (14, 8), (12, 10), (5, 3)])
        line([(2, 14), (13, 14)], SHADE)
        if name == 'forfeit': line([(3, 3), (12, 12)], GOLD)
    elif name in ['retroactive', 'return']:
        draw.arc((2, 2, 13, 13), 30, 330, fill=GOLD)
        line([(1, 6), (4, 3), (5, 7)], LIGHT)
        if name == 'retroactive': line([(8, 4), (8, 8), (11, 9)], LIGHT)
        else: line([(11, 5), (6, 10), (9, 10)], LIGHT)
    elif name in ['due', 'spend']:
        draw.ellipse((3, 2, 12, 13), outline=GOLD); line([(8, 4), (8, 11)], LIGHT)
        line([(5, 6), (10, 6)]); line([(5, 9), (10, 9)])
        if name == 'spend': line([(0, 4), (3, 4)]); line([(1, 3), (1, 5)])
    elif name == 'class_action':
        for x in [1, 6, 11]: box((x, 3, x + 3, 6)); line([(x + 1, 8), (x + 1, 12)], LIGHT)
        line([(2, 14), (13, 14)])
    elif name == 'jurisdiction':
        box((1, 1, 14, 14), SHADE); line([(5, 13), (5, 3), (12, 3), (10, 6), (5, 6)], LIGHT)
    elif name == 'summon':
        draw.ellipse((4, 5, 11, 12), outline=LIGHT); line([(6, 12), (5, 14), (9, 12)])
        line([(8, 1), (5, 4)], GOLD); line([(8, 1), (11, 4)], GOLD)
    else:
        line([(8, 13), (8, 7), (3, 3)], LIGHT, 2); line([(8, 7), (13, 3)], GOLD, 2)
    return image


if __name__ == '__main__':
    OUT.mkdir(parents=True, exist_ok=True)
    for name in NAMES: make(name).save(OUT / f'covenant_{name}.png')
    print(f'Covenant icons generated: {len(NAMES)}')
