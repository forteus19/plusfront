package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.gun.GunConfigRegistry;
import dev.vuis.plusfront.gun.CustomGunConfigs;
import dev.vuis.plusfront.registry.PFItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GunConfigRegistry.class)
public abstract class GunConfigRegistryMixin {
	@Inject(
		method = "<clinit>",
		at = @At("TAIL")
	)
	private static void registerCustom(CallbackInfo ci) {
		CustomGunConfigs.load(PFItems.GUN_COWBOY_REVOLVER);
	}
}
