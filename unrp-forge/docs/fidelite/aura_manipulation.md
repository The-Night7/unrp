# Aura Manipulation

Sources : `powers/aura_manipulation.json`, `kubejs/aura_manip_drain.js`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| **The Copy Wheel** (X) | roue pour choisir quel pouvoir (à 24 blocs) copier ; la copie dure jusqu'à Expulser, aura à 0 ou mort | copie le pouvoir du joueur visé (24 blocs), sans minuteur ; Maj + R passe d'un pouvoir copié à l'autre | ✅ roue du modèle (AmWheelScreen) : pouvoirs à 24 blocs, 65 d'aura, règles du modèle |
| **Expel Powers** (C) | recharge 40 ; rejette les pouvoirs copiés | identique | ✅ |
| Plusieurs copies en réserve | oui, toutes actives ensemble | plusieurs en réserve, une seule en main à la fois (Maj + R pour changer) ; la barre n'a que 5 touches | ≈ |
| Écoles à étudier, combinaisons de pouvoirs, stats copiées | oui | non | ❌ |
| Rendu | moteur d'effets (ClientPassAm) : activation, prise d'une copie, expulsion ; copies sombres | le même moteur, porté ; la roue de copie du modèle (AmWheelScreen) n'est pas encore portée | ≈ |
| Fusions (14 pouvoirs combinés) | dans la roue | affichées, pas jouables : les 14 pouvoirs fusionnés ne sont pas portés | ❌ |
