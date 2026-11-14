# Hydrofreeze

Sources : `powers/hydrofreeze.json`, `events/HydrofreezeEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | résistance au recul +0,3 | identique | ✅ |
| **Changer de posture** | recharge 10 ; eau ↔ glace | identique ; la barre change de capacités | ✅ |
| **Forme liquide** (eau, interrupteur) | flaque : silhouette réduite, +20 % de vitesse, coups au corps à corps divisés par 2 | vitesse et dégâts identiques ; silhouette normale | ≈ |
| **Enveloppement** (eau, maintenir) | la cible (8 blocs) est enfermée dans l'eau, 260 ticks max, elle peut s'échapper | identique ; elle se noie | ✅ |
| **Vague** (eau) | 12 aura, recharge 55 | vague qui repousse | ≈ (forme exacte inconnue) |
| **Lames de glace** (glace, interrupteur) | +6 dégâts, les coups ralentissent 60 ticks | identique | ✅ 🎨 (lames de glace dessinées en traits lumineux) |
| **Lance de glace** (glace) | 14 aura, recharge 35 | projectile qui ralentit | ✅ (dégâts ≈) |
| **Prison de glace** (glace) | 100 aura, recharge 120 ; cible (18 blocs) gelée 200 ticks | identique | ✅ |
| Poses | `anim_envelop` | 7 poses `hy_*` | 🎨 |
| Déblocages | capacités à débloquer | rang 2, 3, 4 : 1, 2, 3 points d'aptitude (1 par niveau), commun à tous les pouvoirs ; chaque capacité exige sa parente | ✅ |
