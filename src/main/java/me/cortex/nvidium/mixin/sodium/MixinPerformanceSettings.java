package me.cortex.nvidium.mixin.sodium;

// @Mixin(value = SodiumGameOptions.DebugSettings.class, remap = false)
// public class MixinPerformanceSettings {
// @Inject(method =
// "getSortBehavior()Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/SortBehavior;", at = @At(value
// = "HEAD"), cancellable = true)
// public void getSortBehavior(CallbackInfoReturnable<SortBehavior> cir) {
// if (Nvidium.IS_ENABLED && Nvidium.config.translucency_sorting_level != TranslucencySortingLevel.SODIUM) {
// cir.setReturnValue(SortBehavior.OFF);
// cir.cancel();
// }
// }
// }
