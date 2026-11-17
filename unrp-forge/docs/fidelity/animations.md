# Move animations

`anim/MoveClipIds.java` assigns pose identifiers and `MoveClips.java` defines their keyframes. A move selects `pose` and optionally `pose_sneak`; its JSON does not directly rotate the player model.

The player pose mixins and animation bridge apply those clips. Keyframe times use ticks and joint rotations use degrees. Related sound signatures are in `pose_sounds.json`.

When changing a clip, test first- and third-person views, left/right arms, crouching, release and interruption. Animation timing must agree with the server move, but animation itself cannot grant damage or bypass a cooldown.
