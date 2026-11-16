# Recovery Bed

`unrp:recovery_bed` is a separate, two-block medical bed in the Recovery Machine's metal/teal style. Right-click either half to lie down using Minecraft's actual synchronized sleeping pose. Use **Leave Bed** to get up. Only one patient can occupy the bed. Both halves need clear space above them.

Treatment restores one health point per second (one heart every two seconds), plus the patient's ordinary regeneration. It also adds about 2% of maximum aura per second, capped at capacity, while normal aura regeneration is permitted. Treatment only runs while the patient is alive and lying in this bed; leaving, breaking the bed or taking damage ends rest through the normal bed mechanics. It does not instantly refill health or aura.

Medical rest works during the day and at night, including dimensions where ordinary beds explode. It does not set the patient's respawn point, count towards skipping the night, or fade the view to black. A normal night's sleep by other players may still wake patients.

Get it from the mod's creative tab or use `/give @s unrp:recovery_bed`. The recipe is three white wool on the top row, iron / Recovery Machine / iron in the middle, and three iron ingots on the bottom row.

- `item/RecoveryBedBlock.java`: placement/use, lying pose, occupancy and treatment tick.
- `item/RecoveryTreatment.java`: gradual aura recovery rate.
- `mixin/RecoveryRestMixin.java` and `RecoverySleepStatusMixin.java`: medical rest without night skipping or sleep fade.
- `tools/design_recovery_bed.py`: generates both block models and a complete item model from the existing machine and vanilla wool textures. The item shows the same two halves as the placed bed.

The original Recovery Machine remains available separately.
