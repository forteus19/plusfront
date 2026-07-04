package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.update.BFUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BFUpdater.class)
public abstract class BFUpdaterMixin {
	@Inject(
		method = "onUpdate",
		at = @At("HEAD"),
		cancellable = true
	)
	private void cancelUpdate(CallbackInfo ci) {
		ci.cancel();
	}
}
