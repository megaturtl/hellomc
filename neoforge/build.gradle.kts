plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val common: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}
configurations.compileClasspath.get().extendsFrom(common)
configurations.runtimeClasspath.get().extendsFrom(common)
configurations["developmentNeoForge"].extendsFrom(common)

// Files in this configuration will be bundled into your mod using the Shadow plugin.
// Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
val shadowBundle: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

repositories {
    maven("https://maven.neoforged.net/releases") {
        name = "NeoForged"
    }
}

dependencies {
    "neoForge"("net.neoforged:neoforge:${rootProject.property("neoforge_version")}")

    implementation("thedarkcolour:kotlinforforge-neoforge:${rootProject.property("kotlin_for_forge_version")}")

    // runtimeOnly (not modRuntimeOnly) so Loom can remap Cobblemon's bundled parsers correctly.
    runtimeOnly("com.cobblemon:neoforge:${rootProject.property("cobblemon_version")}")

    modLocalRuntime("me.djtheredstoner:DevAuth-neoforge:1.2.2")

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowBundle(project(path = ":common", configuration = "transformProductionNeoForge"))
}

tasks.processResources {

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            "mod_version" to rootProject.property("mod_version"),
            "mod_id" to rootProject.property("mod_id"),
            "mod_name" to rootProject.property("mod_name"),
            "mod_author" to rootProject.property("mod_author"),
            "mod_description" to rootProject.property("mod_description"),
        )
    }
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
}
