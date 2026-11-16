# Recovery Bed

`unrp:recovery_bed` is a separate, two-block medical bed in the Recovery Machine's metal/teal style. Right-click either half to lie down using Minecraft's actual synchronized sleeping pose. Use **Leave Bed** to get up. Only one patient can occupy the bed. Both halves need clear space above them.

The placing player owns the bed. Its owner alone chooses its health-points-per-second treatment rate (default 1). Sneak-right-click with an empty hand cycles 0.25, 0.5, 1, 2 and 5; while looking at the bed, `/unrp recoverybed rate <value>` sets an exact rate from 0.1 to 20. Aura recovery scales with the same rate (about 2% of capacity per second at rate 1), capped at capacity and subject to ordinary regeneration restrictions. Ordinary natural regeneration remains separate. Treatment only runs while the patient is alive and lying in this bed. Leaving, removing the bed or taking damage ends rest through the normal bed mechanics. It does not instantly refill health or aura.

Medical rest works during the day and at night, including dimensions where ordinary beds explode. It does not set the patient's respawn point, count towards skipping the night, or fade the view to black. A normal night's sleep by other players may still wake patients.

Get it from the mod's creative tab or use `/give @s unrp:recovery_bed`. The recipe is three white wool on the top row, iron / Recovery Machine / iron in the middle, and three iron ingots on the bottom row.

- `item/RecoveryBedBlock.java`: placement/use, lying pose, occupancy and treatment tick.
- `item/RecoveryTreatment.java`: gradual aura recovery rate.
- `mixin/RecoveryRestMixin.java` and `RecoverySleepStatusMixin.java`: medical rest without night skipping or sleep fade.
- `tools/design_recovery_bed.py`: generates both block models and a complete item model from the existing machine and vanilla wool textures. The item shows the same two halves as the placed bed.

The original Recovery Machine remains available separately.

## Ownership and source ability

The bed records its placer UUID, their base ability at placement, and its rate in the world save (`data/unrp_recovery_beds.dat`). Both halves share the same settings. A linked ability must be active and unsuppressed, and its owner online, for treatment to run. Switching the ability off pauses treatment without deleting the bed. Only the owner may break either half, and only after the linked ability is off or disabled. Other players cannot change the rate or break the bed, including in creative mode. Explosions, pistons and entity destruction cannot remove it. Beds placed without a base ability keep owner-only controls and can be removed by their owner at any time. Beds placed before ownership support have no recorded owner and cannot infer one automatically.
