#!/usr/bin/env python3
"""Generate item names and loot tables. The equipment design tools maintain the runtime models and textures."""
import json, os, hashlib
from PIL import Image, ImageDraw

RES = "src/main/resources"
A = RES + "/assets/unrp"
D = RES + "/data/unrp"

def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5)) + (255,)

def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (255,)


ITEMS = [
    # --- amplification
    ("amplifier", "vial", "#ff9a3c", "#ffd9a0", "Amplifier", "Amplificateur"),
    ("deamplifier", "vial", "#4aa3ff", "#bfe0ff", "Dampener", 'Dampener'),
    ("disabler", "vial", "#8a8f99", "#d0d4db", "Disabler", 'Disabler'),
    ("syringe", "syringe", "#e8e8ee", "#c9ccd6", "Syringe", "Seringue"),
    ("syringe_amplifier", "syringe", "#ff9a3c", "#ffd9a0", "Amplifier Syringe", "Seringue d'amplificateur"),
    ("syringe_deamplifier", "syringe", "#4aa3ff", "#bfe0ff", "Dampener Syringe", 'Dampener Syringe'),
    ("syringe_disabler", "syringe", "#8a8f99", "#d0d4db", "Disabler Syringe", 'Disabler Syringe'),
    ("syringe_blood", "syringe", "#c8202f", "#ff7a86", "Blood Syringe", "Seringue de sang"),
    ("rewriter_serum", "vial", "#b36bff", "#e9d2ff", "Rewriter Serum", 'Rewriter Serum'),

    ("tower_core", "core", "#ff6a2a", "#ffd27a", "EMBER Tower Core", 'EMBER Tower Core'),
    ("vital_sand", "sand", "#c9803a", "#f0c27a", "Vital Sand", "Sable vital"),
    ("ossified_tail", "club", "#e6dcc3", "#b9ab8a", "Ossified Tail", 'Ossified Tail'),
    ("binding_link", "link", "#ffc84a", "#fff0b0", "Link of the Binding", "Binding Link"),
    ("carved_rune", "rune", "#d9d2bd", "#7d7660", "Carved Rune", 'Engraved Rune'),
    ("bone_charm", "charm", "#efe8d4", "#9c9378", "Bone Charm", "Charme d'os"),
    ("soul", "soul", "#7fe3ff", "#e6fbff", "Soul", 'Soul'),
    ("soul_sword", "sword", "#7fe3ff", "#2a7a99", "Soul Sword", 'Soul Sword'),
    ("unordinary_book", "book", "#f2f2f2", "#222222", "unOrdinary", "unOrdinary"),
    ("contract", "contract", "#e9dcb4", "#9a8450", "Contract", "Contrat"),
    ("sunsteel_axe", "axe", "#ffc13a", "#b86a10", "Sunsteel Axe", "Hache d'acier solaire"),
    ("salt_spear", "spear", "#f4f6f8", "#aeb6c0", "Salt Spear", "Lance de sel"),
    ("ascalon", "sword", "#67e07a", "#1d7a3a", "Dragonbane", "Pourfendeur de dragon"),
    ("armament", "sword", "#c9ccd3", "#7a7f8a", "Armament", "Armement"),

    ("edict_shard", "shard", "#ffd24a", "#fff3b0", "Edict Shard", 'Edict Shard'),
    ("limit_breaker_shard", "shard", "#ff4a4a", "#ffb0b0", "Limit Breaker Shard", 'Limit Breaker Shard'),
    ("dragon_engine_shard", "shard", "#4aff8a", "#b0ffd0", "Dragon Engine Shard", 'Wyrm Engine Shard'),
    ("singularity_shard", "shard", "#6a4aff", "#c0b0ff", "Singularity Shard", 'Singularity Shard'),
    ("sarcophage_shard", "shard", "#d9a24a", "#ffe0b0", "Sarcophage Shard", 'Sarcophagus Shard'),
    ("crimson_mark_shard", "shard", "#b01030", "#ff90a0", "Crimson Mark Shard", 'Crimson Mark Shard'),
    ("dealmaker_shard", "shard", "#3acfa0", "#b0ffe8", "Dealmaker Shard", 'Dealmaker Shard'),
    ("hollow_mark_shard", "shard", "#6b6b8a", "#c8c8e0", "Hollow Mark Shard", 'Hollow Mark Shard'),
    ("counterweight_shard", "shard", "#8a8f99", "#e0e4ea", "Counterweight Shard", 'Counterweight Shard'),
    ("vertigo_shard", "shard", "#e04aff", "#f6b0ff", "Vertigo Shard", 'Vertigo Shard'),
    # --- manuscrits et tomes
    ("manuscript_aug", "scroll", "#ff8a3c", "#f0e6c8", "Manuscript of Augmentation", "Manuscrit d'Augmentation"),
    ("manuscript_med", "scroll", "#4adf7a", "#f0e6c8", "Manuscript of Medical Theory", 'Medical Theory Manuscript'),
    ("manuscript_proj", "scroll", "#4aa3ff", "#f0e6c8", "Manuscript of Projectile Generation", 'Projectile Generation Manuscript'),
    ("manuscript_con", "scroll", "#ffd24a", "#f0e6c8", "Manuscript of Construct Generation", 'Construct Generation Manuscript'),
    ("manuscript_qtm", "scroll", "#b36bff", "#f0e6c8", "Manuscript of Quantum Manipulation", "Manuscrit de Quantum Manipulation"),
    ("tome_aug", "book", "#ff8a3c", "#4a2a10", "Tome of Augmentation", "Tome d'Augmentation"),
    ("tome_med", "book", "#4adf7a", "#10401f", "Tome of Medical Theory", 'Medical Theory Tome'),
    ("tome_proj", "book", "#4aa3ff", "#10304a", "Tome of Projectile Generation", 'Projectile Generation Tome'),
    ("tome_con", "book", "#ffd24a", "#4a3a10", "Tome of Construct Generation", 'Construct Generation Tome'),
    ("tome_qtm", "book", "#b36bff", "#2f104a", "Tome of Quantum Manipulation", "Tome de Quantum Manipulation"),
    # --- disques
    ("music_disc_turf_war", "disc", "#e0502a", "#ffb08a", "Music Disc", "Disque de musique"),
    ("music_disc_crown_match", "disc", "#e0c02a", "#fff0a0", "Music Disc", "Disque de musique"),
    ("music_disc_guardian", "disc", "#2a8ae0", "#a0d0ff", "Music Disc", "Disque de musique"),
    ("music_disc_tyrant", "disc", "#a02ae0", "#e0a0ff", "Music Disc", "Disque de musique"),
    ("music_disc_surge", "disc", "#2ae0a0", "#a0ffe0", "Music Disc", "Disque de musique"),
    ("music_disc_the_hunt", "disc", "#e02a50", "#ffa0b0", "Music Disc", "Disque de musique"),

    ("agent_spawn_egg", "egg", "#1e1e26", "#c4c01a", "EMBER Agent Spawn Egg", 'EMBER Agent Spawn Egg'),
    ("agent_leader_spawn_egg", "egg", "#1e1e26", "#e0602e", "EMBER Leader Spawn Egg", 'EMBER Agent Leader Spawn Egg'),
    ("spectre_spawn_egg", "egg", "#141a1a", "#60a07a", "Spectre Operative Spawn Egg", 'Spectre Operative Spawn Egg'),
]


