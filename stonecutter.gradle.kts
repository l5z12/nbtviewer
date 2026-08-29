// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

plugins {
    id("dev.kikugie.stonecutter")
}

// Active node — controls the shared src/ state in the IDE and a plain `./gradlew build`.
// Switch with: ./gradlew "Set active project to 1.21.11-fabric"
stonecutter active "1.21.1-fabric" /* [SC] DO NOT EDIT */

// Loader/mapping constants for the //? if guards in the facades. `yarn` = Fabric on obfuscated 1.x
// (uses yarn mappings). 26.x is unobfuscated -> official/Mojmap names even on Fabric, so mapping-name
// guards key off `yarn`, and 26.x takes the `else` (Mojmap) branch.
stonecutter.parameters {
    val loader = current.project.substringAfterLast('-')
    val mcMajor = current.version.substringBefore('.').toInt()
    constants["fabric"] = loader == "fabric"
    constants["yarn"] = loader == "fabric" && mcMajor < 26
}

// Build every Fabric version node at once (CI "build all"):  ./gradlew chiseledBuild
tasks.register("chiseledBuild") {
    group = "project"
    description = "Builds every Stonecutter Fabric version node (1.20 – 1.21.11)."
    dependsOn(stonecutter.tasks.named("build").map { it.values })
}
