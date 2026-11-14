# Architecture et feuille de route

## Principe
**Les données décident, le code exécute.** Personnages, écoles, capacités et probabilités sont des JSON
générés depuis l'Unodex. Le mod les charge, les expose aux commandes et (plus tard) au menu, aux PNJ et aux pouvoirs.

```
Unodex (Google Sheet)
   │ export
   ▼
tools/build_unodex.py  ◄── tools/overrides/*.json (tes corrections)
   │ génère
   ▼
data/unrp/unodex/*.json ──► UnodexLoader (reload listener) ──► UnodexData (en mémoire)
                                                                   │
              ┌────────────────────────┬───────────────────────────┤
              ▼                        ▼                           ▼
        RarityTable (roll)       ClaimData (pris/libre)      commandes /unrp
```

## Code actuel (phase 1)
| Classe | Rôle |
|---|---|
| `UnrpMod` | point d'entrée Forge : enregistre le chargeur et les commandes |
| `data/UnodexLoader` | lit `data/unrp/unodex/**.json`, tolère un fichier cassé (log + ignore), `/reload` |
| `data/UnodexData` | état chargé (écoles, personnages, capacités, rareté) |
| `data/Models` | structures des JSON (clés snake_case) |
| `data/RarityTable`, `RarityTier` | table et logique de tirage, **sans dépendance Minecraft** (donc testable) |
| `data/ClaimData` | qui joue quel personnage, sauvegardé dans le monde ; 1 joueur = 1 personnage |
| `command/UnrpCommands` | `menu`, `whoami` pour tous ; le reste réservé aux admins (niveau 2) |
| `menu/MenuService` | côté serveur du menu : construit les données envoyées, valide les candidatures et les OC |
| `net/*` | paquets : `MenuDataPacket` (serveur vers client), `ClaimRequestPacket` et `CreateOcPacket` (client vers serveur) |
| `client/CandidatureScreen`, `ClientHooks` | l'écran ; chargé uniquement côté client |

## Compatibilité avec les autres mods
Principe : **dépendances obligatoires minimales** (Palladium et GeckoLib seulement, et seulement quand le code les utilise).
Tout le reste est « amical » : le mod ne casse rien et s'y adapte.

| Mod | Rôle prévu |
|---|---|
| Palladium (+ PalladiumCore), GeckoLib | moteur des pouvoirs et animations (phase 4). Obligatoires à partir de là |
| KubeJS, Rhino, Architectury | requis par Palladium/KubeJS ; KubeJS pourra servir aux scripts de règles (aura, duels) |
| Pehkui | optionnel : applique la **taille** de la fiche (`height_cm`) aux personnages |
| Ragdollified (+ pc/pr/reactions), Zarith | optionnels : ragdoll à la mise KO ; Zarith est la couche physique dont ils dépendent |
| VisualHealth, BetterBloodOverlay | purement visuels : rien à coder, juste ne pas dupliquer leur affichage dans notre HUD |
| GeckoLib Animation Optimizer | optimisation : à tester avec nos entités PNJ |
| Xaero (minimap / carte du monde) | on ne remplace pas la carte : le HUD de l'aura sera déplaçable pour ne pas recouvrir la minimap ; marqueurs d'écoles envisagés **si** l'API de Xaero le permet (à vérifier en phase 3) |

Règle de test : chaque mod est essayé dans une **vraie instance** (client + serveur) avec le jar `unrp`,
pas seulement dans l'environnement de développement.

## Feuille de route
| Phase | Contenu | État |
|---|---|---|
| 1 | Pipeline Unodex, chargement des données, roll, claims, commandes, docs | fait |
| 2 | **Menu de candidature** à la connexion : onglets Lore / RP / OC, liste des personnages libres, création d'OC avec tirage de potentiel, envoi des données au client, validation côté serveur | **fait** (à tester chez toi) |
| 3 | **Écoles, principaux et PNJ** (voir ci-dessous) | à faire |
| 4 | **Pouvoirs** par vagues (voir ci-dessous) | à faire |
| 5 | Finitions : HUD d'aura configurable, réglages joueur, duels, niveaux | à faire |

