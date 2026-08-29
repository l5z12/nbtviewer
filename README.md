# NBT Viewer

A **client-side** Fabric mod that lets you read the NBT / data-component data the game already
sends to your client — for **items**, **blocks / block entities** and **entities** — without needing
operator `/data` access.

It works on any server: it only shows data the vanilla client already has (so on a plain multiplayer
server you see exactly what was synced to you), and in singleplayer / on servers that also run this
mod it can pull the full block-entity & entity NBT.

> Multi-version project built with [Stonecutter](https://stonecutter.kikugie.dev/) — **one single
> tree** for **21 Minecraft versions, 1.20 → 26.2**, nodes named `<mc>-fabric`:
> - yarn (obfuscated): **1.20 through 1.21.11** — 19 nodes, JDK 17/21, `build.gradle.kts`.
> - Mojmap (official names): **26.1, 26.2** — JDK 25, their own `build.fabric26.gradle`.
>
> Both mapping worlds live in the same `src/`. Every version and mapping difference is funnelled
> through a small **facade package** (`Txt`/`Nbt`/`Gfx`/`Ui`/`Mc`/`Cmd`) plus `NbtScreenBase`, carrying
> `//? if yarn` guards; the logic files name no Minecraft type and are written once against `Object`.
> Full details — the facades, the API-generation boundaries (1.20.5 / 1.21.5 / 1.21.6 / 1.21.9 / 26.x)
> and the layout — are in **[MULTIVERSION.md](MULTIVERSION.md)**.

---

## Features

### Inspect anything
- **Held item**, **item under the cursor in any container** (inventory, chest, shulker, …),
  **targeted block / block entity**, and **targeted entity**.
- **Robust targeting for moving entities** — a single-tick crosshair raycast often misses a fast or
  dodgy mob the instant you press the key, so entity targeting also falls back to (1) the nearest
  entity within your look cone, then (2) the last entity the crosshair was on within a short sticky
  window. Both are configurable (or off); see *Nearest-entity fallback* / *Sticky target* in the
  options.
- Everything is the *client-visible* NBT — no `/data`, no operator permissions.
- For blocks you also get the block id, blockstate `properties`, position and the block-entity NBT
  combined into one view.
- Items are shown in their real serialized form: `{id, Count, tag}` on 1.20.x, or
  `{id, count, components}` on 1.20.5+ (data components).

### Configurable hotkeys ("what key reviews what")
Every action is its own rebindable key under **Options ▸ Controls ▸ NBT Viewer**:

| Action | Default |
|---|---|
| Open NBT (auto: block ▸ entity ▸ item) | `N` |
| Open NBT of held item | unbound |
| Open NBT of hovered slot | unbound |
| Open NBT of targeted block | unbound |
| Open NBT of targeted entity | unbound |
| Copy NBT (auto) to clipboard | unbound |
| Copy held item NBT to clipboard | unbound |
| Toggle NBT HUD overlay | unbound |
| Cycle HUD overlay source | unbound |
| Hold to show NBT overlay | unbound |
| Open NBT Viewer config | unbound |

The *Open hovered slot* / *Open (auto)* keys also work **while a container GUI is open**, so you can
inspect an item straight from your inventory.

### Searchable, collapsible tree GUI
- Full-screen NBT tree with **expand/collapse**, **search** (keys & values, auto-expands matches),
  keyboard navigation (`↑ ↓ ← →`, `+`/`-`, `Enter`), mouse-wheel + draggable scrollbar.
- **Copy All**, **Copy Node** (selected subtree) and **Copy Path** (e.g. `blockEntity.Items[0].id`).
- Live stats: tag count and byte size.

### HUD overlay
- Live NBT of the current target in any screen corner.
- Configurable source (auto / held / hovered slot / block / entity), corner, scale, max lines,
  background, and visibility (always / while a key is held / while sneaking).

### `/viewdata` command (alias `/nbtview`)
```
/viewdata                      open the tree GUI for the auto target
/viewdata item|held|slot|block|entity|auto [chat|gui|copy]
/viewdata copy                 copy the auto target's SNBT to the clipboard
/viewdata overlay              toggle the HUD overlay
/viewdata config               open the options screen
```
`chat` prints coloured, pretty SNBT (capped to avoid flooding); `copy` puts SNBT on the clipboard.

**Specify a target explicitly** (no line of sight needed — handy for things you can't aim at):
```
/viewdata entity <id|uuid|type>   by network id, UUID, or nearest of an entity type (minecraft:zombie)
/viewdata entity @e[type=…,sort=nearest,limit=1]   a vanilla-style selector (see below)
/viewdata player <name>           a player by name
/viewdata block <x> <y> <z>       the block at coordinates
```
The selector is resolved client-side against loaded entities and supports `@s @p @a @r @e @n` with
`type` (`!` negation; bare names get `minecraft:`), `name` (`!`, quotes), `distance` (`a..b`, `..b`,
`a..`, or `n` as a max), `limit`, and `sort` = `nearest|furthest|random|arbitrary`. Options that need
server state (scores, teams, gamemode, nbt, tags) are ignored; distance/sort are from you.

When a selector or a bare type matches **more than one** entity, a **picker** opens — a scrollable
list (name · type · distance) you click, or navigate with `↑↓` and `Enter`, to choose which to
inspect. A single match opens the tree view directly.

### Jade extension (optional)
If [Jade](https://modrinth.com/mod/jade) is installed, the looked-at **block entity** and **entity**
NBT is added to Jade's tooltip. Toggle each in Jade's own plugin-config screen. In singleplayer the
full server-side NBT is synced; on servers it falls back to client-visible data.

### Mod Menu & config
- A dependency-free options screen (`/viewdata config`, the *Open config* key, or **Mod Menu**).
- Settings persist to `config/nbtviewer.json`.

---

## Building

Requirements: **JDK 21** (yarn nodes 1.20 – 1.21.11) and **JDK 25** (26.x nodes). The Gradle wrapper
handles the rest.

**Behind a proxy?** Proxy settings are deliberately *not* committed (so CI and other contributors
aren't forced onto a localhost proxy). If your network needs one, put it in your **Gradle user home**
(`~/.gradle/gradle.properties`) so every build picks it up:

```properties
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
```

The Gradle **wrapper bootstrap** downloads its distribution before it reads any `gradle.properties`,
so the first run also needs the proxy via `GRADLE_OPTS`:

```bash
export GRADLE_OPTS="-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 \
                    -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890"

# Build one specific yarn version (any node 1.20-fabric … 1.21.11-fabric), JDK 17/21:
./gradlew :1.21.11-fabric:build
./gradlew :1.20.1-fabric:build

# Build a 26.x version (Mojmap) — requires JDK 25:
JAVA_HOME=/path/to/jdk-25 ./gradlew :26.2-fabric:build      # or :26.1-fabric:build

# Build every version at once (matching JDKs must be discoverable by Gradle toolchains):
./gradlew chiseledBuild
```

Jars are written to `versions/<mc>-fabric/build/libs/nbtviewer-1.0.0+<mc>.jar`. The 26.x nodes are
part of the **same** tree — they just carry a `yarn=false` constant and their own Groovy buildscript
(Mojmap-native Loom on JDK 25), so their `else` guard branches compile against the 26.x APIs
(render-state extraction, `HudElementRegistry`, `KeyEvent`/`MouseButtonEvent` input, `TagValueOutput`
serialization, `ClassTweaker`, `ClientCommands`).

### Switching the active (IDE) version

Stonecutter registers one task per node. Switching rewrites the `//? if` guards in `src/` so the
chosen version's branch is the active (uncommented) one — handy for IDE resolution:

```bash
./gradlew "Set active project to 1.21.11-fabric"   # switch the IDE/source view to a node
./gradlew "Refresh active project"                 # re-run the comment processor on the active node
./gradlew "Reset active project"                   # back to 1.21.1-fabric — run this before committing
```

The committed source is always at the `vcsVersion` (`1.21.1-fabric`); reset before you commit so the
guard states are consistent in git.

---

## Project layout (Stonecutter)

```
settings.gradle.kts           registers every node 1.20-fabric … 26.2-fabric in one tree
stonecutter.gradle.kts        active node + fabric/yarn constants + chiseledBuild
build.gradle.kts              yarn node buildscript (Loom 1.17.20, JDK 17/21)
build.fabric26.gradle         26.x node buildscript (Mojmap Loom 1.16-SNAPSHOT, JDK 25)
gradle.properties             mod id/version + proxy
versions/<mc>-fabric/gradle.properties   per-node dependency pins (yarn, fabric api, jade, modmenu)

common/src/main/java/…        mapping-agnostic code (config, NbtViewer constants)
src/main/java/…/facade/       the facades that carry the //? if yarn guards (Txt/Nbt/Gfx/Ui/Mc/Cmd)
src/main/java/…               logic written once against Object + the facades
```

Every Minecraft API difference — across versions *and* across the yarn↔Mojmap split — is isolated in
the facade package and `NbtScreenBase`. The logic files (`TargetResolver`, the screens, the HUD, the
command, the Jade providers, …) name no Minecraft type at all.

### Adding another Minecraft version
1. Add `version("<mc>-fabric", "<mc>")` to `settings.gradle.kts` (append `.buildscript("build.fabric26.gradle")` for a 26.x+ node).
2. Create `versions/<mc>-fabric/gradle.properties` with the yarn / fabric-api / jade / modmenu pins.
3. Build the node. If a facade guard needs a new boundary, add it there (e.g. `//? if yarn && >=<mc>`)
   — never in the logic files.

---

## Notes & limitations
- Client-only mod: on multiplayer servers you see the NBT that was actually synced to the client
  (many block entities sync little; that is the honest "sent to client" data). Add this mod to the
  server (or play singleplayer) to have Jade sync the full block-entity/entity NBT.
- The yarn nodes build with Gradle 9.7.1 + Fabric Loom 1.17.20 on JDK 17/21; the 26.x nodes with the
  Mojmap-native Loom on JDK 25. The API-generation boundaries the guards encode are listed in
  [MULTIVERSION.md](MULTIVERSION.md) (1.20.5 data components, 1.21.5 `Optional` getters, 1.21.6 matrix
  + entity serialization, 1.21.9 event-object input, 26.x Mojmap).

---

## License

NBT Viewer is free software licensed under the **GNU General Public License v3.0 or later**
(`GPL-3.0-or-later`) — see [LICENSE](LICENSE) for the full text. Every source file carries an
[SPDX](https://spdx.dev/) header:

```java
// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later
```
