package cc.turtl.hellomc.neoforge;

import net.neoforged.fml.common.Mod;

import cc.turtl.hellomc.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
