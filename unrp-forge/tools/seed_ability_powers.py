#!/usr/bin/env python3
"""Génère la PREMIÈRE version de data/unrp/unodex/ability_powers.json.

Chaque pouvoir = un interrupteur (touche R) + des MOUVEMENTS sur les touches Z X C V B,
comme la barre de pouvoirs du mod modèle :
  - "action" : une pression = un effet (coût en aura, recharge propre au mouvement)
  - "held"   : tant que la touche est maintenue, flux continu (coût en aura PAR SECONDE)
Pas de recharge sur l'activation. Les pouvoirs vedettes sont écrits à la main ci-dessous ;
les autres sont déduits de leur type d'effet (tools/input/ability_effects_seed.json).
Après génération, le JSON est la source de vérité : on l'édite à la main (--force écrase tout)."""
import json, sys, pathlib, copy

ROOT = pathlib.Path(__file__).resolve().parent.parent
DATA = ROOT / "src/main/resources/data/unrp/unodex"
OUT = DATA / "ability_powers.json"
seed = json.load(open(ROOT / "tools/input/ability_effects_seed.json", encoding="utf-8"))["effects"]
abilities = {a["id"]: a for a in json.load(open(DATA / "abilities.json", encoding="utf-8"))["abilities"]}

def E(i, s, a=0): return {"id": i, "amp": a, "secs": s}

def fx_of(e):
    d = {k: v for k, v in e.items() if k not in ("cooldown", "cost")}
    return d

def move(name, fx, input="action", cost=15, cooldown=40, k=None):
    m = {"name": name, "input": input, "cost": cost, "cooldown": cooldown, "fx": fx}
    if k is not None: m["k"] = k
    return m

def auto(aid):
    e = seed[aid]; kind = e["kind"]; name = abilities[aid]["name"]; cost = e.get("cost", 20)
    fx = fx_of(e)
    p = {"drain": 0.0, "passive": [], "moves": []}
    if kind == "buff":
        p["drain"] = max(1.0, round(cost / 8, 1))
        p["passive"] = [dict(x, secs=4) for x in e["self_effects"]]
        boost = [dict(x, amp=x["amp"] + 1, secs=8) for x in e["self_effects"]]
        p["moves"].append(move("Surcharge", {"kind": "buff", "self_effects": boost, "particle": e.get("particle", "crit")},
                               cost=cost, cooldown=200))
    elif kind in ("strike", "beam", "blast"):
        held_name = {"strike": "Rafale", "beam": "Flux continu", "blast": "Onde soutenue"}[kind]
        p["moves"].append(move(name, fx, cost=cost, cooldown=30 if kind != "blast" else 60))
        p["moves"].append(move(held_name, fx, "held", cost=max(2, round(cost * 0.5)), cooldown=0, k=0.25))
    elif kind == "heal":
        p["moves"].append(move(name, fx, cost=cost, cooldown=60))
        p["moves"].append(move("Flux de soin", fx, "held", cost=max(2, round(cost * 0.5)), cooldown=0, k=0.25))
    else:  # dash, teleport, debuff, pulse, scan, dispel
        p["moves"].append(move(name, fx, cost=cost, cooldown=40 if kind in ("dash", "teleport") else 80))
    return p

P = {}
for aid in abilities:
    P[aid] = auto(aid)

