# Heat Palm

Sources : `powers/heat_palm.json`, `events/HeatPalmEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | -1 aura / 20 ticks ; coups de poing +1,5 à +3 de dégâts selon le niveau, et enflamment (1 s) dans la seconde moitié des niveaux | identique | ✅ |
| **Frappe de paume** (X) | 10 aura, recharge 70 ; cône (3 blocs, 22°) : 7 à 13 dégâts selon le niveau, feu 1 à 2 s, recul 0,3 | identique | ✅ |
| **Paume de fonte** (C, rang 2, interrupteur) | -1 aura / 27 ticks ; l'objet en main cuit, une unité tous les `max(4, 30 - 20 x niveau)` ticks | identique | ✅ |
| Apparence | paumes incandescentes (couche de rendu) | braises dans la main | 🎨 |
| Poses | `anim_smelt` | `cast_strike`, `cast_buff` | 🎨 |
