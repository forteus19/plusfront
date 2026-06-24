package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.BFVersionChecker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BFVersionChecker.class)
public abstract class BFVersionCheckerMixin {
	@Inject(
		method = "onUpdate",
		at = @At("HEAD"),
		cancellable = true
	)
	private void disableUpdate(CallbackInfo ci) {
		ci.cancel();
	}
}
