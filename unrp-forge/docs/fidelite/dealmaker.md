# Dealmaker

Sources : `events/DealmakerEvents`, `PactEnforcement`, `PactFate`, `data/PactLedger`, `client/ContractScreen`, `LedgerScreen`,
`PactHudOverlay`, `item/ContractItem`, `SoulItem`, `SoulSwordItem`, `SoulBindRecipe`, `events/SoulEvents`, `SoulPlaneEvents`,
`PlaneGuest`, `client/SoulPlaneSky`, dialogue (`LeilahScreen`). Code : `deal/` (portage direct), `ability/Superpowers`.

| Élément | Statut |
|---|---|
| Pouvoir : 2 barres (Contrat, Sélecteur, Convocation, Arrachement / Négociateur, Lier, Sous ma protection, Imprégnation liée, Auto-imprégnation), recharges du modèle | ✅ (deux pages, Maj + R) |
| Écran de rédaction des contrats, pactes, clauses, signature, application des termes, sort des pactes | ✅ code du modèle |
| Âmes, épée d'âme, recette de liaison, plan des âmes (dimension `unrp:soul_planes`) et son ciel | ✅ code du modèle |
| Registre, HUD des pactes, dialogue de liaison | ✅ code du modèle |
| Pouvoirs accordés/retirés (« superpower add/remove ») | ✅ via `Superpowers` : pouvoirs accordés (Maj + R pour les prendre en main), pouvoir scellé, copies |
| `soul_bound`, `disguise` | ✅ fiches créées d'après le modèle |
| Apparences des Origines scellées/arrachées (OriginLooks), sièges d'Origine distants | ❌ pas portés (sans effet) |
| Interactions avec Singularity, Titans, My World, Puppet, End of All Things | ❌ ces pouvoirs ne sont pas portés |

Non vérifié en jeu au moment du portage.
