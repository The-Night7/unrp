# Where is what

Actual equipment assets: [design_equipment.py](../tools/design_equipment.py) builds placed/held models and worn UV sheets; [CosmeticLayer.java](../src/main/java/com/myriam/unrp/client/CosmeticLayer.java) renders garments. [preview_equipment.py](../tools/preview_equipment.py) renders those same production JSONs/PNGs; [asset preview](unrp-items-preview.png). Block items inherit their placed models, and clothing items use their worn textures.

[Ability-level commands](LEVEL_COMMANDS.md): `/unrp level get`, `/unrp level set 5.0 [player]`, `/unrp level add 0.5 [player]`. Operator-only OC edits, persisted and synced immediately. Implementation: [LevelCommands.java](../src/main/java/com/myriam/unrp/command/LevelCommands.java).

[Item lore and visual references](ITEM_LORE.md) map the canon corrections and RP adaptations.

The active HUD uses the original [background PNG](../src/main/resources/assets/unrp/textures/gui/hud/rpg_hud_back.png) and [foreground PNG](../src/main/resources/assets/unrp/textures/gui/hud/rpg_hud_front.png), both 200 × 80. Edit them directly or regenerate them with [design_hud_textures.py](../tools/design_hud_textures.py). [UnrpHudOverlay.java](../src/main/java/com/myriam/unrp/look/UnrpHudOverlay.java) retains the original 3D portrait and meter layout.

The gold status HUD and original inventory icon sources are mapped in [VISUAL_STYLE.md](VISUAL_STYLE.md), with [HUD](unrp-hud-preview.svg) and [item](unrp-items-preview.svg) previews.

## School buildings and world generation

All six schools have dedicated layouts. [SCHOOLS.md](SCHOOLS.md) covers unique seed-based sites, generation, saved versions, materials and preview commands. [Preview all six campuses](schools-preview.svg).

| Campus | Guide | Layout code | Detailed preview |
| --- | --- | --- | --- |
| Wellston | [WELLSTON.md](WELLSTON.md) | [WellstonBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/WellstonBlueprint.java) | [Floors](wellston-preview.svg) |
| Agwin | [AGWIN.md](AGWIN.md) | [AgwinBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/AgwinBlueprint.java) | [Elevation/floors](agwin-preview.svg) |
| New Bostin | [NEW_BOSTIN.md](NEW_BOSTIN.md) | [NewBostinBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/NewBostinBlueprint.java) | [Floors](new-bostin-preview.svg) |
| Rowden | [ROWDEN.md](ROWDEN.md) | [RowdenBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/RowdenBlueprint.java) | [Floors](rowden-preview.svg) |
| Westview | [WESTVIEW.md](WESTVIEW.md) | [WestviewBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/WestviewBlueprint.java) | [Floors](westview-preview.svg), [exterior](westview-exterior.svg) |
| University | [WELLSTON_UNIVERSITY.md](WELLSTON_UNIVERSITY.md) | [UniversityBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/UniversityBlueprint.java) | [Floors](wellston-university-preview.svg) |

Westview’s photo-inspired blocks, glass links, covered paths, gateway and leaf mural are defined in `WestviewBlueprint.java`; its block colours use `westview_modern` in `SchoolPiece.java`. The former layout is preserved in `WestviewV4Blueprint.java` for saved version 4.

[SchoolSites.java](../src/main/java/com/myriam/unrp/worldgen/SchoolSites.java) decides the one natural site per school. [SchoolBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/SchoolBlueprint.java) selects current/saved layouts. [CampusBuilder.java](../src/main/java/com/myriam/unrp/worldgen/CampusBuilder.java) supplies shared room/stair primitives for the four latest campuses. [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java) chooses block palettes and places them inside chunk bounds. Use `/unrp school locate <id>` to find a reserved Overworld site.

## Créer et modifier des capacités

Voir [ABILITY_DEVELOPMENT.md](ABILITY_DEVELOPMENT.md) pour le guide complet : fichiers JSON et Java, chaîne d’exécution client/serveur, ajout d’un pouvoir, mouvements, animations, prérequis de l’arbre, effets visuels et tests en jeu. La référence détaillée des champs JSON reste dans [POUVOIRS.md](POUVOIRS.md).

## Covenant

Rule entry: activate Covenant with R, then press Enact to open its command prompt, or use `/unrp covenant law No running. Penalty: Speed` directly. Command registration lives in `UnrpCommands.java`; both input paths use `Specials17.finish`.

