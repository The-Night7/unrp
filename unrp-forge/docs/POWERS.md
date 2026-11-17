# Power definitions

Edit `src/main/resources/data/unrp/unodex/ability_powers.json` to change moves, aura costs, prerequisites and visual settings. Java models are `data/AbilityPower.java` and `AbilityEffect.java`; `AbilityEngine` executes the common effects. JSON uses snake_case field names.

For a new ability, follow [ABILITY_DEVELOPMENT.md](ABILITY_DEVELOPMENT.md). This page is the field reference. Existing power IDs are also used in saves, so change labels rather than renaming an ID to translate it.

## Controls and aura

**H** opens the hub and **R** toggles the power. The move bar shows current bindings. Change them under Controls if another mod conflicts. The list/page key exposes additional moves; stance and mode changes filter the active bar.

Activation has no general cooldown. Individual moves can require aura, spend it on press, drain while held and impose cooldowns. Generic costs and special-handler costs are separate: a move with zero JSON cost may still charge resources in its event implementation.

Aura capacity is `100 + 10 × (Trick + Recovery)`. Normal regeneration is `1 + Recovery / 2` per second, using integer stats, when regeneration is allowed. Active drains and exhaustion can block it. `PlayerStateService` owns these formulas; `AbilityEngine` owns spending and locks.

## A small power

```json
{
  "powers": {
    "example": {
      "look": {"style": "light", "color": "#e0c16a"},
      "moves": [{
        "name": "Flash",
        "desc": "Blind nearby targets briefly.",
        "input": "action",
        "cost": 12,
        "cooldown": 60,
        "fx": {
          "kind": "pulse",
          "radius": 5,
          "effects": [{"id": "blindness", "amp": 0, "secs": 2}]
        }
      }]
    }
  }
}
```

This defines an effect, not a complete OC pool entry. Add the corresponding catalog/pool data when you want it to be rolled.

## Power fields

| Field | Meaning |
| --- | --- |
| `drain`, `drain_every` | Active aura drain and interval in ticks. Check the existing implementation before combining them. |
| `passive` | Potion effects maintained while the power is active. |
| `attributes` | Active Minecraft attribute modifiers. |
| `thorns` | Retaliation damage. |
| `on_activate`, `activate_pose` | Activation effect/pose identifiers. |
| `activate_sound`, `activate_vol`, `activate_pitch` | Activation sound settings. |
| `pass_on` | Reference-effect bridge identifier. |
| `active_tag`, `extra_active_tags`, `off_tag` | Tags used by event-driven powers. |
| `icon`, `bar_color` | Bar appearance. |
| `sense_radius` | Passive sensing radius. |
| `heal_every`, `heal_amount` | Periodic active healing. |
| `climb`, `fall_mult`, `drain_when_hurt` | Movement, fall and damage-drain options. |
| `modes` | Named forms with their own stat changes, drain and passives. |
| `stances` | Page/stance names. |
| `moves` | Ordered move definitions. |
| `look` | Colour, style and particles. |

Stats arrays use **Power, Defense, Recovery, Trick, Speed** in that order.

## Move fields

| Field | Meaning |
| --- | --- |
| `name`, `desc` | English label and player description. |
| `input` | Input handling: action, held, toggle or charge, as supported by the engine. |
| `cost`, `press_cost`, `min_aura` | Spend on use/press and minimum aura required. |
| `cooldown`, `charge`, `drain_every` | Tick durations; twenty ticks equal one second. |
| `passive`, `attributes` | Effects and attributes maintained by a held/toggled move. |
| `surge`, `cloak`, `heal_every`, `heal_amount` | Common movement, cloak and healing behaviour. |
| `group` | Mutually exclusive toggle group. |
| `on_hit` | Effects applied by a hit. |
| `k` | Effect scaling value. |
| `unlock`, `requires`, `mastery`, `min_level` | Purchased rank, parent move index, mastery requirement and effective-level threshold. |
| `stance`, `mode` | Restrict the move to a stance or mode; -1 means unrestricted. |
| `stats` | Temporary five-stat changes. |
| `special` | Handler identifier routed through `Specials`. |
| `fx` | A generic effect object. |
| `trigger_tag`, `hold_tag`, `release_tag` | Tags sent to event-driven implementations. |
| `need_tag`, `need_any`, `block_tag`, `show_tag` | Tag-based use and visibility conditions. |
| `bar_slot`, `icon`, `bar_color` | Move-bar placement and appearance. |
| `pose`, `pose_sneak`, `pass` | Animation and visual-bridge identifiers. |
| `sound`, `sound_vol` | Move sound. |
| `flash_tag`, `flash_ticks` | Temporary visual tag and duration. |

