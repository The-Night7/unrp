# Réputation

Sources : `events/ReputationEvents`, `commands/ReputationCommands`, `events/ScoreboardBootstrap`, `entity/AgentEntity`, `client/AgentRenderer`, `item/UnOrdinaryBookItem`. Code : `social/Reputation.java`, `entity/Agent.java`, `client/AgentRenderer.java`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Score | objectif `unrp.reputation`, -200 à 200, créé au démarrage | objectif `unrp.reputation`, mêmes bornes | ✅ |
| Livre unOrdinary | +15, une seule lecture | identique | ✅ |
| Voyou de gang tué | +6 | identique | ✅ |
| Villageois / golem de fer tué | -12 / -4 | identique | ✅ |
| Joueur tué en duel officiel ou guerre de territoire | écart de niveau ≥ 20 : -6 ; ≤ -20 : +5 | identique (duels « formal » et « turf » du hub ; niveau x 10) | ✅ |
| Monstre tué près d'un village (24 blocs) | +1, 5 s d'attente | identique | ✅ |
| Seuils | 60 Gardien, -60 Tyran (tags + message), 120 / -120 titre dans le nom | identique, titre devant l'alias | ✅ |
| Tyran | les golems de fer (16 blocs) te prennent pour cible | identique | ✅ |
| Chasseurs | à 120 : 2 chasseurs, 4 % × fréquence, 30 min d'attente | identique | ✅ |
| Autorité | à ±120 : 2 officiers, 3 % × fréquence, 40 min ; leurs coups coupent le pouvoir 5 s (menottes) | identique | ✅ |
| Gangs | près de 3 villageois ou plus : 4 voyous, 6 % × fréquence, ~13 min | identique ; les zones réservées des écoles du modèle n'existent pas encore chez nous, donc rien n'est bloqué | ≈ |
| Réglages | objectif `unrp.config` : `npcs_enabled`, `civil_enabled`, `npc_freq_x1000` | objectif `unrp.config`, mêmes clés | ✅ |
| Commande | `/unrp reputation [cibles]` (op 2) | `/unrp reputation [cibles]` (op 2) | ✅ |
| Hub | bloc STANDING de l'onglet Pouvoir | bloc RÉPUTATION de l'onglet Pouvoir | ✅ |
| Agent | 4 apparences, meneur qui s'embrase, griffes de feu | identique, textures du modèle ; factions EMBER/Spectre pas encore portées | ✅ |
