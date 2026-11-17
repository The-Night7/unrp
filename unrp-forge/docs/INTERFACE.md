# Hub and controls

Press **H** to open the hub. The left dock selects eight tabs; notifications and pending offers remain accessible from the same screen.

| Tab | What you can do |
| --- | --- |
| Power | Inspect effective level, the five-stat radar, aura, school, role and reputation. |
| Abilities | Select moves, inspect costs and prerequisites, and buy unlock ranks. Drag empty space to pan; use Ctrl + wheel to zoom. |
| Training | Buy or refund potential stages. Each stage adds up to 0.5, with four stages per stat and a 10.0 potential cap. |
| Style | Set aura colour, eyes, display alias, HUD options and click sounds. Apply changes to save them. |
| School | View students and leaders, assign eligible Queen/Jack seats as King, or challenge for the crown. |
| Group | Create a group, invite players and manage membership. Chat remains public. |
| Territory | Claim land as a King or contest another school's territory. |
| Duels | Offer a friendly duel, fight for the crown or forfeit. Fights end at two hearts. |

The server validates purchases, invitations, roles, distances and combat results. Client previews do not spend points or grant permissions.

**R** toggles the power. Move keys are configurable under Controls; the bar shows the current bindings. The list/page binding exposes additional moves and stance switches. Check conflicts when installing other mods.

`client/HubScreen.java` draws the hub, `client/UiSkin.java` supplies its palette, and `social/SocialService.java` handles its actions. Social state is stored by `SocialData`; combat events are in `SocialEvents`.

The active portrait HUD is drawn by `look/UnrpHudOverlay.java` using the PNGs in `textures/gui/hud/`. See [VISUAL_STYLE.md](VISUAL_STYLE.md) before replacing those assets.
