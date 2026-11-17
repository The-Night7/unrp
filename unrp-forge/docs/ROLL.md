# OC rolls, mastery and training

The server rolls rarity/starting level, then an available ability, then five stat potentials. Ability selection is independent of rarity: a Low-tier OC can receive Time Manipulation if it is eligible and unclaimed.

## Rarity

`rarity.json` contains relative weights and starting-level ranges. Weights are normalised.

| Rarity | Weight | Starting level |
| --- | --- | --- |
| Lifelong Cripple | 2.36 | 1.0 |
| Cripple | 3.54 | 1.0 |
| Low | 25 | 1.0–1.9 |
| Mid | 54.99 | 2.0–3.4 |
| Elite | 12 | 3.5–4.9 |
| High | 2 | 5.0–5.9 |
| God | 0.1 | 6.0–7.4 |
| Exo-god | 0.01 | 7.5–10.0 |

Both Cripple variants currently start without an ability. A lifelong Cripple cannot receive one. Exo-god is the project's label for levels 7.5–10, not an additional canon tier. Existing OC levels above ten are clamped on load.

## Ability and stats

`ability_pool.json` assigns relative ability weights. A zero or absent entry excludes an ability. Already held abilities and `UnodexData.NOT_ROLLABLE` entries are excluded too. Covenant and assignment-only Origins remain excluded even if a positive pool weight is added.

Each stat potential uses its range from `stat_ranges.json`, taking the lowest of five uniform samples. Missing ranges default to 1.0–2.0. Current OC stats are:

```text
round((potential + training) × sqrt(level / 10)), minimum 1
```

The character's level grows to ten. Character-sheet profiles use their defined stats plus training instead of rerolling potentials. Partial Recovery Bed restoration applies an additional effective-strength limit without rewriting the underlying profile.

## Mastery

Spending aura fills mastery. A +0.1 level step costs `round(level × 10) × 120` aura at the default `Mastery.RATE`. For example, level 2.5 needs 3,000 aura. Fixed-level characters do not gain OC mastery levels.

## Training

Training and ability-rank points are earned from level, with at least one point available from level two or after twenty minutes of play. Combat no longer grants extra training points.

A stage adds up to 0.5 potential. There are four stages per stat, costing 1, 2, 3 and 4 points. Potential stops at 10.0; a stage near the cap adds only the remaining amount. Refunding restores the stage's actual cost and removes its actual gain. Old training formats are reset rather than interpreted as the new stage system.

Use `/unrp rolltest 100000` to inspect rarity frequencies, or `/unrp rollability <player>` for an administrative ability reroll. The implementation is in `RarityTable`, `UnodexData`, `ClaimData`, `Mastery` and `Training`.
