package me.cortex.nvidium.mixin.sodium;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;

import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.config.ConfigGuiBuilder;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;
import me.cortex.nvidium.sodiumCompat.NvidiumOptionFlags;
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.OptionFlag;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;

@Mixin(value = SodiumOptionsGUI.class, remap = false)
public class MixinSodiumOptionsGUI {

    @Shadow
    @Final
    private List<OptionPage> pages;

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
            ordinal = 3,
            shift = At.Shift.AFTER))
    private void nvidium$addNvidiumOptions(GuiScreen prevScreen, CallbackInfo ci) {
        ConfigGuiBuilder.addNvidiumGui(pages);
    }

    @Inject(method = "applyChanges", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void nvidium$applyShaderReload(CallbackInfo ci, HashSet<OptionStorage<?>> dirtyStorages,
        EnumSet<OptionFlag> flags) {

        CeleritasWorldRenderer swr = CeleritasWorldRenderer.getInstance();
        if (swr != null) {

            NvidiumWorldRenderer pipeline = ((INvidiumWorldRendererGetter) swr.getRenderSectionManager())
                .nvidium$getRenderer();
            if (pipeline != null && flags.contains(NvidiumOptionFlags.REQUIRES_SHADER_RELOAD)) {
                pipeline.reloadShaders();
            }
        }

    }
}
