# Fidélité — Covenant (Origin VII-I)

**Pas de modèle** : capacité originale, conçue d'après la fiche de l'auteure (docs/PLAN_COVENANT.md). Cette fiche compare la fiche d'origine et ce que fait le code (`Specials17`, `CovenantParse`).
Légende : ✅ conforme à la fiche · ≈ interprété · 🎨 rendu à notre façon · ❌ absent.

| Élément de la fiche | Statut | Ce que fait le mod |
|---|---|---|
| Souverain, Tribunal = portée | ✅ | rayon autour de lui tant que le pouvoir est actif ; 10 / 30 / 75 / 150 m selon le niveau (< 4, < 7, < 9, ≥ 9) |
| Tout entrant signe le Covenant | ✅ | les **joueurs** dans le rayon sont signataires (≈ pas les monstres) |
| Sceau d'or au sol, signature sur la main, lois dans l'air | 🎨 | anneau doré sous lui, étincelles sur chaque signataire, un « parchemin » flottant par loi (6 au plus) ; textes aussi envoyés dans le chat |
| Loi + clause pénale | ≈ | écrite dans le chat, comprise par mots-clés FR/EN (testé) ; une phrase non comprise est refusée avec de l'aide |
| Peine collectée sur-le-champ → Gage | ✅ | 1 unité prélevée au manquement, 2 de plus en dette (prélevées 1 par 10 s, ou d'un coup par Foreclosure) |
| Gage : stat, branche, sens, mémoire, mouvement, âme | ≈ | stat : vraie stat (via `statDelta`) ; mouvement : course/saut/marche ; branche : la capacité est bloquée ; sens : vue (cécité) et toucher ; mémoire : dernière phrase + étourdissement + perte d'aura ; âme : −4 PV max. **Pas d'odorat, d'ouïe** |
| Dépenser / garder / donner / rendre le Gage | ✅ | gardé = bonus de stat ; Endowment prête ; Pact échange ; retour programmé |
| Enact | ✅ | + accroupi : abroge la dernière loi (ajout) |
| Terms of Entry | ✅ | armes, armures, élytres, perles ou tout ; objets mis au coffre du Souverain, sauvegardé, rendus à la fermeture ou à la reconnexion du propriétaire |
| Fine Print | ≈ | cache la dernière loi ; accroupi, la révèle |
| Foreclosure | ✅ | toute la dette + 25 % |
| Exile | ✅ | 3 manquements ; expulsé, ne peut plus entrer jusqu'à la fermeture |
| Pact | ≈ | « je donne X contre Y », accepté en chat ; appliqué des deux côtés ; l'offre doit contenir un vrai gage. Accroupi : convoque une âme |
| Endowment | ≈ | prête un gage ; retiré à la première infraction de l'allié ou à la fermeture. Une branche prêtée = copie du pouvoir entier (≈) |
| Proxy | ✅ | 30 s, les blessures passent au lié |
| Appropriation | ≈ | dépense une branche : copie 20 s du **pouvoir entier** de la victime (via Aura Manipulation), pas de la branche seule |
| Sovereign Immunity | ✅ | par défaut non lié ; interrupteur pour s'y lier |
| Indemnity | ✅ | 6 s, blessure annulée, l'auteur subit un manquement (stat au hasard) |
| Escrow | ≈ | protège une stat contre les peines de Covenant ; **ne protège pas** d'autres copies (ex. Aura Transference) |
| Arbitration | ✅ | pouvoir suspendu tant qu'il y a une dette |
| Passif niveau 5 | ≈ | barre d'action : signataires, qui est en infraction et combien il doit ; âmes gardées ; convocation à distance |
| Limitations bas / moyen / élite / haut | ✅ | 2 lois et peines stat+mouvement ; 5 lois ; lois illimitées, branches et sens, max 5 branches volées ; mémoire et âme, Tribunal scellé |
| Délai de lecture bas niveau | ✅ | 3 s |
| Retour du gage | ≈ | stats / mouvement / mémoire : 1 h après la fermeture ; sens et branche d'un plus haut niveau : 10 min ; âme prise par Pact au haut niveau : permanente (jusqu'au redémarrage) |
| > 10 âmes : signature + une loi par jour sans immunité | ✅ | lueur sur sa main ; loi tirée chaque jour (24000 ticks) |
| Tribunal permanent sur un bâtiment | ≈ | Tribunal scellé en mémoire : ses lois valent en son absence, mais **disparaissent au redémarrage** |

## Limites
- **Tout est en mémoire** sauf les objets confisqués : un redémarrage rend tout aux victimes et ferme tous les Tribunaux.
- Jamais lancé en jeu : l'enchaînement chat → loi, les parchemins flottants et les pactes sont à vérifier.
- Le rang est attribué avec `/unrp giveability <joueur> covenant` (non tirable).
