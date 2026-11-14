#!/usr/bin/env python3
"""
build_unodex.py : export de la feuille Unodex -> fichiers JSON lus par le mod.

Usage (depuis la racine du projet) :
    python3 tools/build_unodex.py                       # valeurs par défaut
    python3 tools/build_unodex.py --check               # valide sans écrire
    python3 tools/build_unodex.py --input autre.txt

Entrée  : tools/input/unodex-export.txt  (export de la feuille, onglets séparés par
          "## Sheet name: <onglet>"). Seuls les onglets 'unodex' et 'unodex-rp' sont lus.
Sortie  : src/main/resources/data/unrp/unodex/{schools,abilities}.json
          src/main/resources/data/unrp/unodex/characters/<categorie>.json
          docs/generated/REPORT.md
Éditions manuelles : tools/overrides/{schools,characters,abilities}.json
          (fusionnées par-dessus les données générées, jamais écrasées).
Aucune dépendance : bibliothèque standard Python 3.9+.
"""
import argparse
import collections
import csv
import io
import json
import re
import sys
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = ROOT / "src/main/resources/data/unrp/unodex"

# Onglets sources -> catégorie de personnage. Pour ajouter un onglet, l'ajouter ici.
ORIGIN_CATEGORY = {
    "unodex": "lore",       # personnages canon du webtoon
    "unodex-rp": "rp",      # personnages du RP
}
# Si un personnage apparaît dans plusieurs onglets, la catégorie retenue suit cet ordre.
CATEGORY_PRIORITY = ["rp", "lore"]
# Anciennes catégories dont les fichiers générés doivent être supprimés s'ils traînent.
STALE_CATEGORIES = ["shine_city", "npc"]

TIER = {
    "cripple": "cripple", "low tier": "low", "mid tier": "mid", "elite tier": "elite",
    "high tier": "high", "god tier": "god", "inconnu": "unknown", "": "unknown",
}
TIER_ORDER = {"cripple": 0, "low": 1, "mid": 2, "elite": 3, "high": 4, "god": 5, "unknown": 9}
TIER_WAVE = {"cripple": 1, "low": 1, "mid": 2, "elite": 3, "high": 4, "god": 5, "unknown": 6}

ABILITY_TYPE = {
    "quantum": "quantum", "quantique": "quantum", "emission": "emission", "creation": "creation",
    "enhancement": "enhancement", "mental": "mental", "meta": "meta", "status": "status",
    "statut": "status", "curative": "curative", "cure": "curative", "soin": "curative",
    "detection": "detection", "offensive": "offensive", "offensif": "offensive",
    "defensif": "defensive", "piege": "trap", "manipulation": "manipulation",
    "transformation": "transformation", "alteration": "alteration", "soutien": "support",
    "predator": "predator", "non specifiee": "unknown", "unknown": "unknown", "///": "unknown",
    "none": "none", "": "unknown",
}
NATURE = {
    "attaque": "attack", "defense": "defense", "vivacite": "agility", "support": "support",
    "soutien": "support", "stratege": "strategist", "none": None, "": None,
}
ROLE = {
    "roi": "king", "king": "king", "reine": "queen", "queen": "queen", "valet": "jack",
    "jack": "jack", "soigneur": "healer", "healer": "healer", "ancien": "alumni",
    "ancien eleve": "alumni", "cache": "hidden",
}
# Identifiants de lieux. kind : school = établissement, organization = groupe/faction.
# Les "kind" marqués non confirmés sont à relire (voir docs/DATA.md).
KNOWN_PLACES = {
    "wellston": ("Wellston", "school", True),
    "wellston_university": ("Wellston University", "school", True),
    "agwin": ("Agwin", "school", True),
    "westview": ("Westview", "school", True),
    "rowden": ("Rowden", "school", True),
    "ember": ("Ember", "organization", False),
    "new_bostin": ("New Bostin", "school", True),
    "matignon": ("Matignon", "school", True),
    "st_clair": ("St Clair", "school", True),
    "st_denis": ("St Denis", "school", True),
    "voile": ("Voile", "organization", False),
    "blizzard": ("Blizzard", "organization", False),
    "vigilante": ("Vigilante", "organization", False),
    "bureau": ("Bureau", "organization", False),
}


