package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.map.effect.AbstractMapEffect;
import com.boehmod.blockfront.map.effect.MapEffectCodecs;
import com.mojang.serialization.MapCodec;
import dev.vuis.plusfront.mapeffect.BrightnessMapEffect;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapEffectCodecs.class)
public abstract class MapEffectCodecsMixin {
	@Shadow
	@Final
	private static @NotNull Map<String, MapCodec<? extends AbstractMapEffect>> CODECS;

	@Shadow
	private static @NotNull <T extends AbstractMapEffect> MapCodec<T> wrapCodec(@NotNull MapCodec<T> codec) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	@Inject(
		method = "<clinit>",
		at = @At("TAIL")
	)
	private static void registerCustom(CallbackInfo ci) {
		CODECS.put(BrightnessMapEffect.ID, wrapCodec(BrightnessMapEffect.CODEC));
	}
}
