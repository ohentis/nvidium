package me.cortex.nvidium.sodiumCompat;

import net.irisshaders.iris.api.v0.IrisApi;

public class IrisCheck {

    public static final boolean IRIS_LOADED = true;

    private static boolean checkIrisShaders() {
        return IrisApi.getInstance()
            .isShaderPackInUse();
    }

    public static boolean checkIrisShouldDisable() {
        return !(IRIS_LOADED && checkIrisShaders());
    }
}
