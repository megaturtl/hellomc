package cc.turtl.hellomc.fabric

import cc.turtl.hellomc.HelloMc.init
import net.fabricmc.api.ModInitializer

class HelloMcFabric : ModInitializer {
    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.

        init()
    }
}