# Recovery Bed

`unrp:recovery_bed` is a two-block machine for restoring a disabled ability. Right-click either half to lie down using Minecraft's synchronized sleeping pose. It does not provide bonus health healing or aura regeneration.

The placing player owns the bed. Only that owner can choose its rate, expressed in **percentage points of ability recovery per second**. The default is 1% per second: a complete course takes 100 seconds. At 5%, a complete course takes 20 seconds. Sneak-right-click with an empty hand cycles presets, or look at the bed and use `/unrp recoverybed rate <percent per second>` for an exact rate from 0.1 to 20.

Recovery advances each second while the patient is lying in the bed. At 100%, the patient's timed ability disable is lifted, allowing them to activate their ability again. It does not automatically activate the power. Use **Leave Bed** to pause treatment; returning resumes progress for the same disable episode. A new disable resets the course. A suspension can also expire naturally before treatment finishes. Other powers retaining an ability as collateral or actively enforcing suppression are separate constraints: the bed does not steal back collateral or override their authority.

The bed continues working when its owner's source ability is inactive or disabled, so the owner can also recover their own ability. The owner's presence is not required. Owner UUID, source ability and rate are saved in `data/unrp_recovery_beds.dat`; treatment progress is stored with the patient.

Only the owner can break either half, after the source ability is inactive or disabled. Other players cannot modify its rate or break it, including in creative. Explosions, pistons and entities cannot remove it. Beds placed without a source ability retain owner-only controls. Beds placed before ownership support have no recorded owner; ownership cannot be inferred automatically.

Medical rest works by day or night without setting spawn, voting to skip the night, or fading the view to black. It needs two clear blocks and holds one patient. A normal night's sleep by other players may still wake patients.

Get it from the creative tab or `/give @s unrp:recovery_bed`. Recipe: white wool across the top; iron / Recovery Machine / iron in the middle; iron across the bottom.

- `item/RecoveryBedBlock.java`: lying pose and treatment ticks.
- `item/RecoveryBeds.java`: ownership, rates, source-ability binding and destruction checks.
- `item/RecoveryTreatment.java`: percentage progression.
- `ability/AbilityEngine.java`: `hasRecoverableDisable` and `recoverDisabledAbility` connect treatment to the actual suspension state.
- `mixin/RecoveryRestMixin.java`, `RecoverySleepStatusMixin.java`: medical rest without night skipping or sleep fade.
- `tools/design_recovery_bed.py`: real block and item geometry using existing machine textures.
