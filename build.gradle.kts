import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*
import java.io.*

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.6.10")
    id("com.github.johnrengelman.shadow") version ("6.1.0")
}

group = "com.github.shynixn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi")
    maven("https://shynixn.github.io/m2/repository/mcutils")
}

dependencies {
    // Compile Only
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.9.2")
    compileOnly("org.geysermc:geyser-api:2.0.4-SNAPSHOT")

    // Plugin.yml Shade dependencies
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.13.0")
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.13.0")
    compileOnly("com.google.inject:guice:5.0.1")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.3.0")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.2.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    compileOnly("com.google.code.gson:gson:2.8.6")

    // Custom dependencies
    implementation("com.github.shynixn.mcutils:common:1.0.25")
    implementation("com.github.shynixn.mcutils:packet:1.0.53")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    testImplementation("org.mockito:mockito-core:2.23.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    failFast = true

    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED
        )
        displayGranularity = 0
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    dependsOn("jar")

    destinationDir = File("C:\\temp\\plugins")

    relocate("com.github.shynixn.mcutils", "com.github.shynixn.mctennis.mcutils")
    exclude("kotlin/**")
    exclude("org/**")
}

tasks.register("languageFile", Exec::class.java) {
    val kotlinSrcFolder = project.sourceSets.toList()[0].allJava.srcDirs.first { e -> e.endsWith("kotlin") }
    val languageKotlinFile = kotlinSrcFolder.resolve("com/github/shynixn/mctennis/MCTennisLanguage.kt")
    val resourceFile = kotlinSrcFolder.parentFile.resolve("resources").resolve("lang").resolve("en_us.properties")
    val bundle = FileInputStream(resourceFile).use { stream ->
        PropertyResourceBundle(stream)
    }

    val contents = ArrayList<String>()
    contents.add("package com.github.shynixn.mctennis")
    contents.add("")
    contents.add("object MCTennisLanguage {")
    for (key in bundle.keys) {
        val value = bundle.getString(key)
        contents.add("  /** $value **/")
        contents.add("  var ${key} : String = \"$value\"")
        contents.add("")
    }
    contents.removeLast()
    contents.add("}")

    languageKotlinFile.printWriter().use { out ->
        for (line in contents) {
            out.println(line)
        }
    }
}
