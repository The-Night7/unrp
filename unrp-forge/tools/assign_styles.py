"""Assign the visual styles and colours listed below to power definitions."""
import json, pathlib

P = pathlib.Path(__file__).resolve().parent.parent / "src/main/resources/data/unrp/unodex/ability_powers.json"

STYLE_COLOR = {
    "lightning": "#FFE45C", "fire": "#FF6A1F", "ice": "#9FE8FF", "water": "#3D8BFF", "wind": "#DDF3F0",
    "shadow": "#5B2E8C", "light": "#FFF3A0", "blood": "#C0182A", "nature": "#4CAF50", "mind": "#B07CFF",
    "gravity": "#7C6CFF", "barrier": "#7FD4FF", "cloak": "#D8E6FF", "speed": "#E6F6FF", "strength": "#FF5A3D",
    "heal": "#7CFFB2", "poison": "#8CE04A", "earth": "#B08050", "metal": "#C9D2DC", "sound": "#FFD0F0",
    "space": "#9D7CFF", "energy": "#B07CFF",
}


MAP = {
    "ability_gauging": ("mind", None), "alchimie": ("metal", "#E8C25A"), "arachnid": ("metal", "#8B6BAA"),
    "arcane_shot": ("energy", "#8FD0FF"), "archer": ("energy", "#FFD27A"), "armor_suit": ("metal", None),
    "aura_deity": ("mind", "#C08CFF"), "aura_manipulation": ("mind", "#A35CFF"), "azure_storm": ("wind", "#7FC8FF"),
    "barrage": ("strength", None), "barrier": ("barrier", "#7FD4FF"), "blade": ("metal", None),
    "blademaster": ("metal", "#8FD8FF"), "bodybuilder": ("strength", None), "botanist": ("nature", None),
    "cancellation": ("mind", "#6FE3FF"), "catalyst_ray": ("energy", "#FF7A3D"), "catch_up": ("speed", None),
    "channel_master": ("mind", None), "charge": ("speed", "#FFB347"), "charge_shot": ("energy", "#8FD0FF"),
    "clairvoyance": ("mind", "#B9A8FF"), "claws": ("metal", None), "clobber": ("strength", "#4D8BFF"),
    "command": ("mind", "#FF6FA8"), "conjure_disks": ("energy", "#8FD0FF"), "conjure_vines": ("nature", "#7CFF8A"),
    "contact_shred": ("blood", None), "crescent_slash": ("light", "#5AA8FF"), "crimson_thread": ("blood", "#E0203A"),
    "dark_blades": ("shadow", None), "death_fire": ("fire", "#3D7BFF"), "debt": ("mind", "#E8C25A"),
    "demolition": ("earth", "#FF8A3D"), "demon_blade": ("shadow", "#B3121F"), "demon_claw": ("shadow", "#B3121F"),
    "detection_de_mensonge": ("mind", None), "digital_hive": ("mind", "#6FE3FF"), "dizzy_punch": ("strength", "#FFD24D"),
    "duplication": ("space", "#D8E6FF"), "energy_beam": ("energy", "#FF3B3B"), "energy_conduit": ("heal", "#FFD27A"),
    "energy_discharge": ("energy", "#FF3B3B"), "explosion": ("fire", "#FF8A3D"), "faucheuse_fantome": ("shadow", "#8FE3D0"),
    "flash_forward": ("mind", None), "fleetstep": ("speed", None), "fortify": ("strength", "#FFC94D"),
    "ghost": ("cloak", "#BFD8FF"), "god_s_mind": ("mind", "#FFD27A"), "graviton": ("gravity", None),
    "grenadier": ("energy", "#FF3DCB"), "ground_pound": ("earth", None), "hair_growth": ("nature", "#C9A27A"),
    "hand_blade": ("metal", None), "heal_link": ("heal", None), "healing": ("heal", None),
    "heat_palm": ("fire", None), "heavy_hitter": ("strength", None), "hunter": ("mind", "#FF5050"),
    "hydrofreeze": ("water", "#5FB8FF"), "hypnosis": ("mind", "#FF7AD9"), "icicle": ("ice", None),
    "illumination": ("light", "#FFFFFF"), "impact": ("strength", "#C9A8FF"), "imprison": ("barrier", "#C08CFF"),
    "invisibility": ("cloak", "#D8E6FF"), "iron_knuckle": ("metal", None), "knuckles": ("strength", None),
    "lazor": ("light", "#9CFF3D"), "lie_detection": ("mind", None), "life_steal": ("blood", None),
    "lightning": ("lightning", "#FFE45C"), "liquids_manipulation": ("water", None), "medium": ("mind", None),
    "memory_recall": ("mind", None), "minefield": ("earth", "#FF8A3D"), "missiles": ("fire", "#FF8A3D"),
    "mistdream": ("wind", "#CFE0FF"), "modes": ("energy", "#FFB347"), "needles": ("metal", "#9FE8FF"),
    "nightmare": ("shadow", None), "non_specifiee": ("energy", "#E02424"), "paralysis": ("mind", "#FFE45C"),
    "particles": ("energy", None), "phantom_fist": ("energy", "#B0D8FF"), "phantom_push": ("energy", "#B0D8FF"),
    "phase_shift": ("space", None), "portals": ("space", None), "possession": ("mind", "#8F6BFF"), "predator": ("mind", "#FF5050"),
    "pride": ("light", "#FFC83D"), "protector": ("barrier", None), "psy_blast": ("mind", None),
    "psychopomp": ("energy", None), "quick_strike": ("speed", None), "regeneration": ("heal", None),
    "repulsion": ("gravity", None), "rock_arm": ("earth", None), "royal_guard": ("metal", "#C0182A"),
    "sanity_control_bal": ("mind", None), "sanity_control_heal": ("heal", None), "sanity_control_hurt": ("mind", "#FF5050"),
    "scanner": ("mind", "#3DDC97"), "sensory_control": ("shadow", None), "shadow_eclipse": ("shadow", None),
    "shadow_flame": ("fire", "#7B3DFF"), "shockwave": ("sound", "#CFE8FF"), "spectral_claw": ("shadow", None),
    "speed": ("speed", None), "stone_skin": ("earth", None), "strength": ("strength", None),
    "strong_kick": ("strength", None), "strong_punch": ("strength", None), "super_strength": ("strength", None),
    "superhuman": ("strength", None), "telekinesis": ("gravity", "#B07CFF"), "telepathy": ("mind", None),
    "teleportation": ("space", None), "time_manipulation": ("space", "#FFD27A"), "tir_concentre": ("energy", None),
    "tremor": ("earth", None), "troll": ("mind", None), "umbral_shift": ("shadow", None), "unknown": ("energy", None),
    "unknown_augmentation_ability": ("strength", None), "vigor": ("strength", None), "vitalize": ("heal", None),
    "whirlwind": ("wind", None), "white_hole": ("gravity", "#FFFFFF"), "wrist_spike": ("metal", None),
}

d = json.loads(P.read_text(encoding="utf-8"))
missing = [k for k in d["powers"] if k not in MAP]
for pid, p in d["powers"].items():
    if pid not in MAP:
        continue
    style, color = MAP[pid]
    look = p.setdefault("look", {})
    look["style"] = style
    look["color"] = color or STYLE_COLOR[style]
    look["jagged"] = style == "lightning"
    look.pop("sprite", None)
P.write_text(json.dumps(d, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
print(len(d["powers"]) - len(missing), "powers styled; without a style:", missing)
