# Tirage à la création d'un OC

Quand un joueur crée un OC, le serveur fait trois choses :

1. il tire sa **rareté**, qui fixe son **niveau de départ** (`rarity.json`, voir ci-dessous) ;
2. il tire sa **capacité** dans un pool éditable (`ability_pool.json`, voir plus bas) ;
3. ses **stats** de départ sont tirées dans les plages de sa capacité (potentiels) et valent `potentiel x racine(niveau / 10)` ; le niveau évolue ensuite jusqu'à **10** et les stats suivent par ratio.

Le client ne tire jamais rien : tout est décidé par le serveur.

## Probabilités (roll normal attendu)
| Rareté | Probabilité | Niveau tiré |
|---|---|---|
| Cripple (à vie) | 2,36 % | 1.0 |
| Cripple | 3,54 % | 1.0 |
| Low | 25 % | 1.0 à 1.9 |
| Mid | 54,99 % | 2.0 à 3.4 |
| Elite | 12 % | 3.5 à 4.9 |
| High | 2 % | 5.0 à 5.9 |
| God | 0,1 % | 6.0 à 7.4 |
| Exo-god | 0,01 % | 7.5 à 10.0 |

Total exact : 100 %. Seules les colonnes « Normal attendu » de ton tableau sont utilisées
(pas les observés, pas les rerolls).

