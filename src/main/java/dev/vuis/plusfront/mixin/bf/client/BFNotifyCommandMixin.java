package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.common.BFNotifyCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BFNotifyCommand.class)
public abstract class BFNotifyCommandMixin {
	@Redirect(
		method = "register",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/util/EnvironmentUtils;isProduction()Z",
			ordinal = 0
		)
	)
	private static boolean overrideProductionCheck() {
		return false;
	}
}
