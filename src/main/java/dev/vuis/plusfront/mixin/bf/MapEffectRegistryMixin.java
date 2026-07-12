package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.map.effect.AbstractMapEffect;
import com.boehmod.blockfront.map.effect.MapEffectRegistry;
import dev.vuis.plusfront.mapeffect.BrightnessMapEffect;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapEffectRegistry.class)
public abstract class MapEffectRegistryMixin {
	@Shadow
	public static void register(@NotNull String key, @NotNull Class<? extends AbstractMapEffect> mapEffectClass) {
	}

	@Inject(
		method = "<clinit>",
		at = @At("TAIL")
	)
	private static void registerCustom(CallbackInfo ci) {
		register(BrightnessMapEffect.ID, BrightnessMapEffect.class);
	}
}
