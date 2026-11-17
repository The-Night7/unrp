# Build and run

The Forge project targets Minecraft 1.20.1, Java 17 and Gradle 8.6. Python 3 is required by the English-language check in the build. Use `./gradlew` from `unrp-forge/`; a newer system Gradle is not a replacement for the wrapper.

```bash
java -version
./gradlew build
./gradlew runClient
```

On Arch, select the installed Java 17 runtime with `sudo archlinux-java set java-17-openjdk` if necessary. The first build downloads Minecraft and Forge. `build` also runs the tests and language audit, then writes the JAR to `build/libs/`. If Python is named differently on your system, pass `-PpythonExecutable=python` (or its full path).

## Development dependencies

Place these Forge 1.20.1 dependencies in `libs/`, using the versions expected by `build.gradle`:

- Palladium 4.5.10;
- Architectury 9.2.14;
- Palladium Core 1.0.1 and Player Animation Library 1.0.2-rc1, embedded in Palladium's `META-INF/jars/`;
- GeckoLib where required by the build.

Check [libs/README.md](../libs/README.md) and the actual dependency declarations before replacing a JAR. Library binaries are not committed. A normal player installation uses the runtime mods in `mods/`; the development classpath additionally needs the embedded libraries.

## Test a profile

Enter a world with commands enabled. Character selection opens for players without a profile. Useful commands:

```mcfunction
/unrp menu
/unrp whoami
/unrp character list rp
/unrp character info myriam
/unrp character claim myriam @s
/unrp unlock @s all
/unrp school list
```

`/unrp reset @s` removes the current profile. Use it deliberately; reopening the menu does not require a reset.

Restart after replacing the mod JAR or changing Java. Data-pack JSON can be reloaded with `/reload`; changing development resources may also require restarting the test client.

## Build problems

If Forge Maven times out while listing dynamic dependency versions, retry once or use `./gradlew build --offline` when the cache is complete. The build already pins the affected Forge helper ranges to Minecraft's Log4j API and ModLauncher versions. Do not globally downgrade unrelated dependencies.

A missing wrapper JAR is different from a dependency timeout. Restore the wrapper files from the project before debugging Forge. For a compiler failure, read the first actual error in the log rather than the final `BUILD FAILED` summary.
