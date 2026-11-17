# Origin abilities

Origin abilities are assignment-only. `UnodexData.NOT_ROLLABLE` excludes them even if a positive pool weight is configured. These are design summaries; the current move definitions and Java handlers determine what is playable.

| Ability | Storage ID | Runtime definition |
| --- | --- | --- |
| Edict | `edict` | Not implemented |
| Limit Breaker | `limit_breaker` | Implemented |
| Wyrm Engine | `wyrm_engine` | Not implemented |
| Singularity | `singularity` | Not implemented |
| Sarcophagus | `sarcophage` | Implemented |
| Crimson Mark | `crimson_mark` | Not implemented |
| Dealmaker | `dealmaker` | Implemented |
| Hollow Mark | `hollow_mark` | Implemented |
| Counterweight | `counterweight` | Implemented |
| Vertigo | `nadir` | Not implemented |
| Chained One | `chained_one` | Not implemented |
| Covenant | `covenant` | Implemented original ability |

Related items can exist before their full ability is implemented. An item name or model does not mean every design mechanic is available.

## Edict — Origin I

A judge establishes Court laws and gains Precedent when they are challenged. Pass Law enacts the selected statute; crouching cycles statutes. Repeal ends a law early. At law rank three, Hands of the Law strikes the aimed ground; rank five adds Writ of Seizure to grab and throw an offender. Decrees at rank two engrave strength and endurance on the user's body. Rank five adds temporary Decree of Inviolability. Related item: Edict Shard.

## Limit Breaker — Origin II

Missing hearts increase strength. Fighting while injured builds Grit, which moves consume. Flare Bolt uses regenerating Ki; crouching fires Barrage. Breaking Wave charges a piercing beam from stage two; crouching casts Nova around the user.

Break selects an unlocked form on the fourteen-form Ladder. Early forms include Surge, Radiance, Azure and Second Nature, with different strength, flight and Grit behaviour. Rush charges a locked target; sprinting adds an explosive finish. The original Limitless design removes a level ceiling, but the current OC progression system still has its own level cap. Related item: Limit Breaker Shard.

## Wyrm Engine — Origin III

Boosts are loans: each stack adds 5% damage, or 6% in DreadScale and Drive. Deeper forms are bought through trials. Crouching Boost dispels all stacks. Drake Cannon spends stacks on an orb, dragonfire and a crater; crouching selects the current form's Draconic Art.

Dragonbane draws an armour-piercing bound blade. Fate Breaker cycles bodies: Dragonplate supplies crimson scales, wings and protection from harmful effects; DreadScale adds black-and-gold armour and chess-themed arts. Crouching Fate Breaker starts a sixty-second Cataclysm Drive with flight and instant boosts, followed by death. Kindled, Stoked and Redline show boost depth; damage at higher depth pays Cycles. Related items include the shard, Dragonslayer and Mk. II.

## Singularity — Origin IV

Devouring creates Mass, spent on techniques and progression through stages I–VII. Devour gains Accretion at stage II. Nothing Between folds space for movement. Pull at III attracts matter and projectiles; Horizon at IV leaves a crushing singularity. Collapse at V pulls targets inward and explodes, with a forward-rift variant.

Later pages add Erasure, Eons Between Us, Touch of Nothing, Orb of the Void, creature control, protective copies of consumed matter, Distortion and Mass-funded survival. Time techniques loop or freeze opponents' moments and disrupt their actions. Stage VII, Anathema, requires consuming a Nether Star and adds aspect choices, a giant-serpent form and the costly DREAD state. Mass Forge spends Mass on upgrades.

## Sarcophagus — Origin V

Living sand grows by consuming Vital Sand from Tail Grab, gaining up to four tails. Tail Grab holds and throws; crouching uses Sand Coffin to crush and suppress. Two tails unlock Duneride and Sever Tail, which creates a temporary club anyone can carry and the owner can consume to regrow the tail.

Three tails unlock Grainshot and Sandshroud armour. Four tails unlock Tempest; crouching during it can Unseal into Phage. Related items: Sarcophagus Shard, Vital Sand and Ossified Tail. The storage ID remains `sarcophage` for save compatibility.

