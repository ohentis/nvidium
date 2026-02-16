package me.cortex.nvidium.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.cortex.nvidium.Nvidium;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;

public class NvidiumConfig {

    // The options
    public boolean enable_temporal_coherence = true;
    public int max_geometry_memory = 2048;
    public boolean automatic_memory = true;

    public boolean async_bfs = false;

    public int region_keep_distance = 32;

    public boolean render_fog = true;
    public boolean use_sodium_vertex_format = false;
    public boolean cull_degenerate_triangles = true;
    public boolean use_nv_fragment_shader_barycentric = true;

    public TranslucencySortingLevel translucency_sorting_level = TranslucencySortingLevel.SODIUM;

    public StatisticsLoggingLevel statistics_level = StatisticsLoggingLevel.NONE;
    public Configuration config;


    public void init(File configFile) {
        config = new Configuration(configFile);
        config.load();
        load();
    }

    public void load() {
            enable_temporal_coherence = config.getBoolean("enable_temporal_coherence", Configuration.CATEGORY_GENERAL, enable_temporal_coherence, I18n.format("nvidium.options.enable_temporal_coherence.tooltip"));
            max_geometry_memory = config.getInt("max_geometry_memory", Configuration.CATEGORY_GENERAL, max_geometry_memory, 0, Integer.MAX_VALUE, I18n.format("nvidium.options.mb"));
            automatic_memory = config.getBoolean("automatic_memory", Configuration.CATEGORY_GENERAL, automatic_memory, I18n.format("nvidium.options.automatic_memory_limit.tooltip"));
            region_keep_distance = config.getInt("region_keep_distance", Configuration.CATEGORY_GENERAL, region_keep_distance, 0, Integer.MAX_VALUE, I18n.format("nvidium.options.region_keep_distance.tooltip"));
            render_fog = config.getBoolean("render_fog", Configuration.CATEGORY_GENERAL, render_fog, I18n.format("nvidium.options.render_fog.tooltip"));
            use_sodium_vertex_format = config.getBoolean("use_sodium_vertex_format", Configuration.CATEGORY_GENERAL, use_sodium_vertex_format, I18n.format("nvidium.options.use_sodium_vertex_format.tooltip"));
            cull_degenerate_triangles = config.getBoolean("cull_degenerate_triangles", Configuration.CATEGORY_GENERAL, cull_degenerate_triangles, I18n.format("nvidium.options.cull_degenerate_triangles.tooltip"));
            use_nv_fragment_shader_barycentric = config.getBoolean("use_nv_fragment_shader_barycentric", Configuration.CATEGORY_GENERAL, use_nv_fragment_shader_barycentric, I18n.format("nvidium.options.use_nv_fragment_shader_barycentric.tooltip"));
            translucency_sorting_level = TranslucencySortingLevel.valueOf(config.getString("translucency_sorting_level", Configuration.CATEGORY_GENERAL, translucency_sorting_level.name(), I18n.format("nvidium.options.translucency_sorting.tooltip")));
            statistics_level = StatisticsLoggingLevel.valueOf(config.getString("statistics_level", Configuration.CATEGORY_GENERAL, statistics_level.name(), I18n.format("nvidium.options.statistics_level.tooltip")));

        if(config.hasChanged()) {
            config.save();
        }
    }



    public void save() {
        config.getCategory(Configuration.CATEGORY_GENERAL).get("enable_temporal_coherence").set(enable_temporal_coherence);
        config.getCategory(Configuration.CATEGORY_GENERAL).get("max_geometry_memory").set(max_geometry_memory);
        config.getCategory(Configuration.CATEGORY_GENERAL).get("automatic_memory").set(automatic_memory);
        config.getCategory(Configuration.CATEGORY_GENERAL).get("region_keep_distance").set(region_keep_distance);
        config.getCategory(Configuration.CATEGORY_GENERAL).get("render_fog").set(render_fog);
        config.getCategory(Configuration.CATEGORY_GENERAL).get("use_sodium_vertex_format").set(use_sodium_vertex_format);
        config.getCategory(Configuration.CATEGORY_GENERAL).get("cull_degenerate_triangles").set(cull_degenerate_triangles);
        config.getCategory(Configuration.CATEGORY_GENERAL).get("use_nv_fragment_shader_barycentric").set(use_nv_fragment_shader_barycentric);
        config.getCategory(Configuration.CATEGORY_GENERAL).get("translucency_sorting_level").set(translucency_sorting_level.name());
        config.getCategory(Configuration.CATEGORY_GENERAL).get("statistics_level").set(statistics_level.name());
        if(config.hasChanged()){
            config.save();
        }
    }

}
