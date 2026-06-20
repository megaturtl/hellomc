import net.fabricmc.loom.api.LoomGradleExtensionAPI
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
    group = rootProject.property("maven_group") as String
    version = rootProject.property("mod_version") as String
}

subprojects {
    plugins.apply("dev.architectury.loom")
    plugins.apply("architectury-plugin")
    plugins.apply("maven-publish")
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
    }

    loom.silentMojangMappingsLicense()

    dependencies {
        "minecraft"("net.minecraft:minecraft:${rootProject.property("minecraft_version")}")
        "mappings"(loom.officialMojangMappings())
    }

    configure<JavaPluginExtension> {
        // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
        // if it is present.
        // If you remove this line, sources will not be generated.
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    // Configure Maven publishing.
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = the<BasePluginExtension>().archivesName.get()
                from(components["java"])
            }
        }

        // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
        repositories {
            // Add repositories to publish to here.
            // Notice: This block does NOT have the same function as the block in the top level.
            // The repositories here will be used for publishing your artifact, not for
            // retrieving dependencies.
        }
    }
}