# ----------------------------------------------------------------- utilitaires
def strip_accents(s):
    return "".join(c for c in unicodedata.normalize("NFKD", s) if not unicodedata.combining(c))


def slug(s):
    s = strip_accents(s or "").lower()
    return re.sub(r"[^a-z0-9]+", "_", s).strip("_")


def norm_key(s):
    return strip_accents((s or "").strip()).lower()


def to_float(s):
    if s is None:
        return None
    s = str(s).strip().replace(",", ".").replace("%", "")
    if not s:
        return None
    try:
        return float(s)
    except ValueError:
        return None


def clean(s):
    s = (s or "").strip()
    return s if s and s.lower() not in {"non disponible", "non", "n/a"} else None


def split_top_level(s, sep=","):
    out, depth, cur = [], 0, ""
    for ch in s:
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth = max(0, depth - 1)
        if ch == sep and depth == 0:
            out.append(cur)
            cur = ""
        else:
            cur += ch
    out.append(cur)
    return [x.strip() for x in out if x.strip()]


def deep_merge(base, patch):
    """Fusion récursive : les dict sont fusionnés, tout le reste est remplacé."""
    for k, v in patch.items():
        if isinstance(v, dict) and isinstance(base.get(k), dict):
            deep_merge(base[k], v)
        else:
            base[k] = v
    return base


# ------------------------------------------------------------------- lecture
def read_source_rows(path):
    """Lit les onglets sources et ajoute à chaque ligne la clé "Table d'origine"."""
    raw = Path(path).read_text(encoding="utf-8")
    parts = re.split(r"^## Sheet name: (.+)$", raw, flags=re.M)
    tabs = {parts[i].strip(): parts[i + 1].lstrip("\n") for i in range(1, len(parts), 2)}
    rows = []
    for tab in ORIGIN_CATEGORY:
        if tab not in tabs:
            sys.exit(f"Onglet '{tab}' introuvable dans l'export (onglets présents : {', '.join(tabs)}).")
        for r in csv.DictReader(io.StringIO(tabs[tab])):
            r["Table d'origine"] = tab
            rows.append(r)
    return rows


def parse_height_sex(s):
    s = (s or "").strip()
    m = re.match(r"^(\d+)\s*cm\s*\(([HF])\)", s)
    if m:
        return int(m.group(1)), m.group(2)
    m = re.match(r"^Inconnue\s*\(([HF])\)", s, flags=re.I)
    return (None, m.group(1).upper()) if m else (None, None)


def parse_schools(raw, warnings, who):
    """'St Clair (caché), St Clair (queen)' -> [{school, roles, region}]"""
    members = {}
    for piece in split_top_level(raw or ""):
        m = re.match(r"^(.*?)\s*(?:\((.*)\))?\s*$", piece)
        name, roles_raw = m.group(1).strip(), m.group(2) or ""
        region = None
        if " : " in name:
            name, region = [x.strip() for x in name.split(" : ", 1)]
        sid = slug(name)
        if not sid:
            continue
        if sid not in KNOWN_PLACES:
            warnings.append(f"Lieu inconnu '{name}' (personnage {who}) : ajouté automatiquement.")
        entry = members.setdefault(sid, {"school": sid, "roles": [], "region": region})
        if region and not entry["region"]:
            entry["region"] = region
        for r in [x.strip() for x in roles_raw.split(",") if x.strip()]:
            role = ROLE.get(norm_key(r), slug(r))
            if role not in entry["roles"]:
                entry["roles"].append(role)
    return list(members.values())


def stat_block(row, suffix=""):
    keys = [("power", "Power"), ("defense", "Defense"), ("recovery", "Recovery"),
            ("trick", "Trick"), ("speed", "Speed")]
    return {k: to_float(row.get(c + suffix)) for k, c in keys}


