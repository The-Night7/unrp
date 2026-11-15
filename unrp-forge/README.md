# Unordinary RP (Forge 1.20.1)

Mod de RP inspiré d'UnOrdinary, pour ton univers (le roman « Unordinary » et le RP).
Il charge **toutes ses données depuis des fichiers JSON** générés à partir de ta feuille Unodex :
personnages, écoles, capacités, table de rareté. Tu modifies les données, pas le code.

**État : phase 2** (menu de candidature). Ce qui marche déjà :
- pipeline Unodex vers JSON (179 personnages, 137 capacités, 8 écoles/groupes, tirés des onglets `unodex` et `unodex-rp`) ;
- chargement des données dans le jeu, avec `/reload` comme un datapack ;
- tirage de potentiel avec tes probabilités (tests inclus) ;
- « personnage pris » sauvegardé dans le monde ;
- **menu à la connexion** : onglets Lore / RP / Créer un OC, liste des personnages libres ou pris, fiche détaillée, bouton « Postuler » ;
- **création d'OC** : nom + école ; le serveur tire la rareté (niveau de départ) avec tes probabilités, puis une capacité dans un pool éditable (`ability_pool.json`), potentiels de stats tirés comme le modèle, stat = potentiel x ratio du niveau (le niveau évolue jusqu'à 10 par la maîtrise, l'aura dépensée) ;
- commandes d'administration pour tout vérifier.

Pouvoirs (phase 4) : 55 pouvoirs sur 55 reproduits d'après le mod modèle, avec déblocages par points, esquive, Masque du Joker, réserve de copies. Objets : les 88 objets et blocs du modèle existent (images, modèles et sons du modèle, copiés par `tools/import_model_assets.py`), seuls certains agissent, voir `docs/fidelite/objets.md`. Fiches de comparaison : `docs/fidelite/`. Procedural school buildings now generate once per school in the Overworld; see [docs/SCHOOLS.md](docs/SCHOOLS.md). Still pending: school NPCs, les 6 pouvoirs restants (Particles, Aura Transference, Armament Dominion, Daystar, Salt Seraph, Black Lash).
Voir `docs/ARCHITECTURE.md` pour le plan complet.

## Démarrage rapide
Voir **docs/SETUP.md** (commandes pour Fish).

## Documentation
| Fichier | Contenu |
|---|---|
| `docs/SETUP.md` | installer, compiler, lancer, mettre les autres mods dans `libs/` |
| `docs/DATA.md` | formats JSON, mise à jour depuis la feuille, comment éditer (overrides) |
| `docs/POUVOIRS.md` | pouvoirs : touches, règles d'aura, référence JSON complète, recettes, déblocages |
| [docs/ABILITY_DEVELOPMENT.md](docs/ABILITY_DEVELOPMENT.md) | code et données des capacités, ajout d’un pouvoir, mouvements, animations, arbre de prérequis et chaîne client/serveur |
| [docs/where is what.md](docs/where%20is%20what.md) | emplacements des ressources, réglages du tirage, interface et JAR généré |
| `docs/fidelite/` | une fiche par pouvoir : modèle contre notre version, ligne par ligne (✅ ≈ 🎨 ❌) |
| [docs/SCHOOLS.md](docs/SCHOOLS.md) | Procedural schools, unique world-generation sites, locate commands and floor-plan previews |
| [docs/COVENANT.md](docs/COVENANT.md) | Covenant: gameplay, God tier, stances, contracts and persistence |
| `docs/ROLL.md` | probabilités, plages de niveau, hypothèses à valider |
| `docs/ARCHITECTURE.md` | structure du code, choix techniques, compatibilité, feuille de route |
| `docs/generated/REPORT.md` | rapport automatique de la dernière génération (avertissements inclus) |

## Arborescence
```
tools/build_unodex.py      export de la feuille -> JSON
tools/input/               l'export de la feuille (unodex-export.txt)
tools/overrides/           tes corrections manuelles (jamais écrasées)
src/main/resources/data/unrp/unodex/   JSON générés (rarity, schools, abilities, characters/*)
src/main/java/com/myriam/unrp/         code du mod
libs/                      jars des autres mods pour le développement
```


## Interface en jeu
Touche **H** : hub à 8 onglets fonctionnels (Pouvoir, Capacités, Entraînement, Style, École, Groupe, Territoire, Duels), notifications, offres, et HUD d'aura. Inventaire complet et état : [docs/INTERFACE.md](docs/INTERFACE.md).

## Capacités
Touche **R** : activer son pouvoir ; **X C V B N** : ses capacités. Tout se modifie en JSON : voir [docs/POUVOIRS.md](docs/POUVOIRS.md).
