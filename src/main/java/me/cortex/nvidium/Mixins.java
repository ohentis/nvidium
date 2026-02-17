package me.cortex.nvidium;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

import io.github.legacymoddingmc.unimixins.all.repackage.common.abstraction.ComparableVersion;

public enum Mixins implements IMixins {

    MINECRAFT_MIXINS(new MixinBuilder()
        .addClientMixins(
            "minecraft.EntityRendererAccessor",
            "minecraft.MixinEntityRenderer",
            "minecraft.MinecraftAccessor")
        .setPhase(Phase.EARLY)),

    ANGELICA_MIXINS(new MixinBuilder()
        .addClientMixins(
            "angelica.AngelicaRenderSectionManagerAccessor",
            "angelica.CameraAccessor",
            "angelica.MixinAngelicaRenderSectionManager",
            "angelica.MixinCeleritasWorldRenderer",
            "angelica.MixinChunkBuilderMeshingTask",
            "angelica.MixinOptionFlag",
            "angelica.MixinSodiumOptionsGUI")
        .setPhase(Phase.EARLY)
        .addRequiredMod(TargetedMod.ANGELICA)),

    CELERITAS_MIXINS(new MixinBuilder()
        .addClientMixins(
            "celeritas.CompactChunkVertexAccessor",
            "celeritas.MixinChunkBuilder",
            "celeritas.MixinChunkBuildOutput",
            "celeritas.MixinChunkJobQueue",
            "celeritas.MixinRenderRegionManager",
            "celeritas.MixinRenderSection",
            "celeritas.MixinRenderSectionManager",
            "celeritas.MixinSimpleWorldRenderer",
            "celeritas.RenderListManagerAccessor",
            "celeritas.RenderSectionManagerAccessor")
        .setPhase(Phase.EARLY)),

    BEDDIUM_MIXINS(new MixinBuilder()
        .addClientMixins(
            "beddium.ArchaicRenderSectionManagerAccessor",
            "beddium.MixinArchaicRenderPassConfigurationBuilder",
            "beddium.MixinArchaicRenderSectionManager",
            "beddium.MixinSimpleChunkBuilderMeshingTask")
        .setPhase(Phase.EARLY)
        .addRequiredMod(TargetedMod.BEDDIUM)),;

    public enum TargetedMod implements ITargetMod {

        ANGELICA("com.gtnewhorizons.angelica.loading.AngelicaTweaker", "angelica"),
        BEDDIUM("com.ventooth.beddium.asm.CoreLoadingPlugin", "beddium");

        private final TargetModBuilder builder;

        TargetedMod(TargetModBuilder builder) {
            this.builder = builder;
        }

        TargetedMod(String modId) {
            this(null, modId, null);
        }

        TargetedMod(String coreModClass, String modId) {
            this(coreModClass, modId, null);
        }

        TargetedMod(String coreModClass, String modId, String targetClass) {
            this.builder = new TargetModBuilder().setCoreModClass(coreModClass)
                .setModId(modId)
                .setTargetClass(targetClass);
        }

        @Nonnull
        @Override
        public TargetModBuilder getBuilder() {
            return builder;
        }

        private static boolean isVersionLessThan(String version, String target) {
            return new ComparableVersion(version).compareTo(new ComparableVersion(target)) < 0;
        }
    }

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
