# Animations du modèle

Deux systèmes, portés tels quels :

1. **Clips** (`anim/MoveAnim`, `MoveAnimClient`, `MoveRig`, `MoveClips`, `MoveClipIds`, paquet `MoveAnimPacket`) : 213 gestes à
   images clés. `Poses.play/hold` joue le clip du modèle quand le geste en est un (sinon notre pose). Mixins :
   `PlayerModelMixin` (corps), `ItemInHandMoveMixin` (main à la première personne).
2. **Poses tenues** (`anim/RosterPoses`, portage de `kubejs_scripts/power_animations.js`) : 140 poses suivant les minuteurs
   d'animation de Palladium, allumées par des tags du joueur (`PoseTags`) : `unrp.pose.<geste>` pendant un geste,
   tags d'état (`hold_tag` des capacités, `flash_tag`, tags posés par le code des pouvoirs). Première personne :
   `PlayerHandPoseMixin`.

Chaque capacité d'un pouvoir du modèle joue le geste que le modèle joue (champ `pose` de `ability_powers.json`) ;
là où le modèle n'anime rien, rien n'est joué. Les pouvoirs propres à l'Unodex gardent nos poses.
