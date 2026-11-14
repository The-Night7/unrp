#!/usr/bin/env python3
"""Génère la PREMIÈRE version de data/unrp/unodex/ability_effects.json (une entrée par capacité).

Après la génération, le JSON est la source de vérité : on l'édite à la main.
Le script refuse d'écraser un fichier existant (sauf --force)."""
import json, sys, pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DATA = ROOT / "src/main/resources/data/unrp/unodex"
OUT = DATA / "ability_effects.json"

abilities = json.load(open(DATA / "abilities.json", encoding="utf-8"))["abilities"]
wave = {a["id"]: a["wave"] for a in abilities}

def E(i, s, a=0):  # effet de potion : id, secondes, niveau (0 = I)
    return {"id": i, "amp": a, "secs": s}

def D(w):  # dégâts de base selon la vague (1 = faibles ... 6 = très forts)
    return 3 + 2 * w

FX = {}
def put(i, kind, **kw):
    FX[i] = dict(kind=kind, **kw)

def strike(ids, rng=3.5, d=0, kb=0.6, hits=1, particle="sweep_attack", cd=25, **kw):
    for i in ids.split():
        put(i, "strike", range=rng, dmg=D(wave[i]) + d if not kw.get("fixed") else kw["fixed"],
            kb=kb, hits=hits, particle=particle, cooldown=cd,
            **{k: v for k, v in kw.items() if k != "fixed"})

strike("blade claws hand_blade demon_blade demon_claw wrist_spike")
strike("crimson_thread", rng=5, particle="crit")
strike("dark_blades", rng=5, d=2, particle="smoke")
strike("blademaster", rng=6, d=3)
strike("spectral_claw", rng=4.5, particle="soul")
strike("contact_shred", rng=2.5, d=3, particle="crit", effects=[E("wither", 3)])
strike("clobber", d=2, kb=1.2)
strike("heavy_hitter", kb=1.2)
strike("strong_punch strong_kick", kb=1.5)
strike("super_strength", d=2, kb=2.0, particle="explosion")
strike("impact", kb=1.4, particle="poof")
strike("iron_knuckle", kb=1.0, self_effects=[E("resistance", 8)])
strike("knuckles", kb=0.9, self_effects=[E("strength", 8)])
strike("unknown_augmentation_ability", kb=1.5, particle="poof")
strike("quick_strike", hits=3, cd=15, fixed=2.0)
strike("barrage", rng=3, hits=6, cd=60, fixed=1.5, kb=0.3)
strike("dizzy_punch", effects=[E("nausea", 6), E("slowness", 2, 1)], particle="crit")
for i in ("dizzy_punch",):
    FX[i]["effects"][1]["id"] = "slowness"

def buff(i, effs, cd=400, particle="happy_villager", **kw):
    put(i, "buff", self_effects=effs, cooldown=cd, particle=particle, **kw)

buff("fortify", [E("strength", 30), E("resistance", 30), E("speed", 30)])
buff("superhuman", [E("strength", 25, 1), E("resistance", 25, 1), E("speed", 25, 1), E("regeneration", 25)])
buff("strength", [E("strength", 30, 1)])
buff("vigor", [E("strength", 30), E("haste", 30)])
buff("bodybuilder", [E("strength", 25, 2), E("resistance", 25)])
buff("pride", [E("strength", 20, 1), E("resistance", 20, 1), E("absorption", 20, 1)], particle="flame")
buff("armor_suit", [E("resistance", 40, 1), E("absorption", 40, 1)], particle="crit")
buff("rock_arm", [E("resistance", 30, 1)], particle="poof")
buff("stone_skin", [E("resistance", 20, 2), E("slowness", 20)], particle="poof")
buff("royal_guard", [E("resistance", 25, 2), E("absorption", 25, 2)], particle="flame")
buff("protector", [E("absorption", 20, 2), E("resistance", 20, 1)], particle="end_rod")
buff("barrier", [E("absorption", 15, 3), E("resistance", 15, 2)], particle="end_rod")
buff("conjure_disks", [E("absorption", 30, 1), E("resistance", 30)], particle="enchant")
buff("arachnid", [E("strength", 30), E("jump_boost", 30, 2), E("speed", 30)], particle="smoke")
buff("phase_shift_def", [E("resistance", 40, 2), E("slowness", 40)], particle="end_rod")
buff("phase_shift_off", [E("strength", 40, 2), E("speed", 40, 1)], particle="end_rod")
buff("modes_atk", [E("strength", 40, 2), E("haste", 40, 1)], particle="crit")
buff("modes_bal", [E("strength", 40), E("resistance", 40), E("speed", 40)], particle="end_rod")
buff("modes_def", [E("resistance", 40, 3), E("absorption", 40, 2), E("slowness", 40)], particle="crit")
buff("modes_dst", [E("strength", 40, 3), E("glowing", 40)], particle="flame")
buff("modes_spd", [E("speed", 40, 3), E("jump_boost", 40, 1)], particle="cloud")
buff("debt", [E("absorption", 20, 3), E("resistance", 20, 1)], particle="soul")
buff("duplication", [E("speed", 20, 1), E("resistance", 20)], particle="cloud")
buff("invisibility", [E("invisibility", 30), E("speed", 30)], particle="cloud")
buff("ghost", [E("invisibility", 20), E("speed", 20, 1), E("night_vision", 20)], particle="soul")
buff("shadow_eclipse", [E("invisibility", 25), E("speed", 25), E("night_vision", 25)], particle="smoke")
buff("alchimie", [E("haste", 20, 1), E("regeneration", 20)], particle="enchant")
buff("flash_forward", [E("speed", 10, 1), E("resistance", 10, 1)], cd=300, particle="end_rod")

