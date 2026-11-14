"""Dessine les particules du mod (en blanc : le jeu les teinte avec la couleur du pouvoir).
Sortie : src/main/resources/assets/unrp/textures/particle/ et assets/unrp/particles/*.json
Relance : python3 tools/make_particle_textures.py   (dessins originaux)"""
import json, math, random, pathlib
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent / "src/main/resources/assets/unrp"
TEX, DEF = ROOT / "textures/particle", ROOT / "particles"
TEX.mkdir(parents=True, exist_ok=True)
DEF.mkdir(parents=True, exist_ok=True)

def put(px, x, y, v, size):
    if 0 <= x < size and 0 <= y < size:
        o = px[x, y][3]
        a = max(o, v)
        px[x, y] = (255, 255, 255, a)

def arc(seed, size=16):
    """Petit éclair : ligne brisée avec un halo plus pâle."""
    random.seed(seed)
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0)); px = im.load()
    pts = [(1, random.randint(4, 11))]
    x = 1
    while x < size - 2:
        x += random.randint(2, 4)
        pts.append((min(size - 2, x), max(2, min(size - 3, pts[-1][1] + random.randint(-4, 4)))))
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        n = max(abs(x1 - x0), abs(y1 - y0)) * 2 + 1
        for i in range(n + 1):
            xx = round(x0 + (x1 - x0) * i / n); yy = round(y0 + (y1 - y0) * i / n)
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    put(px, xx + dx, yy + dy, 90, size)
            put(px, xx, yy, 255, size)
    return im

def ring(thick, size=32):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0)); px = im.load()
    c, r = (size - 1) / 2, size / 2 - 3
    for x in range(size):
        for y in range(size):
            d = abs(math.hypot(x - c, y - c) - r)
            if d < thick:
                put(px, x, y, int(255 * (1 - d / thick) ** 1.5), size)
    return im

def flare(arm, size=16):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0)); px = im.load()
    c = (size - 1) / 2
    for x in range(size):
        for y in range(size):
            d = math.hypot(x - c, y - c)
            v = max(0, 1 - d / 4.0) ** 1.2
            if abs(x - c) < 0.6 or abs(y - c) < 0.6:
                v = max(v, max(0, 1 - d / arm) * 0.8)
            if v > 0.03:
                put(px, x, y, int(255 * min(1, v)), size)
    return im

def hexa(fill, size=16):
    """Hexagone (éclat de barrière) : contour net, intérieur léger."""
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0)); px = im.load()
    c, R = (size - 1) / 2, size / 2 - 1.5
    def inside(x, y, r):
        x, y = abs(x - c), abs(y - c)
        return y <= r * math.sqrt(3) / 2 and x <= r - y / math.sqrt(3)
    for x in range(size):
        for y in range(size):
            if inside(x, y, R):
                put(px, x, y, 255 if not inside(x, y, R - 1.6) else fill, size)
    return im

def mote(r, size=8):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0)); px = im.load()
    c = (size - 1) / 2
    for x in range(size):
        for y in range(size):
            v = max(0, 1 - math.hypot(x - c, y - c) / r)
            if v > 0:
                put(px, x, y, int(255 * v), size)
    return im


def blank(size=16):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    return im, im.load()

def ember(seed, size=16):
    """Langue de flamme : large en bas, pointue en haut."""
    random.seed(seed); im, px = blank(size); c = (size - 1) / 2
    for y in range(size):
        t = y / (size - 1)               # 0 en haut, 1 en bas
        half = (0.5 + 5.5 * t ** 0.8) * (1 - 0.35 * max(0, t - 0.8) / 0.2)
        off = math.sin(t * 5 + seed) * 1.2 * (1 - t)
        for x in range(size):
            d = abs(x - c - off)
            if d <= half:
                put(px, x, y, int(255 * (1 - d / (half + 0.01)) ** 0.6 * (0.5 + 0.5 * t)), size)
    return im

def shard(seed, size=16):
    """Éclat de cristal : losange allongé avec une arête claire."""
    random.seed(seed); im, px = blank(size); c = (size - 1) / 2
    ang = random.uniform(-0.6, 0.6)
    for x in range(size):
        for y in range(size):
            dx, dy = x - c, y - c
            u = dx * math.cos(ang) + dy * math.sin(ang); v = -dx * math.sin(ang) + dy * math.cos(ang)
            if abs(u) / 2.6 + abs(v) / 7.0 <= 1:
                put(px, x, y, 255 if abs(u) < 0.7 else 150, size)
    return im

def drop(size=16):
    im, px = blank(size); c = (size - 1) / 2
    for x in range(size):
        for y in range(size):
            dy = y - 10.0
            r = 3.6 if dy >= 0 else 3.6 * (1 + dy / 9.0)
            if r > 0 and abs(x - c) <= r and (dy < 0 or math.hypot(x - c, dy) <= 3.6):
                put(px, x, y, 255 if (x - c < -1 and dy < 0) else 190, size)
    return im

def streak(length, size=16):
    im, px = blank(size); c = (size - 1) / 2
    for x in range(size):
        t = x / (size - 1)
        if t > 1 - length:
            v = (t - (1 - length)) / length
            for dy in (-1, 0, 1):
                put(px, x, int(c) + dy, int(255 * v * (1 if dy == 0 else 0.35)), size)
    return im

