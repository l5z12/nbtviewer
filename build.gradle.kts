// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

plugins {
    id("fabric-loom") version "1.17.20"
}

// Node buildscript, applied to each versions/<mc>-fabric node. Shared, mapping-agnostic code
// (config + constants) lives in common/; the Minecraft-facing code (with //? if guards) is in src/.
sourceSets {
    named("main") {
        java.srcDir(rootProject.file("common/src/main/java"))
        resources.srcDir(rootProject.file("common/src/main/resources"))
    }
}

val modId = property("mod.id") as String
val loaderDep = property("deps.fabric_loader") as String
val mcVersion = stonecutter.current.version
val isModern = stonecutter.current.parsed >= "1.20.5"          // data-components era
val javaVersion = if (isModern) JavaVersion.VERSION_21 else JavaVersion.VERSION_17

version = "${property("mod.version")}+$mcVersion"
group = property("mod.group") as String
base { archivesName.set(modId) }

val modVersion = version.toString()

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content { includeGroup("maven.modrinth") }
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:${property("deps.yarn")}:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderDep")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    // Optional integrations — compile-only so the mod never *requires* them at runtime.
    modCompileOnly("maven.modrinth:jade:${property("deps.jade")}")
    (findProperty("deps.modmenu") as String?)?.let {
        modCompileOnly("maven.modrinth:modmenu:$it")
    }
}

loom {
    accessWidenerPath.set(rootProject.file("src/main/resources/nbtviewer.accesswidener"))
    runConfigs.configureEach {
        ideConfigGenerated(true)
    }
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(if (isModern) 21 else 17)
}

tasks.processResources {
    val props = mapOf(
        "version" to modVersion,
        "mcDep" to mcVersion,
        "loaderDep" to loaderDep,
        "accessWidener" to "nbtviewer.accesswidener",
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
    // The ClassTweaker is 26.x-only (build.fabric26.gradle); keep it out of the yarn jars.
    exclude("nbtviewer.classtweaker")
}

tasks.named<Jar>("jar") {
    from(rootProject.file("LICENSE")) { rename { "${it}_$modId" } }
}
