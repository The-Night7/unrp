# Unordinary RP (Fabric 1.20.1)

Noyau : aura, 5 stats (power, speed, trick, recovery, defense), un pouvoir par joueur, touche R.

## Build (Fish)
    sudo pacman -S jdk17-openjdk gradle
    gradle wrapper --gradle-version 8.6
    ./gradlew build          # jar dans build/libs/
    ./gradlew runClient      # lancer le jeu de test

## Commandes (op niveau 2)
    /unrp info [joueur]
    /unrp power list | set <joueur> <pouvoir> | roll <joueur> | clear <joueur>
    /unrp aura set|add <joueur> <valeur>
    /unrp stat set <joueur> <stat> <1-10>

## Ajouter un pouvoir
Dans `power/Powers.java`, `init()` : un `register(new Power(id, coût, recharge, action))`,
puis la clé `power.unrp.<id>` dans les deux fichiers de langue.
