# Unordinary RP — Fabric prototype

This is the earlier Fabric 1.20.1 prototype. The active mod is in [../unrp-forge](../unrp-forge/).

The prototype implements aura, five stats, one power per player and an R binding. It uses Java 17 and its own Gradle wrapper.

```bash
./gradlew build
./gradlew runClient
```

Operator commands:

```mcfunction
/unrp info [player]
/unrp power list
/unrp power set <player> <power>
/unrp power roll <player>
/unrp power clear <player>
/unrp aura set <player> <value>
/unrp aura add <player> <value>
/unrp stat set <player> <stat> <1-10>
```

To add a prototype power, register a `Power` in `power/Powers.java` and add its `power.unrp.<id>` language entry. These classes and commands are separate from the Forge ability engine.
