package me.cortex.nvidium.config;
import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;
import me.cortex.nvidium.sodiumCompat.NvidiumOptionFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;

import java.util.List;

public class NvidiumGuiConfig extends GuiConfig{
    public NvidiumGuiConfig(GuiScreen parentScreen) {
        super(parentScreen, getConfigElements(), Nvidium.MODID, false, false, GuiConfig.getAbridgedConfigPath(Nvidium.config.config.toString()));
    }
    private static List<IConfigElement> getConfigElements() {
        Nvidium.config.save();
        return new ConfigElement(Nvidium.config.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements();
    }
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if(Nvidium.config.config.hasChanged()) {
            Nvidium.config.load();
            Minecraft.getMinecraft().renderGlobal.loadRenderers();

            RenderSectionManager manager = Nvidium.isWithAngelica()? CeleritasWorldRenderer.getInstance().getRenderSectionManager() : com.ventooth.beddium.modules.TerrainRendering.CeleritasWorldRenderer.instance().getRenderSectionManager();
            if (manager != null) {

                NvidiumWorldRenderer pipeline = ((INvidiumWorldRendererGetter) manager)
                    .nvidium$getRenderer();
                    pipeline.reloadShaders();

            }
        }

    }
}
