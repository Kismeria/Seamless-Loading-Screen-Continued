# Seamless Loading Screen Continued

Unofficial continuation of [Seamless Loading Screen](https://github.com/Minenash/Seamless-Loading-Screen) by Minenash,
ported to every Minecraft version from 1.21 to 26.3 on Fabric.

The mod takes a screenshot of the game when you leave a world or server, and shows it as the loading screen when you
rejoin. Once the world is loaded, the screenshot fades out into the game.

A video of the original mod:
[![Seamless Loading Screen demonstration](https://i.imgur.com/NbG9zBP.png)](http://www.youtube.com/watch?v=PB9NinPPTwA "Seamless Loading Screen - Demonstration")

## Supported versions

| Jar        | Minecraft        |
|------------|------------------|
| `+1.21.1`  | 1.21 - 1.21.1    |
| `+1.21.3`  | 1.21.2 - 1.21.3  |
| `+1.21.4`  | 1.21.4           |
| `+1.21.5`  | 1.21.5           |
| `+1.21.8`  | 1.21.6 - 1.21.8  |
| `+1.21.10` | 1.21.9 - 1.21.10 |
| `+1.21.11` | 1.21.11          |
| `+26.1.2`  | 26.1 - 26.1.2    |
| `+26.2`    | 26.2             |
| `+26.3`    | 26.3             |

Requires [Fabric API](https://modrinth.com/mod/fabric-api) and [YetAnotherConfigLib](https://modrinth.com/mod/yacl).
[Mod Menu](https://modrinth.com/mod/modmenu) is optional, the settings also open with the `G` key.

## Usage

- The screenshot is taken when you quit through the pause menu, when the server disconnects you and when you close
  the game window.
- Screenshots are saved in `screenshots/worlds/` (`singleplayer`, `servers`, `realms`).
- Every server has its own mode in the edit server screen: **Enabled**, **Custom / Freeze** (keeps the current
  screenshot, put your own picture there) or **Disabled**.

## Building

The project uses [Stonecutter](https://stonecutter.kikugie.dev/) to build all Minecraft versions from one source tree.
Java 25 is needed to run Gradle, Java 21 and 25 toolchains are needed for the builds.

```
./gradlew buildAndCollect
```

The jars are collected in `build/libs/<mod version>/`.

`./gradlew :<version>:prodAutotest` runs an automated smoke test of the release jar in a production environment: it
creates a world, leaves it, rejoins it and closes the game, then writes the result to
`versions/<version>/run/prod-autotest-<version>/sls-test-result.txt`.

## License

MIT, see [LICENSE](LICENSE). Original mod by Minenash and contributors.
