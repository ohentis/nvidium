package me.cortex.nvidium.mixin.minecraft;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Accessor("timer")
    net.minecraft.util.Timer nvidium$getTimer();
}