OUTFITS = [
    ("wellstone_blazer", "chest", "wellstone_uniform", "Wellston Uniform Blazer", "Blazer d'uniforme Wellston"),
    ("wellstone_shirt", "chest", "wellstone_shirt", "Wellston Uniform Shirt", "Chemise d'uniforme Wellston"),
    ("wellstone_trousers", "legs", "wellstone_uniform", "Wellston Uniform Trousers", "Pantalon d'uniforme Wellston"),
    ("wellstone_shoes", "feet", "wellstone_uniform", "Wellston Uniform Shoes", "Chaussures d'uniforme Wellston"),
    ("wellstone_girl_blazer", "chest", "wellstone_girl_uniform", "Wellston Girls' Blazer", "Blazer Wellston (filles)"),
    ("wellstone_girl_shirt", "chest", "wellstone_girl_shirt", "Wellston Girls' Shirt", "Chemise Wellston (filles)"),
    ("wellstone_girl_skirt", "legs", "wellstone_girl_uniform", "Wellston Girls' Skirt", "Jupe Wellston (filles)"),
    ("wellstone_girl_shoes", "feet", "wellstone_girl_uniform", "Wellston Girls' Shoes", "Chaussures Wellston (filles)"),
    ("wellstone_one_shoulder_blazer", "chest", "wellstone_one_shoulder", "Wellston One-Shoulder Blazer", 'Wellston One-Shoulder Blazer'),
    ("wellstone_seraphina_blazer", "chest", "wellstone_seraphina", "Wellston Off-Shoulder Blazer", 'Wellston Off-Shoulder Blazer'),
    ("agwin_blazer", "chest", "agwin_uniform", "Agwin Uniform Blazer", "Blazer d'uniforme Agwin"),
    ("agwin_shirt", "chest", "agwin_shirt", "Agwin Uniform Shirt", "Chemise d'uniforme Agwin"),
    ("agwin_trousers", "legs", "agwin_uniform", "Agwin Uniform Trousers", "Pantalon d'uniforme Agwin"),
    ("agwin_shoes", "feet", "agwin_uniform", "Agwin Uniform Shoes", "Chaussures d'uniforme Agwin"),
    ("agwin_girl_blazer", "chest", "agwin_uniform", "Agwin Girls' Blazer", "Blazer Agwin (filles)"),
    ("agwin_girl_shirt", "chest", "agwin_shirt", "Agwin Girls' Shirt", "Chemise Agwin (filles)"),
    ("agwin_girl_skirt", "legs", "agwin_girl_uniform", "Agwin Girls' Skirt", "Jupe Agwin (filles)"),
    ("agwin_girl_shoes", "feet", "agwin_girl_uniform", "Agwin Girls' Shoes", "Chaussures Agwin (filles)"),
    ("agwin_off_shoulder_blazer", "chest", "agwin_off_shoulder", "Agwin Off-Shoulder Blazer", 'Agwin Off-Shoulder Blazer'),
    ("agwin_one_shoulder_blazer", "chest", "agwin_one_shoulder", "Agwin One-Shoulder Blazer", 'Agwin One-Shoulder Blazer'),
    ("xstatic_top", "chest", "xstatic", "X-Static Suit Top", "Haut de combinaison X-Static"),
    ("xstatic_pants", "legs", "xstatic", "X-Static Suit Pants", "Pantalon de combinaison X-Static"),
    ("xstatic_boots", "feet", "xstatic", "X-Static Suit Boots", "Bottes de combinaison X-Static"),
    ("ember_leader_hat", "head", "ember_hat", "EMBER Leader Hat", "Chapeau de chef EMBER"),
    ("ember_leader_coat", "chest", "ember_leader", "EMBER Leader Coat", "Manteau de chef EMBER"),
    ("ember_leader_pants", "legs", "ember_leader", "EMBER Leader Pants", "Pantalon de chef EMBER"),
    ("ember_leader_boots", "feet", "ember_leader", "EMBER Leader Boots", "Bottes de chef EMBER"),
]


