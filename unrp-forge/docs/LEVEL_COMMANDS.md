# Ability-level commands

These commands edit the UNRP ability level, not Minecraft XP. They require operator permission level 2 (or cheats in single-player).

| Command | Effect |
| --- | --- |
| `/unrp level get` | Show your ability level |
| `/unrp level get PlayerName` | Show an online player's ability level |
| `/unrp level set 5.0` | Set your OC's level to 5.0 |
| `/unrp level set 7.5 PlayerName` | Set that online player's OC level |
| `/unrp level add 0.5` | Increase your OC level by 0.5 |
| `/unrp level add -0.5 PlayerName` | Reduce that player's OC level by 0.5 |

A single-player selector such as `@s` or `@p` can replace `PlayerName`. Console and command blocks must supply a target. Multi-player selectors such as an unrestricted `@a` are not supported.

The resulting level must be between **1.0 and 10.0**, and is rounded to one decimal place. Invalid results are rejected, without modifying the profile. A successful edit resets progress towards the next level, saves the profile and immediately refreshes stats, maximum aura and client state. Existing aura is capped if the new maximum is lower; the command does not refill aura.

Edits apply to **OCs**, except permanently powerless OCs. Unodex characters retain their fixed sheet levels; `get` shows those levels, while `set`/`add` explain why an edit is rejected. The command does not reroll abilities, change ability ownership, grant purchased move ranks, alter trained stat bonuses or change the originally rolled potential/tier label. Level-dependent mechanics and OC stats use the new level normally.

Implementation: [LevelCommands.java](../src/main/java/com/myriam/unrp/command/LevelCommands.java), attached under `/unrp` by [UnrpCommands.java](../src/main/java/com/myriam/unrp/command/UnrpCommands.java). Existing persistence uses `ClaimData.Profile.level` and `masteryXp`; no new save field is required.
