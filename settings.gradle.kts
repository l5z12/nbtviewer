// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

// One Stonecutter tree for the whole Fabric range 1.20 – 26.2. Every mapping/version divergence is
// funnelled through the facade package (Txt/Nbt/Gfx/Ui/Mc/Cmd) and NbtScreenBase, so the logic is
// written once against Object; the `//? if yarn` guards there switch between yarn (obfuscated, mc<26)
// and Mojmap (official names, 26.x). The 26.x nodes carry a `yarn=false` constant and their own
// Groovy buildscript (build.fabric26.gradle) — Mojmap-native Loom on JDK 25, no intermediary — while
// every yarn node uses the Kotlin build.gradle.kts (legacy Loom, JDK 17/21).
stonecutter {
    create(rootProject) {
        // Minecraft 26.x: unobfuscated (official names) on the Mojmap-native Loom + JDK 25.
        version("26.2-fabric", "26.2").buildscript("build.fabric26.gradle")
        version("26.1-fabric", "26.1").buildscript("build.fabric26.gradle")
        version("1.21.11-fabric", "1.21.11")
        version("1.21.10-fabric", "1.21.10")
        version("1.21.9-fabric", "1.21.9")
        version("1.21.8-fabric", "1.21.8")
        version("1.21.7-fabric", "1.21.7")
        version("1.21.6-fabric", "1.21.6")
        version("1.21.5-fabric", "1.21.5")
        version("1.21.4-fabric", "1.21.4")
        version("1.21.3-fabric", "1.21.3")
        version("1.21.2-fabric", "1.21.2")
        version("1.21.1-fabric", "1.21.1")
        version("1.21-fabric", "1.21")
        version("1.20.6-fabric", "1.20.6")
        version("1.20.5-fabric", "1.20.5")
        version("1.20.4-fabric", "1.20.4")
        version("1.20.3-fabric", "1.20.3")
        version("1.20.2-fabric", "1.20.2")
        version("1.20.1-fabric", "1.20.1")
        version("1.20-fabric", "1.20")
        vcsVersion = "1.21.1-fabric"
    }
}

rootProject.name = "NBTViewer"