SETS = {
    "wellstone_uniform": ("#27408b", "#e8e8f0"), "wellstone_shirt": ("#f0f0f5", "#27408b"),
    "wellstone_girl_uniform": ("#27408b", "#c0304a"), "wellstone_girl_shirt": ("#f0f0f5", "#c0304a"),
    "wellstone_one_shoulder": ("#27408b", "#e8e8f0"), "wellstone_seraphina": ("#3a2f7a", "#e8c860"),
    "agwin_uniform": ("#8b1f2a", "#e8e8f0"), "agwin_shirt": ("#f0f0f5", "#8b1f2a"),
    "agwin_girl_uniform": ("#8b1f2a", "#303038"), "agwin_off_shoulder": ("#8b1f2a", "#e8c860"),
    "agwin_one_shoulder": ("#8b1f2a", "#f0f0f5"), "xstatic": ("#1c1c26", "#38c8ff"),
    "ember_hat": ("#2a2a32", "#e0602e"), "ember_leader": ("#2a2a32", "#e0602e"),
}


BLOCKS = [
    ("salt_block", "#f2f4f6", "#c8ced6", "Salt", "Sel", 0),
    ("barrier_block", "#4ad8ff", "#e0faff", "Aura Barrier", 'Aura Barrier', 0),
    ("barrier_block_am", "#ff8a3c", "#ffe0c0", "Aura Barrier", 'Aura Barrier', 0),
    ("particle_block", "#d94aff", "#f6c8ff", "Particle Lattice", "Treillis de particules", 7),
    ("conversion_machine", "#5a6270", "#ffc84a", "Conversion Machine", "Machine de conversion", 0),
    ("recovery_machine", "#5a6270", "#4adf7a", "Recovery Machine", 'Recovery Machine', 0),
    ("suppression_field", "#303038", "#ff4a4a", "Suppression Field", "Champ de suppression", 0),
    ("phylactery", "#2a1a3a", "#b36bff", "Phylactery", 'Phylactery', 0),
]