## Crimson Mark — Origin VI

Sustained gaze forms red threads to marked targets. Sever cuts them for damage, with Tripline from stage two. Thread Step swings from a skyward thread; Suspension at five allows fighting while suspended. Redirection links wounds between targets; Self-Edge links the user's next wound to another person.

Loom at four throws healing crimson clots and enables Woven Hands. Strings of Destiny at six creates a large thread cage. At stage five, Puppeteer lets Sever interrupt an enemy's cast. Related item: Crimson Mark Shard.

## Dealmaker — Origin VII

Contracts enforce accepted terms and transfer souls. Contract creates a blank document or cancels a held unsigned one. Selector chooses held souls; crouching selects all. Soul Summon brings them across the world, or takes the user to the first while crouching. Soul Rip extracts a soul; crouching with it held shreds it.

Bind assigns a soul to a seat. Under My Protection forwards a bound ally's wounds; Bound Infusion temporarily raises their tier; Self-Infusion sacrifices the lowest bound seat for personal strength. Crouching the active power toggle opens the Ledger. Soul Sword strength comes from the Dealmaker's rank. Contracts, souls and the Soul Plane are implemented in `deal/`.

## Hollow Mark — Origin VIII

Runes buy Codex favours. Milestones include Favored at eight, Chosen at twenty and Hollow at forty, with an extra charm slot at the latter. Blink crosses the gray; Grasp holds a distant target. Void Gaze reveals life through stone; Gray Veil blinds and chokes. Gale Slam repels targets and arrows; Briar controls ground with stone thorns. Bone Darts fires shards and opens the Codex while crouching or holding a rune.

Favours include Thrall and Borrowed Eyes, Sympathy and Void Gift, Shade Step and Doppel, Calcify and Bend Time. Related items include engraved runes, bone charms, the Hollow Mark Shard and phylacteries.

## Counterweight — Origin IX

Parries bank incoming force. Tap Absorb for a short window, with greater capture on perfect timing; hold Guard for sustained but weaker capture. Perfect parries briefly chain later hits. Wave spends Bank in a knockdown cone; Kinetic Bound at stage two spends it on movement. Fist empowers the next punch; Reknit at four converts Bank into health after a delay. Point at five spends the Bank on a piercing spear.

Later stages expand arrow capture, capacity, explosion handling and perfect-counter behaviour. Related item: Counterweight Shard.

## Vertigo — Origin X

The user chooses gravity direction. Falling builds Heft; moving while reoriented builds Poise. Ceiling, Freefall, Compass and Six Walls control personal down. Lift raises and slams targets; Hurl sends them in the user's direction. Plummet rewards fast falling attacks.

Shear redirects nearby gravity; Millstone increases it fourfold and suppresses jumps. Meteor crashes along the user's down; Landfall tears out a sphere of earth. Capsize temporarily reorients world down, and Orbit readies another fall. The storage ID is `nadir`.

## Chained One — Origin N

The design combines the Ten, elemental Titans and numerous forms. Negation starts with wings, two chain tails and two claws. Shapes and Legion use selection wheels. Chain-tails lash, tighten and crush; Lashstorm is the crouching variant. Hellfall calls meteors; Tenfold manifests ten coloured chains.

Tatterwings, Rending Claws, Rend and Rake provide flight and slashes. Kneel forces nearby targets down, Rampage charges through terrain, and Ruin Breath fires a trench-cutting beam. Raise the Mantle grants sustained invulnerability until dismissed; the first daily activation is Descent. Crouching accesses Nulla and Ascend. The design's Binding Link raises the level cap from ten to twenty, but that progression is not implemented in the current OC engine.

## Covenant — original Origin VII-I

A Sovereign writes laws, collects penalties as Collateral and makes binding Pacts. See [COVENANT_SPEC.md](COVENANT_SPEC.md) for the design and [COVENANT.md](COVENANT.md) for actual controls and Minecraft representations.

Assign a playable Origin with `/unrp giveability <player> <id>`. Myriam's fixed level-ten Covenant profile can be assigned with `/unrp character claim myriam <player>`.
