package me.cortex.nvidium.mixin.beddium;

import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.ventooth.beddium.modules.TerrainRendering.ArchaicRenderPassConfigurationBuilder;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.config.TranslucencySortingLevel;

@Mixin(value = ArchaicRenderPassConfigurationBuilder.class, remap = false)
public class MixinArchaicRenderPassConfigurationBuilder {

    @Redirect(
        method = "build",
        at = @At(
            value = "INVOKE",
            target = "Lorg/embeddedt/embeddium/impl/render/chunk/terrain/TerrainRenderPass$TerrainRenderPassBuilder;useTranslucencySorting(Z)Lorg/embeddedt/embeddium/impl/render/chunk/terrain/TerrainRenderPass$TerrainRenderPassBuilder;"))
    private static TerrainRenderPass.TerrainRenderPassBuilder useTranslucencySorting(
        TerrainRenderPass.TerrainRenderPassBuilder instance, boolean useTranslucencySorting) {
        instance.useTranslucencySorting(
            useTranslucencySorting && Nvidium.config.translucency_sorting_level == TranslucencySortingLevel.SODIUM);
        return instance;
    }
}
