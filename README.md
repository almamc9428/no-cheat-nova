# NovaClient

NovaClient is a client-side HUD and graphics-settings mod for Minecraft Java 1.21.11 using Fabric. It contains no combat automation, through-wall visuals, or anti-cheat evasion.

## Included features

- HUD widgets for FPS, CPS, coordinates, server ping, and keystrokes.
- Enable or disable each widget, drag it to a new position, and change its scale.
- HUD-wide panel opacity and accent-color controls.
- Rebindable **Open NovaClient** key (Right Shift by default), listed under **Options → Controls → Key Binds → NovaClient**.
- Performance and balanced presets, plus controls for render distance, simulation distance, particles, clouds, and entity shadows.
- Saved HUD layout and appearance in `config/novaclient.properties`. Graphics settings use Minecraft's built-in options.

## Get a JAR from GitHub Actions

1. Create a GitHub repository and upload the contents of this project folder.
2. Push the files to GitHub.
3. Open the repository's **Actions** tab and select the latest **Build NovaClient** run.
4. Download the `novaclient-1.21.11` artifact and extract `novaclient-1.0.0.jar`.

The workflow runs on pushes and pull requests. It builds with Java 21 and uploads the JAR as a workflow artifact.

## Build locally

Requires Java 21. From the project folder, run:

- Windows: `gradlew.bat build`
- macOS/Linux: `./gradlew build`

The distributable mod is written to `build/libs/novaclient-1.0.0.jar`. Install Fabric Loader for Minecraft 1.21.11, then place that JAR in the Minecraft `mods` folder. Fabric API for 1.21.11 is also required.