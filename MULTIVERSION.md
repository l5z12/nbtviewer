# Multi-version architecture

NBT Viewer is one mod built for **21 Minecraft versions, 1.20 → 26.2**, from a **single Stonecutter
tree** ([Stonecutter](https://stonecutter.kikugie.dev/)) — one `src/`, one set of logic files, every
mapping and per-version divergence funnelled through a small facade package.

## One tree, two mapping worlds

| Nodes | Versions | Mappings | Loom | JDK | Buildscript |
|---|---|---|---|---|---|
| yarn | **1.20 – 1.21.11** (19) | yarn (obfuscated) | `fabric-loom` 1.17.20 | 17 / 21 | `build.gradle.kts` |
| Mojmap | **26.1, 26.2** | Mojang (official names) | `net.fabricmc.fabric-loom` 1.16-SNAPSHOT | 25 | `build.fabric26.gradle` |

Every node is named `<mc>-fabric` (e.g. `1.21.11-fabric`, `26.2-fabric`) and lives in the **same**
Stonecutter tree. Each is a Gradle subproject with its own buildscript, so the two Looms coexist: the
26.x nodes attach `build.fabric26.gradle` (Groovy) via `version(…).buildscript(…)`, every yarn node
uses `build.gradle.kts`. The controller (`stonecutter.gradle.kts`) sets two constants that the guards
switch on:

- `fabric = loader == "fabric"` — always true here.
- `yarn = loader == "fabric" && mcMajor < 26` — **true for 1.20–1.21.11, false for 26.x**.

## How one source serves all 21 versions

Logic lives once in `src/` and names no Minecraft type directly — it passes everything around as
`Object` and goes through the **facade package** (`dev.l5z12.nbtviewer.facade`). Only the facades and
`NbtScreenBase` carry `//? if yarn { … } //?} else { … } //?}` guards; the active version's branch is
uncommented, the others wrapped in `/* */`. Mapping-agnostic code (config, constants) lives in
`common/`.

| Facade | Absorbs |
|---|---|
| `Txt` | `Text` ↔ `Component` — literal/translatable/append/colour (RGB, no `Formatting`/`ChatFormatting`) |
| `Nbt` | `NbtCompound`/`NbtElement` ↔ `CompoundTag`/`Tag` — traversal, construction, and item/block-entity/entity serialisation |
| `Gfx` | `DrawContext` ↔ `GuiGraphicsExtractor` — text/fill/scissor + the `MatrixStack`→`Matrix3x2fStack` HUD scale |
| `Ui` | `ButtonWidget`/`TextFieldWidget` ↔ `Button`/`EditBox` — create/render/mutate |
| `Mc` | `MinecraftClient` ↔ `Minecraft`, `World` ↔ `Level`, both `Identifier` packages, `Registries` ↔ `BuiltInRegistries`, HUD/scroll hook registration, crosshair-target + hovered-slot primitives |
| `Cmd` | `ClientCommandManager` → `ClientCommands` (renamed in the 26.x Fabric API) |
| `NbtScreenBase` | the whole `Screen` superclass: `render` ↔ `extractRenderState`, three input models, `close`/`onClose`, `shouldPause`/`isPauseScreen`, widget add, focus |

### Version/mapping boundaries the guards encode

| Boundary | What changed |
|---|---|
| **1.20.5** | data components: item NBT via `ItemStack.CODEC`, `Identifier.of`, registries arg on block-entity NBT |
| **1.21.5** | `NbtCompound.getCompound` returns `Optional` |
| **1.21.6** | `Matrix3x2fStack` HUD matrix, `createNbtWithIdentifyingData`, `WriteView` entity serialisation |
| **1.21.9** | event-object input (`KeyInput`/`Click`), `KeyBinding.Category`, 6-arg `afterMouseScroll`, `KeyBinding.matchesKey(KeyInput)`, `hasControlDown` removed |
| **26.x** (`yarn=false`) | Mojang mappings (`Component`, `GuiGraphicsExtractor`, `Minecraft`, …), `extractRenderState` render model, `MouseButtonEvent`/`KeyEvent` input, `HudElementRegistry`, `TagValueOutput`, `KeyMapping`, `ClientCommands`, `ClassTweaker`, `setScreenAndShow`, `sendOverlayMessage` |

The only guards **outside** the facades are the container-screen key hook in `NbtViewerClient`
(its `afterKeyPress` event object differs across all three input tiers) and the Jade providers
(whose method signatures are dictated by the Jade API's mapping-specific types).

## Access widener / class tweaker

The mod widens one field — the hovered container slot — to read the item under the cursor. yarn nodes
ship `src/main/resources/nbtviewer.accesswidener` (`focusedSlot`); the 26.x nodes ship
`nbtviewer.classtweaker` (`hoveredSlot`, ClassTweaker v1 format). `fabric.mod.json`'s `accessWidener`
field and each buildscript's `accessWidenerPath` are parameterised, and processResources excludes the
file that does not belong to the active node.

## Layout

```
settings.gradle.kts        registers every node 1.20-fabric … 26.2-fabric in one tree
stonecutter.gradle.kts     active node + fabric/yarn constants + chiseledBuild
build.gradle.kts           yarn node buildscript (Loom 1.17.20, JDK 17/21)
build.fabric26.gradle      26.x node buildscript (Mojmap Loom 1.16-SNAPSHOT, JDK 25)
common/src/main/java       mapping-agnostic: config, NbtViewer constants
src/main/java/.../facade   the facades that carry the //? if yarn guards
src/main/java              logic written once against Object + facades
versions/<mc>-fabric/gradle.properties   per-node yarn / fabric-api / jade / modmenu pins
```

## Building

```bash
export GRADLE_OPTS="-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890 -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890"

# one yarn version (JDK 17/21):
JAVA_HOME=/path/to/jdk-21 ./gradlew :1.21.11-fabric:build
# a 26.x version (JDK 25):
JAVA_HOME=/path/to/jdk-25 ./gradlew :26.2-fabric:build
# every version (matching JDKs must be discoverable by Gradle toolchains):
./gradlew chiseledBuild
```
