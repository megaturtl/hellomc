architectury {
    common(rootProject.property("enabled_platforms").toString().split(","))
}

val generatedModMetadataDir = layout.buildDirectory.dir("generated/sources/modMetadata/kotlin/main")

val generateModMetadata by tasks.registering {
    description = "Generates a ModMetadata source file from gradle.properties so metadata is available at runtime"
    val packageName = rootProject.property("group").toString()
    val fields =
        mapOf(
            "MOD_ID" to rootProject.property("mod_id").toString(),
            "MOD_NAME" to rootProject.property("mod_name").toString(),
            "MOD_VERSION" to rootProject.property("mod_version").toString(),
            "MOD_AUTHOR" to rootProject.property("mod_author").toString(),
            "MOD_DESCRIPTION" to rootProject.property("mod_description").toString(),
        )
    inputs.property("packageName", packageName)
    inputs.properties(fields) // regenerate when any property changes
    outputs.dir(generatedModMetadataDir)
    doLast {
        val out = generatedModMetadataDir.get().file("ModMetadata.kt").asFile
        out.parentFile.mkdirs()
        out.writeText(
            buildString {
                appendLine("package $packageName")
                appendLine()
                appendLine("object ModMetadata {")
                fields.forEach { (key, value) ->
                    val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
                    appendLine("    const val $key: String = \"$escaped\"")
                }
                appendLine("}")
            },
        )
    }
}

kotlin.sourceSets.named("main") {
    kotlin.srcDir(generateModMetadata)
}

dependencies {
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")

    // Cobblemon common API (compile time only since each platform defines their own runtime jar)
    modCompileOnly("com.cobblemon:mod:${rootProject.property("cobblemon_version")}") { isTransitive = false }
}
