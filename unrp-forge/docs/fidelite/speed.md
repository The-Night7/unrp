# Speed

Sources : `powers/speed.json`, `events/SpeedEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | interrupteur, sans bonus direct | identique | ✅ |
| **Ruée** (X, interrupteur) | +55 % de vitesse (op 2), -1 aura / 10 ticks, traînée d'images | identique ; traînée en particules | ✅ 🎨 |
| **Élan éclair** (C) | 8 aura, recharge 50 ; bond de 1,8 droit devant | identique | ✅ |
| Poses | `spd_rush` | `spd_rush`, `spd_dash` | 🎨 |
| Esquive (`SpeedDodgeEvents`) | pour tout joueur : 12 % x (Vitesse / 10) de chances d'annuler une attaque, hors dégâts d'environnement | identique (sans le multiplicateur de stat, absent ici) ; son et particules refaits | ✅ 🎨 |
| Déblocage (`unlock_s2`) | Élan éclair à débloquer | rang 2, 3, 4 : 1, 2, 3 points d'aptitude (1 par niveau), commun à tous les pouvoirs ; chaque capacité exige sa parente | ✅ |
