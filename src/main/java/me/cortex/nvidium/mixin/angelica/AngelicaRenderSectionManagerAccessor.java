package me.cortex.nvidium.mixin.angelica;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.gtnewhorizons.angelica.rendering.celeritas.AngelicaRenderSectionManager;

@Mixin(value = AngelicaRenderSectionManager.class, remap = false)
public interface AngelicaRenderSectionManagerAccessor {

    @Invoker("markSpriteActive")
    public static void nvidium$markSpriteActive(TextureAtlasSprite sprite) {
        throw new UnsupportedOperationException();
    }
}
