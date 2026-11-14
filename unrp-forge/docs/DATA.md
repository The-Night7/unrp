# Données : formats et édition

Tout le contenu du RP vit dans `src/main/resources/data/unrp/unodex/`. Le mod relit ces fichiers à chaque
chargement. Un **datapack** peut remplacer n'importe lequel (même chemin `data/unrp/unodex/...`) sans toucher au mod.

## Deux façons de modifier
1. **Corriger l'Unodex** (la feuille Google), réexporter, relancer le script. À privilégier pour tout ce qui vient de la feuille.
2. **Overrides** dans `tools/overrides/` pour ce que la feuille ne contient pas ou ce que tu veux forcer
   (principaux, campus, avancement des capacités, corrections). Ils sont fusionnés par-dessus la génération, **jamais écrasés**.

## Mettre à jour depuis la feuille
1. Exporte la feuille (tous les onglets dans un fichier texte comme `tools/input/unodex-export.txt`).
2. Lance (depuis la racine du projet) :
```fish
python3 tools/build_unodex.py --check     # valide sans écrire
python3 tools/build_unodex.py             # écrit les JSON et docs/generated/REPORT.md
```
3. Lis `docs/generated/REPORT.md` : avertissements (noms en double, lieux inconnus) et erreurs de cohérence.
Le script s'arrête avec une erreur si une capacité ou une école référencée n'existe pas.

**Onglets sources : `unodex` (catégorie **lore**) et `unodex-rp` (catégorie **rp**)**, rien d'autre.
Les autres onglets (`unodex-shine-city`, `unodex-pnj`, `unodex-capa`, `unodex-meta`) ne sont pas lus.
Pour en ajouter un, ajoute une ligne dans `ORIGIN_CATEGORY` en haut de `tools/build_unodex.py`.
Les lignes sans nom (tier « Inconnu » seul) sont ignorées.

## Syntaxe des overrides
Clé = `id`. On n'écrit que les champs à changer (fusion récursive). `"_remove": true` supprime. Une clé inconnue crée une entrée.
Les clés commençant par `_` sont ignorées (commentaires, exemples).
```json
{
  "wellston": {
    "leaders": [ { "character_id": "kree", "role": "king" } ],
    "campus": { "variant": "wellston", "palette": "brique" }
  }
}
```

## Fichiers générés
Ne les édite pas à la main (ils seraient écrasés) : passe par la feuille ou les overrides.

### `schools.json` : écoles et groupes
| Champ | Sens |
|---|---|
| `id`, `name` | identifiant et nom affiché |
| `kind` / `kind_confirmed` | `school` ou `organization`. **`kind_confirmed: false` = deviné, à relire** (Vigilante, Ember) |
| `regions` | ex. `Shine-City` quand l'Unodex écrit « Vigilante : Shine-City » |
| `leaders` | **les principaux de l'école** : personnages à rang actuel (`king`, `queen`, `jack`, `healer`), calculés depuis la colonne « École ». Les anciens élèves sont exclus |
| `campus` | réglages de construction (phase 3) |
| `members` / `member_count` | ids des personnages, calculés |

### `characters/<categorie>.json`
| Champ | Sens |
|---|---|
| `id`, `name`, `category` | `lore` ou `rp` |
| `sources` | onglets où le personnage apparaît |
| `schools[]` | `school`, `roles` (`king`, `queen`, `jack`, `healer`, `alumni`, `hidden`), `region` |
| `tier`, `level`, `ability_id`, `stats` | état actuel = ligne de plus haut niveau |
| `sex` (H/F), `height_cm`, `color`, `status` (`missing`/`deceased`), `charges` | infos de fiche |
| `snapshots[]` | historique de progression (une ligne de l'Unodex = un niveau) |

### `abilities.json`
`id`, `name`, `aliases`, `type`, `nature`, `description`, `users`, `level_min/max`,
`wave` (ordre d'implémentation, voir la feuille de route), `impl` (`status` todo/wip/done, `palladium_power`, `notes`).
Les types sont normalisés (Quantique/Quantum, Émission/Emission, Statut/Status…).

### `rarity.json`
Voir `docs/ROLL.md`.

## Règles d'identifiants
Nom sans accents, minuscules, `_` entre les mots (« Théo » → `theo`, « St Clair » → `st_clair`).
Deux lignes de même nom = même personnage (progression sur plusieurs niveaux). **Si ce sont en fait deux personnes**
(le rapport liste les cas : Terrence, Isen, Blyke, Rein, Remi, Arlo, John, Seraphina, présents dans `unodex` et `unodex-rp`),
sépare-les en leur donnant un nom distinct dans la feuille.

## À savoir
- Les 3 lignes dont le nom est « ??? » sont ignorées (pas d'identifiant possible).
- Le statut long (texte de stats) présent sur 2 lignes est ignoré : seuls `missing` et `deceased` sont retenus.
