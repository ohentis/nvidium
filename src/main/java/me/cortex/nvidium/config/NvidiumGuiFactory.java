package me.cortex.nvidium.config;

import java.util.Set;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.client.IModGuiFactory;

public class NvidiumGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public Class<? extends cpw.mods.fml.client.config.GuiConfig> mainConfigGuiClass() {
        return NvidiumGuiConfig.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
