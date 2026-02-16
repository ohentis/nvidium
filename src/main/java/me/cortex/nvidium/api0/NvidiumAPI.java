package me.cortex.nvidium.api0;

import me.cortex.nvidium.NvidiumWorldRenderer;
import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;
import org.joml.Matrix4fc;

import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class NvidiumAPI {

    private final String modName;
    private List<NvidiumWorldRenderer> renderers;

    public NvidiumAPI(String modName) {
        this.modName = modName;
        List<NvidiumWorldRenderer> renderers = new LinkedList<NvidiumWorldRenderer>();
        if (Nvidium.isWithAngelica()) {
            renderers.add(((INvidiumWorldRendererGetter) CeleritasWorldRenderer.getInstance()).nvidium$getRenderer());
        }
        if (Nvidium.isWithAngelica()) {
            renderers.add(((INvidiumWorldRendererGetter) com.ventooth.beddium.modules.TerrainRendering.CeleritasWorldRenderer.instance()).nvidium$getRenderer());
        }
    }


    /***
     * Forces a render section to not render, guarantees the section will stay hidden until it is marked as visible
     *
     * @param x sectionX pos
     * @param y sectionY pos
     * @param z sectionZ pos
     */
    public void hideSection(int x, int y, int z) {
        if (Nvidium.IS_ENABLED) {
            for(NvidiumWorldRenderer renderer: renderers) {
                if(renderer != null) {
                    renderer.getSectionManager()
                        .setHideBit(x, y, z, true);
                }
            }
        }
    }

    /***
     * Unhides a render section if it was previously hidden
     *
     * @param x sectionX pos
     * @param y sectionY pos
     * @param z sectionZ pos
     */
    public void showSection(int x, int y, int z) {
        if (Nvidium.IS_ENABLED) {
            for(NvidiumWorldRenderer renderer: renderers) {
                if (renderer != null) {
                    renderer.getSectionManager()
                        .setHideBit(x, y, z, false);
                }
            }
        }
    }

    /***
     * Assigns a specified region to the supplied transformation id
     *
     * @param id id to set the region too (all regions have the default id of 0)
     * @param x  region X pos
     * @param y  region Y pos
     * @param z  region Z pos
     */
    public void setRegionTransformId(int id, int x, int y, int z) {
        if (Nvidium.IS_ENABLED) {
            for(NvidiumWorldRenderer renderer : renderers) {
                if (renderer != null) {
                    renderer.getSectionManager()
                        .getRegionManager()
                        .setRegionTransformId(x, y, z, id);
                }
            }
        }
    }

    /***
     * Sets the transform for the supplied id
     *
     * @param id        The id to set the transform of
     * @param transform The transform to set it too
     */
    public void setTransformation(int id, Matrix4fc transform) {
        if (Nvidium.IS_ENABLED) {
            for(NvidiumWorldRenderer renderer : renderers) {
                if (renderer != null) {
                    renderer.setTransformation(id, transform);
                }
            }
        }
    }

    /***
     * Sets the origin point of the transformation id, this is in chunk coordinates.
     *
     * @param id The id to set the origin of
     * @param x  Chunk coord x
     * @param y  Chunk coord y
     * @param z  Chunk coord z
     */
    public void setOrigin(int id, int x, int y, int z) {
        if (Nvidium.IS_ENABLED) {
            for(NvidiumWorldRenderer renderer : renderers) {
                if (renderer != null) {
                    renderer.setOrigin(id, x, y, z);
                }
            }
        }
    }

}
