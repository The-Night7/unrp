# Fidélité au modèle de référence

Une fiche par pouvoir. Elle compare le modèle et notre version, ligne par ligne, sur les trois couches :

1. **la fiche du pouvoir** (`palladium/powers/*.json`) : capacités, touches, coûts, recharges, bonus ;
2. **le comportement**, écrit dans le code Java du modèle (`events/*Events`) ;
3. **le rendu** : poses du corps, effets visuels, sons, modèles 3D.

Légende des statuts :

- ✅ identique ;
- ≈ même comportement, mais la valeur exacte n'est pas lisible dans le modèle, donc elle est estimée ;
- 🎨 même intention, mais l'effet visuel n'est pas encore celui du modèle. Les images, modèles 3D, géométries, animations et sons du modèle sont maintenant copiés tels quels dans le jar (`assets/unrp/`, par `tools/import_model_assets.py`) ; ce qui reste 🎨, c'est leur branchement : surtout le moteur d'effets « Pass » du modèle (`ClientPass*`, shaders), pas encore porté ;
- ❌ absent.

| Pouvoir | Personnage | Fiche |
|---|---|---|
| Barrier | Arlo | [barrier.md](barrier.md) |
| Lightning | Remi | [lightning.md](lightning.md) |
| Invisibility | Terrence | [invisibility.md](invisibility.md) |
| Speed | | [speed.md](speed.md) |
| Phase Shift | | [phase_shift.md](phase_shift.md) |
| Healing | | [healing.md](healing.md) |
| Regeneration | | [regeneration.md](regeneration.md) |
| Telekinesis | | [telekinesis.md](telekinesis.md) |
| Hydrofreeze | | [hydrofreeze.md](hydrofreeze.md) |
| Nightmare | | [nightmare.md](nightmare.md) |
| Time Manipulation | | [time_manipulation.md](time_manipulation.md) |

Faits : 11 sur 48. Les 37 autres pouvoirs présents dans le modèle seront ajoutés ici, lot par lot.
