# Plan — Covenant (Origin VII-I) et portage des Origin

Covenant est une capacité **originale** (conçue pour l'univers, pas dans le modèle). C'est une variante de rang Origin VII, voisine du Dealmaker. Les onze Origin du modèle suivent ensuite (`docs/ORIGIN.md`).

## Règles du chantier
- **Aucun écart avec le modèle** (demande de l'auteure) : écrans, jauges, entités, tailles, interdits… tout ce que fait le modèle est reproduit ; seul l'aspect (images, animations, sons) est refait, puisqu'on ne copie aucune ressource.
- On reproduit les **comportements** : pas de code, de texture ni de son copiés. Visuels sans particules (lumière dessinée), sons composés de sons vanilla.
- Chaque capacité : un pouvoir dans `ability_powers.json`, un module `SpecialsNN`, des poses, une fiche `docs/fidelite/<id>.md`, des tests de ce qui se teste sans le jeu, et la doc à jour.
- Honnêteté : jamais lancé en jeu ; tout ce qui est estimé est marqué ≈.
- Les Origin ne se tirent pas au roll (`UnodexData.NOT_ROLLABLE`) : on les attribue avec `/unrp giveability <joueur> <id>`.

## Covenant : traduction en système de jeu
Un Souverain. Son **Tribunal** (Court) est un rayon autour de lui. Tout joueur qui y entre est **signataire** : il est tenu par les lois du Souverain. Chaque loi a une **clause pénale** ; l'enfreindre = **manquement**, et la peine est prélevée sur-le-champ et devient du **Gage** (Collateral) pour le Souverain.

| Élément de la fiche | Ce que fait le mod |
|---|---|
| Rayon par niveau | bas (< 4) : 10 m, 2 lois · moyen (< 7) : 30 m, 5 lois · élite (< 9) : 75 m, lois illimitées · haut (≥ 9) : 150 m + Tribunal scellé sur un lieu |
| Écrire une loi | Enact : la **prochaine phrase tapée dans le chat** devient la loi. Format libre : `Personne ne court. Pénalité : sa Vitesse`. Le mod reconnaît des mots-clés (courir, sauter, attaquer, pouvoir, parler, voler, casser, poser, s'accroupir, sortir ; puissance, défense, récupération, ruse, vitesse, mouvement, vue, toucher, branche, mémoire, âme) en français et en anglais ; sinon il explique ce qu'il n'a pas compris |
| Délai de lecture (bas niveau) | une loi nouvelle ne compte qu'après 3 s pour ceux déjà présents |
| Peines | stats et mouvements dès le bas niveau ; branches de capacité et sens à l'élite ; mémoire et âme au haut niveau |
| Gage | liste tenue par le Souverain : il le garde (bonus de stat), le prête (Endowment), l'échange (Pact) ou le rend |
| Retour des gages | stats, mouvements, mémoire : 1 h après la fermeture ; sens : 10 min ; branche prise à plus haut rang : 10 min ; âme : permanente si prise par Pact au haut niveau |
| Termes d'entrée | Terms of Entry : la phrase suivante fixe ce qu'on laisse en entrant (armes, armures, élytres, perles, tout l'inventaire). Les objets vont dans le coffre du Souverain, **sauvegardé** ; rendus à la fermeture |
| Clause cachée | Fine Print cache la dernière loi (elle s'applique, mais n'est pas affichée) ; accroupi : la révèle |
| Foreclosure | prélève toute la dette d'une personne en infraction + 25 % |
| Exile | à partir de 3 manquements : jeté hors du Tribunal, ne peut plus y entrer jusqu'à la fermeture |
| Pact | offre privée à une personne : `je donne X contre Y`, acceptée en répondant `accepter` ; appliquée tout de suite des deux côtés. Accroupi : convoque une âme détenue |
| Endowment | prête un gage au choix à un allié visé ; retiré s'il enfreint une loi ou à la fermeture. Accroupi : choisit le gage |
| Proxy | lie un signataire : les blessures du Souverain lui passent, 30 s |
| Appropriation | dépense une branche volée : le pouvoir de la victime est copié 20 s (≈ le modèle d'Aura Manipulation, branche entière) |
| Immunité souveraine | par défaut le Souverain n'est lié par aucune loi ; l'interrupteur peut le lier |
| Indemnity | 6 s : les blessures reçues sont annulées et deviennent un manquement pour l'auteur |
| Escrow | protège une de ses stats : elle ne peut pas être prise ni volée |
| Arbitration | suspend le pouvoir d'une personne en manquement jusqu'à ce que sa dette soit réglée |
| Passif (niveau 5) | registre : le Souverain voit en barre d'action qui est en infraction et ce que chacun doit ; garde les âmes ; convoque une âme de n'importe où |
| Haut niveau | à plus de 10 âmes : signature lumineuse sur sa main et une loi de son Tribunal, tirée chaque jour, s'applique à lui sans immunité |

**Barre (5 touches, 3 postures)** : *Législation* (Enact, Terms of Entry, Fine Print, Foreclosure) · *Contrats* (Pact, Endowment, Proxy, Appropriation) · *Immunité* (Immunité souveraine, Indemnity, Escrow, Arbitration). Les variantes « accroupi » portent Exile, Tribunal scellé, convocation d'âme, etc.

### Choix qui s'écartent de la fiche (à valider)
- La loi s'écrit en **chat** (le jeu n'a pas d'autre entrée de texte) et est comprise par mots-clés, pas par intelligence libre.
- Tout est en **mémoire serveur** sauf le coffre d'objets : un redémarrage rend tout aux victimes (aucune perte durable possible). Les Tribunaux scellés disparaissent aussi au redémarrage.
- Les **sens** se limitent à la vue (cécité) et au toucher (fatigue + faiblesse) ; la **mémoire** prise est la dernière phrase tapée, la victime est étourdie et perd de l'aura.
- Les victimes sont les **joueurs** (pas les monstres).

## Portage des Origin du modèle (ordre)
1. Counterweight 2. Limit Breaker 3. Sarcophage 4. Hollow Mark 5. Dealmaker 6. Crimson Mark 7. Vertigo (nadir) 8. Edict 9. Dragon Engine 10. Singularity 11. The Chained One. Chacune avec ses objets liés (voir `docs/fidelite/objets.md`).