# --------------------------------------------------------------- construction
def build(rows, warnings):
    chars = collections.OrderedDict()
    skipped = 0
    for row in rows:
        name = (row.get("Name") or "").strip()
        origin = (row.get("Table d'origine") or "").strip()
        category = ORIGIN_CATEGORY.get(origin)
        if not name or category is None:
            skipped += 1
            continue
        cid = slug(name)
        if not cid:
            warnings.append(f"Nom illisible ignoré : {name!r}")
            continue
        ability_name = (row.get("Ability") or "").strip()
        ability_id = None if norm_key(ability_name) in {"", "n/a", "none"} else slug(ability_name)
        snap = {
            "level": to_float(row.get("Level")),
            "tier": TIER.get(norm_key(row.get("Tier")), "unknown"),
            "ability": ability_name or None,
            "ability_id": ability_id,
            "type": ABILITY_TYPE.get(norm_key(row.get("Type")), slug(row.get("Type")) or "unknown"),
            "nature": NATURE.get(norm_key(row.get("Nature")), slug(row.get("Nature")) or None),
            "stats": stat_block(row),
            "rate": to_float(row.get("Taux")),
            "bonuses": stat_block(row, " Bonus"),
            "description": clean(row.get("Ability Description")),
            "reference": clean(row.get("Reference")),
            "graph": clean(row.get("Graph Link")),
            "image": clean(row.get("Image")),
            "school_raw": clean(row.get("École")),
            "source": origin,
        }
        c = chars.setdefault(cid, {"id": cid, "name": name, "snapshots": [], "_rows": []})
        c["_rows"].append((category, row, snap))
        key = (snap["level"], snap["ability_id"], origin)
        if all((s["level"], s["ability_id"], s["source"]) != key for s in c["snapshots"]):
            c["snapshots"].append(snap)

    characters = []
    for cid, c in chars.items():
        c["snapshots"].sort(key=lambda s: (s["level"] is None, s["level"] or 0))
        last = max(c["_rows"], key=lambda t: (t[2]["level"] or 0))
        _, last_row, last_snap = last
        cats = {t[0] for t in c["_rows"]}
        category = next(p for p in CATEGORY_PRIORITY if p in cats)
        height, sex = parse_height_sex(last_row.get("Taille"))
        # École/rang : on prend la ligne la plus récente de l'onglet prioritaire (rp avant lore)
        # qui renseigne une école, pour ne pas perdre un rang parce que la ligne de plus haut niveau est ailleurs.
        with_school = [t for t in c["_rows"] if t[2]["school_raw"]]
        school_raw = None
        if with_school:
            best = min(with_school, key=lambda t: (CATEGORY_PRIORITY.index(t[0]), -(t[2]["level"] or 0)))
            school_raw = best[2]["school_raw"]
        schools = parse_schools(school_raw, warnings, c["name"])
        status = (last_row.get("Status") or "").strip().lower()
        charges_raw = (last_row.get("Charges") or "").strip()
        out = {
            "id": cid,
            "name": c["name"],
            "category": category,
            "sources": sorted({t[2]["source"] for t in c["_rows"]}),
            "schools": schools,
            "tier": last_snap["tier"],
            "level": last_snap["level"],
            "ability_id": last_snap["ability_id"],
            "stats": last_snap["stats"],
            "sex": sex,
            "height_cm": height,
            "color": clean(last_row.get("Code Hex")),
            "status": status if status in {"missing", "deceased"} else None,
            "charges": [x.strip() for x in charges_raw.split(",") if x.strip()],
            "reference": last_snap["reference"],
            "image": last_snap["image"],
            "snapshots": [{k: v for k, v in s.items() if k != "school_raw"} for s in c["snapshots"]],
        }
        if len(cats) > 1:
            warnings.append(f"'{c['name']}' apparaît dans plusieurs onglets ({', '.join(sorted(cats))}) : "
                            f"fusionné, catégorie '{category}'. Séparer via overrides si ce sont deux personnes.")
        characters.append(out)
    characters.sort(key=lambda c: (CATEGORY_PRIORITY.index(c["category"]), c["id"]))
    return characters, skipped


