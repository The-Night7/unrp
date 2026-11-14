# Nightmare

Sources : `powers/nightmare.json`, `events/NightmareEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| **Champ de terreur** (X, interrupteur) | -1 aura / 8 ticks ; ce qui est à 8 blocs subit la peur ; murmures | lenteur + obscurité, murmures | ✅ |
| Murmures | « It's behind you. », « Don't look up. »… | traduits | ✅ |
| **Hallucination** (C) | 22 aura, recharge 220 ; terreur forte sur la cible (18 blocs) | lenteur, obscurité, cécité, faiblesse (le modèle n'a pas d'autres visions : la fiche se trompait) | ✅ |
| **Pic de panique** (V) | 16 aura, recharge 160 ; agrippe puis projette (4,5 à 5,5) | identique | ✅ |
| Résistance selon le niveau | les puissants résistent mieux | durées selon la Ruse du lanceur | ≈ |
| Poses | `anim_clutch`, `anim_fling`, `anim_reach` | `nm_field`, `nm_hallucinate`, `nm_clutch` | 🎨 |
