package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.cloud.BFFeatureFlags;
import dev.vuis.plusfront.PlusFront;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BFFeatureFlags.class)
public abstract class BFFeatureFlagsMixin {
	@Redirect(
		method = "updateFeatureFlags",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/cloud/BFFeatureFlags;isEnabled(Ljava/lang/String;)Z"
		)
	)
	private boolean overrideEnabledCheck(BFFeatureFlags instance, String key) {
		if (PlusFront.FEATURE_FLAGS.containsKey(key)) {
			return PlusFront.FEATURE_FLAGS.get(key);
		} else {
			return instance.isEnabled(key);
		}
	}
}