def build_abilities(characters):
    abil = {}
    for c in characters:
        for s in c["snapshots"]:
            aid = s["ability_id"]
            if not aid:
                continue
            a = abil.setdefault(aid, {"names": collections.Counter(), "types": collections.Counter(),
                                      "natures": collections.Counter(), "desc": "", "users": set(),
                                      "levels": [], "tiers": []})
            a["names"][s["ability"]] += 1
            a["types"][s["type"]] += 1
            if s["nature"]:
                a["natures"][s["nature"]] += 1
            if s["description"] and len(s["description"]) > len(a["desc"]):
                a["desc"] = s["description"]
            a["users"].add(c["id"])
            if s["level"] is not None:
                a["levels"].append(s["level"])
            a["tiers"].append(s["tier"])
    out = []
    for aid, a in sorted(abil.items()):
        main_name = a["names"].most_common(1)[0][0]
        best_tier = min(a["tiers"], key=lambda t: TIER_ORDER.get(t, 9)) if a["tiers"] else "unknown"
        item = {
            "id": aid,
            "name": main_name,
            "aliases": sorted(n for n in a["names"] if n != main_name),
            "type": a["types"].most_common(1)[0][0],
            "nature": a["natures"].most_common(1)[0][0] if a["natures"] else None,
            "description": a["desc"] or None,
            "users": sorted(a["users"]),
            "level_min": min(a["levels"]) if a["levels"] else None,
            "level_max": max(a["levels"]) if a["levels"] else None,
            "wave": TIER_WAVE.get(best_tier, 6),
            # Suivi d'implémentation : à mettre à jour à la main dans overrides/abilities.json
            "impl": {"status": "todo", "palladium_power": None, "notes": None},
        }
        out.append(item)
    out.sort(key=lambda x: (x["wave"], -len(x["users"]), x["id"]))
    return out


LEADER_ROLES = ["king", "queen", "jack", "healer"]


def build_schools(characters):
    """Écoles/groupes présents dans les données. 'leaders' = personnages à rang actuel
    (roi, reine, valet, soigneur ; les anciens élèves sont exclus)."""
    members = collections.defaultdict(set)
    leaders = collections.defaultdict(list)
    regions = collections.defaultdict(set)
    for c in characters:
        for m in c["schools"]:
            members[m["school"]].add(c["id"])
            if m.get("region"):
                regions[m["school"]].add(m["region"])
            if "alumni" in m["roles"]:
                continue
            for r in LEADER_ROLES:
                if r in m["roles"]:
                    leaders[m["school"]].append({"character_id": c["id"], "role": r})
    out = []
    for sid in sorted(members):
        name, kind, confirmed = KNOWN_PLACES.get(sid, (sid.replace("_", " ").title(), "school", False))
        out.append({
            "id": sid,
            "name": name,
            "kind": kind,
            "kind_confirmed": confirmed,
            "regions": sorted(regions[sid]),
            "leaders": sorted(leaders[sid], key=lambda x: (LEADER_ROLES.index(x["role"]), x["character_id"])),
            "campus": {"variant": None, "palette": None, "notes": None},
            "member_count": len(members[sid]),
            "members": sorted(members[sid]),
        })
    return out


def apply_overrides(items, path, label, warnings):
    p = ROOT / "tools/overrides" / path
    if not p.exists():
        return items
    ov = json.loads(p.read_text(encoding="utf-8"))
    by_id = {i["id"]: i for i in items}
    for oid, patch in ov.items():
        if oid.startswith("_"):
            continue
        if patch.get("_remove"):
            by_id.pop(oid, None)
        elif oid in by_id:
            deep_merge(by_id[oid], patch)
        else:
            by_id[oid] = deep_merge({"id": oid}, patch)
            warnings.append(f"Override {label} '{oid}' ajouté (n'existait pas dans l'export).")
    return list(by_id.values())


def validate(characters, abilities, schools, warnings):
    errors = []
    aids = {a["id"] for a in abilities}
    sids = {s["id"] for s in schools}
    cids = set()
    for c in characters:
        if c["id"] in cids:
            errors.append(f"Personnage en double : {c['id']}")
        cids.add(c["id"])
        if c.get("ability_id") and c["ability_id"] not in aids:
            errors.append(f"{c['id']} : capacité inconnue '{c['ability_id']}'")
        for m in c.get("schools", []):
            if m["school"] not in sids:
                errors.append(f"{c['id']} : école inconnue '{m['school']}'")
    no_ability = [c["id"] for c in characters if not c.get("ability_id") and c["tier"] != "cripple"]
    if no_ability:
        warnings.append(f"{len(no_ability)} personnages non-cripple sans capacité connue (ex. {', '.join(no_ability[:5])}).")
    return errors


