# Interface UNRP

Reproduit la **structure et le fonctionnement** du hub du mod modèle (touche H, 8 onglets en 2 groupes,
dock à gauche, notifications, offres). Les icônes du dock sont des lettres ; les panneaux sont dessinés en Java dans `client/UiSkin.java` et `client/HubScreen.java`, avec les textures dans `assets/unrp/textures/`.

| Onglet | Contenu | État |
|---|---|---|
| **Pouvoir** | niveau de pouvoir, stats et radar à cinq axes, barre d’aura, école, rang, réputation et description | Fait |
| **Capacités** | arbre cliquable des mouvements et de leurs prérequis, icônes et verrous, panneau de détails (coût, recharge, description), points d’aptitude et trois boutons de déblocage (rangs 2 à 4) | Fait ; molette pour les grands arbres et descriptions longues |
| **Entraînement** | points de stats (1 par niveau), paliers de +0,5 de potentiel par stat (4 au plus, coût 1-2-3-4, plafond 10,0), retrait remboursé : règles du modèle | Fait |
| **Style** | couleur d'aura / yeux / rayons (12 teintes + hex), yeux (4 formes, taille, écart, position), alias (nom au-dessus de la tête et dans le chat), aperçu en direct, Appliquer / Réinitialiser, options du HUD et sons | Fait ; visible par tous les joueurs |
| **École** | les 3 rangs (Roi/Reine/Valet), élèves, donner/retirer un siège (Roi), défier le Roi, classement des écoles | Fait |
| **Groupe** | créer, inviter (offres acceptées/refusées), retirer, quitter, dissoudre | Fait (pas de chat de groupe) |
| **Territoire** | revendiquer le terrain (Roi), liste, déclarer la guerre sur un territoire adverse | Fait (en coordonnées, pas en dimensions) |
| **Duels** | duel amical (14 blocs), combat formel pour la couronne, abandonner ; fin à 2 cœurs | Fait (un seul round, pas « meilleur des trois ») |
| Notifications (cloche) | historique, non lu, « tout lu » | Fait |
| Offres reçues | bandeau Accepter / Refuser en haut du hub | Fait |
| HUD | nom, tier, barre d'aura, coin réglable ; barre de capacités à gauche (cases verrouillées grisées avec leur rang) | Fait |

## Pas encore (hors hub)
Écran de révélation à la création d'OC, roues de sélection, arbres de moves par capacité,
jauges d'effets propres à chaque pouvoir lore, modèles/animations (GeckoLib), sons, campus bâtis (phase 3).

## Règles du serveur
- Le serveur décide de tout ; le client affiche un JSON d'état et envoie des actions (`HubActionPacket`).
- Points de stats : 1 par niveau (plus d'xp de combat), voir [ROLL.md](ROLL.md).
- Rangs : ceux de l'Unodex au départ ; un Roi peut désigner Reine/Valet parmi les élèves ; gagner un combat formel donne la couronne, l'ancien Roi devient Reine.
- Territoires : rayon 24 blocs, un Roi les revendique ; gagner une guerre transfère le territoire.

## Où modifier
`client/HubScreen.java` (affichage), `social/SocialService.java` (règles), `social/SocialData.java` (sauvegarde),
`social/SocialEvents.java` (combat et xp).


## Touches
H : hub · R : activer le pouvoir · X C V B N : capacités de la barre (à gauche de l'écran). Détails : [POUVOIRS.md](POUVOIRS.md).
