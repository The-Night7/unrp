# Interface integration

The hub, portrait HUD and move bar are separate renderers. `HubScreen` handles menus; `UnrpHudOverlay` handles the active portrait/status display; `AbilityBar` handles move slots and pages.

State arrives through server snapshots. Hub buttons send requests to `SocialService`, while move input goes through `AbilityEngine`. Server validation remains required even when a client hides or disables a button.

See [INTERFACE.md](../INTERFACE.md) for controls and [where is what](<../where is what.md>) for the source files and PNGs.
