# Les capacités « Origin »

Onze capacités du modèle de référence (version 1.6.1) forment le **rang Origin**. Dans son tirage elles ont un poids de **0** : personne ne les reçoit par le roll, elles s'attribuent à la main. Dans notre mod elles sont verrouillées (`UnodexData.NOT_ROLLABLE`) et ne sont **pas encore jouables** (aucun pouvoir dans `ability_powers.json`). Les objets liés existent (éclats, Pourfendeur, queue ossifiée, sable vital, phylactère…), voir `docs/fidelite/objets.md`.

Source : la fiche du hub du modèle (`trees.json`) et ses textes. Les noms sont ceux du modèle ; les explications sont réécrites. « Sneak » = en étant accroupi. Les paliers (« stage », « rung ») sont des déblocages progressifs : chaque capacité se renforce et gagne de nouveaux mouvements.

| Rang | Capacité | En une phrase |
|---|---|---|
| Origin I | Edict | Il vote des lois qui rendent ton pouvoir illégal |
| Origin II | Limit Breaker | Plus tu es blessé, plus tu frappes fort, et tu voles |
| Origin III | Dragon Engine (`wyrm_engine`) | Une puissance prêtée à intérêt, que le Moteur finit toujours par réclamer |
| Origin IV | Singularity | Tout dévorer, puis dépenser ce qu'on a mangé |
| Origin V | Sarcophage | Du sable vivant porté sur la colonne, nourri par les morts |
| Origin VI | Crimson Mark | Un fil rouge relie ta cible ; son destin est à toi |
| Origin VII | The Dealmaker | Tout ce qu'il possède, quelqu'un le lui a signé |
| Origin VIII | Hollow Mark | Une marque pâle qui grandit grâce aux runes |
| Origin IX | Counterweight | Tout ce qu'on lui lance est un dépôt rendu avec intérêts |
| Origin X | Vertigo (`nadir`) | Le bas, c'est là où il le décide |
| Origin N | The Chained One | Le Tout dont les Dix ont été brisés |

---

## Edict — Origin I
*Il passe des lois qui rendent ton pouvoir illégal.*

Un juge. Il fait voter des lois sur un **tribunal** (une zone autour de lui) et gagne du **Précédent** quand la loi est contestée.
- **Pass Law** : frappe du marteau ; la loi en attente s'applique sur un dôme à ses pieds. **Sneak** : passe à la loi suivante (Cycle Statute).
- **Repeal** : abroge sa propre loi tôt ; le dôme tombe.
- **Hands of the Law** (rang de loi 3) : une main géante de loi s'abat sur le sol visé. **Sneak** : *Writ of Seizure* (rang 5), attrape le fautif et le jette.
- **Decrees** (rang de loi 2) : la loi s'écrit sur son propre corps, force et endurance en écriture dorée. **Sneak** : *Decree of Inviolability* (rang 5), quelques secondes où le mal fait contre lui est « rayé du registre ».
- Objet lié : l'éclat de l'Édit.

## Limit Breaker — Origin II
*La force tirée du bord. Plus tu es blessé, plus tu frappes fort. Il vole tant que ça brûle.*

Chaque cœur manquant le rend plus fort. Il accumule du **Grit** (cran) uniquement en combattant blessé, et chaque mouvement en dépense.
- **Flare Bolt** : un trait de Ki en réserve, qui se recharge seul. **Sneak** : *Barrage*, cinq traits en éventail.
- **Breaking Wave** (palier 2) : on maintient pour charger, puis un rayon qui ne s'arrête pas. **Sneak** : *Nova*, la même charge dans toutes les directions.
- **Break** : ouvre l'**Échelle** et choisit une forme que ton potentiel a atteinte. Quatorze formes, chacune plus forte et plus affamée. Les quatre premières : *SURGE* (rouge, tape fort, mange le Grit), *RADIANCE* (or, plus fort, vole plus vite, pas de prix en sang), *AZURE* (bleu, le meilleur bonus stable), *SECOND NATURE* (argent, les coups te ratent plus quand tu es près du bord).
- **Rush** : fonce sur une cible verrouillée et l'écrase. Sprint + lancer pour finir par une explosion.
- **Limitless** : pas de plafond de niveau.
- Objet lié : l'éclat du Briseur de limites.

## Dragon Engine — Origin III
*Le Moteur te prête de la puissance avec intérêts, et il encaisse toujours.*

