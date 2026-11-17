# Reputation

`social/Reputation.java` tracks how a player treats weaker opponents, protects villages and behaves around the Authorities. The hub displays the resulting standing; it is separate from ability level and school rank.

Reputation events are server-side. Authority encounters, suppression cuffs and village threats use the same service and its saved social data. Read the event handlers and thresholds before changing a reputation reward or penalty: client text does not determine the score.

The HUD and hub labels are English. See [INTERFACE.md](../INTERFACE.md) for the screens and [ARCHITECTURE.md](../ARCHITECTURE.md) for the data flow.
