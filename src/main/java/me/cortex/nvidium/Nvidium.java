package me.cortex.nvidium;

import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.common.Loader;
import net.minecraft.util.Util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;

import com.gtnewhorizons.angelica.AngelicaMod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import me.cortex.nvidium.config.NvidiumConfig;
import me.cortex.nvidium.config.TranslucencySortingLevel;
import me.cortex.nvidium.sodiumCompat.IrisCheck;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
@Mod(modid = Nvidium.MODID, version = Tags.VERSION, name = "Nvidium", acceptedMinecraftVersions = "[1.7.10]", guiFactory = "me.cortex.nvidium.config.NvidiumGuiFactory")
public class Nvidium {

    public static final String MODID = "nvidium";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static boolean IS_COMPATIBLE = true;
    public static boolean IS_ENABLED = false;
    public static boolean IS_DEBUG = System.getProperty("nvidium.isDebug", "false")
        .equals("TRUE");
    public static boolean SUPPORTS_PERSISTENT_SPARSE_ADDRESSABLE_BUFFER = true;
    public static boolean FORCE_DISABLE = false;


    public static NvidiumConfig config = new NvidiumConfig();

    public static void checkSystemIsCapable() {
        var cap = GL.getCapabilities();
        boolean supported = cap.GL_NV_mesh_shader && cap.GL_NV_uniform_buffer_unified_memory
            && cap.GL_NV_vertex_buffer_unified_memory
            && cap.GL_NV_representative_fragment_test
            && cap.GL_ARB_sparse_buffer
            && cap.GL_NV_bindless_multi_draw_indirect;
        IS_COMPATIBLE = supported;
        if (IS_COMPATIBLE) {
            LOGGER.info("All capabilities met");
        } else {
            LOGGER.warn("Not all requirements met, disabling nvidium");
        }
        if (IS_COMPATIBLE && Util.getOSType() == Util.EnumOS.LINUX) {
            LOGGER.warn(
                "Linux currently uses fallback terrain buffer due to driver inconsistencies, expect increase vram usage");
            SUPPORTS_PERSISTENT_SPARSE_ADDRESSABLE_BUFFER = false;
        }

        if (IS_COMPATIBLE) {
            LOGGER.info("Enabling Nvidium");
        }
        IS_ENABLED = IS_COMPATIBLE;
    }

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {

        config.init(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide()
            .isClient()) {
            checkSystemIsCapable();
        }
    }

    public static void updateNvidiumIsEnabled() {
        Nvidium.IS_ENABLED = (!Nvidium.FORCE_DISABLE) && Nvidium.IS_COMPATIBLE && IrisCheck.checkIrisShouldDisable();

        // Disable sodium translucency sorting since nvidium is doing it
        if (Nvidium.IS_ENABLED) {
            LOGGER.info("Ensuring translucency sorting is enabled");
            AngelicaMod.options().performance.translucencySorting = (config.translucency_sorting_level
                == TranslucencySortingLevel.SODIUM);
        }
    }

    private static Boolean WITH_ANGELICA;

    public static boolean isWithAngelica() {
        if(WITH_ANGELICA == null) {
            WITH_ANGELICA = Loader.isModLoaded("angelica");
        }
        return WITH_ANGELICA;
    }

    private static Boolean WITH_BEDDIUM;
    public static boolean isWithBeddium() {
        if(WITH_BEDDIUM == null) {
            WITH_BEDDIUM = Loader.isModLoaded("beddium");
        }
        return WITH_BEDDIUM;
    }
}