## Comment la rareté est tirée
1. Un nombre aléatoire choisit la rareté selon les poids (les poids sont normalisés : le total n'a pas besoin de faire 100).
2. Le niveau est tiré uniformément dans `[level_min, level_max]`, arrondi à 1 décimale.

## Une capacité est unique
Dès qu'un joueur a une capacité (personnage de l'Unodex réclamé, ou OC qui l'a tirée), **plus aucun autre joueur ne peut la tirer**. Le pool ne propose que les capacités libres ; les variantes d'un même pouvoir comptent pour la même (`modes_atk` et `modes_def`). Si toutes sont prises, l'OC n'a « aucune capacité libre » pour l'instant ; `/unrp rollability` retente. Une capacité se libère quand son joueur efface son profil. Seuls les **joueurs** comptent : un personnage de l'Unodex que personne n'a réclamé ne bloque pas sa capacité.

## La capacité (pool `ability_pool.json`)
- Fichier : `src/main/resources/data/unrp/unodex/ability_pool.json` (un datapack peut le remplacer). Une ligne par capacité : `"id": poids`.
- (Explication de chacune : `docs/ORIGIN.md`.)
- **Jamais tirées** : les capacités du modèle au poids 0 (Sarcophage, Edict, Wyrm Engine, Singularity, Limit Breaker, Dealmaker, Counterweight, Crimson Mark, Hollow Mark, Nadir, Chained One). Elles sont bloquées dans le code (`UnodexData.NOT_ROLLABLE`) même si on les écrit dans `ability_pool.json` : elles ne s'obtiennent que par attribution.
- Départ : les capacités du lore déjà codées (55 pouvoirs ; Phase Shift est coupé en ses deux formes), avec les **poids du mod modèle** (`POWER_WEIGHT`). Exemples : Regeneration, Dizzy Punch, Strong Punch, Invisibility et Ability Gauging 100 ; Telekinesis et Aura Manipulation 13 ; Time Manipulation 8.
- **Pour ajouter une capacité** : écris son id et un poids. Elle doit avoir un pouvoir dans `ability_powers.json` pour être jouable. Poids 0 ou ligne supprimée = jamais tirée.
- **Cripple** et **Cripple (à vie)** n'ont pas de capacité à la création. Un OC créé avant ce système (sans capacité, ni Cripple ni à vie) en reçoit une au premier usage.
- Admin : `/unrp rollability <joueur>` retire une capacité à un OC.

## Les stats d'un OC et son évolution
- À la création, chaque stat reçoit un **potentiel de départ** tiré dans les plages de sa capacité (`stat_ranges.json`, reprises du modèle : `STAT_RANGES`), avec le biais bas du modèle (le plus petit de 5 tirages). Exemple Strong Punch : Puissance 5,5 à 6,5, Défense 3,25 à 4,25, Récupération 1,75 à 2,75, Ruse 1,5 à 2,5, Vitesse 2,0 à 3,0. Un Cripple a 1,0 partout.
- La stat actuelle suit le niveau par un **ratio** : `stat = (potentiel + entraînement) x racine(niveau / 10)`, arrondie, minimum 1 (c'est le `levelFactor` du modèle). Au niveau 10 (le maximum), la stat vaut le potentiel.
- Le niveau est un **potentiel d'évolution** : il monte jusqu'à **10 au maximum** (`ability/Mastery.java`). Il n'existe pas de niveau au-dessus de 10 ; « Exo-god » est un terme inventé pour les niveaux 7,5 à 10.
- **Maîtrise** (comme le modèle, `aura_system.js`) : chaque point d'aura dépensé remplit une barre. Il faut `(niveau x 10) x 120` aura pour passer au niveau suivant (+0,1), puis la barre repart à zéro. Exemples : niveau 1,0 : 1200 aura ; niveau 2,5 : 3000 ; niveau 9,9 : 11880.
- Chaque niveau gagné donne aussi des points d'aptitude (rangs de capacités, voir POUVOIRS.md) et des points de stats.
- Les personnages de l'Unodex ont des stats de fiche fixes, mais s'entraînent aussi (+0,5 par palier).

## L'entraînement (règles exactes du modèle)
- **Points de stats** : 1 par niveau (au moins 1, dès le niveau 2 ou après 20 minutes de jeu), comme les points d'aptitude. Il n'y a plus de points gagnés au combat.
- **Un palier** monte le **potentiel** d'une stat de **+0,5**. **4 paliers au plus** par stat (+2,0 au total).
- **Coût** : le palier n°n coûte n points (1, 2, 3 puis 4 : 10 points pour une stat à fond).
- **Plafond** : le potentiel ne dépasse jamais **10,0**. Près du plafond le palier est raccourci (97 → 100) ; au plafond, refusé.
- **Retirer un palier** (bouton −) rend son coût et enlève le gain exact du palier.
- Hub, onglet Entraînement : points disponibles, potentiel de chaque stat, paliers pris (k/4), coût du prochain.
- Les anciennes sauvegardes (points +1 par stat) repartent de zéro.

## Hypothèses à valider
1. **Plages de niveau** : déduites des niveaux de ton Unodex (Mid 2.0–3.4, Elite 3.5–4.9, High 5.0–5.9, Low 1.2–1.9 étendu à 1.0). God 6.0–7.4 et Exo-god 7.5–10.0 : Exo-god est un terme inventé pour les niveaux 7,5 à 10.
2. **Niveau maximum 10** : les anciens OC au-dessus de 10 sont ramenés à 10 au chargement.
3. **Cripple vs Cripple (à vie)** : « à vie » ne peut jamais avoir de capacité. Pour « Cripple » simple, une capacité pourra s'éveiller plus tard (pas encore codé : aujourd'hui, il n'en a pas). À confirmer.
4. **Ratio** : j'ai pris la racine du modèle (`racine(niveau / 10)`). Si tu préfères un ratio linéaire (`niveau / 10`), dis-le et je change une ligne.
6. **Multiplicateurs de stats** du modèle (`mult_*`, 1,0 à 2,0, tirés avec les potentiels) : non portés ; je n'ai vu leur effet que dans l'esquive.
5. **Vitesse d'évolution** : celle du modèle (`niveau x 10 x 120` aura par niveau). C'est lent (de 1,0 à 10, plus de 500 000 aura au total) ; baisse ou monte `Mastery.RATE` si tu veux plus vite.

## Modifier
Édite `weight`, `level_min`, `level_max` dans `rarity.json` (ou via datapack). Garde le total à 100 si tu veux
que le test `expectedProbabilitiesSumToOne` reste vert.

## Vérifier
- Tests automatiques : `./gradlew build` (2 millions de tirages comparés aux probabilités attendues).
- En jeu : `/unrp rolltest 1000000` affiche obtenu / attendu pour chaque rareté.