- [COVENANT_SPEC.md](COVENANT_SPEC.md): full author specification, God tier and permanent Indemnity from 5.0.
- [COVENANT.md](COVENANT.md): implemented rules, six stances, chat controls, persistence and Minecraft representations.
- [CovenantRules.java](../src/main/java/com/myriam/unrp/ability/CovenantRules.java), [Specials17.java](../src/main/java/com/myriam/unrp/ability/Specials17.java), [CovenantParse.java](../src/main/java/com/myriam/unrp/ability/CovenantParse.java): tiers, gameplay and law grammar.
- [CovenantStateData.java](../src/main/java/com/myriam/unrp/ability/CovenantStateData.java): world ledger saved in `<world>/data/unrp_covenant.dat`.
- [CovenantClient.java](../src/main/java/com/myriam/unrp/client/CovenantClient.java) and [CovenantSignatureLayer.java](../src/main/java/com/myriam/unrp/client/CovenantSignatureLayer.java): signatures, Constitutions, markers and private parchments.
- [make_covenant_icons.py](../tools/make_covenant_icons.py): native gold icon generation.

## Ressources et identifiants UNRP

Toutes les ressources du mod utilisent le namespace `unrp` :

| Éléments | Emplacement |
| --- | --- |
| Sons et liste des événements sonores | [assets/unrp/sounds/](../src/main/resources/assets/unrp/sounds/) et [sounds.json](../src/main/resources/assets/unrp/sounds.json) |
| Modèles d’objets et de blocs | [assets/unrp/models/](../src/main/resources/assets/unrp/models/) |
| États des blocs | [assets/unrp/blockstates/](../src/main/resources/assets/unrp/blockstates/) |
| Géométries et animations | [assets/unrp/geo/](../src/main/resources/assets/unrp/geo/) et [animations/](../src/main/resources/assets/unrp/animations/) |
| Calques, textures dynamiques et traînées Palladium | [assets/unrp/palladium/](../src/main/resources/assets/unrp/palladium/) |
| Shaders | [assets/unrp/shaders/](../src/main/resources/assets/unrp/shaders/) |
| Traductions | [assets/unrp/lang/](../src/main/resources/assets/unrp/lang/) |
| Dimensions, dégâts, butin, recettes et données de jeu | [data/unrp/](../src/main/resources/data/unrp/) |
| Apparence du hub | [client/UiSkin.java](../src/main/java/com/myriam/unrp/client/UiSkin.java) : palette or et ambre, panneaux et bordures ; [HubScreen.java](../src/main/java/com/myriam/unrp/client/HubScreen.java) : disposition et widgets |

Le radar de l’onglet Pouvoir et l’arbre cliquable de l’onglet Capacités sont dessinés dans `HubScreen.java`, avec la géométrie dans [HubGraphLayout.java](../src/main/java/com/myriam/unrp/client/HubGraphLayout.java). Les branches reprennent les champs `requires` et `unlock` de [ability_powers.json](../src/main/resources/data/unrp/unodex/ability_powers.json), envoyés au client par [SocialService.java](../src/main/java/com/myriam/unrp/social/SocialService.java). Les achats restent contrôlés par le serveur (`Unlocks.buy`).

Ability-tree controls: drag empty space with the left button, or drag anywhere with the right/middle button, to pan; wheel scrolls vertically and Ctrl + wheel zooms. Covenant’s stance switch is on its second ability-bar page (default list key X), leaving all four main actions reachable. The power toggle also has a dedicated R binding in Controls.

Les tags d’intégration à Minecraft et Forge restent dans `data/minecraft/` et `data/forge/`.
Les identifiants persistants, objectifs de score et tags du mod utilisent également `unrp`.
Les sauvegardes créées avant cette unification ne sont pas converties automatiquement.

## JAR généré après le build

Le JAR du mod Forge est généré dans [unrp-forge/build/libs/](../build/libs/) (depuis la racine du dépôt).

Avec la version actuelle : [unrp-0.1.0.jar](../build/libs/unrp-0.1.0.jar).
Le nom suit le format `<mod_id>-<mod_version>.jar`, défini dans [gradle.properties](../gradle.properties).

Pour le générer, depuis `unrp-forge/` :

```bash
./gradlew build
```

## Tirage des capacités et stats d’un OC

Le serveur tire séparément la rareté et le niveau de départ, puis la capacité, puis les potentiels des cinq stats. La capacité n’est pas filtrée par rareté : un OC Low peut tirer Time Manipulation.

