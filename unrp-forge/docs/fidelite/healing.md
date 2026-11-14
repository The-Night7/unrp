# Healing

Sources : `powers/healing.json`, `kubejs/healing_target.js`, `EarlyPowerEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +1 PV / 80 ticks | identique | ✅ |
| **Soin personnel** (X, maintenir) | +2 PV / 20 ticks, -1 aura / 10 ticks | identique | ✅ |
| **Marquer** (C) | recharge 40 ; la cible visée brille et devient la cible du soin | identique | ✅ |
| **Soin à distance** (V, maintenir) | régénération III 2 s toutes les 10 ticks pour la cible marquée à 10 blocs ; -1 aura / 9 ticks | régénération II, renouvelée toutes les 4 ticks | ≈ |
| **Soin d'urgence** (B) | 30 aura, recharge 600 ; soigne autour de soi, le lanceur est affaibli 80 ticks | soigne les alliés à 8 blocs (8 PV × Récupération), faiblesse 4 s | ≈ |
| Poses | `anim_mend_self`, `anim_mend_other` | `heal_self`, `heal_other`, `heal_mark`, `heal_triage` | 🎨 |
| Déblocages | capacités 2 à 4 | rang 2, 3, 4 : 1, 2, 3 points d'aptitude (1 par niveau), commun à tous les pouvoirs ; chaque capacité exige sa parente | ✅ |