Chaque **Boost** est un prêt : +5 % sur tous tes coups par cran (6 % en DreadScale et dans le Drive). Les formes profondes s'achètent par des épreuves.
- **Boost** : le Moteur inspire. **Sneak** : *Dispel*, évacue tous les crans d'un coup.
- **Drake Cannon** : dépense tous les crans en un orbe qui frappe comme deux coups pleins, laisse du feu de dragon et un cratère. **Sneak** : *Draconic Art*, le coup signature de la forme portée.
- **Dragonbane** (Ascalon) : tire ou range la lame, qui perce l'armure et ne quitte pas ta main.
- **Fate Breaker** : fait défiler les corps du Moteur. *Dragonplate* : écailles et ailes cramoisies, les effets nuisibles ne passent pas. *DreadScale* : plaque noire à bords dorés, ouvre l'**échiquier** sur Dragonbane (sneak + Dragonbane : Tour/*Dawnbreaker*, Fou/*Godspell*, Cavalier/*Vanguard*, Paladin/*Shepherd*).
- **Cataclysm Drive** (sneak + Fate Breaker) : soixante secondes de vol et de Boosts instantanés. **Puis tu meurs.**
- Les « états » (Kindled, Stoked, Redline) montrent combien de crans sont allumés ; à moitié, chaque coup reçu paie des Cycles.
- Objets liés : éclat du Moteur-dragon, Pourfendeur et Mk. II.

## Singularity — Origin IV
*Mange tout. Dépense ce que tu as mangé.*

Tu dévores le monde et tu gagnes de la **masse**, que tu dépenses ensuite. Elle monte par **stades** (I à VII) :
- **Devour** : déchire et mange ce que tu regardes. II *Accrétion* : manger plus dur, être rassasié donne de l'aura.
- **Nothing Between** : plie l'espace, tu te téléportes où tu regardes.
- **Pull** (III) : attire tout devant toi (les flèches deviennent nourriture) ; Shift lance, viser le sol fait un puits. IV *Horizon* : laisse une singularité qui attire et broie.
- **Collapse** (V) : tout tombe vers toi puis explose ; Shift ouvre une faille devant toi.
- **Deuxième page** : *Erasure* (ligne de néant maintenue, draine la masse), *Eons Between Us* (rien ne t'atteint, coût croissant), *Touch of Nothing* (marque une cible, elle disparaît quand la marque se remplit).
- **Troisième page** (VI) : *Orb of the Void*, *You are the puppet of mine* (prend le contrôle d'une créature), *The Act of Creation* (des copies de ce que tu as mangé te gardent), *Distortion*, *The Deathless* (mourir coûte de la masse).
- **Quatrième page** : le temps — *Timey-wimey* (leurs instants se bloquent en boucle), *Speeding by*, *Can we just talk?* (le temps les fige), *I'm everywhere you look*, *Whoopsy* (tout ce qu'ils font rate).
- **VII Anathema** : manger une étoile du Nether. *Roue d'aspects* : *My world, not your world*, *Primordial Fear*, *The End of All Things* (devenir un serpent géant). *ANATHEMA/DREAD* : tout empire, puis 240 de masse pour DREAD.
- **Mass Forge** : dépenser de la masse pour se renforcer (menu du hub).
- Objet lié : l'éclat de la Singularité.

## Sarcophage — Origin V
*Du sable vivant porté sur la colonne, nourri par les tombés.*

Le sable ne grandit qu'en dévorant : on récolte du **Sable vital** avec Tail Grab, et on gagne des queues (jusqu'à quatre).
- **Tail Grab** : un tentacule saisit la cible ; relancer pour la lancer. **Sneak** : *Sand Coffin*, un cocon broyeur, relancer pour l'écraser.
- **Duneride** (2 queues) : surfe sur une vague de sable, relancer pour s'écraser vers l'avant. **Sneak** : *Sever Tail*, durcit une queue en massue que n'importe qui peut porter (la **queue ossifiée**, qui s'effrite en 5 minutes ; le Sarcophage peut la reconsommer pour la faire repousser).
- **Grainshot** (3 queues) : une volée de grains durcis. **Sneak** : *Tempest* (4 queues), devenir l'avatar à quatre queues dans une tempête de sable.
- **Sandshroud** : le sable en armure, qui broie ce qui s'approche. **Sneak** dans la Tempest : *Unseal*, se lever en **Phage**.
- Objets liés : éclat du Sarcophage, sable vital, queue ossifiée.

## Crimson Mark — Origin VI
*Fixe quelqu'un jusqu'à ce qu'un fil rouge s'allume. Son destin est à couper.*