def dash(i, power, up=0.2, effs=(), cd=100, particle="cloud"):
    put(i, "dash", power=power, up=up, self_effects=list(effs), cooldown=cd, particle=particle)

dash("speed", 2.4, effs=[E("speed", 6, 2)])
dash("fleetstep", 2.0, effs=[E("speed", 8, 1)])
dash("catch_up", 2.8)
dash("charge", 2.4, effs=[E("strength", 6, 1)])
dash("umbral_shift", 2.0, effs=[E("invisibility", 3)], particle="smoke")
dash("hair_growth", 1.6, up=0.7, particle="crit")

put("teleportation", "teleport", range=12, cooldown=60, particle="portal")
put("portals", "teleport", range=20, cooldown=80, particle="reverse_portal")

def beam(i, d=0, rng=24, cd=40, particle="crit", mult=1.0, **kw):
    put(i, "beam", range=rng, dmg=round((D(wave[i]) + d) * mult, 1), cooldown=cd, particle=particle, **kw)

beam("non_specifiee", particle="angry_villager")
beam("needles", mult=0.6, rng=20, cd=12)
beam("tir_concentre", rng=30, cd=50, explosion=1.5, particle="flame")
beam("arcane_shot", rng=40, particle="enchanted_hit")
beam("archer", d=2, rng=60, cd=60)
beam("catalyst_ray", particle="end_rod")
beam("charge_shot", d=3, cd=80, particle="end_rod")
beam("lazor", d=4, rng=40, cd=80, particle="end_rod")
beam("missiles", rng=40, cd=80, explosion=2.0, particle="smoke")
beam("phantom_fist", rng=16, kb=2.0, particle="poof")
beam("phantom_push", rng=10, mult=0.2, kb=3.5, up=0.4, particle="poof")
beam("crescent_slash", d=2, rng=10, kb=1.0, particle="sweep_attack")
beam("energy_beam", d=2, particle="crit")
beam("energy_discharge", d=2, particle="crit")
beam("grenadier", rng=20, cd=120, explosion=2.5, particle="flame")
beam("shadow_flame", fire=4, particle="soul_fire_flame")
beam("faucheuse_fantome", d=2, particle="soul", effects=[E("wither", 3)])
beam("white_hole", kb=3.0, up=0.3, particle="end_rod")
beam("heat_palm", rng=4, fire=5, particle="flame")
beam("lightning", d=4, rng=30, particle="electric_spark", fire=2)
beam("lightining", d=4, rng=30, particle="electric_spark", fire=2)
beam("psy_blast", d=2, rng=16, particle="witch", effects=[E("nausea", 3)])
beam("psychopomp", particle="soul", effects=[E("weakness", 6)])
beam("telekinesis", mult=0.2, up=1.0, kb=2.5, particle="enchant", effects=[E("levitation", 2)])
beam("particles", rng=20, particle="end_rod")
beam("demolition", rng=30, cd=140, explosion=3.0, particle="smoke")
beam("death_fire", d=2, rng=20, cd=100, fire=8, particle="flame")
beam("icicle", particle="snowflake", effects=[E("slowness", 4, 2)])
beam("life_steal", rng=4, lifesteal=1.0, particle="heart")

def blast(i, radius, d=0, kb=1.5, up=0.4, cd=100, particle="poof", mult=1.0, **kw):
    put(i, "blast", radius=radius, dmg=round((D(wave[i]) + d) * mult, 1), kb=kb, up=up,
        cooldown=cd, particle=particle, **kw)

blast("azure_storm", 8, kb=2.5, up=0.8, particle="cloud")
blast("whirlwind", 6, kb=2.0, up=1.0, particle="cloud")
blast("repulsion", 5, kb=3.0, particle="poof")
blast("shockwave", 8, kb=2.2, particle="explosion")
blast("tremor", 7, kb=0.5, up=0.4, particle="poof", effects=[E("slowness", 3, 2)])
blast("explosion", 6, mult=0.0, explosion=3.0, kb=0, up=0)
blast("minefield", 10, mult=0.7, explosion=2.0, particle="smoke")
blast("ground_pound", 5, kb=1.0, up=0.6, particle="explosion")
blast("graviton", 8, mult=0.0, kb=-1.5, up=0.0, particle="portal",
      effects=[E("slowness", 5, 3)])
blast("liquids_manipulation", 7, mult=0.5, kb=1.6, up=0.2, particle="falling_water",
      effects=[E("slowness", 4, 1)])
