# Code structure

UNRP separates data, server gameplay and client presentation. The server owns characters, aura, moves, school roles, inventories and saved effects. Clients send requests and render the state returned by the server.

| Directory | Responsibility |
| --- | --- |
| `data/` | JSON models, resource loading and persistent character claims |
| `player/` | Effective stats, body attributes and state packets |
| `ability/` | Input, costs, cooldowns, mastery, unlocks and special ability handlers |
| `social/` | Schools, roles, groups, training, territories, duels and reputation |
| `deal/` | Physical contracts, Pact enforcement, souls and the Soul Plane |
| `item/` | Items, blocks, equipment and Recovery Bed ownership |
| `worldgen/` | Campus sites, saved layout versions, terrain and block placement |
| `net/` | Forge packets and their registration |
| `client/`, `look/`, `pass/`, `anim/` | Screens, HUD, player layers, world effects and animation |
| `uo/` | Ability event implementations and scoreboard/tag bridges |

Paths are under `src/main/java/com/myriam/unrp/`.

## Ability execution

`AbilityEngine` resolves the player's power, checks input and resource requirements, then applies a generic effect or dispatches a special handler. `AbilityEvents` and the `uo/` event classes connect Forge events to that state. Some powers use tags consumed by their event handlers rather than a generic `fx`.

`PlayerStateService` calculates stats, aura capacity and regeneration. Keep those calculations there rather than duplicating them in a screen. Effective level and stats can also be reduced by partial ability restoration; the stored character sheet remains unchanged.

`HubScreen` sends actions through the hub packet. `SocialService` validates them and returns a snapshot. A button being disabled in the client is not an authorization check.

## Saving and loading

`ClaimData` saves player profiles and character claims. `SocialData` saves social state and training. Covenant, Pact and Recovery Bed ledgers use their own `SavedData`. Home coordinates and recovery progress use persistent player NBT.

Keep field names and identifiers compatible when editing saved records. Display names can be translated; resource IDs, UUIDs and existing registry keys identify stored objects.

## World generation and assets

School starts store their layout version. New layouts must not silently replace pieces already saved in a world. `SchoolPiece` applies the selected plan within chunk bounds; terrain and border work are handled separately.

Models and textures under `assets/unrp/` are runtime assets. An inventory model should show the same geometry as the placed or worn object. Generators in `tools/` are maintenance tools, not part of a player's runtime.

## Dependencies

Palladium supplies movement integration; Architectury, animation libraries and GeckoLib are used where declared by the build. Forge registrations and packet handling are shared by the mod. A single exported ability is therefore not a standalone mod project.

See [ABILITY_DEVELOPMENT.md](ABILITY_DEVELOPMENT.md) for an implementation walkthrough and [where is what](<where is what.md>) for specific files.
