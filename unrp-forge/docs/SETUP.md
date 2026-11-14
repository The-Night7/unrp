# Installation et build (Arch, Fish)

Prérequis : Java 17 par défaut et Gradle 8.6 (via le wrapper). Ton Gradle système (9.x) est trop récent
pour ForgeGradle 6, d'où le wrapper.

## 1. Java 17
```fish
sudo archlinux-java set java-17-openjdk
java -version
```

## 2. Wrapper Gradle (une seule fois)
```fish
mkdir -p /tmp/wrap; and cd /tmp/wrap; and touch settings.gradle; and gradle wrapper --gradle-version 8.6
cp -r /tmp/wrap/gradlew /tmp/wrap/gradlew.bat /tmp/wrap/gradle ~/GitHub/unrp/unrp-forge/
cd ~/GitHub/unrp/unrp-forge; and chmod +x gradlew
```
(adapte le chemin si tu as dézippé ailleurs)

## 3. Compiler et tester
```fish
./gradlew build
```
Le premier build télécharge Minecraft et Forge (plusieurs minutes). `build` lance aussi les tests
(tirage de rareté). Le jar est dans `build/libs/unrp-0.1.0.jar`.

## 4. Lancer le jeu de test
```fish
./gradlew runClient
```
**Le menu de candidature s'ouvre tout seul** quand tu entres dans un monde sans personnage.
Si tu le fermes (Échap), tu le rouvres avec `/unrp menu`. Pour recommencer : `/unrp reset @s`.

Commandes de vérification (monde avec commandes activées) :
```
/unrp menu                 rouvre le menu de candidature
/unrp whoami               quel personnage tu joues
/unrp reset @s             efface ton personnage (admin)
/unrp rolltest 100000      compare le tirage aux probabilités attendues
/unrp roll                 un tirage pour toi
/unrp rollability @s       retire une capacité à un OC (admin)
/unrp unlock @s all        débloque tous les rangs de capacités (admin) ; reset pour tout reverrouiller
/unrp school list
/unrp school info wellston
/unrp character list rp
/unrp character info telemachus
/unrp character claim telemachus @s
/unrp character list rp    (telemachus apparaît avec une étoile = pris)
```
Après avoir modifié un JSON dans `src/main/resources/...`, relance le jeu (ou fais `/reload` si tu
utilises un datapack qui remplace le fichier).

## 5. Les autres mods (dossier `libs/`)
**Palladium est obligatoire** (vol et saut des pouvoirs, comme dans le modèle). Pour le développement, `libs/` doit contenir :
`palladium-4.5.10+1.20.1-forge.jar`, `architectury-9.2.14-forge.jar`, et les deux jars que Palladium embarque
(`palladiumcore-forge-1.0.1+1.20.1-forge.jar`, `player-animation-lib-forge-1.0.2-rc1+1.20.jar`, à extraire de son
`META-INF/jars/`). En jeu, il suffit de mettre Palladium et Architectury dans `mods/`.

Pas nécessaire pour la phase 1. Dès la phase 2/4, dépose dans `libs/` les jars Forge 1.20.1 utiles au développement :

| Jar | Rôle |
|---|---|
| `palladium-4.5.101.20.1-forge.jar` | moteur de pouvoirs |
| **`palladiumcore-...-forge.jar`** | **manquant dans ta liste** : Palladium le déclare obligatoire |
| `geckolib-forge-1.20.1-4.8.4.jar` | animations |
| `architectury-9.2.14-forge.jar`, `rhino-forge-2001.2.3-build.10.jar`, `kubejs-forge-2001.6.5-build.26.jar` | dépendances de KubeJS/Palladium |
| `Pehkui-3.8.21.20.1-forge.jar` | taille des personnages (colonne « Taille » de l'Unodex) |

Les jars sont ajoutés automatiquement : il faut juste qu'ils s'appellent `nom-version.jar`
(le nom d'origine du téléchargement convient). Ils ne sont pas versionnés dans git.

Les autres jars que tu as joints (Ragdollified x4, Zarith, VisualHealth, BetterBloodOverlay,
GeckoLib Animation Optimizer) servent à ton **serveur / instance de jeu**, pas à compiler :
mets-les dans le dossier `mods/` de l'instance avec le jar `unrp`. Voir `docs/ARCHITECTURE.md`, section compatibilité.

## Problèmes courants
- `No Java environment is set as default` : refaire l'étape 1.
- `Could not create an instance of type ...LoomProblemReporter` : tu es sur un Gradle 9, utilise `./gradlew`.
- Erreur de compilation : copie la sortie complète, surtout les premières lignes `error:`.
