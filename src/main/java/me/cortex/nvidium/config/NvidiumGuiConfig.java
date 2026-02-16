package me.cortex.nvidium.config;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import me.cortex.nvidium.Nvidium;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

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
        Nvidium.config.load();
    }
}
