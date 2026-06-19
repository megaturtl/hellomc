architectury {
    common(rootProject.property("enabled_platforms").toString().split(","))
}

dependencies {
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")

    // Cobblemon common API (compile time only since each platform defines their own runtime jar)
    modCompileOnly("com.cobblemon:mod:${rootProject.property("cobblemon_version")}") { isTransitive = false }
}