GLYPHS = [
    ["..#####..", ".#.....#.", "#..###..#", "#.#...#.#", "#..###..#", ".#.....#.", "..#####.."],
    ["....#....", "...###...", "..#.#.#..", ".#..#..#.", "#########", "....#....", "....#...."],
    ["#.......#", ".#.....#.", "..#...#..", "...###...", "..#...#..", ".#.....#.", "#.......#"],
    ["..#...#..", "..#...#..", "#########", "..#...#..", "#########", "..#...#..", "..#...#.."],
]

def glyph(rows, size=16):
    im, px = blank(size); oy = (size - len(rows)) // 2; ox = (size - len(rows[0])) // 2
    for j, r in enumerate(rows):
        for i, ch in enumerate(r):
            if ch == "#":
                put(px, ox + i, oy + j, 255, size)
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    put(px, ox + i + dx, oy + j + dy, 70, size)
    return im

def smoke(seed, size=16):
    random.seed(seed); im, px = blank(size)
    blobs = [(random.uniform(4, 11), random.uniform(4, 11), random.uniform(2.5, 4.5)) for _ in range(4)]
    for x in range(size):
        for y in range(size):
            v = max(max(0, 1 - math.hypot(x - bx, y - by) / br) for bx, by, br in blobs)
            if v > 0.05:
                put(px, x, y, int(200 * v ** 0.7), size)
    return im

def wisp(seed, size=16):
    """Volute : spirale qui s'affine."""
    im, px = blank(size); c = (size - 1) / 2
    for i in range(160):
        t = i / 159
        a = t * 5.5 + seed
        r = 6.2 * (1 - t) + 0.5
        x, y = c + math.cos(a) * r, c + math.sin(a) * r
        w = 1.6 * (1 - t) + 0.4
        for dx in range(-2, 3):
            for dy in range(-2, 3):
                d = math.hypot(dx, dy)
                if d <= w:
                    put(px, round(x + dx), round(y + dy), int(255 * (1 - d / (w + 0.5)) * (0.4 + 0.6 * (1 - t))), size)
    return im

def glint(arm, size=16):
    im, px = blank(size); c = (size - 1) / 2
    for x in range(size):
        for y in range(size):
            dx, dy = abs(x - c), abs(y - c)
            v = 0
            if dy < 0.6: v = max(v, 1 - dx / arm)
            if dx < 0.6: v = max(v, 1 - dy / arm)
            if abs(dx - dy) < 0.5: v = max(v, (1 - dx / (arm * 0.45)) * 0.7)
            if v > 0: put(px, x, y, int(255 * v), size)
    return im

def leaf(flip, size=16):
    im, px = blank(size); c = (size - 1) / 2
    for x in range(size):
        for y in range(size):
            u, v = (x - c), (y - c)
            if flip: u = -u
            a, b = (u + v) / 1.41, (u - v) / 1.41
            if (a / 6.5) ** 2 + (b / 2.8) ** 2 <= 1:
                put(px, x, y, 255 if abs(b) < 0.6 else 170, size)
    return im

def cross(size=16):
    im, px = blank(size); c = (size - 1) / 2
    for x in range(size):
        for y in range(size):
            dx, dy = abs(x - c), abs(y - c)
            if (dx <= 1.6 and dy <= 5.5) or (dy <= 1.6 and dx <= 5.5):
                put(px, x, y, 255 if (dx <= 0.8 or dy <= 0.8) else 180, size)
    return im

def rock(seed, size=16):
    random.seed(seed); im, px = blank(size); c = (size - 1) / 2
    k = [random.uniform(3.5, 6.0) for _ in range(7)]
    for x in range(size):
        for y in range(size):
            a = (math.atan2(y - c, x - c) + math.pi) / (2 * math.pi) * 7
            i = int(a) % 7; f = a - int(a)
            r = k[i] * (1 - f) + k[(i + 1) % 7] * f
            d = math.hypot(x - c, y - c)
            if d <= r:
                put(px, x, y, 255 if d > r - 1.2 else (150 if (x + y) % 3 else 200), size)
    return im

SETS = {
    "arc": [arc(s) for s in (3, 11, 19, 27)],
    "ring": [ring(2.2), ring(1.4)],
    "flare": [flare(7.5), flare(5.5)],
    "hex": [hexa(70), hexa(30)],
    "mote": [mote(3.6), mote(2.6), mote(1.8)],
    "ember": [ember(1), ember(2.5), ember(4), ember(5.5)],
    "shard": [shard(1), shard(2), shard(3)],
    "drop": [drop()],
    "streak": [streak(0.9), streak(0.6)],
    "glyph": [glyph(g) for g in GLYPHS],
    "smoke": [smoke(1), smoke(2), smoke(3)],
    "wisp": [wisp(0), wisp(2), wisp(4)],
    "glint": [glint(7.5), glint(5)],
    "leaf": [leaf(False), leaf(True)],
    "cross": [cross()],
    "rock": [rock(1), rock(2), rock(3)],
}
for name, frames in SETS.items():
    names = []
    for i, im in enumerate(frames):
        im.save(TEX / f"{name}_{i}.png")
        names.append(f"unrp:{name}_{i}")
    (DEF / f"{name}.json").write_text(json.dumps({"textures": names}, indent=2) + "\n", encoding="utf-8")
print("particules ->", TEX)