### Phase 2 : menu de candidature (réalisé)
- Écran à la première connexion (et via une touche) : onglets **Lore**, **RP**, **OC**. Chaque fiche affiche tier, niveau, capacité, école, image.
- Les personnages **pris** sont grisés (`ClaimData`). La candidature part au serveur, qui valide (règle : 1 joueur = 1 personnage, personnage libre).
- **OC** : nom, apparence, école ; le serveur tire le potentiel (`RarityTable`) : le client n'a aucun pouvoir sur le résultat.
- Un joueur = un profil (personnage ou OC), sauvegardé dans le monde (`ClaimData`). Un admin peut le réinitialiser (`/unrp reset`).
- Les OC rejoignent une **école** (pas un groupe comme Vigilante). Le nom doit être unique et différent des personnages de l'Unodex.
- Si deux joueurs visent le même personnage, le serveur départage (le premier arrivé) et rafraîchit les menus ouverts.
- Pas encore fait : validation par un admin avant attribution d'un personnage canon (à décider), et rafraîchissement du menu après un `/reload`.

### Phase 3 : écoles, principaux, PNJ (conception)
Structure de génération des campus UNRP :
- les campus sont **construits par le mod, bloc par bloc, via une file d'attente à budget par tick** (pas de lag) ;
- choix d'un **site** parmi des candidats (éviter l'eau, terraformer, zones réservées) ;
- les **principaux** de chaque école = ses `leaders` (roi, reine, valet, soigneur), lus depuis l'Unodex : ce sont des PNJ placés en priorité ;
- les autres **personnages de l'école** (lore et rp) apparaissent aussi en PNJ **tant qu'ils ne sont pas pris** (`ClaimData`) ; dès qu'un joueur prend un personnage, son PNJ disparaît ;
- les 8 écoles/groupes de `schools.json` ; chacune aura son `campus` (variante, palette) dans les overrides.
**À prévoir pour cette phase** : l'apparence des campus (plans, références visuelles) ; sinon je pars sur un style par école.

### Phase 4 : pouvoirs (en cours)
**État** : 55 pouvoirs sur 55 portés (`docs/fidelite/README.md`). Code : `ability/AbilityEngine.java` (entrées, coûts, recharges, passifs), `Specials.java` à `Specials16.java` (capacités propres), `Projectiles.java` (projectiles communs), `Unlocks.java` (déblocages), `Fx.java` et `Styles.java` (effets visuels), `Poses.java` ; données : `ability_powers.json`. Détails : [POUVOIRS.md](POUVOIRS.md).

**Conception initiale :**
137 capacités distinctes dans l'Unodex (onglets `unodex` et `unodex-rp`). Elles sont classées en **vagues** (`wave` dans `abilities.json`) :
vague 1 : tiers bas, 10 capacités ; vague 2 : 43 ; vague 3 : 32 ; vague 4 : 24 ; vague 5 : 26 ; vague 6 (tier inconnu) : 2.
- On regroupe d'abord les capacités par **archétypes** (type + nature : projectile, renforcement, soin, statut, création, détection…) :
  un archétype codé une fois sert à beaucoup de capacités, piloté par les stats (power, defense, recovery, trick, speed) et le niveau.
- Les capacités uniques (ex. Aura Deity, Spectre) sont faites à la main, une par une, en dernier.
- Le suivi se fait dans `abilities.json` > `impl.status` via `tools/overrides/abilities.json`.
- Honnêteté : écrire 137 pouvoirs avec effets et animations est un gros chantier, il se fera en plusieurs étapes, pas en une seule.

## Choix techniques
- **Forge 1.20.1** : imposé par tes jars (Palladium, KubeJS, GeckoLib, Ragdollified… sont Forge) et ton serveur Forge local.
- **Java + JSON** plutôt que tout en scripts : le code reste petit, les données sont éditables par toi.
- Les ressources de jeu sont regroupées sous `assets/unrp/` et `data/unrp/`. Le script `tools/import_model_assets.py` adapte les références des ressources importées à ce namespace.
- Le mod avait d'abord été commencé en Fabric (projet `unrp/` précédent) : il est remplacé par celui-ci ; sa logique (aura, stats) sera reprise en phase 4.

## Objets et blocs
`item/ModItems` (registre + onglet « Unordinary RP »), `item/ModBlocks`, `item/UnItems` (comportements : hache, armement, Pourfendeur, livres, sérum, vêtements…). Les images, modèles, blockstates, tables de butin et noms EN/FR sont produits par `tools/gen_items.py` à partir d'une seule table. État objet par objet : `docs/fidelite/objets.md`.
Commande `/unrp undress` : retire toute la tenue cosmétique.
Vêtements : `item/Cosmetics` (état par joueur, durable, diffusé par `net/CosmeticPacket`) et `client/CosmeticLayer` (dessin). Ce ne sont pas des armures.