On maintient le regard sur une cible : un fil rouge grandit entre vous.
- **Sever** : coupe tous les fils d'un coup, chaque coupure blesse sa cible. **Sneak** (palier 2) : *Tripline*, tend un fil entre deux points, qui piège ce qui passe.
- **Thread Step** (2) : tire un fil vers le ciel et se balance. **Sneak** (5) : *Suspension*, rester suspendu et se battre depuis ses fils.
- **Redirection** (3) : lie deux cibles marquées, ce qui frappe l'une frappe l'autre. **Sneak** : *Self-Edge*, te lie à quelqu'un, ta prochaine blessure est la sienne.
- **Loom** (4) : jette des caillots cramoisis en éventail, chacun te soigne un peu. *Woven Hands* : tes mains nues portent le tissage (sneak pour un marteau). **Sneak** (6) : *Strings of Destiny*, une immense cage de fils dont personne ne sort avant sa chute.
- Au palier 5 (*Puppeteer*), Sever interrompt ce que la cible lançait.
- Objet lié : l'éclat de la Marque cramoisie.

## The Dealmaker — Origin VII
*Tout ce qu'il possède, quelqu'un le lui a signé.*

Il écrit des **contrats** qui s'appliquent d'eux-mêmes ; n'importe qui peut lui faire une demande. Il détient des **âmes**.
- **Contract** : écrit un contrat vierge (ou annule celui, non signé, que tu portes).
- **Selector** : choisit quelle âme possédée reçoit tes sceaux (sneak = toutes).
- **Soul Summon** : attire à toi les âmes choisies, où qu'elles soient (sneak : te rendre à la première).
- **Soul Rip** : tire une âme dans ta main ; sneak avec elle en main pour la déchirer.
- **Bind** : lie l'âme devant toi à un siège (sneak pour choisir le siège). **Under My Protection** : prend les blessures d'un allié lié. **Binded Imbuement** : élève temporairement le rang d'un allié lié. **Self-Imbuement** : dépense ton siège lié le plus bas pour ta propre puissance.
- **Ledger** (sneak sur l'interrupteur actif) : le registre. Le Dealmaker peut aussi tirer l'**épée d'âme** de son propre rang.
- Objets liés : éclat du Négociateur, contrats, âme, épée d'âme.

## Hollow Mark — Origin VIII
*Une marque pâle qui pousse à partir des runes. Les monstres forts en lâchent ; on les dépense au Codex.*

La marque ne grandit que par les **runes** que tu trouves et dépenses au **Codex** (sneak + Bone Darts, ou une rune en main) pour acheter des **faveurs**. Paliers : 8 runes (*Favored*), 20 (*Chosen* : les ennemis proches sont affaiblis et ralentis), 40 (*Hollow* : un quatrième emplacement de charme).
- **Blink** : traverse le gris ; l'arrivée se déchire et blesse les ennemis proches. **Sneak** : *Grasp*, attrape quelque chose à distance et le retient en l'air.
- **Void Gaze** : ouvre l'œil, les formes se voient à travers la pierre. **Sneak** : *Gray Veil*, nuage de cendre qui aveugle et étouffe.
- **Gale Slam** : un cône d'air mort renverse tout le monde et renvoie les flèches. **Sneak** : *Briar*, des ronces de pierre tiennent le terrain.
- **Bone Darts** : une gerbe d'éclats d'os.
- **Faveurs** (deuxième page) : *Thrall* (plier une créature à te suivre ; sneak : voir par ses yeux), *Sympathy* (lier des ennemis, la blessure de l'un frappe les autres ; sneak : *Void Gift*, prêter la force d'un charme), *Shade Step* (le gris te cache ; sneak : *Doppel*, un double marche devant toi), *Calcify* (pétrifier), *Bend Time* (ralentir ennemis et flèches).
- Objets liés : éclat de la Marque vide, rune gravée, **charmes d'os** (neuf sortes dont quatre corrompues), phylactère.

## Counterweight — Origin IX
*Tout ce qu'on lui lance est un dépôt, rendu avec intérêts.*

Chaque coup encaissé est de la force qu'on peut dépenser (la **banque**).
- **Absorb** : un tap pare, attrape le coup et en banque la force ; un timing parfait en rapporte plus. *Chain* : une parade parfaite continue d'attraper les coups suivants un instant. *Guard* : maintenir pour garder la garde, elle attrape moins mais dure.
- **Wave** : vide la banque en un cône d'air qui renverse tout le monde. **Sneak** (palier 2) : *Kinetic Bound*, bondir dans la direction du regard et atterrir en douceur.
- **Fist** : charge la banque dans ton prochain coup de poing, qui explose au contact. **Sneak** (4) : *Reknit*, transforme la force en santé, lentement.
- **Point** (5) : concentre toute la banque en une lance qui traverse tout le monde en ligne.
- Paliers : *Recoil* (la garde attrape les flèches), *Fulcrum* (la banque contient plus), *Crucible* (les explosions), *Apex Lever* (le Point), *Equilibrium* (une parade parfaite renvoie le coup sans dépenser la banque).
- Objet lié : l'éclat du Contrepoids.

## Vertigo (`nadir`) — Origin X
*Le bas, c'est là où il le dit.*

Il choisit la direction de la gravité. Les chutes accumulent du **Heft** ; la **Poise** grandit quand il bouge « tourné ».
- **Ceiling / Freefall** : se retourne au plafond, puis tombe dans la direction du regard. *The Compass* : choisir son propre bas. *Six Walls* (palier 2) : atterrir sur n'importe quelle face.
- **Lift** (3, *Gravitas*) : soulève qui tu vises, retaper pour l'écraser. **Sneak** : *Hurl*, ils tombent dans ta direction. *Plummet* : tomber sur un ennemi pour le frapper, plus fort si la chute est rapide.
- **Shear** (4, *Askew*) : le bas pointe là où tu regardes, pour tout ce qui est proche. **Sneak** (5) : *Millstone*, gravité ×4, les sauts meurent.
- **Meteor** (5, *Headlong*) : tomber comme une étoile le long de son propre bas ; l'atterrissage fend le sol. **Sneak** (6) : *Landfall*, arracher une boule de terre et la lâcher.
- **Capsize** (6, *Overturn*) : l'anneau extérieur du Compass, retourner le bas du monde un moment ; *Orbit* : la prochaine chute est prête tout de suite.
- Objet lié : l'éclat du Vertige.

## The Chained One — Origin N
*Le Tout dont les Dix ont été brisés. Lève le Manteau et rien ne passe.*

La capacité la plus vaste : elle regroupe les Dix, les **Titans** (un par élément : terre, feu, glace, lumière, foudre, métal, nature, ombre, eau, vent), et un grand nombre de « formes ». On s'y réveille en **Négation** avec les ailes, deux queues de chaînes et deux griffes.
- **Shapes** : une roue de formes (maintenir) ; taper lance le mouvement de la forme, chaque forme a un grand mouvement (sneak).
- **Chain-tails** : taper fouette le plus proche, maintenir ressert, relâcher écrase. **Sneak** : *Lashstorm*.
- **Hellfall** : des météores tombent là où tu regardes. **Tenfold** : dix chaînes aux couleurs des Dix.
- **The Legion** : une roue de légion, on appelle en cliquant ; les morts proches donnent des âmes.
- **Tatterwings** (ailes), **Rending Claws** (cinq griffures d'un souffle, chacune laisse une entaille dans l'air), **Rend** (deux croissants de force), **Rake** (deux griffures, cinq pour les griffes du Souverain).
- **Kneel** : tout ce qui est proche est forcé de s'agenouiller. **Rampage** : charge à travers terre et pierre. **Ruin Breath** : un rayon violet maintenu, qui creuse une tranchée.
- **Raise the Mantle** : lève le Manteau pour de bon, jusqu'à la chute ; la première levée du jour est la **Descente**. Le Manteau rend intouchable.
- **Sneak** : *Nulla*, *Ascend*.
- Objets liés : les éclats des Dix (« Titan, enfant de… »), le Maillon du lien (plafond de niveau 10 → 20).

---

## Où on en est dans notre mod
- **Non tirables** : verrou en code + test (`originOnlyAbilitiesAreNeverRolled`).
- **Jouables** : Covenant (originale), Counterweight, Limit Breaker, Sarcophage et Hollow Mark (avec faveurs profondes et phylactère) (fiches dans `docs/fidelite/`). Les autres ne sont pas encore portées (pas de pouvoir, de poses, ni de sons). Les objets existent, sans leurs effets.
- Ce document décrit le **modèle** ; rien n'a été vérifié en jeu. Les chiffres exacts (dégâts, coûts, recharges) ne sont pas repris ici : ils sont dans les fiches du modèle (`palladium/powers/*.json` et les classes `events/*Events`) et seront relevés au moment de porter chaque capacité.

## Covenant — Origin VII-I (capacité originale, jouable)
*Stepping into his Court is signing his contract.* The currently implemented Origin ability: see `docs/PLAN_COVENANT.md` for design and `docs/fidelity/covenant.md` for coverage. Assign it with `/unrp giveability <player> covenant`.