| Réglage / logique | Emplacement | À modifier / fonctionnement |
| --- | --- | --- |
| Chances des raretés et niveaux de départ | [rarity.json](../src/main/resources/data/unrp/unodex/rarity.json) | `weight`, `level_min`, `level_max`, `lifelong`. |
| Capacités disponibles et poids du tirage | [ability_pool.json](../src/main/resources/data/unrp/unodex/ability_pool.json) | Poids relatif de chaque capacité ; 0 ou absence = exclue. La chance vaut son poids divisé par la somme des poids des capacités encore éligibles. |
| Plages des potentiels des stats | [stat_ranges.json](../src/main/resources/data/unrp/unodex/stat_ranges.json) | Plages par capacité ; chaque potentiel prend le plus petit de cinq tirages dans sa plage. |
| Tirage de rareté et de niveau | [RarityTable.java](../src/main/java/com/myriam/unrp/data/RarityTable.java) | `roll()` : rareté pondérée, puis niveau uniforme dans sa plage, arrondi à une décimale. |
| Tirage de capacité et exclusions | [UnodexData.java](../src/main/java/com/myriam/unrp/data/UnodexData.java) | `rollAbility()`, `NOT_ROLLABLE`, `rollPotentials()` ; une capacité doit avoir une implémentation jouable. |
| Unicité des capacités et sauvegarde des potentiels | [ClaimData.java](../src/main/java/com/myriam/unrp/data/ClaimData.java) | `heldAbilities()`, `rollFreeAbility()`, `setOcAbility()` ; les capacités détenues par d’autres joueurs sont exclues. |
| Déclenchement à la création de l’OC | [MenuService.java](../src/main/java/com/myriam/unrp/menu/MenuService.java) | Les deux types de Cripple commencent sans capacité. |
| Commande admin de nouveau tirage | [UnrpCommands.java](../src/main/java/com/myriam/unrp/command/UnrpCommands.java) | `/unrp rollability <player>` : retire une capacité au hasard pour un OC, sauf Cripple à vie, et recalcule ses potentiels. |

| Rareté | Chance | Niveau de départ |
| --- | --- | --- |
| Cripple à vie | 2,36 % | 1,0 |
| Cripple | 3,54 % | 1,0 |
| Low | 25 % | 1,0–1,9 |
| Mid | 54,99 % | 2,0–3,4 |
| Elite | 12 % | 3,5–4,9 |
| High | 2 % | 5,0–5,9 |
| God | 0,1 % | 6,0–7,4 |
| Exo-god | 0,01 % | 7,5–10,0 |

Exemples de poids de capacité : Regeneration 100, Aura Manipulation 13, Time Manipulation 8. Les capacités spéciales de `NOT_ROLLABLE` (dont Limit Breaker, Sarcophage et Covenant) nécessitent une attribution, même avec un poids positif dans le pool.

Les stats suivent la formule `stat = (potentiel + entraînement) × √(niveau / 10)`, avec arrondi et minimum 1. Détails sur la progression et l’entraînement : [ROLL.md](ROLL.md).

## Textures des pouvoirs et des menus

Les ressources graphiques du mod Forge se trouvent dans `unrp-forge/src/main/resources/assets/` (depuis la racine du dépôt).

Toutes les textures sont regroupées dans [unrp/textures/](../src/main/resources/assets/unrp/textures/), y compris celles importées du modèle. Leurs références utilisent `unrp:textures/...` (ou `unrp:item/...`, `unrp:block/...` dans les modèles).
Les particules importées `aura_mote_0.png` à `aura_mote_2.png` conservent un nom distinct des particules UNRP `mote_0.png` à `mote_2.png`.

| Éléments | Emplacement | Exemples / contenu |
| --- | --- | --- |
| Menus et HUD | [unrp/textures/gui/](../src/main/resources/assets/unrp/textures/gui/) | `power_background.png` : fond des pouvoirs ; `ability_bars/` : barres des pouvoirs ; `hud/` : jauges d’aura ; `lb_forms/` : sigils. |
| Icônes des pouvoirs | [unrp/textures/icons/](../src/main/resources/assets/unrp/textures/icons/) | Images des icônes. |
| Effets visuels des pouvoirs | [unrp/textures/fx/](../src/main/resources/assets/unrp/textures/fx/) | Textures des effets. |
| Particules des pouvoirs | [unrp/textures/particle/](../src/main/resources/assets/unrp/textures/particle/) | Flammes, éclats, arcs, glyphes et autres particules. |
| Éléments d’interface UNRP | [unrp/textures/gui/](../src/main/resources/assets/unrp/textures/gui/) | `aura_back.png`, `aura_front.png`, `stars.png`. |

## Scripts de génération des textures

Dans [tools/](../tools/) :

- [make_gui_textures.py](../tools/make_gui_textures.py) : génère les textures d’interface et applique la palette or aux PNG importés.
- [recolor_gui_gold.py](../tools/recolor_gui_gold.py) : convertit les teintes violettes des PNG de `assets/unrp/textures/gui/` en or, en conservant leurs dimensions, leur transparence et leurs coordonnées d’atlas. Relance après un import forcé : `python3 tools/recolor_gui_gold.py`.
- [make_particle_textures.py](../tools/make_particle_textures.py) : textures de particules.
- [make_item_textures.py](../tools/make_item_textures.py) : textures d’objets.
- [import_model_assets.py](../tools/import_model_assets.py) : import des ressources d’un dossier `assets/<namespace>` dans `unrp`, avec adaptation des références. Usage : `python3 tools/import_model_assets.py /chemin/vers/assets/<namespace>` ; les fichiers UNRP existants sont conservés par défaut.
