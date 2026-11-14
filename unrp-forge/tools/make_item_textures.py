"""Dessine la texture du masque du Joker (16x16) dans assets/unrp/textures/item/. Dessin original (rien du mod modèle).
Relance :  python3 tools/make_item_textures.py"""
import pathlib
from PIL import Image

OUT = pathlib.Path(__file__).resolve().parent.parent / "src/main/resources/assets/unrp/textures/item"
OUT.mkdir(parents=True, exist_ok=True)

W = (238, 236, 244, 255)   # porcelaine
S = (190, 184, 214, 255)   # ombre
K = (24, 20, 34, 255)      # contour et traits
R = (200, 40, 70, 255)     # sourire

img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
px = img.load()
rows = [  # ovale de masque, 12 de large
    "....KKKKKKKK....",
    "...KWWWWWWWWK...",
    "..KWWWWWWWWWWK..",
    "..KWWWWWWWWWWK..",
    "..KWKKWWWWKKWK..",
    "..KWKKWWWWKKWK..",
    "..KWWWWWWWWWWK..",
    "..KWWWWKKWWWWK..",
    "..KWWWWWWWWWWK..",
    "..KWRWWWWWWRWK..",
    "..KWWRRRRRRWWK..",
    "...KSWWWWWWSK...",
    "...KSSWWWWSSK...",
    "....KSSSSSSK....",
    ".....KKKKKK.....",
    "................",
]
pal = {"K": K, "W": W, "S": S, "R": R}
for y, row in enumerate(rows):
    for x, c in enumerate(row):
        if c in pal:
            px[x, y] = pal[c]
img.save(OUT / "joker_mask.png")
print("ok")
