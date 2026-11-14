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
| Ability Gauging | | [ability_gauging.md](ability_gauging.md) |
| Arachnid | | [arachnid.md](arachnid.md) |
| Arcane Shot | | [arcane_shot.md](arcane_shot.md) |
| Armor Suit | | [armor_suit.md](armor_suit.md) |
| Aura Manipulation | | [aura_manipulation.md](aura_manipulation.md) |
| Azure Storm | | [azure_storm.md](azure_storm.md) |
| Blademaster | | [blademaster.md](blademaster.md) |
| Catalyst Ray | | [catalyst_ray.md](catalyst_ray.md) |
| Strong Punch | | [strong_punch.md](strong_punch.md) |
| Strong Kick | | [strong_kick.md](strong_kick.md) |
| Dizzy Punch | | [dizzy_punch.md](dizzy_punch.md) |
| Stone Skin | | [stone_skin.md](stone_skin.md) |
| Quick Strike | | [quick_strike.md](quick_strike.md) |
| Rock Arm | | [rock_arm.md](rock_arm.md) |
| Heat Palm | | [heat_palm.md](heat_palm.md) |
| Hand Blade | | [hand_blade.md](hand_blade.md) |
| Contact Shred | | [contact_shred.md](contact_shred.md) |
| Missiles | | [missiles.md](missiles.md) |
| Needles | | [needles.md](needles.md) |
| Phantom Fist | | [phantom_fist.md](phantom_fist.md) |
| Phantom Push | | [phantom_push.md](phantom_push.md) |
| Crescent Slash | | [crescent_slash.md](crescent_slash.md) |
| Spectral Claw | | [spectral_claw.md](spectral_claw.md) |
| Demon Claw | | [demon_claw.md](demon_claw.md) |
| Flame Claw | | [flame_claw.md](flame_claw.md) |
| Shadow Flame | | [shadow_flame.md](shadow_flame.md) |
| Energy Discharge | | [energy_discharge.md](energy_discharge.md) |
| Explosion | | [explosion.md](explosion.md) |
| Tremor | | [tremor.md](tremor.md) |
| Illumination | | [illumination.md](illumination.md) |
| Umbral Shift | | [umbral_shift.md](umbral_shift.md) |
| Whirlwind | | [whirlwind.md](whirlwind.md) |
| Conjure Disks | | [conjure_disks.md](conjure_disks.md) |
| Conjure Vines | | [conjure_vines.md](conjure_vines.md) |
| Spikes | | [spikes.md](spikes.md) |
| Hypnosis | | [hypnosis.md](hypnosis.md) |
| Predator | | [predator.md](predator.md) |
| Energy Conduit | | [energy_conduit.md](energy_conduit.md) |
| Particles | | [particles.md](particles.md) |
| Aura Transference | | [aura_transference.md](aura_transference.md) |
| Armament Dominion | | [armament_dominion.md](armament_dominion.md) |
| Daystar | | [daystar.md](daystar.md) |
| Salt Seraph | | [salt_seraph.md](salt_seraph.md) |
| Black Lash | | [black_lash.md](black_lash.md) |

Faits : 55 sur 55. Tous les pouvoirs du modèle sont portés ; les écarts restants sont dans chaque fiche (≈, 🎨, ❌).

| *(objets et blocs)* | | [objets.md](objets.md) |
| *(réputation)* | | [reputation.md](reputation.md) |
| *(moteur d'effets du modèle)* | | [moteur_effets.md](moteur_effets.md) |
| *(animations du modèle)* | | [animations.md](animations.md) |
| Dealmaker | | [dealmaker.md](dealmaker.md) |

| Covenant (Origin VII-I, originale) | | [covenant.md](covenant.md) |
| Counterweight (Origin IX) | | [counterweight.md](counterweight.md) |
| Limit Breaker (Origin II) | | [limit_breaker.md](limit_breaker.md) |
| Sarcophage (Origin V) | | [sarcophage.md](sarcophage.md) |
| Hollow Mark (Origin VIII) | | [hollow_mark.md](hollow_mark.md) |
| *(interface du modèle)* | | [interface.md](interface.md) |
