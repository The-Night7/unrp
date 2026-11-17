"""Add missing power looks and chargeable attacks. Existing look definitions are retained."""
import json, re, pathlib

D = pathlib.Path(__file__).resolve().parent.parent / "src/main/resources/data/unrp/unodex"
pw = json.loads((D / "ability_powers.json").read_text(encoding="utf-8"))
ab = json.loads((D / "abilities.json").read_text(encoding="utf-8"))
ab = ab if isinstance(ab, list) else ab.get("abilities", list(ab.values()))
info = {a["id"]: a for a in ab}

COLORS = {"red": "#E02424", "crimson": "#B3121F", "blue": "#3D8BFF", "light blue": "#8FD8FF", "cyan": "#33E0E0", "green": "#4CD964",
          "yellow": "#FFE45C", "golden": "#FFC83D", "gold": "#FFC83D", "orange": "#FF8A1F", "purple": "#A35CFF", "violet": "#A35CFF",
          "pink": "#FF7AC8", "white": "#F2F6FF", "black": "#2A2233", "silver": "#C9D2DC", "grey": "#9AA0A6", "gray": "#9AA0A6", "brown": "#8B5A2B"}
KEYS = [
    (r"lightning|electric|thunder|shock", "#FFE45C", "electric_spark", True),
    (r"fire|flame|burn|heat|incinerat|explo", "#FF6A1F", "flame", False),
    (r"\bice\b|cold|frost|freez|snow", "#9FE8FF", "snowflake", False),
    (r"wind|air\b|gust|tornado|sonic|sound", "#E8F4F4", "cloud", False),
    (r"water|rain|liquid|ocean|wave", "#3D8BFF", "bubble_pop", False),
    (r"poison|toxic|acid|venom", "#6BE04A", "happy_villager", False),
    (r"shadow|dark|void|nightmare", "#4B2A6E", "large_smoke", False),
    (r"\blight\b|radian|laser|sun|photon|holy", "#FFF3A0", "end_rod", False),
    (r"blood|vampir|life.?steal", "#B3121F", "crimson_spore", False),
    (r"plant|nature|wood|vine|flower|tree", "#4CAF50", "happy_villager", False),
    (r"mental|mind|psychic|telepath|illusion|memory|emotion|hypno", "#B07CFF", "enchant", False),
    (r"gravity|telekin|magnet|force", "#7C8CFF", "enchant", False),
    (r"barrier|shield|creat|construct", "#7FD4FF", "end_rod", False),
    (r"invisib|camouflage", "#D8E6FF", "cloud", False),
    (r"heal|regenerat|restor", "#7CFFB2", "heart", False),
    (r"speed|fast|agility|teleport", "#F2F6FF", "cloud", False),
]
NATURE = {"attack": ("#FF4D3D", "crit"), "defense": ("#7FB6FF", "end_rod"), "agility": ("#E8F4F4", "cloud"),
          "support": ("#7CFFB2", "heart"), "utility": ("#C9D2DC", "enchant")}

HAND = {
    "lightning": {"color": "#FFE45C", "particle": "electric_spark", "jagged": True},
    "barrier": {"color": "#7FD4FF", "particle": "end_rod", "jagged": False},
    "invisibility": {"color": "#D8E6FF", "particle": "cloud", "jagged": False},
}

def look_for(pid):
    if pid in HAND:
        return dict(HAND[pid])
    a = info.get(pid, {})
    text = ((a.get("description") or "") + " " + (a.get("name") or "") + " " + pid).lower()
    m = re.search(r"\b(light blue|crimson|red|blue|cyan|green|yellow|golden|gold|orange|purple|violet|pink|white|black|silver|gr[ae]y|brown)\b[^.]{0,12}\b(aura|glow|light|energy|flame|fire|lightning|beam)", text)
    col = COLORS[m.group(1)] if m else None
    part, jag = None, False
    for rx, c, p, j in KEYS:
        if re.search(rx, text):
            col = col or c
            part, jag = p, j
            break
    n = NATURE.get(a.get("nature", ""), ("#B07CFF", "enchant"))
    return {"color": col or n[0], "particle": part or n[1], "jagged": jag}

changed = 0
for pid, p in pw["powers"].items():
    if "look" not in p:
        p["look"] = look_for(pid)
        changed += 1
    for m in p["moves"]:
        fx = m.get("fx") or {}
        if m.get("input") == "action" and fx.get("kind") in ("strike", "beam", "blast", "pulse") and not m.get("special"):
            m["input"] = "charge"
            m["charge"] = 40 if fx.get("kind") == "beam" else 30
(D / "ability_powers.json").write_text(json.dumps(pw, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
print(changed, "looks added;", sum(1 for p in pw["powers"].values() for m in p["moves"] if m.get("input") == "charge"), "mouvements chargeables")
