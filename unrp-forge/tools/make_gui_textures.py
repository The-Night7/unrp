"""Generate the HUD and hub textures. Edit the palette constants, then run this script."""
import math, random, pathlib
from PIL import Image
from recolor_gui_gold import recolor_gui

OUT = pathlib.Path(__file__).resolve().parent.parent / "src/main/resources/assets/unrp/textures/gui"
OUT.mkdir(parents=True, exist_ok=True)

FRAME_LIGHT = (245, 216, 144, 255)
FRAME_DARK = (112, 81, 33, 255)      # cadre (ombre)
FRAME_OUT = (20, 14, 6, 255)        # contour
INNER = (14, 11, 5, 255)
RIB_A = (232, 196, 92)
RIB_B = (190, 132, 38)
SKY_TOP, SKY_BOT = (24, 18, 8), (57, 41, 16)
STARS = [(171, 132, 54), (245, 216, 144), (255, 240, 194)]

# ---------------------------------------------------------------- barre d'aura : 108 x 20
W, H = 108, 20
BX0, BX1, BY0, BY1 = 2, 105, 3, 16
IX0, IX1, IY0, IY1 = 9, 98, 6, 13
TIP = 6

def in_body(x, y):
    cy = (BY0 + BY1) / 2
    half = (BY1 - BY0) / 2
    if x < BX0 or x > BX1:
        return False
    d = min(x - BX0, BX1 - x)
    if d < TIP:
        return abs(y - cy) <= half * (d + 0.5) / TIP
    return BY0 <= y <= BY1

back = Image.new("RGBA", (W, H), (0, 0, 0, 0))
px = back.load()
for x in range(W):
    for y in range(H):
        if not in_body(x, y):
            continue
        edge = not (in_body(x - 1, y) and in_body(x + 1, y) and in_body(x, y - 1) and in_body(x, y + 1))
        if IX0 <= x <= IX1 and IY0 <= y <= IY1:
            px[x, y] = INNER
        elif edge:
            px[x, y] = FRAME_OUT
        elif y <= (BY0 + BY1) / 2:
            px[x, y] = FRAME_LIGHT
        else:
            px[x, y] = FRAME_DARK

for x in range(IX0 - 1, IX1 + 2):
    for y in (IY0 - 1, IY1 + 1):
        px[x, y] = FRAME_OUT
for y in range(IY0 - 1, IY1 + 2):
    for x in (IX0 - 1, IX1 + 1):
        px[x, y] = FRAME_OUT

front = Image.new("RGBA", (W, H), (0, 0, 0, 0))
fp = front.load()
P, A, CY = 30.0, 8.6, (BY0 + BY1) / 2
for strand, col, phase in ((0, RIB_A, 0.0), (1, RIB_B, math.pi)):
    for i in range(0, (W - 4) * 4):
        x = 2 + i / 4
        t = 2 * math.pi * x / P + phase
        y = CY + A * math.sin(t)
        front_side = math.cos(t) > 0
        X, Y = int(round(x)), int(round(y))
        if not (0 <= X < W and 0 <= Y < H):
            continue
        shade = 1.0 if front_side else 0.55
        c = tuple(int(v * shade) for v in col) + (255,)
        if front_side:
            fp[X, Y] = c
            if 0 <= Y + 1 < H:
                fp[X, Y + 1] = tuple(int(v * 0.7) for v in col) + (255,)
        elif not in_body(X, Y):
            px[X, Y] = c
back.save(OUT / "aura_back.png")
front.save(OUT / "aura_front.png")


random.seed(7)
sky = Image.new("RGBA", (64, 64))
sp = sky.load()
for y in range(64):
    t = y / 63
    c = tuple(int(SKY_TOP[i] * (1 - t) + SKY_BOT[i] * t) for i in range(3)) + (255,)
    for x in range(64):
        sp[x, y] = c
for _ in range(22):
    x, y = random.randrange(64), random.randrange(64)
    sp[x, y] = random.choice(STARS) + (255,)
for _ in range(3):
    x, y = random.randrange(2, 62), random.randrange(2, 62)
    for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
        sp[x + dx, y + dy] = (255, 240, 194, 255) if (dx, dy) == (0, 0) else (171, 132, 54, 255)
sky.save(OUT / "stars.png")
print("recoloured imported textures ->", len(recolor_gui(OUT)))
print("textures ->", OUT)
