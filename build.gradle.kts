import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.architectury.loom") version "1.11-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("com.gradleup.shadow") version "8.3.6" apply false
    kotlin("jvm") version "2.2.20" apply false
}

architectury {
    minecraft = property("minecraft_version") as String
}

allprojects {
    group = rootProject.property("group") as String
    version = rootProject.property("mod_version") as String
}

subprojects {
    plugins.apply("dev.architectury.loom")
    plugins.apply("architectury-plugin")
    plugins.apply("org.jetbrains.kotlin.jvm")

    val loom = extensions.getByType<LoomGradleExtensionAPI>()

    configure<BasePluginExtension> {
        // Set up a suffixed format for the mod jar names, e.g. `example-fabric`.
        archivesName.set("${rootProject.property("mod_id")}-${project.name}")
    }

    repositories {
        mavenCentral()
        maven("https://maven.impactdev.net/repository/development") { name = "ImpactDev (Cobblemon)" }
        maven("https://thedarkcolour.github.io/KotlinForForge/") { name = "Kotlin for Forge" }
        maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") { name = "DevAuth" }
    }

    loom.silentMojangMappingsLicense()

    // Expand gradle.properties values into the loader metadata
    tasks.withType<ProcessResources>().configureEach {
        val expandProps =
            mapOf(
                "mod_id" to rootProject.property("mod_id"),
                "mod_name" to rootProject.property("mod_name"),
                "mod_version" to rootProject.property("mod_version"),
                "mod_author" to rootProject.property("mod_author"),
                "mod_description" to rootProject.property("mod_description"),
                "mod_license" to rootProject.property("mod_license"),
                "minecraft_version" to rootProject.property("minecraft_version"),
                "fabric_loader_version" to rootProject.property("fabric_loader_version"),
                "neoforge_version" to rootProject.property("neoforge_version"),
                "cobblemon_version" to rootProject.property("cobblemon_version"),
            )
        inputs.properties(expandProps)
        filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
            expand(expandProps)
        }
    }

    dependencies {
        "minecraft"("net.minecraft:minecraft:${rootProject.property("minecraft_version")}")
        "mappings"(loom.officialMojangMappings())
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
}
