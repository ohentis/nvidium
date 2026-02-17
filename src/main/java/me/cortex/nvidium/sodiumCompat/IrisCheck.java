package me.cortex.nvidium.sodiumCompat;

import net.irisshaders.iris.api.v0.IrisApi;

import me.cortex.nvidium.Nvidium;

public class IrisCheck {

    public static final boolean IRIS_LOADED = true;

    private static boolean checkIrisShaders() {
        return IrisApi.getInstance()
            .isShaderPackInUse();
    }

    public static boolean checkIrisShouldDisable() {
        if (Nvidium.isWithAngelica()) {
            return !(IRIS_LOADED && checkIrisShaders());
        }
        return true;
    }
}
