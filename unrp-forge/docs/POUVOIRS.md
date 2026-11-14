# Pouvoirs : guide complet pour les modifier

Ce guide explique comment marchent les pouvoirs du mod `unrp` et comment les changer **sans toucher au code Java**. Il suffit d'éditer un seul fichier JSON.

Pour créer un nouveau pouvoir, trouver son code Java et comprendre ses mouvements, animations et prérequis, voir le [guide de développement des capacités](ABILITY_DEVELOPMENT.md). Les descriptions historiques des touches ci-dessous doivent être comparées aux raccourcis actuels du jeu.

Sommaire :

1. [En jeu : touches et règles](#1-en-jeu--touches-et-règles)
2. [Le fichier à modifier, et comment tester](#2-le-fichier-à-modifier-et-comment-tester)
3. [Un pouvoir : les champs](#3-un-pouvoir--les-champs)
4. [Une capacité : les champs](#4-une-capacité--les-champs)
5. [L'effet `fx` : les 17 types](#5-leffet-fx--les-17-types)
6. [Attributs, effets de potion, particules : les noms valides](#6-attributs-effets-de-potion-particules--les-noms-valides)
7. [Recettes : les modifications courantes](#7-recettes--les-modifications-courantes)
8. [Correspondance avec le mod modèle](#8-correspondance-avec-le-mod-modèle)
9. [Ce qui n'est pas encore porté](#9-ce-qui-nest-pas-encore-porté)
10. [Pour aller plus loin : le code Java](#10-pour-aller-plus-loin--le-code-java)
11. [Le style de l'interface](#11-le-style-de-linterface)
12. [Les effets visuels](#12-les-effets-visuels)
13. [Les poses du corps](#13-les-poses-du-corps)

---

## 1. En jeu : touches et règles

| Touche | Action |
|---|---|
| **R** | Activer ou désactiver le pouvoir. Il **reste actif** jusqu'à ce que tu appuies de nouveau. Aucune recharge sur l'activation. |
| **X C V B N** | Les capacités 1 à 5 de ta barre. Elles ne marchent que si le pouvoir est actif. |
| **H** | Le hub. L'onglet **Capacités** liste tes capacités avec leur touche, leur coût, leur recharge et leur description. |

Toutes les touches se changent dans **Options → Contrôles**, catégorie *unrp*.

**La barre de capacités** est à gauche de l'écran, au milieu. Chaque case montre :

- la touche (petit badge en haut à droite) ;
- l'initiale de la capacité ;
- son nom, puis son coût et sa façon de se déclencher (charger, maintenir, ON/OFF).

Une case allumée est prête. Un voile sombre qui descend indique une recharge, avec les secondes restantes. Une jauge claire qui monte indique une charge en cours. Une case pleine signale un interrupteur allumé.

**Les règles de l'aura** reprennent celles du mod modèle :

- Activer le pouvoir est gratuit. Ensuite, tant qu'il est actif, il peut coûter **-1 aura toutes les N ticks** (champ `drain_every`). Les capacités maintenues, les interrupteurs allumés et les barrières qui existent peuvent ajouter leur propre perte.
- Une capacité « action » retire son coût d'un coup quand tu la lances.
- **L'aura ne remonte que si rien ne la consomme.** Tant qu'une perte est en cours, il n'y a aucune régénération.
- **À 0 aura**, le pouvoir se coupe tout seul. Tout s'éteint, y compris les barrières. L'aura reste bloquée **10 secondes** (« Aura récupérée dans X s »), puis revient à **25 % du maximum** (minimum 10).

Unités de temps : **20 ticks = 1 seconde**. Toutes les recharges (`cooldown`), charges (`charge`), durées de vie (`life`) et pertes (`drain_every`) sont en ticks. Les durées d'effets de potion (`secs`) et de barrière (`glow`) sont en secondes.

| ticks | secondes |
|---|---|
| 5 | 0,25 |
| 10 | 0,5 |
| 20 | 1 |
| 40 | 2 |
| 100 | 5 |
| 200 | 10 |
| 1200 | 60 |

---

## 2. Le fichier à modifier, et comment tester

Le fichier :

```
src/main/resources/data/unrp/unodex/ability_powers.json
```

Il contient deux parties :

```json
{
 "aliases": { "lightining": "lightning", "modes_atk": "modes" },
 "powers": {
  "lightning": { ... },
  "barrier":   { ... }
 }
}
```

- **`powers`** : un pouvoir par clé. La clé est l'**identifiant de la capacité** dans l'Unodex, c'est-à-dire la colonne `ability_id` des personnages (`characters/*.json`). Par exemple, Remi a `"ability_id": "lightning"`, donc il reçoit `powers.lightning`.
- **`aliases`** : plusieurs identifiants qui partagent le même pouvoir. Par exemple, `"lightining": "lightning"` corrige une faute de frappe de la feuille.
- Un champ dont le nom commence par `_` (comme `_modele` ou `_aide`) est un **commentaire**. Le mod l'ignore.

**Pour tester une modification :**

1. Modifie le JSON.
2. Si le jeu est fermé : `./gradlew runClient`. Si le jeu tourne déjà : quitte le monde, puis relance `./gradlew runClient`. En effet, `runClient` relit les fichiers de `src/main/resources` à chaque lancement.
3. Dans un monde déjà lancé, `/reload` recharge aussi le fichier : utile pour ajuster des chiffres sans relancer.

**Si le JSON contient une erreur** (virgule en trop, guillemet oublié), le fichier est ignoré. Le log affiche alors une ligne de ce type :

```
[unrp] Fichier ignoré (unrp:unodex/ability_powers.json): com.google.gson.JsonSyntaxException: ... line 812 column 4
```

La ligne et la colonne indiquent où chercher. Dans VS Code, une erreur JSON est soulignée en rouge.

Pour vérifier le fichier dans Fish avant de lancer le jeu (aucun message = fichier valide) :

```fish
python3 -m json.tool src/main/resources/data/unrp/unodex/ability_powers.json > /dev/null
```

> **Attention** : les scripts `tools/seed_ability_powers.py --force` et `tools/add_looks.py` génèrent le fichier automatiquement. Avec `--force`, le premier **écrase tes modifications**. Une fois que tu édites le JSON à la main, c'est lui qui fait foi : ne relance plus le générateur avec `--force`.

---

## 3. Un pouvoir : les champs

Exemple réel (Lightning de Remi, version courte) :

```json
"lightning": {
 "drain_every": 7,
 "attributes": [ { "attribute": "minecraft:generic.movement_speed", "amount": 0.12, "operation": 2 } ],
 "thorns": 2,
 "passive": [],
 "look": { "color": "#FFE45C", "particle": "electric_spark", "jagged": true },
 "moves": [ ... ]
}
```

| Champ | Type | Défaut | Rôle |
|---|---|---|---|
| `drain_every` | entier (ticks) | 0 | Tant que le pouvoir est actif : -1 aura toutes les N ticks. 0 = activation gratuite à l'entretien. Lightning : 7 (≈ 2,9 aura/s). |
| `drain` | nombre | 0 | **Ancien format** : aura perdue par seconde. Ignoré si `drain_every` > 0. Les pouvoirs générés automatiquement l'utilisent encore. |
| `attributes` | liste | vide | Attributs Minecraft modifiés tant que le pouvoir est actif (vitesse, armure...). Voir [§6](#attributs). |
| `passive` | liste | vide | Effets de potion maintenus tant que le pouvoir est actif. Format : `{ "id": "speed", "amp": 0, "secs": 4 }`. `amp` 0 = niveau I, 1 = niveau II... |
| `thorns` | nombre | 0 | Dégâts infligés à qui te frappe au corps à corps pendant que le pouvoir est actif. Lightning : « hitting you hurts ». |
| `look` | objet | aucun | Apparence : `style` (un des 22 styles visuels : `fire`, `ice`, `mind`...), `color` (`"#RRGGBB"`, couleur de tous les effets et de l'aura), et en option `sprite`, `jagged`, `particle`. Voir [§12](#12-les-effets-visuels). |
| `modes` | liste | vide | Pour les pouvoirs à formes (Modes de Marc, Phase Shift). Chaque forme : `{ "name", "drain_every", "passive" }`. Une capacité avec `"special": "mode_next"` passe à la forme suivante. |
| `moves` | liste | vide | Les capacités de la barre, **dans l'ordre des touches** : la 1re va sur X, la 2e sur C, puis V, B et N. Cinq au maximum. |

---

## 4. Une capacité : les champs

Exemple réel (l'Éclair de Remi) :

```json
{
 "name": "Éclair",
 "desc": "Tire un éclair. Il ralentit ce qu'il touche.",
 "input": "action",
 "cost": 12,
 "cooldown": 36,
 "fx": { "kind": "bolt", "speed": 2.0, "life": 15, "dmg": 6,
         "effects": [ { "id": "slowness", "amp": 1, "secs": 3 } ],
         "particle": "electric_spark" }
}
```

| Champ | Type | Défaut | Rôle |
|---|---|---|---|
| `name` | texte | | Nom affiché dans la barre et dans le hub (40 caractères maximum). |
| `desc` | texte | | Description affichée dans le hub (200 caractères maximum). |
| `input` | texte | `action` | Façon de déclencher la capacité, voir le tableau suivant. |
| `cost` | entier | 0 | Aura retirée. Pour `action` : à chaque utilisation. Pour `charge` : au maximum de la charge (proportionnel sinon). Pour `held` **sans** `drain_every` : 20 % du coût à chaque pulsation (toutes les 4 ticks). |
| `min_aura` | entier | 0 | Aura minimale pour lancer. 0 = égale au coût (au moins 1). |
| `cooldown` | entier (ticks) | 0 | Recharge de **cette** capacité. Pour un `toggle` : la recharge démarre quand tu l'éteins. |
| `charge` | entier (ticks) | 30 | `charge` uniquement : temps de maintien pour atteindre 100 %. |
| `drain_every` | entier (ticks) | 0 | `held` / `toggle` : -1 aura toutes les N ticks tant que la capacité est active. Effet `shell` : tant qu'au moins une barrière existe. |
| `passive` | liste | vide | `held` / `toggle` : effets de potion tant que la capacité est active. Exemple : invisibilité de la Cape optique. |
| `attributes` | liste | vide | `held` / `toggle` : attributs modifiés tant que la capacité est active. Exemple : +60 % de vitesse pour Course foudroyante. |
| `k` | nombre | 0,25 | `held` : force de chaque pulsation (1 = pleine puissance). |
| `special` | texte | | `"mode_next"` = passer à la forme suivante (pouvoirs avec `modes`). |
| `fx` | objet | | L'effet déclenché, voir [§5](#5-leffet-fx--les-17-types). Peut être absent pour un interrupteur qui ne fait que des passifs. |

**Les 4 façons de déclencher (`input`) :**

| `input` | Ce que fait le joueur | Ce qui se passe |
|---|---|---|
| `action` | Une pression | L'effet part, `cost` est retiré, la recharge démarre. |
| `charge` | Maintenir puis relâcher | Au relâchement, l'effet part avec une puissance de 30 à 100 % selon le temps maintenu (`charge` ticks pour 100 %). Le coût est proportionnel. La recharge démarre ensuite. |
| `held` | Maintenir | L'effet se répète toutes les 4 ticks tant que la touche est enfoncée. `passive` et `attributes` s'appliquent pendant ce temps. Pas de recharge. |
| `toggle` | Une pression pour allumer, une pour éteindre | Reste allumé. `passive` et `attributes` s'appliquent, `drain_every` consomme l'aura. Si `fx` existe, il se déclenche une fois à l'allumage. |

**Mise à l'échelle par les stats du personnage** (Puissance, Défense, Récupération, Ruse, Vitesse) :

- Dégâts : `dmg` × (0,7 + 0,15 × Puissance).
- Durées d'effets : `secs` × (0,8 + 0,05 × Ruse).
- Soins et PV de barrière : × (0,7 + 0,15 × Récupération). Les PV de barrière sont aussi multipliés par (0,8 + 0,1 × Défense).

Un personnage à Puissance 2 inflige donc dmg × 1,0, et à Puissance 6 dmg × 1,6.

---

## 5. L'effet `fx` : les 17 types

**Champs communs** à tous les types :

| Champ | Rôle |
|---|---|
| `kind` | Le type (voir ci-dessous). |
| `particle` | Particule de l'effet. Si le pouvoir a un `look`, sa couleur est prioritaire pour les traînées. |
| `effects` | Effets de potion appliqués à la **cible**. |
| `self_effects` | Effets de potion appliqués au **lanceur**. |
| `dmg` | Dégâts de base (1 = un demi-cœur). |
| `kb` | Recul. Valeur négative = attire. |
| `up` | Projection vers le haut. |
| `fire` | Secondes d'enflammement de la cible. |

**Les types :**

| `kind` | Ce qu'il fait | Champs utilisés |
|---|---|---|
| `strike` | Coup dans un cône devant soi, jusqu'à 5 cibles. | `range` (portée, défaut 3,5), `dmg`, `kb`, `hits` (multiplie les dégâts), `fire`, `effects` |
| `beam` | Rayon instantané sur la ligne de visée : première créature ou premier bloc. | `range` (défaut 24), `dmg`, `kb`, `up`, `fire`, `lifesteal` (part des dégâts rendue en vie), `explosion` (rayon, sans casser de blocs), `bolt` (`true` = éclair visuel au point touché) |
| `bolt` | **Projectile** qui avance réellement et peut être esquivé. | `speed` (blocs par tick, défaut 2), `life` (ticks, défaut 15 ; portée = speed × life), `dmg`, `kb`, `fire`, `effects` |
| `blast` | Onde autour de soi. | `radius` (défaut 6), `dmg`, `kb`, `up`, `explosion` |
| `pulse` | Zone autour de soi, sans recul. | `radius` (défaut 8), `dmg`, `effects`, `self_effects` |
| `buff` | Effets sur soi, avec une gerbe de particules. | `self_effects` |
| `dash` | Élan dans la direction regardée. | `power` (défaut 2), `up`, `self_effects` |
| `teleport` | Téléportation sur la ligne de visée. | `range` (défaut 10) |
| `heal` | Soin de soi, ou de la cible visée si `range` > 0. | `range`, `amount` (PV), `cleanse` (`true` = retire les effets négatifs) |
| `debuff` | Affaiblit la créature visée. | `range` (défaut 16), `dmg`, `effects`, `root` (secondes d'immobilisation) |
| `scan` | Détecte les personnages proches. | `radius` (défaut 24), `glow` (secondes de surbrillance), `report` (`true` = liste dans le chat) |
| `dispel` | Retire des effets. | voir le code |
| `wall` | Mur de blocs temporaires devant soi (posés sur l'air seulement). | `range` (distance), `radius` (demi-largeur), `amount` (hauteur), `glow` (secondes avant disparition), `block` (ex. `"light_blue_stained_glass"`) |
| `shell` | **Barrière sphérique** autour du lanceur (Barrier Deploy). Voir le détail juste après. | `radius` (défaut 3), `amount` (PV de base, défaut 20), `glow` (secondes de vie, défaut 60), `max` (barrières en même temps, défaut 2), `recoil` (dégâts au lanceur si elle se brise, défaut 4) |
| `grow` | Agrandit toutes tes barrières. | `amount` (+rayon, défaut 1), `range` (rayon maximal, défaut 8) |
| `ram` | **Bélier** : élan derrière un bouclier ; chaque créature rencontrée est touchée une fois. | `power` (vitesse, défaut 1,6), `life` (durée en ticks, défaut 12), `radius` (largeur du bouclier, défaut 2,5), `dmg`, `kb` (défaut 2), `up`, `effects` |
| `share` | Donne des effets au **joueur le plus proche** (Shared Cloak). | `range` (défaut 8), `effects` (pour l'allié), `self_effects` |

**Le détail de `shell` (les barrières d'Arlo) :**

- Le lanceur la traverse librement. **Personne d'autre ne passe**, ni pour entrer ni pour sortir : elle sert de bouclier ou de piège.
- Une attaque d'un côté vers l'autre est arrêtée, et c'est la barrière qui perd les PV. Les projectiles tirés de l'autre côté sont **renvoyés** à leur tireur.
- Elle se fissure à 2/3 puis à 1/3 de ses PV (son et particules) et rougit en s'abîmant. Quand elle se brise, le lanceur prend `recoil` dégâts, comme dans le webtoon.
- Elle disparaît quand le pouvoir est coupé, quand l'aura tombe à 0, ou après `glow` secondes.
- Au-delà de `max` barrières, la plus ancienne disparaît.

**Contrainte de la mise à l'échelle :** pour `charge` en dessous de 100 % et pour `held` (selon `k`), le moteur réduit `dmg`, `kb`, `amount`, `power` et les durées. À 100 %, les valeurs du JSON s'appliquent telles quelles.

---

## 6. Attributs, effets de potion, particules : les noms valides

<a id="attributs"></a>
### Attributs (`attributes`)

```json
{ "attribute": "minecraft:generic.movement_speed", "amount": 0.12, "operation": 2 }
```

| `operation` | Calcul | Exemple |
|---|---|---|
| 0 | ajoute `amount` | armure +6 : `"amount": 6, "operation": 0` |
| 1 | ajoute `amount` × valeur de base | vitesse +6 % de la base : `0.06, 1` |
| 2 | multiplie le total par (1 + `amount`) | vitesse ×1,12 : `0.12, 2` ; ×1,6 : `0.6, 2` |

Attributs utiles :

- `minecraft:generic.movement_speed` (vitesse)
- `minecraft:generic.armor` (armure, 0 à 30)
- `minecraft:generic.armor_toughness`
- `minecraft:generic.attack_damage` (dégâts au corps à corps)
- `minecraft:generic.attack_speed`
- `minecraft:generic.knockback_resistance` (0 à 1)
- `minecraft:generic.max_health` (PV max, 2 = un cœur)
- `forge:swim_speed`
- `forge:entity_reach`

Un nom d'attribut inconnu est simplement ignoré.

### Effets de potion (`passive`, `effects`, `self_effects`)

```json
{ "id": "slowness", "amp": 1, "secs": 3 }
```

Noms valides :

- `speed`, `slowness`, `haste`, `mining_fatigue`
- `strength`, `weakness`, `resistance`
- `regeneration`, `instant_health`, `instant_damage`, `absorption`, `health_boost`
- `jump_boost`, `slow_falling`, `levitation`
- `invisibility`, `night_vision`, `blindness`, `darkness`, `glowing`
- `nausea`, `poison`, `wither`, `hunger`
- `fire_resistance`, `water_breathing`, `dolphins_grace`, `conduit_power`

> **Piège :** les anciens noms comme `confusion`, `damage_boost` ou `damage_resistance` ne marchent pas. Il faut `nausea`, `strength` et `resistance`.

### Particules (`particle`, `look.particle`)

Particules utiles :

- `electric_spark`, `end_rod`, `cloud`, `flame`, `soul_fire_flame`
- `snowflake`, `bubble_pop`, `splash`, `happy_villager`, `large_smoke`, `smoke`
- `crimson_spore`, `warped_spore`, `enchant`, `portal`, `witch`
- `heart`, `crit`, `enchanted_hit`, `sweep_attack`
- `glow`, `wax_on`, `totem_of_undying`, `dragon_breath`, `sonic_boom`
- `falling_water`, `dripping_lava`, `ash`, `white_ash`, `cherry_leaves`

Une particule inconnue est remplacée par `crit`.

---

## 7. Recettes : les modifications courantes

**Changer une recharge ou un coût.** Exemple : Éclair toutes les 1 s au lieu de 1,8 s.

```json
"cooldown": 20
```

**Rendre une attaque chargeable.** Exemple : charger l'Éclair 1,5 s pour la pleine puissance.

```json
"input": "charge", "charge": 30
```

**Rendre le pouvoir plus cher à maintenir actif.** Plus le nombre est petit, plus ça coûte :

```json
"drain_every": 4
```

**Ajouter une capacité.** Ajoute un objet à la fin de `moves`. Il prend la touche suivante libre (5 au maximum). Exemple, une onde électrique :

```json
{ "name": "Décharge", "desc": "Onde électrique autour de toi.", "input": "action", "cost": 25, "cooldown": 200,
  "fx": { "kind": "blast", "radius": 5, "dmg": 6, "kb": 1.5, "particle": "electric_spark" } }
```

**Un interrupteur qui ne fait que des passifs.** Exemple, une peau de pierre :

```json
{ "name": "Peau de pierre", "input": "toggle", "drain_every": 10,
  "passive": [ { "id": "resistance", "amp": 1, "secs": 2 } ],
  "attributes": [ { "attribute": "minecraft:generic.knockback_resistance", "amount": 0.5, "operation": 0 } ] }
```

**Changer la couleur d'un pouvoir :**

```json
"look": { "color": "#3D8BFF", "particle": "bubble_pop", "jagged": false }
```

**Créer un pouvoir de zéro.** Ajoute une clé dans `powers` avec le même identifiant que la capacité dans l'Unodex (`ability_id`) :

```json
"mon_pouvoir": {
 "drain_every": 10,
 "attributes": [],
 "passive": [ { "id": "night_vision", "amp": 0, "secs": 12 } ],
 "look": { "color": "#A35CFF", "particle": "enchant", "jagged": false },
 "moves": [
  { "name": "Projectile", "input": "action", "cost": 10, "cooldown": 30,
    "fx": { "kind": "bolt", "speed": 1.5, "life": 20, "dmg": 5 } },
  { "name": "Bouclier", "input": "action", "cost": 15, "cooldown": 60, "drain_every": 8,
    "fx": { "kind": "shell", "radius": 2.5, "amount": 15, "glow": 20 } }
 ]
}
```

**Faire partager un pouvoir à deux capacités de l'Unodex.** Dans `aliases` :

```json
"barrier_plus": "barrier"
```

---

## 8. Correspondance avec le mod modèle

Ce qui suit vient des fichiers du modèle : `palladium/powers/*.json` pour les capacités, recharges et coûts, et `kubejs/server_scripts/aura_system.js` pour les règles d'aura. Les chiffres sont repris tels quels. La colonne « Hypothèse » signale ce qui est codé en Java dans le modèle et dont la valeur n'est pas lisible dans ses fichiers : la valeur est alors estimée.

### Lightning (Remi)

| Modèle | unrp | Touche | Valeurs |
|---|---|---|---|
| Lightning (toggle) | activation du pouvoir | R | +12 % de vitesse (op 2), -1 aura / 7 ticks, `thorns` 2 (hypothèse : dégâts exacts) |
| Lightning Bolt | Éclair (`bolt`) | X | 12 aura, recharge 36, projectile vitesse 2 et durée 15, ralentit ; dégâts 6 (hypothèse) |
| Shock Touch | Contact électrique (`strike`) | C | 10 aura, recharge 50 ; dégâts 10 et recul 1,6 (hypothèse) |
| Surge Run | Course foudroyante (`held`) | V | +60 % de vitesse (op 2), -1 aura / 5 ticks |
| Lightning Beam | Rayon foudroyant (`held` + `beam`) | B | portée 30, -1 aura / 3 ticks |

### Barrier (Arlo)

| Modèle | unrp | Touche | Valeurs |
|---|---|---|---|
| Barrier (toggle) | activation du pouvoir | R | +6 d'armure (op 0) |
| Barrier Deploy | Dresser une barrière (`shell`) | X | 15 aura, recharge 40, -1 aura / 8 ticks tant qu'une barrière existe ; 2 barrières au maximum ; PV, rayon et contrecoup en hypothèse |
| Barrier Growth | Agrandir les barrières (`grow`) | C | 6 aura, recharge 8 |
| Barrier Ram | Bélier (`ram`) | V | 20 aura, recharge 300 |

### Invisibility (Terrence)

| Modèle | unrp | Touche | Valeurs |
|---|---|---|---|
| Invisibility (toggle) | activation du pouvoir | R | +6 % de vitesse (op 1), saut amélioré (Saut I, car l'attribut Palladium n'existe pas sans Palladium) |
| Optical Cloak (toggle) | Cape optique (`toggle`) | X | invisibilité, -1 aura / 8 ticks |
| Shared Cloak | Cape partagée (`share`) | C | 20 aura, recharge 120, portée 12, 10 secondes |

### Règles d'aura

| Modèle (`aura_system.js`) | unrp |
|---|---|
| perte : 1 aura toutes les N ticks par source active (`UN_DRAIN_CADENCE`) | `drain_every` du pouvoir, des capacités et des barrières |
| régénération seulement si rien ne consomme | identique |
| à 0 : « Aura Depleted! », blocage 200 ticks, retour à 25 % (minimum 10) | identique |

---

## 9. Ce qui n'est pas encore porté

- **Les déblocages progressifs** (les 55 pouvoirs modélisés). Comme dans le modèle : le rang 2, 3 ou 4 d'une capacité coûte 1, 2 ou 3 points d'aptitude, valable pour tous tes pouvoirs. Tu gagnes 1 point par niveau (au moins 1, dès le niveau 2 ou après 20 min de jeu). Champs JSON `unlock` (rang) et `requires` (capacité parente, par index) sur chaque capacité ; achat dans le hub (onglet Capacités) ; admin : `/unrp unlock <joueur> all|reset`. Les pouvoirs non modélisés restent entièrement disponibles. La « maîtrise » à 100 exigée par le Domaine des mille lames (champ `mastery` de la capacité) est acquise pour un personnage de l'Unodex, et pour un OC au niveau 10.
- **La maîtrise** est portée pour les OC (`ability/Mastery.java`, voir ROLL.md) : l'aura dépensée fait monter le niveau jusqu'à 10. Les personnages de l'Unodex gardent leur niveau fixe.
- **Les animations et poses** (bras tendus pour le rayon, tremblement), **les shaders, sons et modèles 3D propres au modèle**. Ici : effets lumineux dessinés (traits, anneaux, sphères, éclats ; aucune particule) et sons Minecraft.
- **Les icônes.** Les cases affichent l'initiale de la capacité. Les icônes du modèle ne peuvent pas être copiées ; il faudra en dessiner.
- **Marc (Modes).** Le modèle n'a pas de pouvoir Modes : celui de Marc reste notre propre version (5 formes, « Changer de mode »).
- **Tous les pouvoirs du modèle sont portés** (55 sur 55, voir `docs/fidelite/README.md`). Les parties encore absentes ou approchées sont listées dans chaque fiche (≈, 🎨, ❌) ; un pouvoir que tu ajoutes toi-même garde la version générée automatiquement.

---

## 10. Pour aller plus loin : le code Java

Tout est dans `src/main/java/com/myriam/unrp/` :

| Fichier | Contenu |
|---|---|
| `data/AbilityPower.java` | Les champs d'un pouvoir et d'une capacité (ce guide, §3 et §4). |
| `data/AbilityEffect.java` | Les champs de `fx` (§5). |
| `ability/AbilityEngine.java` | Le moteur. `input` = les touches ; `tick` = pertes d'aura, passifs, épuisement ; `tickFast` = projectiles, barrières, bélier ; `run` = l'aiguillage `kind` → fonction. |
| `ability/AbilityEvents.java` | Branche les combats sur le moteur (barrières, `thorns`). |
| `player/PlayerStateService.java` | Aura maximale, régénération, stats. |
| `client/HudOverlay.java` | Carte du personnage et barre de capacités. |
| `client/ClientEvents.java` | Les touches. |

**Ajouter un nouveau type d'effet :**

1. Ajoute ses champs dans `AbilityEffect.java` s'il en faut de nouveaux. Recopie-les aussi dans la fonction `scale()` de `AbilityEngine.java`, sinon ils seront remis à zéro.
2. Écris une fonction `private static boolean monType(Ctx c)` dans `AbilityEngine.java`. Elle renvoie `true` si l'effet a eu lieu (l'aura est alors retirée et la recharge démarre) et `false` sinon.
3. Ajoute une ligne `case "mon_type" -> monType(c);` dans `run()`.
4. Documente-le dans ce fichier.

---

## 11. Le style de l'interface

**Le panneau d'état** (en haut à gauche) reprend la disposition du HUD du modèle :

- un portrait avec ton personnage en 3D qui tourne lentement, et un liseré de la couleur de ta rareté ;
- ton nom, ta rareté, ton niveau, et un badge « ACTIF » quand ton pouvoir est activé ;
- la barre de **vie** (rouge), puis la barre d'**aura** (dégradé violet → cyan, ou ta couleur si tu en as choisi une dans l'onglet Style), puis la **faim** et l'**armure**.

Quand une valeur baisse, la partie perdue reste un instant en sombre, puis s'efface (« traînée »). Les cœurs, la faim et l'armure de Minecraft sont masqués en survie, puisque le panneau les affiche déjà. Ils restent visibles en créatif et en spectateur.

| Où | Quoi |
|---|---|
| `client/HudOverlay.java` | Le panneau (`card`), ses couleurs (`HP_FILL`, `AURA_FROM`, `AURA_TO`...) et la barre de capacités (`moveBar`). Les positions sont en pixels, dans un panneau de 200 × 80. |
| `client/UiSkin.java` | Les couleurs communes (`BORDER`, `GOLD`, `HEAD`...) et les panneaux du hub. |
| `client/ClientEvents.java` | `onOverlay` : masque les barres de Minecraft. Supprime ce bloc pour les garder. |
| `tools/make_gui_textures.py` | Les textures `aura_back.png`, `aura_front.png` et `stars.png` (ciel du hub). Les couleurs sont en haut du fichier. Relance : `python3 tools/make_gui_textures.py` |

Le coin, la taille du HUD et la couleur de l'aura se règlent dans le hub, onglet **Style**.


---

## 12. Les effets visuels

Chaque pouvoir a un **style visuel** (`look.style`). Le style décide :

- de la forme des traits lumineux : droit, zigzag, ondulé, double hélice, ou sans cœur blanc (sombre) ;
- de la particule principale et de celle des impacts ;
- de l'aura visible autour du joueur quand le pouvoir est actif.

La couleur vient de `look.color`. Les 131 pouvoirs ont reçu un style et une couleur d'après leur description dans l'Unodex, par `tools/assign_styles.py`.

### Les 22 styles

| Style | Traits | Particules | Aura quand actif | Pouvoirs (exemples) |
|---|---|---|---|---|
| `lightning` | zigzag qui crépite, avec branche | arc, flare | petits éclairs sur le corps | Lightning |
| `fire` | ondulé | ember (flammèche), smoke | flammèches qui montent | Heat Palm, Explosion, Death Fire (bleu) |
| `ice` | droit | shard (cristal), glint | cristaux qui tournent autour | Icicle |
| `water` | ondulé | drop (goutte), ring | gouttes qui tombent | Hydrofreeze, Liquides |
| `wind` | double hélice | streak (traînée), mote | traînées en spirale | Azure Storm, Whirlwind |
| `shadow` | ondulé, sombre | smoke, wisp (volute) | fumée qui monte | Nightmare, Dark Blades, Umbral Shift |
| `light` | droit, épais | flare, glint | éclats qui tournent autour | Illumination, Lazor, Crescent Slash |
| `blood` | ondulé, sombre | drop, smoke | gouttes qui tombent | Life Steal, Crimson Thread |
| `nature` | ondulé | leaf (feuille), mote | feuilles qui tombent | Botanist, Conjure Vines |
| `mind` | double hélice | glyph (runes), ring | runes qui tournent autour | Hypnosis, Telepathy, Aura Manipulation |
| `gravity` | double hélice | ring, wisp | poussière aspirée vers le joueur + anneau | Graviton, Telekinesis, White Hole |
| `barrier` | droit | hex (hexagone), glint | hexagones qui tournent autour | Barrier, Protector, Imprison |
| `cloak` | droit | mote, glint | miroitement | Invisibility, Ghost |
| `speed` | droit | streak, mote | traînées derrière soi | Speed, Fleetstep, Quick Strike |
| `strength` | droit, épais | glint, ring | éclats qui montent | Strength, Super Strength, Clobber |
| `heal` | ondulé | cross (croix), mote | croix qui montent | Healing, Regeneration, Heal Link |
| `poison` | ondulé | drop, smoke | fumée qui monte | (aucun pour l'instant) |
| `earth` | droit, épais | rock (roche), smoke | roches qui tournent autour | Tremor, Rock Arm, Stone Skin |
| `metal` | droit | glint, shard | reflets qui tournent autour | Armor Suit, Blade, Iron Knuckle |
| `sound` | ondulé | ring | anneaux qui pulsent | Shockwave |
| `space` | double hélice | wisp, glint | volutes qui tournent autour | Portals, Teleportation, Time Manipulation |
| `energy` | droit | flare, mote | poussière lumineuse | Energy Discharge, Grenadier, Arcane Shot |

**Par type d'effet**, en plus du style :

| `kind` | Rendu |
|---|---|
| `beam` | trait lumineux du style jusqu'au point touché, éclats à l'impact |
| `bolt` | tête lumineuse qui avance, traînée du style, anneau à l'impact |
| `strike` | arc de balayage devant soi, trait vers chaque cible, éclats |
| `blast`, `pulse`, `scan` | onde au sol qui s'élargit, éclats |
| `teleport` | trait du point de départ au point d'arrivée |
| `shell` | sphère quadrillée qui se déploie, flashe sous les coups, se fissure et vole en éclats |
| `ram` | bouclier hexagonal devant soi |
| `buff`, `heal`, `share`, `debuff` | éclats du style sur la cible |

### Changer l'apparence d'un pouvoir

Dans `ability_powers.json`, change `look` :

```json
"look": { "style": "fire", "color": "#3D7BFF" }
```

Champs de `look` :

- `style` : un des 22 noms ci-dessus.
- `color` : la couleur.
- `sprite` (facultatif) : une autre particule principale que celle du style.
- `jagged` : `true` force les traits en zigzag.
- `particle` (facultatif) : une particule Minecraft en plus.

> **Attention :** `python3 tools/assign_styles.py` réécrit le style et la couleur de **tous** les pouvoirs. Pour garder tes changements, modifie plutôt la ligne du pouvoir dans ce script, puis relance-le.

### Les 16 particules du mod

`arc`, `ring`, `flare`, `hex`, `mote`, `ember`, `shard`, `drop`, `streak`, `glyph`, `smoke`, `wisp`, `glint`, `leaf`, `cross`, `rock`.

Elles sont blanches dans les fichiers et teintées par le jeu. Elles restent lumineuses dans le noir, sauf la fumée, qui peut être sombre. Chacune a son mouvement :

- l'arc change de forme ;
- l'anneau s'agrandit ;
- la flammèche monte en rétrécissant ;
- la fumée gonfle ;
- la goutte tombe ;
- la feuille tourne en tombant ;
- les cristaux, roches et runes tournent.

### Où modifier

| Fichier | Contenu |
|---|---|
| `ability/Styles.java` | Les 22 styles (particules, forme des traits, aura) et le dessin des auras (`aura`) et des impacts (`impact`). |
| `client/FxRenderer.java` | Le dessin des traits (zigzag, ondulé, hélice), des barrières, boucliers et ondes. |
| `client/FxParticle.java` | Le mouvement de chaque particule. |
| `ability/Fx.java` | Les fonctions que le serveur appelle pour déclencher un effet. |
| `tools/make_particle_textures.py` | Dessine les images des particules. Relance : `python3 tools/make_particle_textures.py` |
| `tools/assign_styles.py` | Le style et la couleur de chaque pouvoir. |

**Tester une particule** dans le jeu (couleur en décimal, taille, durée en ticks) :

```
/particle unrp:ember 16738847 1.0 20 ~ ~1 ~ 0.3 0.3 0.3 0 15
```


---

## 13. Les poses du corps

Quand une capacité est utilisée, le personnage prend une pose : bras tendus, bras levé, élan... Tout le monde la voit.

**Où elles sont** : `src/main/resources/assets/unrp/poses.json`. Chaque pose a :

```json
"br_ward": { "len": 8, "ease": "out_cubic", "keys": [
  { "t": 0, "right_arm": [-60, -20, 0], "left_arm": [-60, 20, 0] },
  { "t": 5, "right_arm": [-92, -10, 0], "left_arm": [-92, 10, 0], "body": [-5, 0, 0], "head": [3, 0, 0] }
] }
```

| Champ | Rôle |
|---|---|
| `len` | durée en ticks |
| `ease` | courbe entre deux images : `linear`, `in_out_sine` (défaut), `out_cubic`, `in_cubic`, `out_back` (dépasse puis revient) |
| `fade_in`, `fade_out` | ticks de fondu avec la posture normale (défaut 3 et 4) |
| `keys` | images-clés : `t` (tick), puis les membres à placer |

Membres : `head`, `body`, `right_arm`, `left_arm`, `right_leg`, `left_leg`. Chacun prend `[x, y, z]` en degrés. Un membre absent d'une image garde sa position normale (marche, regard).

Repères pour les angles :

- bras `x = -90` : tendu devant ; `x = -180` : levé vers le ciel ; `x = 40` : en arrière ;
- bras droit `z > 0` : écarté sur le côté ; bras gauche : `z < 0` ;
- bras droit `y < 0` : ramené vers le centre ; bras gauche : `y > 0` ;
- `body x > 0` : penché en avant ;
- `head x > 0` : regarde en bas.

**Relier une pose à une capacité**, dans `ability_powers.json` :

- `"pose": "br_ward"` : la pose jouée par la capacité ;
- `"pose_sneak": "br_point"` : la pose si le joueur est accroupi ;
- au niveau du pouvoir, `"activate_pose"` : la pose jouée à l'activation (R).

Sans `pose`, une pose par défaut est choisie selon le type d'effet : `cast_beam`, `cast_bolt`, `cast_strike`, `cast_blast`, `cast_pulse`, `cast_buff`, `cast_dash`, `cast_heal`, `cast_mind`, `cast_wall`.

Une capacité maintenue tient sa pose tant que la touche est enfoncée. Les autres la jouent une fois.

**Poses des capacités originales** (les 83 pouvoirs maison de l'Unodex) : 19 poses `og_*` dessinées pour l'occasion, choisies selon le geste de la capacité. `og_point` (tir au doigt), `og_beam` (rayon à deux mains), `og_bow` (arc), `og_toss` (lancer), `og_punch` / `og_flurry` (coup puissant / rafale), `og_slash` / `og_claw` (lame, griffes), `og_slam` (écrasement), `og_wave` (onde, répulsion), `og_temple` (esprit), `og_scan` (détection), `og_surge` (surcharge du corps), `og_heal`, `og_dash`, `og_summon`, `og_command`, `og_guard`, `og_touch`. Une capacité à charge joue la pose du geste, sa version maintenue joue la pose tenue. Pour en changer une, édite le champ `pose` du mouvement.

**Poses des pouvoirs du modèle** : 17 poses `md_*` s'ajoutent aux poses existantes (`tk_*`, `heal_*`, `tm_*`, `hy_*`...) pour les 112 mouvements qui n'avaient que la pose générique : `md_orb` (sphère tenue en l'air), `md_launch` (deux mains, lancer), `md_hook`, `md_uppercut`, `md_stance`, `md_harden`, `md_spin`, `md_fly`, `md_lash`, `md_wall`, `md_rise`, `md_axe`, `md_spear`, `md_grab`, `md_palm`, `md_dive`, `md_flare`. Elles servent entre autres à Daystar, Salt Seraph, Black Lash, Armament Dominion et aux pouvoirs de combat.

**Effets visuels sans particule** : plus aucun pouvoir n'émet de particule. Tout passe par `ability/Vfx.java` (même signature que `sendParticles` : type, position, nombre, étalement, vitesse), qui envoie un paquet `GLOW` ; `client/FxRenderer.java` le dessine en lumière additive, comme les traits et les barrières : éclats (petits traits qui s'éparpillent), anneaux (coup balayé), éclairs de lumière (explosion). Le type de particule d'origine ne sert qu'à choisir la couleur (`flame` orange, `end_rod` blanc, `squid_ink` sombre...), une poussière colorée garde sa teinte. Une capacité qui veut une forme précise appelle `Fx.line`, `Fx.wave`, `Fx.shell` ou `Vfx.glow`.

**Sons des capacités** : chaque pose (le geste) joue sa signature sonore, faite de couches de sons Minecraft (événement, volume, hauteur), sans aucun fichier audio ajouté. Tout est dans `src/main/resources/data/unrp/unodex/pose_sounds.json` : `signatures` (par exemple `slam` = explosion étouffée + choc d'enclume grave) et `poses` (quelle pose joue quelle signature). Le son suit donc l'animation : une capacité qui change de pose change de son. Le code est `ability/Sounds.java`, appelé par `Poses.play` et `Poses.hold` (au plus un son toutes les 6 ticks par joueur). Les sons que certains modules jouent déjà directement (explosions, cristaux, claquements) restent en plus.

**Objets et entités des capacités** (`ability/Anim.java`, tout en lumière dessinée) : la hache solaire de Daystar et les armements d'Armament Dominion ont une lame lumineuse qui pulse dans la main, avec trois traits en éventail quand on frappe ; le Masque du Joker porté a un halo au-dessus de la tête ; les constructions de Particles (ArmorStand `unrp.pconstruct`) tournent, flottent, ont un halo au sol et une colonne de lumière ; un bloc soulevé par la télékinésie est entouré d'un cadre lumineux qui tourne ; les blocs temporaires (`Walls`, murs de sel, barrières) jaillissent et s'effritent en éclats. Le mod n'a pas d'entités ni de modèles d'objets propres (pas de textures) : l'animation est donc ajoutée par-dessus les objets et entités vanilla.

**Projectiles** (`ability/ProjFx.java`, lumière dessinée) : chaque sorte a son dessin. Missile = orbe + sillage ondulé, explosion en éclair et onde ; poing / paume = orbe + cinq doigts en éventail ; chute de main = bras de lumière qui tombe + doigts, éclats au sol ; croissant = arc de lumière dont la largeur suit la taille ; aiguille = trait fin. Le projectile générique du moteur (rayons et tirs des pouvoirs automatiques) porte une tête en orbe. Daystar : le soleil est un orbe à sa vraie taille avec une traînée, le croissant un arc doré, la hache lancée tournoie en croix. Salt Seraph : la lance est un long fût blanc à pointe en V (plus long et plus épais pour la Grande lance).

**Tester une modification sans relancer le jeu** : modifie `poses.json`, puis appuie sur **F3 + T** en jeu (rechargement des ressources).

**Technique** : la pose est appliquée par un mixin (`mixin/PlayerModelMixin.java`) après que Minecraft a placé les membres. Le calcul est dans `client/PoseClient.java`.

---

## 14. Fidélité au mod modèle

Le dossier `docs/fidelite/` contient une fiche par pouvoir reproduit. Chaque fiche compare le modèle et notre version, ligne par ligne : fiche du pouvoir, comportement et rendu, avec un statut pour chaque ligne.

---

## 15. Champs ajoutés pour reproduire le modèle

**Au niveau du pouvoir :**

| Champ | Rôle | Exemple |
|---|---|---|
| `stances` | noms des postures ; la barre affiche les capacités de la posture courante | Hydrofreeze : eau / glace |
| `on_activate` | effet à l'activation : `sky_strike` (la foudre tombe sur toi si tu vois le ciel) | Lightning |
| `activate_pose` | pose jouée à l'activation | `lt_call` |
| `sense_radius` | tant que le pouvoir est actif, ce qui vit dans ce rayon brille | Lightning : 14 |
| `heal_every`, `heal_amount` | soin passif : `heal_amount` PV toutes les `heal_every` ticks | Regeneration : 2 PV / 40 ticks |
| `drain_when_hurt` | la perte d'aura ne s'applique que si tu es blessé | Regeneration |

**Au niveau d'une capacité :**

| Champ | Rôle | Exemple |
|---|---|---|
| `stance` | posture où elle apparaît (0, 1…) ; absente ou -1 = toujours | Lance de glace : 1 |
| `special` | capacité propre au pouvoir, codée en Java (`ability/Specials.java`, `Specials2.java`, `Specials3.java`, `Specials4.java`) ; `stance_next` = posture suivante | `tm_rewind`, `tk_force`, `sp_haymaker` |
| `unlock` | rang de déblocage (2, 3 ou 4) : coûte rang - 1 points d'aptitude ; 0 ou absent = disponible d'emblée | Bélier : 3 |
| `requires` | index (dans `moves`) de la capacité à débloquer avant celle-ci ; -1 ou absent = aucune | Domaine des mille lames : 3 |
| `min_aura` | aura minimale pour lancer, si différente du coût | Frappe au sol : 75 (coût 40) |
| `group` | un seul interrupteur allumé par groupe | formes de Phase Shift |
| `heal_every`, `heal_amount` | soin tant que la capacité est tenue ou allumée | Soin personnel |
| `on_hit` | effets donnés à chaque coup au corps à corps tant que l'interrupteur est allumé | Lames de glace : lenteur |
| `surge` | course forcée droit devant (blocs par tick) tant que c'est maintenu | Course foudroyante : 1,2 |
| `cloak` | cape : chutes amorties, les monstres repèrent mal, attaquer te trahit 1 s | Cape optique |

**Les capacités spéciales existantes** sont réparties en dix-sept fichiers (`Specials.java` à `Specials16.java`). Chaque fonction dit en commentaire ce qu'elle reproduit du modèle ; la comparaison détaillée est dans `docs/fidelite/`. Un module a toujours les mêmes entrées : `handles`, `run`, `tick`, `onHurt`, `onLogout`.

**`Specials.java`**

- Telekinesis : `tk_pressure`, `tk_force`, `tk_orbit`, `tk_dominion` ;
- Healing et Regeneration : `heal_mark`, `heal_external`, `heal_triage`, `regen_adrenaline`, `martyr` ;
- Hydrofreeze : `hydro_envelop`, `hydro_freeze`, `water_form` ;
- Nightmare : `nm_field`, `nm_hallucinate`, `nm_panic` ;
- Time Manipulation : `tm_flow`, `tm_rewind`, `tm_shift`, `tm_freeze`, `tm_strike`, `tm_delayed`.

**`Specials2.java`** (lot 3) :

- Ability Gauging : `gauge_touch`, `gauge_scope` (analyse masquée par le Masque du Joker) ;
- Arachnid : `ar_assault`, `ar_guard`, `ar_leap`, `ar_rush` ; Arcane Shot : `arcane_charge`, `arcane_volley` ;
- Armor Suit : `suit_brace`, `suit_crash`, `suit_shed`, `suit_volley` ; Azure Storm : `storm_field`, `storm_guard`, `storm_flight`, `storm_lance` ;
- Aura Manipulation : `am_copy` (réserve de copies), `am_expel` ;
- Blademaster : `bm_blade`, `bm_ranged`, `bm_arc`, `bm_manifest`, `bm_domain` ;
- Catalyst Ray : `cr_catalyze`, `cr_clean`, `cr_stasis`, `cr_imbue` (20 mutations avec poids, surcharge selon le niveau).

**`Specials3.java`** (Augmentation, 1re partie) :

- Strong Punch : `sp_haymaker`, `sp_break`, `sp_smash` ; Strong Kick : `sk_axe`, `sk_sweep`, `sk_smash` ;
- Dizzy Punch : `dz_spin`, `dz_sucker`, `dz_weave` (et l'éclat d'ivresse au 3e coup) ; Stone Skin : `st_harden`, `st_immovable`.

**`Specials4.java`** (Augmentation, suite) :

- Quick Strike : `qs_blitz`, `qs_stance`, `qs_combo`, `qs_pursuit` ; Rock Arm : `ra_smash`, `ra_guard` ;
- Heat Palm : `hp_palm`, `hp_smelt` ; Hand Blade : `hb_thrust`, `hb_release` ; Contact Shred : `cs_grip`, `cs_rake` (marques et déchirures).

**`Specials5.java`** (projectiles) : Missiles `ms_*`, Needles `nd_*`, Phantom Fist `pf_*`, Phantom Push `pp_*`, Crescent Slash `cs_*` (charges maintenues avec recharge propre, séquences retardées). Tous passent par **`Projectiles.java`**, la boucle commune des missiles, mains, croissants, aiguilles et sphères (remplace les entités du modèle).

**`Specials6.java`** (griffes et flammes) : Spectral Claw `sc_*`, Demon Claw `dc_*`, Flame Claw `fc_*`, Shadow Flame `sf_*`, Energy Discharge `ed_*`.

**`Specials7.java`** (quantiques) : Explosion `ex_*`, Tremor `tm_*`, Illumination `il_*`, Umbral Shift `us_*`, Whirlwind `ww_*`.

**`Specials8.java`** (construction) : Conjure Disks `cd_*`, Conjure Vines `cv_*`, Spikes `sk2_*` (pointes et épines du modèle).

**`Specials9.java`** (esprit et chasse) : Hypnosis `hy_*`, Predator `pd_*`.

**`Specials10.java`** (liens d'aura) : Energy Conduit `ec_*`.

**`Specials11.java`** (particules) : Particles `pt_*` (vols, motes, marques, constructions en ArmorStand marquées `unrp.pconstruct`, plaque de tissage en blocs temporaires).

**`Specials12.java`** (traits d'aura) : Aura Transference `at_*` (table de 48 traits, liste de traits dans la donnée persistante du joueur, imprégnation d'objet ou de personne).

**`Specials13.java`** (arsenal) : Armament Dominion `ad_*` (coffre de 12 armements dans la donnée persistante, épées vanilla marquées `unrp_ad*`).

**`Specials14.java`** (soleil) : Daystar `ds_*` (jauge de soleil, aura de chaleur, hache lancée, soleil chargé, faille, High Noon et Burnout ; formules de `DaystarMath`).

**`Specials15.java`** (sel) : Salt Seraph `ss_*` (sel accumulé, statues et piliers, lance, mur, flèches, sceau, Éveil et trompette ; formules de `SaltMath`).

**`Specials16.java`** (tentacules) : Black Lash `bl_*` (tension et frénésie, agrippement, plongeon, entrave, fouet, garde, tempête ; formules de `LashMath`). Le balancier, le lance-pierre et le vent mortel ne sont pas repris.

**Règle d'aura des capacités spéciales** : le moteur retire le coût (`cost`) **après** une capacité réussie. Pour une capacité maintenue, le coût de départ est retiré dans le code (`AbilityEngine.spendAura`) et `drain_every` gère l'entretien. Une capacité qui échoue renvoie `false` : rien n'est retiré, il n'y a rien à rendre.

**Apparences** : `Specials2.looks` dessine des traits et des particules (lames de Blademaster et de Hydrofreeze, plaques d'Armor Suit, traînée de Phase Shift). Aucune image ni aucun modèle du modèle n'est copié.

---

## 16. Les stats agissent sur le corps

Les cinq stats du personnage ne servent pas qu'aux capacités : elles modifient **vraiment** le corps du joueur, en permanence, pouvoir actif ou non.

| Stat | Effet sur le corps (par point) |
|---|---|
| Puissance | +0,4 dégât au corps à corps, +0,05 recul infligé |
| Défense | +1 armure, +0,4 robustesse, +4 % de résistance au recul (maximum 60 %) |
| Récupération | +1 PV max (un demi-cœur) ; récupération naturelle de 0,5 PV toutes les (200 - 15 × Récup.) ticks, 40 au minimum |
| Vitesse | +2 % de vitesse de déplacement, +0,1 vitesse d'attaque |
| Ruse | aucun effet sur le corps : elle allonge la durée des effets des capacités et augmente l'aura max |

Les stats utilisées sont les **stats effectives** :

> fiche de l'Unodex + points d'entraînement + bonus du mode ou de la forme en cours.

Elles sont recalculées à chaque changement : passer en mode Offensif augmente aussitôt tes dégâts au corps à corps.

**Où régler** : `player/BodyStats.java`. Les valeurs par point sont en haut du fichier.

### Modes, formes et stats

- Un **mode** (`modes`) peut porter `"stats": [Puissance, Défense, Récupération, Ruse, Vitesse]` : des bonus ou malus appliqués tant qu'il est actif.
- Une capacité tenue ou un interrupteur allumé peut aussi porter `"stats"`. Exemple : la Garde de Marc donne Défense +3.
- Une capacité avec `"mode": 2` n'apparaît que dans le 3e mode. Changer de mode change donc les capacités de la barre. Ce qui n'est plus sur la barre s'éteint.
- Le message de changement de mode affiche les bonus. Exemple : « Mode : Offensif (Puissance +3, Défense -1, Vitesse +1) ».

**Marc (Modes)** :

| Mode | Stats | Capacité propre |
|---|---|---|
| Équilibré | — | Frappe du mode (chargée) |
| Offensif | Puissance +3, Défense -1, Vitesse +1 | Assaut (3 coups sur une cible) |
| Défensif | Puissance -1, Défense +4, Récupération +1, Vitesse -2 | Garde (interrupteur, Défense +3, inébranlable) |
| Destructeur | Puissance +5, Défense -2, Récupération -1, Vitesse -1 | Frappe destructrice (onde chargée) |
| Vélocité | Puissance -1, Défense -1, Ruse +1, Vitesse +4 | Élan (bond) |

Dans tous les modes, Marc garde « Changer de mode » et « Scanner ».

**Phase Shift** : la forme défensive donne Puissance -2, Défense +3, Récupération +1, Vitesse -2, et la forme offensive Puissance +3, Défense -3, Vitesse +2. Ces bonus s'ajoutent aux attributs du modèle.


---

## 17. Systèmes ajoutés pour coller au modèle

**Déblocages (`ability/Unlocks.java`).** Le rang 2, 3 ou 4 d'une capacité coûte 1, 2 ou 3 points d'aptitude et vaut pour tous les pouvoirs du joueur. Points gagnés : 1 par niveau, au moins 1, dès le niveau 2 ou après 20 minutes de jeu depuis la création du personnage (`playTicks`, `social/SocialData.java`). Une capacité exige aussi que sa parente (`requires`) soit débloquée. Achat : hub, onglet Capacités. La barre de capacités grise les cases verrouillées avec leur rang. Les copies d'Aura Manipulation restent libres. Admin : `/unrp unlock <joueur> all|reset`.

**Esquive (`AbilityEngine.dodge`).** Tout joueur a 12 % x (Vitesse / 10) de chances d'annuler une attaque, sauf les dégâts d'environnement (chute, feu, noyade, explosion, etc.).

**Aura Manipulation, réserve de copies.** « Copier » ajoute le pouvoir du joueur visé (24 blocs) à une réserve, sans minuteur. Maj + R passe au pouvoir copié suivant, puis revient au tien. « Expulser », l'aura à 0 ou la mort vident la réserve. Une seule copie est en main à la fois.

**Masque du Joker (`item/ModItems.java`).** Se porte sur la tête ; recette : 5 laines noires + 1 ficelle (`WWW` / `WSW`). Une cible qui le porte donne une analyse d'aura vide. Texture : `tools/make_item_textures.py`.

**Capacité d'un OC.** Tirée à la création dans `ability_pool.json` (poids du modèle) ; détails dans [ROLL.md](ROLL.md). `Profile.ocAbility` la mémorise ; `Profile.abilityOf(personnage)` renvoie celle du personnage de l'Unodex ou celle de l'OC.

**Effet Désorienté (`item/ModEffects.java`).** Inverse les touches de déplacement (côté client, `ClientEvents`). Donné par le Haymaker tournoyant de Dizzy Punch.
