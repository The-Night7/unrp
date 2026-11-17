# Ability visual coverage

This is a source and asset audit of **140 non-Origin power definitions** and **320 moves**. It checks animation lookup, player-model hooks, icons, renderer resource paths and PNG integrity. Run `python tools/check_ability_visuals.py --write-report` from `unrp-forge/` to refresh it.

All moves resolve an animation or are explicit stance controls. All move icons resolve to 16 × 16 PNGs. The audit read 477 referenced textures. Model clip indices agree with the network ID table. Special-handler names are located in the server sources; this does not establish that every handler produces a visible effect in every situation.

## Shared artwork

127 move icons reuse existing effect glyphs. The added legacy pose IDs also reuse existing gestures. These are shared visuals, not newly drawn, ability-specific artwork. Generic power definitions still use the common effect engine.

## In-game review

No ability is marked visually approved by this audit. Verify the first- and third-person pose, held/toggled/released states, target effects, visibility to another player, resource reload and interaction with clothing in Minecraft. A valid asset reference does not prove that a motion looks good or that an event fires at the right time.

The [per-move checklist](ability-visual-checklist.csv) records the exact animation, icon and effect path. Its review column starts as `Not checked`; do not treat it as a playtest result.

| Power ID | Moves | Shared move icons |
| --- | ---: | ---: |
| `ability_gauging` | 2 | 0 |
| `alchimie` | 1 | 1 |
| `arachnid` | 4 | 0 |
| `arcane_shot` | 2 | 0 |
| `archer` | 2 | 2 |
| `armament_dominion` | 4 | 0 |
| `armor_suit` | 4 | 0 |
| `aura_deity` | 1 | 1 |
| `aura_manipulation` | 2 | 0 |
| `aura_transference` | 3 | 0 |
| `azure_storm` | 4 | 0 |
| `barrage` | 2 | 2 |
| `barrier` | 3 | 0 |
| `black_lash` | 4 | 0 |
| `blade` | 2 | 2 |
| `blademaster` | 5 | 0 |
| `bodybuilder` | 1 | 1 |
| `botanist` | 1 | 1 |
| `cancellation` | 1 | 1 |
| `catalyst_ray` | 4 | 0 |
| `catch_up` | 1 | 1 |
| `channel_master` | 1 | 1 |
| `charge` | 1 | 1 |
| `charge_shot` | 2 | 2 |
| `clairvoyance` | 1 | 1 |
| `claws` | 2 | 2 |
| `clobber` | 2 | 2 |
| `command` | 1 | 1 |
| `conjure_disks` | 3 | 0 |
| `conjure_vines` | 4 | 0 |
| `contact_shred` | 2 | 0 |
| `crescent_slash` | 4 | 0 |
| `crimson_thread` | 2 | 2 |
| `dark_blades` | 2 | 2 |
| `daystar` | 4 | 0 |
| `death_fire` | 2 | 2 |
| `debt` | 1 | 1 |
| `demolition` | 2 | 2 |
| `demon_blade` | 2 | 2 |
| `demon_claw` | 3 | 0 |
| `detection_de_mensonge` | 1 | 1 |
| `digital_hive` | 1 | 1 |
| `disguise` | 1 | 0 |
| `dizzy_punch` | 3 | 0 |
| `duplication` | 1 | 1 |
| `energy_beam` | 2 | 2 |
| `energy_conduit` | 4 | 0 |
| `energy_discharge` | 4 | 0 |
| `explosion` | 3 | 0 |
| `faucheuse_fantome` | 2 | 2 |
| `flame_claw` | 3 | 0 |
| `flash_forward` | 1 | 1 |
| `fleetstep` | 1 | 1 |
| `fortify` | 1 | 1 |
| `ghost` | 1 | 1 |
| `god_s_mind` | 1 | 1 |
| `graviton` | 2 | 2 |
| `grenadier` | 2 | 2 |
| `ground_pound` | 2 | 2 |
| `hair_growth` | 1 | 1 |
| `hand_blade` | 2 | 0 |
| `heal_link` | 2 | 2 |
| `healing` | 4 | 0 |
| `heat_palm` | 2 | 0 |
| `heavy_hitter` | 2 | 2 |
| `hunter` | 1 | 1 |
| `hydrofreeze` | 7 | 0 |
| `hypnosis` | 4 | 0 |
| `icicle` | 2 | 2 |
| `illumination` | 4 | 0 |
| `impact` | 2 | 2 |
| `imprison` | 1 | 1 |
| `invisibility` | 2 | 0 |
| `iron_knuckle` | 2 | 2 |
| `knuckles` | 2 | 2 |
| `lazor` | 2 | 2 |
| `lie_detection` | 1 | 1 |
| `life_steal` | 2 | 2 |
| `lightning` | 4 | 0 |
| `liquids_manipulation` | 2 | 2 |
| `medium` | 1 | 1 |
| `memory_recall` | 1 | 1 |
| `minefield` | 2 | 2 |
| `missiles` | 4 | 0 |
| `mistdream` | 1 | 1 |
| `modes` | 7 | 7 |
| `needles` | 3 | 0 |
| `nightmare` | 3 | 0 |
| `non_specifiee` | 2 | 2 |
| `paralysis` | 1 | 1 |
| `particles` | 5 | 0 |
| `phantom_fist` | 4 | 0 |
| `phantom_push` | 4 | 0 |
| `phase_shift` | 2 | 0 |
| `portals` | 1 | 1 |
| `possession` | 1 | 1 |
| `predator` | 5 | 0 |
| `pride` | 1 | 1 |
| `protector` | 1 | 1 |
| `psy_blast` | 2 | 2 |
| `psychopomp` | 2 | 2 |
| `quick_strike` | 4 | 0 |
| `regeneration` | 2 | 0 |
| `repulsion` | 2 | 2 |
| `rock_arm` | 2 | 0 |
| `royal_guard` | 1 | 1 |
| `salt_seraph` | 4 | 0 |
| `sanity_control_bal` | 1 | 1 |
| `sanity_control_heal` | 2 | 2 |
| `sanity_control_hurt` | 1 | 1 |
| `scanner` | 1 | 1 |
| `sensory_control` | 1 | 1 |
| `shadow_eclipse` | 1 | 1 |
| `shadow_flame` | 2 | 0 |
| `shockwave` | 2 | 2 |
| `soul_bound` | 4 | 0 |
| `spectral_claw` | 2 | 0 |
| `speed` | 2 | 0 |
| `spikes` | 4 | 0 |
| `stone_skin` | 2 | 0 |
| `strength` | 1 | 1 |
| `strong_kick` | 3 | 0 |
| `strong_punch` | 3 | 0 |
| `super_strength` | 2 | 2 |
| `superhuman` | 1 | 1 |
| `telekinesis` | 4 | 0 |
| `telepathy` | 1 | 1 |
| `teleportation` | 1 | 1 |
| `time_manipulation` | 7 | 0 |
| `tir_concentre` | 2 | 2 |
| `tremor` | 4 | 0 |
| `troll` | 1 | 1 |
| `umbral_shift` | 2 | 0 |
| `unknown` | 1 | 1 |
| `unknown_augmentation_ability` | 2 | 2 |
| `vigor` | 1 | 1 |
| `vitalize` | 2 | 2 |
| `whirlwind` | 4 | 0 |
| `white_hole` | 2 | 2 |
| `wrist_spike` | 2 | 2 |
