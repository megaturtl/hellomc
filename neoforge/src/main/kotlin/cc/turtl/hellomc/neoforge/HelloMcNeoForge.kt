package cc.turtl.hellomc.neoforge

import cc.turtl.hellomc.HelloMc
import cc.turtl.hellomc.ModMetadata
import net.neoforged.fml.common.Mod

@Mod(ModMetadata.MOD_ID)
class HelloMcNeoForge {
    init {
        // Run our common setup.
        HelloMc.init()
    }
}
