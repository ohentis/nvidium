package me.cortex.nvidium;

import net.minecraft.util.Util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import me.cortex.nvidium.config.NvidiumConfig;
import me.cortex.nvidium.config.TranslucencySortingLevel;
import me.cortex.nvidium.sodiumCompat.AngelicaCompat;
import me.cortex.nvidium.sodiumCompat.BeddiumCompat;
import me.cortex.nvidium.sodiumCompat.ISodiumCalls;
import me.cortex.nvidium.sodiumCompat.IrisCheck;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;
import org.lwjgl.opengl.GL11;

@Lwjgl3Aware
@Mod(
    modid = Nvidium.MODID,
    version = Tags.VERSION,
    name = "Nvidium",
    acceptedMinecraftVersions = "[1.7.10]",
    guiFactory = "me.cortex.nvidium.config.NvidiumGuiFactory")
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
        boolean supported = ((cap.GL_NV_mesh_shader && cap.GL_NV_bindless_multi_draw_indirect)
            || cap.GL_EXT_mesh_shader);
        IS_COMPATIBLE = supported;
        if (IS_COMPATIBLE) {
            LOGGER.info("All capabilities met");
        } else {
            LOGGER.warn("Not all requirements met, disabling nvidium");
        }
        if(cap.GL_EXT_mesh_shader) {
            LOGGER.info("We are using GL_EXT_mesh_shader");
        }
        SUPPORTS_PERSISTENT_SPARSE_ADDRESSABLE_BUFFER = cap.GL_ARB_sparse_buffer;
        String version = GL11.glGetString(GL11.GL_VERSION);
        boolean isMesa = version != null && (version.toLowerCase().contains("mesa"));
        if (IS_COMPATIBLE && isMesa) {
            LOGGER.warn(
                "Mesa currently uses fallback terrain buffer due to driver inconsistencies, expect increase vram usage");
            SUPPORTS_PERSISTENT_SPARSE_ADDRESSABLE_BUFFER = false;
        }

        if (IS_COMPATIBLE) {
            LOGGER.info("Enabling Nvidium");
        }
        IS_ENABLED = IS_COMPATIBLE;
    }

    public static ISodiumCalls Compat;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        if (isWithAngelica()) {
            Compat = new AngelicaCompat();
        } else if (isWithBeddium()) {
            Compat = new BeddiumCompat();
        }

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
            Compat.setTranslucencySorting(config.translucency_sorting_level == TranslucencySortingLevel.SODIUM);
        }
    }

    private static Boolean WITH_ANGELICA;

    public static boolean isWithAngelica() {
        if (WITH_ANGELICA == null) {
            WITH_ANGELICA = Loader.isModLoaded("angelica");
        }
        return WITH_ANGELICA;
    }

    private static Boolean WITH_BEDDIUM;

    public static boolean isWithBeddium() {
        if (WITH_BEDDIUM == null) {
            WITH_BEDDIUM = Loader.isModLoaded("beddium");
        }
        return WITH_BEDDIUM;
    }
}