def write_json(path, payload):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")


def write_report(characters, abilities, schools, skipped, warnings, errors):
    rep = ROOT / "docs/generated/REPORT.md"
    rep.parent.mkdir(parents=True, exist_ok=True)
    by_cat = collections.Counter(c["category"] for c in characters)
    by_tier = collections.Counter(c["tier"] for c in characters)
    by_wave = collections.Counter(a["wave"] for a in abilities)
    lines = ["# Rapport de génération Unodex", "",
             "_Fichier généré par `tools/build_unodex.py` : ne pas éditer à la main._", "",
             f"- Personnages : **{len(characters)}** ({', '.join(f'{k} {v}' for k, v in by_cat.items())})",
             f"- Capacités distinctes : **{len(abilities)}** (vague 1 : {by_wave[1]}, 2 : {by_wave[2]}, "
             f"3 : {by_wave[3]}, 4 : {by_wave[4]}, 5 : {by_wave[5]}, 6 : {by_wave[6]})",
             f"- Écoles / groupes : **{len(schools)}**",
             f"- Lignes ignorées (sans nom ou onglet inconnu) : {skipped}", "",
             "## Personnages par tier (dernier niveau connu)", ""]
    lines += [f"- {t} : {n}" for t, n in sorted(by_tier.items(), key=lambda x: TIER_ORDER.get(x[0], 9))]
    lines += ["", "## Écoles et groupes", "", "| id | nom | type | confirmé | membres | dirigeants |", "|---|---|---|---|---|---|"]
    for s in schools:
        lines.append(f"| {s['id']} | {s['name']} | {s['kind']} | {'oui' if s['kind_confirmed'] else '**à relire**'} "
                     f"| {s['member_count']} | {', '.join(l['character_id'] + ' (' + l['role'] + ')' for l in s['leaders']) or '-'} |")
    lines += ["", "## Avertissements", ""] + ([f"- {w}" for w in warnings] or ["Aucun."])
    lines += ["", "## Erreurs", ""] + ([f"- {e}" for e in errors] or ["Aucune."])
    rep.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--input", default=str(ROOT / "tools/input/unodex-export.txt"))
    ap.add_argument("--check", action="store_true", help="valide sans écrire de fichiers")
    args = ap.parse_args()

    warnings = []
    rows = read_source_rows(args.input)
    characters, skipped = build(rows, warnings)
    abilities = build_abilities(characters)
    schools = build_schools(characters)

    characters = apply_overrides(characters, "characters.json", "personnage", warnings)
    abilities = apply_overrides(abilities, "abilities.json", "capacité", warnings)
    schools = apply_overrides(schools, "schools.json", "école", warnings)

    errors = validate(characters, abilities, schools, warnings)
    print(f"{len(characters)} personnages, {len(abilities)} capacités, {len(schools)} écoles/groupes, "
          f"{len(warnings)} avertissements, {len(errors)} erreurs")
    for e in errors:
        print("ERREUR:", e, file=sys.stderr)
    if errors:
        sys.exit(1)
    if args.check:
        return

    cats = collections.defaultdict(list)
    for c in characters:
        cats[c["category"]].append(c)
    for cat in list(ORIGIN_CATEGORY.values()) + STALE_CATEGORIES:
        f = DATA_DIR / "characters" / f"{cat}.json"
        if cat in cats:
            write_json(f, {"characters": sorted(cats[cat], key=lambda c: c["id"])})
        elif f.exists():
            f.unlink()
    write_json(DATA_DIR / "abilities.json", {"abilities": abilities})
    write_json(DATA_DIR / "schools.json", {"schools": sorted(schools, key=lambda s: s["id"])})
    write_report(characters, abilities, schools, skipped, warnings, errors)
    print("Écrit dans", DATA_DIR, "et docs/generated/REPORT.md")


if __name__ == "__main__":
    main()
