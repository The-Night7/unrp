# Covenant — implementation comparison

Authoritative design: [COVENANT_SPEC.md](../COVENANT_SPEC.md). Controls, code and practical mappings: [COVENANT.md](../COVENANT.md).

| Requirement | Implementation |
| --- | --- |
| Tier progression and Court range | Standard Low/Mid/Elite/High/God boundaries, 10/30/75/150/500 m. |
| Enact, Terms, Fine Print, Foreclosure, Exile | Structured English chat laws; entry confiscation; one hidden law; unpaid collateral plus 25%; exile after three breaches. |
| Pact, Endowment, Proxy, Appropriation | Persistent accepted transfers; revocable loans; wound forwarding; indexed branch access using the source's recorded stats, without unrelated power passives. |
| Sovereign Immunity, Indemnity, Escrow, Arbitration | Voluntary self-binding; six-second Indemnity below 5.0 and untimed coverage from 5.0; protected stat/branch; debt-linked suspension. |
| Level-five passive and souls | Private signature/debt markers and registry; permanent Pact souls and cross-dimension summoning of their online owners. |
| High-level permanent building Court | World-saved stationary Court continues while its owner is absent. |
| High-level soul burden | More than ten held souls, including loaned souls, causes a self-signature and daily self-law below God tier. |
| God-tier removal of old limits | No old storage cap, collateral timers or soul burden; unilateral `nothing` consideration is allowed; wield cap is ten branches. Existing held collateral is upgraded on reaching God. |
| Jurisdiction and sight signatures | Persistent 24-hour districts; line-of-sight seal recognition creates week-long global signatures. |
| Constitutions | Five maximum; permanent signed-person enforcement, author-only repeal, gold body runes with full readable text. |
| Retroactive Law | Court-local recorded actions from the last hour; outside actions are not included. |
| Total Forfeit and Class Action | Entire-ability access is blocked while held; one collection per debtor in a class action, including known offline signers. |
| Sovereign Pacts, daily tribute, remote communication | Private remote offers, refused-Pact immunity, scheduled tribute and queued hand mail; contract state survives saves. |
| Visual identity | Gold native icon set, ground seal effects, hand signatures, Constitution markings, private parchment cards and owner-only registry markers. |

## Explicit game representations

- Participants are player profiles. Ordinary mobs/NPCs are not contracted.
- Laws use the documented supported action/penalty grammar; arbitrary natural-language rules are rejected.
- Sight/touch/hearing map to Minecraft effects and sound suppression. Memories map to recorded public chat and effects; real memories and third-party voice chat are outside this system.
- Soul collateral is a persistent ownership token with a max-health effect. Offline people retain their ledger state; summoning requires their avatar to be online.
- Constitution text is read through a targeted display; body rendering uses compact gold rune strokes.
- Durations advance with server game time, not while the server process is stopped. The prototype's aura costs remain balancing values.

Automated regression tests cover rules, parsing, persistence, world-state isolation, branch restrictions, move definitions and networking. This is not a claim of completed multiplayer or visual playtesting; those interactions still need an in-game check.
