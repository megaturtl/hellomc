package cc.turtl.hellomc.neoforge

import cc.turtl.hellomc.HelloMc
import net.neoforged.fml.common.Mod

@Mod(HelloMc.MOD_ID)
class HelloMcNeoForge {
    init {
        // Run our common setup.
        HelloMc.init()
    }
}