# ------------------------------------------------------------------ pouvoirs vedettes écrits à la main
P["lightning"] = {
    "drain": 1.0,
    "passive": [E("speed", 4, 0)],
    "moves": [
        move("Éclair", {"kind": "beam", "range": 40, "dmg": 9, "kb": 0.6, "bolt": True, "fire": 2, "particle": "electric_spark",
                         "effects": [E("slowness", 2, 1)]}, cost=12, cooldown=36),
        move("Contact électrique", {"kind": "strike", "range": 3, "dmg": 7, "kb": 0.4, "particle": "electric_spark",
                                     "effects": [E("slowness", 3, 3), E("weakness", 3, 0)]}, cost=10, cooldown=50),
        move("Course foudroyante", {"kind": "buff", "self_effects": [E("speed", 0.8, 4), E("jump_boost", 0.8, 1)],
                                     "particle": "electric_spark"}, "held", cost=4, cooldown=0, k=1.0),
        move("Rayon foudroyant", {"kind": "beam", "range": 30, "dmg": 7, "kb": 0.2, "particle": "electric_spark"},
             "held", cost=6, cooldown=0, k=0.25),
    ],
}
P["barrier"] = {
    "drain": 0.5,
    "passive": [E("resistance", 4, 0)],
    "moves": [
        move("Dresser une barrière", {"kind": "wall", "range": 3, "radius": 2, "amount": 3, "glow": 14, "block": "light_blue_stained_glass",
                                       "particle": "end_rod"}, cost=15, cooldown=40),
        move("Étendre la barrière", {"kind": "wall", "range": 3, "radius": 4, "amount": 4, "glow": 10, "block": "light_blue_stained_glass",
                                      "particle": "end_rod"}, cost=6, cooldown=8),
        move("Bélier de barrière", {"kind": "strike", "range": 5, "dmg": 8, "kb": 3.5, "particle": "end_rod",
                                      "effects": [E("slowness", 2, 2)]}, cost=20, cooldown=120),
        move("Purge mentale", {"kind": "heal", "amount": 0, "cleanse": True, "range": 6, "particle": "end_rod",
                                "effects": [E("absorption", 8, 1)]}, cost=10, cooldown=100),
    ],
}
P["modes"] = {
    "drain": 0.5,
    "passive": [],
    "modes": [
        {"name": "Équilibré", "drain": 0.5, "passive": [E("strength", 4, 0), E("resistance", 4, 0), E("speed", 4, 0)]},
        {"name": "Offensif", "drain": 1.5, "passive": [E("strength", 4, 2), E("speed", 4, 1), E("haste", 4, 1)]},
        {"name": "Défensif", "drain": 1.5, "passive": [E("resistance", 4, 3), E("absorption", 4, 1), E("slowness", 4, 0)]},
        {"name": "Destructeur", "drain": 3.0, "passive": [E("strength", 4, 4), E("glowing", 4, 0)]},
        {"name": "Vélocité", "drain": 2.0, "passive": [E("speed", 4, 3), E("jump_boost", 4, 1)]},
    ],
    "moves": [
        {"name": "Changer de mode", "input": "action", "cost": 0, "cooldown": 15, "special": "mode_next"},
        move("Scanner", {"kind": "scan", "radius": 12, "glow": 5, "report": True, "particle": "enchant"}, cost=8, cooldown=80),
        move("Frappe du mode", {"kind": "strike", "range": 3.5, "dmg": 8, "kb": 1.2, "particle": "sweep_attack"}, cost=10, cooldown=30),
    ],
}
P["phase_shift"] = {
    "drain": 1.0,
    "passive": [],
    "modes": [
        {"name": "Forme offensive", "drain": 1.5, "passive": [E("strength", 4, 2), E("speed", 4, 1), E("weakness", 0.1, 0)]},
        {"name": "Forme défensive", "drain": 1.5, "passive": [E("resistance", 4, 2), E("regeneration", 4, 0), E("slowness", 4, 0)]},
    ],
    "moves": [
        {"name": "Changer de forme", "input": "action", "cost": 0, "cooldown": 15, "special": "mode_next"},
        move("Frappe", {"kind": "strike", "range": 3.5, "dmg": 8, "kb": 1.0, "particle": "sweep_attack"}, cost=10, cooldown=30),
    ],
}
P["speed"] = {
    "drain": 1.0,
    "passive": [E("speed", 4, 1)],
    "moves": [
        move("Élan", {"kind": "dash", "power": 2.4, "up": 0.2, "particle": "cloud", "self_effects": [E("speed", 5, 2)]}, cost=10, cooldown=30),
        move("Ruée", {"kind": "buff", "self_effects": [E("speed", 0.8, 4)], "particle": "cloud"}, "held", cost=5, cooldown=0, k=1.0),
    ],
}

ALIASES = {"modes_atk": "modes", "modes_bal": "modes", "modes_def": "modes", "modes_dst": "modes", "modes_spd": "modes",
           "phase_shift_def": "phase_shift", "phase_shift_off": "phase_shift", "lightining": "lightning"}
for a in ALIASES:
    P.pop(a, None)

if OUT.exists() and "--force" not in sys.argv:
    sys.exit(f"{OUT} existe déjà (--force pour regénérer et perdre tes modifications)")
OUT.write_text(json.dumps({"_aide": "Un pouvoir par clé. Voir docs/ABILITIES.md.", "aliases": ALIASES, "powers": P},
                          ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
print(len(P), "pouvoirs,", sum(len(v["moves"]) for v in P.values()), "mouvements ->", OUT)
