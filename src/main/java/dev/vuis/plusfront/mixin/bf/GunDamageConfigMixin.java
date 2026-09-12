package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.gun.GunDamageConfig;
import dev.vuis.plusfront.gun.PFGunDamageConfigs;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GunDamageConfig.class)
public abstract class GunDamageConfigMixin {
	@Shadow
	public static void register(@NotNull ResourceLocation rl, @NotNull GunDamageConfig config) {
	}

	@Inject(
		method = "<clinit>",
		at = @At("TAIL")
	)
	private static void registerCustom(CallbackInfo ci) {
		register(PFGunDamageConfigs.ONE_TAP, new GunDamageConfig(20f, 20f));
	}
}
