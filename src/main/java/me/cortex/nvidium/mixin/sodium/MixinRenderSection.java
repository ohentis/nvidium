package me.cortex.nvidium.mixin.sodium;

import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import me.cortex.nvidium.sodiumCompat.IRenderSectionExtension;

@Mixin(value = RenderSection.class, remap = false)
public class MixinRenderSection implements IRenderSectionExtension {

    @Unique
    private volatile boolean isEnqueued;
    @Unique
    private volatile boolean isSeen;

    @Override
    public boolean nvidium$isSubmittedRebuild() {
        return isEnqueued;
    }

    @Override
    public void nvidium$isSubmittedRebuild(boolean state) {
        isEnqueued = state;
    }

    @Override
    public boolean nvidium$isSeen() {
        return isSeen;
    }

    @Override
    public void nvidium$isSeen(boolean state) {
        isSeen = state;
    }
}