ARMAMENT_VARIANTS = ["oak", "stone", "iron", "gold", "diamond", "netherite", "burst", "sear", "fang", "warp", "rot"]
ARM_COL = {"oak": "#a9824a", "stone": "#8a8f99", "iron": "#d8dce4", "gold": "#ffd24a", "diamond": "#4af0e0",
           "netherite": "#4a4048", "burst": "#ff7a2a", "sear": "#ff3a1a", "fang": "#e8e0c8", "warp": "#b36bff", "rot": "#6a8a3a"}

def jitter(name, n):
    h = hashlib.md5(name.encode()).digest()
    return h[n % len(h)]

def canvas():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    return im, ImageDraw.Draw(im)

def outline(im):
    px = im.load()
    out = im.copy()
    po = out.load()
    for y in range(16):
        for x in range(16):
            if px[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and px[nx, ny][3] > 0:
                        po[x, y] = (20, 18, 28, 255)
                        break
    return out

def draw(kind, c1, c2, name):
    im, d = canvas()
    a, b = rgb(c1), rgb(c2)
    dk = shade(a, 0.6)
    if kind in ("sword",):
        for i in range(10):
            d.point((3 + i, 12 - i), a); d.point((4 + i, 12 - i), b)
        d.line((3, 13, 6, 10), fill=(110, 80, 40, 255), width=1); d.line((2, 14, 3, 13), fill=(80, 55, 30, 255))
        d.line((5, 9, 8, 12), fill=dk)
    elif kind == "axe":
        d.line((4, 13, 11, 4), fill=(120, 84, 44, 255), width=1)
        d.polygon([(8, 2), (13, 3), (14, 8), (10, 7)], fill=a); d.line((9, 3, 13, 4), fill=b)
    elif kind == "spear":
        d.line((2, 14, 11, 5), fill=(190, 190, 200, 255))
        d.polygon([(10, 6), (13, 1), (14, 2), (13, 5)], fill=a); d.line((11, 4, 13, 2), fill=b)
    elif kind == "club":
        d.line((3, 13, 11, 5), fill=a, width=2); d.ellipse((9, 2, 14, 7), fill=a); d.point((11, 4), b); d.point((3, 12), b)
    elif kind == "shard":
        d.polygon([(8, 1), (12, 6), (9, 14), (5, 8)], fill=a); d.polygon([(8, 1), (12, 6), (9, 8)], fill=b)
        d.line((8, 2, 8, 12), fill=shade(a, 1.2))
    elif kind == "vial":
        d.rectangle((6, 2, 9, 4), fill=(150, 150, 160, 255)); d.rectangle((5, 5, 10, 13), fill=(210, 230, 240, 255))
        d.rectangle((6, 8, 9, 12), fill=a); d.rectangle((6, 6, 9, 7), fill=b)
    elif kind == "syringe":
        d.line((3, 13, 11, 5), fill=(220, 225, 235, 255), width=2); d.line((4, 12, 9, 7), fill=a, width=1)
        d.line((11, 5, 14, 2), fill=(170, 175, 185, 255)); d.line((11, 3, 13, 5), fill=(120, 120, 130, 255))
        d.point((2, 14), (120, 120, 130, 255)); d.point((5, 11), b)
    elif kind == "book":
        d.rectangle((3, 2, 12, 13), fill=a); d.rectangle((3, 2, 4, 13), fill=shade(a, 0.7))
        d.rectangle((5, 4, 11, 11), fill=b); d.rectangle((6, 6, 10, 6), fill=a); d.rectangle((6, 8, 9, 8), fill=a)
    elif kind == "scroll":
        d.rectangle((4, 3, 11, 12), fill=b); d.rectangle((3, 2, 12, 3), fill=shade(b, 0.8)); d.rectangle((3, 12, 12, 13), fill=shade(b, 0.8))
        d.line((5, 5, 10, 5), fill=a); d.line((5, 7, 9, 7), fill=a); d.line((5, 9, 10, 9), fill=a)
    elif kind == "contract":
        d.rectangle((3, 2, 12, 13), fill=a); d.rectangle((4, 3, 11, 12), fill=shade(a, 1.1))
        for y in (4, 6, 8): d.line((5, y, 10, y), fill=b)
        d.ellipse((8, 9, 11, 12), fill=(170, 40, 40, 255))
    elif kind == "disc":
        d.ellipse((2, 2, 13, 13), fill=(30, 30, 38, 255)); d.ellipse((4, 4, 11, 11), fill=a); d.ellipse((6, 6, 9, 9), fill=(30, 30, 38, 255))
        d.arc((3, 3, 12, 12), 200, 290, fill=b)
    elif kind == "egg":
        d.ellipse((3, 1, 12, 14), fill=a)
        for (x, y) in ((6, 4), (9, 6), (5, 9), (8, 11), (10, 9), (7, 7)): d.rectangle((x, y, x + 1, y + 1), fill=b)
    elif kind == "core":
        d.ellipse((3, 3, 12, 12), fill=a); d.ellipse((5, 5, 10, 10), fill=b); d.rectangle((7, 1, 8, 3), fill=dk); d.rectangle((7, 12, 8, 14), fill=dk)
    elif kind == "sand":
        d.polygon([(3, 13), (12, 13), (10, 7), (5, 7)], fill=a); d.polygon([(5, 7), (10, 7), (8, 3)], fill=b)
        for (x, y) in ((6, 11), (9, 10), (7, 9)): d.point((x, y), shade(a, 0.7))
    elif kind == "link":
        d.ellipse((2, 4, 9, 11), outline=a, width=2); d.ellipse((7, 5, 14, 12), outline=b, width=2)
    elif kind == "rune":
        d.polygon([(4, 2), (11, 2), (12, 13), (5, 14)], fill=a); d.line((8, 4, 8, 12), fill=b); d.line((6, 7, 10, 5), fill=b); d.line((6, 10, 10, 8), fill=b)
    elif kind == "charm":
        d.line((8, 1, 8, 5), fill=(120, 90, 50, 255)); d.ellipse((4, 5, 11, 13), fill=a); d.ellipse((5, 7, 6, 8), fill=b); d.ellipse((9, 7, 10, 8), fill=b); d.line((6, 11, 9, 11), fill=b)
    elif kind == "soul":
        d.ellipse((4, 2, 11, 9), fill=a); d.polygon([(4, 7), (11, 7), (9, 14), (6, 14)], fill=a); d.rectangle((6, 5, 6, 6), fill=(20, 40, 60, 255)); d.rectangle((9, 5, 9, 6), fill=(20, 40, 60, 255))
        d.line((7, 3, 9, 3), fill=b)
    return outline(im)

def draw_outfit(piece, setname):
    im, d = canvas()
    a, b = rgb(SETS[setname][0]), rgb(SETS[setname][1])
    if piece == "chest":
        d.rectangle((4, 3, 11, 14), fill=a); d.rectangle((1, 3, 4, 9), fill=a); d.rectangle((11, 3, 14, 9), fill=a)
        d.rectangle((6, 3, 9, 5), fill=(0, 0, 0, 0)); d.line((7, 3, 8, 6), fill=b); d.line((4, 12, 11, 12), fill=b); d.point((7, 8), b); d.point((7, 10), b)
    elif piece == "legs":
        d.rectangle((4, 2, 11, 5), fill=a); d.rectangle((4, 6, 7, 14), fill=a); d.rectangle((8, 6, 11, 14), fill=a); d.line((4, 2, 11, 2), fill=b)
    elif piece == "feet":
        d.rectangle((2, 9, 6, 13), fill=a); d.rectangle((9, 9, 13, 13), fill=a); d.line((2, 13, 6, 13), fill=b); d.line((9, 13, 13, 13), fill=b)
    else:  # head
        d.rectangle((4, 6, 11, 10), fill=a); d.rectangle((1, 10, 14, 11), fill=a); d.line((4, 9, 11, 9), fill=b)
    return outline(im)

def draw_block(c1, c2, name):
    im = Image.new("RGBA", (16, 16), rgb(c1)); d = ImageDraw.Draw(im)
    a, b = rgb(c1), rgb(c2)
    for y in range(16):
        for x in range(16):
            j = jitter(name, x * 16 + y) % 24 - 12
            im.putpixel((x, y), tuple(max(0, min(255, v + j)) for v in a[:3]) + (255,))
    d.rectangle((0, 0, 15, 15), outline=shade(a, 0.55))
    d.rectangle((3, 3, 12, 12), outline=b); d.rectangle((6, 6, 9, 9), fill=b)
    return im

def armor_layer(setname, legs):
    im = Image.new("RGBA", (64, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    a, b = rgb(SETS[setname][0]), rgb(SETS[setname][1])
    rects = [(16, 16, 40, 32), (40, 16, 56, 32), (0, 16, 16, 32)] if legs else [(16, 16, 40, 32), (40, 16, 56, 32), (0, 16, 16, 32), (0, 0, 32, 16)]
    for (x0, y0, x1, y1) in rects:
        d.rectangle((x0, y0, x1 - 1, y1 - 1), fill=a)
    if legs:
        d.rectangle((0, 16, 15, 17), fill=b)
    else:
        d.rectangle((20, 20, 27, 31), fill=shade(a, 1.15)); d.rectangle((28, 20, 29, 31), fill=b); d.rectangle((16, 30, 39, 31), fill=b)
        d.rectangle((0, 28, 15, 31), fill=b)
    return im

def save(path, im):
    return
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)

def wjson(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)

def item_model(name, handheld=False, overrides=None):
    return
    m = {"parent": "minecraft:item/handheld" if handheld else "minecraft:item/generated", "textures": {"layer0": "unrp:item/" + name}}
    if overrides:
        m["overrides"] = overrides
    wjson(f"{A}/models/item/{name}.json", m)

def main():
    lang_en, lang_fr = {}, {}
    # --- objets
    for (iid, kind, c1, c2, en, fr) in ITEMS:
        if iid == "armament":
            continue
        save(f"{A}/textures/item/{iid}.png", draw(kind, c1, c2, iid))
        item_model(iid, handheld=kind in ("sword", "axe", "spear", "club"))
        lang_en[f"item.unrp.{iid}"] = en; lang_fr[f"item.unrp.{iid}"] = fr

    ov = []
    for i, v in enumerate(ARMAMENT_VARIANTS, start=1):
        save(f"{A}/textures/item/armament_{v}.png", draw("sword", ARM_COL[v], shade(rgb(ARM_COL[v]), 0.7) and "#" + "%02x%02x%02x" % shade(rgb(ARM_COL[v]), 0.7)[:3], v))
        if False: wjson(f"{A}/models/item/armament_{v}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": "unrp:item/armament_" + v}})
        ov.append({"predicate": {"custom_model_data": i}, "model": f"unrp:item/armament_{v}"})
    save(f"{A}/textures/item/armament.png", draw("sword", "#c9ccd3", "#7a7f8a", "armament"))
    item_model("armament", True, ov)
    lang_en["item.unrp.armament"] = "Armament"; lang_fr["item.unrp.armament"] = "Armement"

    cov = []
    for i, (suffix, col) in enumerate([("stamped", "#e9dcb4"), ("soul", "#bfeaff"), ("soul_stamped", "#bfeaff"), ("proposed", "#f3d9d9"), ("proposed_stamped", "#f3d9d9")], start=1):
        im = draw("contract", col, "#9a8450", "contract_" + suffix)
        if "stamped" in suffix:
            ImageDraw.Draw(im).ellipse((8, 9, 11, 12), fill=(200, 30, 30, 255))
        save(f"{A}/textures/item/contract_{suffix}.png", im)
        if False: wjson(f"{A}/models/item/contract_{suffix}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "unrp:item/contract_" + suffix}})
        cov.append({"predicate": {"custom_model_data": i}, "model": f"unrp:item/contract_{suffix}"})
    item_model("contract", False, cov)

    for (iid, piece, setname, en, fr) in OUTFITS:
        save(f"{A}/textures/item/{iid}.png", draw_outfit(piece, setname))
        item_model(iid)
        lang_en[f"item.unrp.{iid}"] = en; lang_fr[f"item.unrp.{iid}"] = fr
    for s in SETS:
        save(f"{A}/textures/models/armor/{s}_layer_1.png", armor_layer(s, False))
        save(f"{A}/textures/models/armor/{s}_layer_2.png", armor_layer(s, True))
    # --- blocs
    for (bid, c1, c2, en, fr, light) in BLOCKS:
        save(f"{A}/textures/block/{bid}.png", draw_block(c1, c2, bid))
        if bid != "phylactery": wjson(f"{D}/loot_tables/blocks/{bid}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"unrp:{bid}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
        lang_en[f"block.unrp.{bid}"] = en; lang_fr[f"block.unrp.{bid}"] = fr

    lang_en["itemGroup.unrp"] = "Unordinary RP"; lang_fr["itemGroup.unrp"] = "Unordinary RP"

    for fn, add in (("en_us.json", lang_en), ("fr_fr.json", lang_fr)):
        p = f"{A}/lang/{fn}"
        cur = json.load(open(p, encoding="utf-8"))
        cur.update(add)
        wjson(p, cur)
    n_items = len(ITEMS) + len(OUTFITS)
    print(f"ok : {n_items} objets, {len(BLOCKS)} blocs, {len(SETS)} jeux d'armure, {len(ARMAMENT_VARIANTS)} variantes d'armement")

if __name__ == "__main__":
    main()