blast("hydrofreeze", 6, particle="snowflake", effects=[E("slowness", 5, 4)])

def debuff(i, effs, rng=16, d=0.0, root=0, particle="witch", cd=140):
    put(i, "debuff", range=rng, dmg=d, effects=effs, root=root, cooldown=cd, particle=particle)

debuff("conjure_vines", [], d=D(4), root=5, particle="happy_villager")
debuff("botanist", [], rng=18, d=2, root=8, particle="happy_villager")
debuff("imprison", [], rng=14, root=10)
debuff("command", [E("slowness", 6, 4), E("weakness", 6)], particle="enchant")
debuff("hypnosis", [E("slowness", 6, 3), E("blindness", 4), E("nausea", 6)], particle="enchant")
debuff("sensory_control", [E("blindness", 8), E("nausea", 8)], particle="smoke")
debuff("god_s_mind", [E("nausea", 10), E("weakness", 10, 2), E("slowness", 10, 2)], rng=40)
debuff("memory_recall", [E("nausea", 8), E("slowness", 8, 1)])
debuff("possession", [E("slowness", 10, 5), E("weakness", 10, 3)], rng=8)
debuff("sanity_control_hurt", [E("nausea", 6), E("weakness", 8, 1)], d=4)
debuff("sanity_control_bal", [E("slowness", 5, 1), E("nausea", 3)])
debuff("troll", [E("nausea", 8), E("slowness", 4, 1), E("blindness", 2)], rng=20)

def pulse(i, radius, effs, selfe=(), d=0.0, particle="witch", cd=200):
    put(i, "pulse", radius=radius, dmg=d, effects=effs, self_effects=list(selfe),
        cooldown=cd, particle=particle)

pulse("paralysis", 12, [E("slowness", 6, 7), E("weakness", 6, 2)])
pulse("time_manipulation", 10, [E("slowness", 8, 3), E("weakness", 8)],
      [E("speed", 8, 2), E("haste", 8, 1)], particle="reverse_portal", cd=300)
pulse("nightmare", 8, [E("darkness", 8), E("weakness", 8, 1), E("nausea", 5)], particle="smoke")
pulse("mistdream", 6, [E("blindness", 6), E("slowness", 6, 1), E("darkness", 6)], particle="cloud")
pulse("illumination", 10, [E("glowing", 8)], [E("night_vision", 40)], particle="end_rod", cd=240)
pulse("digital_hive", 8, [E("slowness", 6, 1), E("weakness", 6), E("glowing", 6)],
      d=D(5), particle="electric_spark")

def heal(i, amount, rng=0, effs=(), cleanse=False, cd=200, particle="heart"):
    put(i, "heal", amount=amount, range=rng, effects=list(effs), cleanse=cleanse,
        cooldown=cd, particle=particle)

heal("regeneration", 10, effs=[E("regeneration", 8, 2)])
heal("healing", 20, rng=8, effs=[E("regeneration", 10, 3)], cleanse=True, cd=300)
heal("heal_link", 6, rng=12, effs=[E("regeneration", 8, 1)], cd=100)
heal("energy_conduit", 8, rng=12, effs=[E("absorption", 20, 1)])
heal("vitalize", 10, effs=[E("regeneration", 10, 1)])
heal("sanity_control_heal", 8, rng=8, effs=[E("regeneration", 6)])

def scan(i, radius, glow=0, report=True, selfe=(), cd=200, particle="enchant"):
    put(i, "scan", radius=radius, glow=glow, report=report, self_effects=list(selfe),
        cooldown=cd, particle=particle)

scan("scanner", 24, glow=8)
scan("ability_gauging", 5)
scan("aura_deity", 40, glow=10)
scan("aura_manipulation", 40)
scan("channel_master", 30)
scan("clairvoyance", 32, glow=10, report=False, selfe=[E("night_vision", 30)])
scan("lie_detection", 8)
scan("detection_de_mensonge", 8)
scan("medium", 24)
scan("unknown", 40, glow=10, report=False)
scan("telepathy", 30)
scan("hunter", 32, glow=10, report=False, selfe=[E("speed", 20)])
scan("predator", 32, glow=10, report=False, selfe=[E("speed", 20)])

put("cancellation", "dispel", range=30, cooldown=160, particle="end_rod")

missing = [a["id"] for a in abilities if a["id"] not in FX]
extra = [i for i in FX if i not in wave]
if missing or extra:
    sys.exit(f"manquantes: {missing} ; inconnues: {extra}")

# coût en aura selon la vague, sauf si déjà précisé
for i, fx in FX.items():
    fx.setdefault("cost", 10 + 5 * wave[i])

if OUT.exists() and "--force" not in sys.argv:
    sys.exit(f"{OUT} existe déjà : on ne l'écrase pas (--force pour regénérer)")
doc = {"_aide": "Un effet par capacité (clé = id de abilities.json). Voir docs/ABILITIES.md.",
       "effects": {a["id"]: FX[a["id"]] for a in abilities}}
OUT.write_text(json.dumps(doc, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
print(len(FX), "effets écrits dans", OUT)
