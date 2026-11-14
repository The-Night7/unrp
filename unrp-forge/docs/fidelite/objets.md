# Fidélité — objets et blocs

Source : les classes d'objets du modèle de référence (version 1.6.1) et sa liste d'objets. Ressources : `tools/gen_items.py`
(relancer après toute modification de la table). **Rien n'est copié** : identifiants, noms, quantités et règles
sont repris ; images, modèles d'objets/blocs, blockstates, textures d'armure et pistes des disques sont ceux du modèle (`tools/import_model_assets.py`), les textes sont réécrits en français.
Légende : ✅ identique · ≈ estimé · 🎨 refait à notre façon · ❌ absent.

## Couverture
Les 88 objets/blocs enregistrés par le modèle sont tous présents (79 objets + 8 blocs + masque du Joker).
Les modèles `school_uniform_*`, `spectre_*` (équipement de créatures) et `shard_3d` ne sont pas des objets
du modèle (non enregistrés, sans nom) : non repris.

## Objets
| Objet | Existe | Comportement | Note |
|---|---|---|---|
| Hache d'acier solaire | ✅ | ✅ 10 dégâts, vitesse −3, allonge +0,6, brise les boucliers, se dissout sans étiquette | donnée par Daystar (Specials14) |
| Armement (11 apparences) | ✅ | ✅ se dissout sans étiquette ; apparence = matériau ou effet | donné par Armament Dominion (Specials13) |
| Lance de sel | ✅ | ✅ visuel, se dissout sans étiquette `unrp_ss_spear` | pas encore donnée par Salt Seraph (la lance est une entité) |
| Pourfendeur / Mk. II | ✅ | ✅ coup perçant 2 / 3,5, indestructible, non lâchable ≈ perce via dégâts magiques | pas de pouvoir « Moteur » : non bloqué ; Mk. II par NBT `unrp_ascalon_tier`, pas d'image propre |
| Queue ossifiée | ✅ | ✅ massue, s'effrite à 5 min, le Sarcophage la reconsomme pour faire repousser sa queue | |
| Épée d'âme | ✅ | ≈ 1,75 de dégâts bruts par coup, non lâchable ; ❌ lien au pacte | |
| Âme | ✅ | ✅ porte le nom ; ❌ effets de pacte | |
| Livre unOrdinary | ✅ | ✅ lecture unique, marque durable, +15 à la réputation (`unrp.reputation`, voir `social/Reputation.java`) | |
| Sérum de réécriture | ✅ | ≈ OC seulement : nouvelle capacité libre + nouveaux potentiels ; niveau et stats entraînées non réécrits | |
| Manuscrits / tomes (10) | ✅ | ✅ +1 / +2 de théorie, plafonds 4/5/6/6/8, réservé à Aura Manipulation ; ❌ bonus de copie non appliqué | |
| Contrat (5 états) | ✅ | 🎨 états par `CustomModelData` ; ❌ clauses, signature, Négociateur | |
| Charme d'os (9 sortes) | ✅ | ✅ s'attache à Hollow Mark, effets et coûts repris | |
| Éclats (10) | ✅ | ❌ usage Titan (système de Godfall absent) ; textes ✅ | |
| Sable vital | ✅ | ✅ nourrit le Sarcophage | |
| Rune gravée | ✅ | ✅ monnaie du Codex, ouvre le Codex | |
| Maillon | ✅ | ❌ effet (Enchaîné pas encore porté) | |
| Amplificateur, atténuateur, désactiveur, seringues (5) | ✅ | ✅ simples ingrédients dans le modèle aussi | pas de machines fonctionnelles |
| Cœur de tour EMBER | ✅ | ✅ simple objet | |
| Disques (6) | ✅ | ✅ vrais disques : pistes du modèle, comparateur 8 à 13, durées du modèle | |
| Œufs d'apparition (3) | ✅ | ❌ créatures non portées | |
| Vêtements (26 pièces) | ✅ | 🎨 **cosmétiques** : ni armure, ni case d'armure. Clic droit = afficher/retirer, ou `/unrp undress` pour tout retirer (la pièce reste dans l'inventaire), visible par tous, mémorisé après la mort ; une pièce qui n'est plus dans l'inventaire disparaît à la reconnexion | dessin par-dessus le corps avec la silhouette d'armure standard ; formes 3D du modèle non reprises |
| Masque du Joker | ✅ | ✅ cache l'analyse d'aura | |

## Blocs
| Bloc | Existe | Comportement |
|---|---|---|
| Sel | ✅ | bloc plein ; le mur de sel de Salt Seraph utilise encore la calcite |
| Barrière d'aura (2) | ✅ | bloc plein ; ❌ pas posé par les pouvoirs (qui utilisent `Walls`) |
| Treillis de particules | ✅ | lumineux ; ❌ pas lié au pouvoir Particles |
| Machines conversion / récupération, champ de suppression | ✅ | ❌ aucune fonction |
| Phylactère | ✅ | ✅ liaison, mort évitée, coût de santé, pistage, effritement (voir hollow_mark.md) |

## Onglet créatif
« Unordinary RP », même contenu que l'onglet du modèle. Les objets de pouvoir (hache, armement, lance, Pourfendeur,
éclats, charme, rune, maillon) n'y sont pas, comme dans le modèle : `/give` ou le pouvoir qui les crée.

## Jamais testé en jeu
Compilation et tests unitaires seulement. À vérifier au lancement : textures, armures portées, onglet, variantes.