A client lock icon is only a preview. The server checks rank, parent, tier, aura and branch permissions again when input arrives. Borrowed Covenant branches keep their own allowed-move restrictions.

## Generic effect types

`fx.kind` selects one of:

| Kind | Purpose |
| --- | --- |
| `strike` | Hit targets in front of the user. |
| `beam` | Trace an aimed beam. |
| `blast` | Area impact. |
| `buff` | Apply effects to the user. |
| `dash` | Move the user rapidly. |
| `teleport` | Relocate the user. |
| `heal` | Restore health. |
| `debuff` | Apply target effects or roots. |
| `pulse` | Apply an area effect. |
| `scan` | Inspect a target. |
| `dispel` | Clear supported effects. |
| `wall` | Place temporary blocks. |
| `bolt` | Fire a moving projectile. |
| `shell` | Create a protective construct. |
| `grow` | Resize supported constructs. |
| `ram` | Charge with a construct. |
| `share` | Share a supported benefit with allies. |

The shared fields are `range`, `radius`, `dmg`, `kb`, `up`, `power`, `amount`, `explosion`, `lifesteal`, `glow`, `hits` and `fire`. Not every kind uses every field. Projectile fields include `speed` and `life`; temporary walls use `block`; roots use `root` seconds. `single` restricts applicable strikes to one target. `effects` affects targets and `self_effects` affects the caster. Read the selected handler before adding an unused field.

Potion entries use `{ "id": "slowness", "amp": 0, "secs": 2 }`; amplifier zero is level I. Attribute entries use an identifier, amount and operation: 0 adds, 1 scales the base and 2 multiplies the total by `1 + amount`. Use valid Minecraft identifiers, such as `minecraft:generic.movement_speed` or `minecraft:generic.armor`.

## Rendering and animation

`look.style` selects an effect style in `ability/Styles.java`. Available styles include lightning, fire, ice, water, wind, shadow, light, blood, nature, mind, gravity, barrier, cloak, speed, strength, heal, poison, earth, metal, sound, space and energy. `look.color` is `#RRGGBB`; `particle` and `sprite` override particle choices where supported.

Runtime textures, models and animations are under `assets/unrp/`. Pose clips are defined in `anim/MoveClips.java` and identified in `MoveClipIds.java`. Angles use degrees; keyframe times use ticks. Sound signatures are in `pose_sounds.json`. Keep model, held-item and worn previews consistent with their real assets.

Some powers use the `pass/` renderer or their own entities instead of generic particles. Their event and renderer classes remain responsible for timing and appearance. See the [power reference](fidelity/README.md) and [VISUAL_STYLE.md](VISUAL_STYLE.md).

## Test changes

Run `./gradlew build`, then test in a world with the relevant ability. Check aura spending, cooldowns, failed-use refunds, stance changes, multiplayer synchronization and saved-state reloads. A valid JSON file does not prove its effects work in game.

For OC stats and training, see [ROLL.md](ROLL.md). For partial ability restoration, see [RECOVERY_BED.md](RECOVERY_BED.md). Assignment-only Origin abilities are listed in [ORIGIN.md](ORIGIN.md).
