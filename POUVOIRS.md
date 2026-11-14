# Pouvoirs : guide complet pour les modifier

Ce guide explique comment marchent les pouvoirs du mod `unrp` et comment les changer **sans toucher au code Java**. Il suffit d'éditer un seul fichier JSON.

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
| `look` | objet | aucun | Apparence : `color` (`"#RRGGBB"`, la couleur des traînées et de l'aura), `particle` (particule d'accent), `jagged` (`true` = traînée en zigzag, comme la foudre). |
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

- **Les déblocages progressifs.** Dans le modèle, les capacités 2 à 4 se débloquent avec des points (`unlock_s2`, `unlock_s3`, `unlock_s4`). Ici, tout est disponible tout de suite.
- **La maîtrise.** Dans le modèle, dépenser de l'aura fait monter le niveau de puissance. Ce n'est pas encore relié.
- **Les animations et poses** (bras tendus pour le rayon, tremblement), **les shaders, sons et modèles 3D propres au modèle**. Ici : particules colorées et sons Minecraft.
- **Les icônes.** Les cases affichent l'initiale de la capacité. Les icônes du modèle ne peuvent pas être copiées ; il faudra en dessiner.
- **Marc (Modes).** Le modèle n'a pas de pouvoir Modes : celui de Marc reste notre propre version (5 formes, « Changer de mode »).
- **Les 128 autres pouvoirs** gardent la version générée automatiquement. Ils se portent un par un sur le même principe que les trois ci-dessus.

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
