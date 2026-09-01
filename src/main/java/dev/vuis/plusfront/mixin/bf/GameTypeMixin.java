package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.GameType;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.game.PFGameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameType.class)
public abstract class GameTypeMixin {
	@Inject(
		method = "<clinit>",
		at = @At("TAIL")
	)
	private static void registerCustom(CallbackInfo ci) {
		PFGameType.init();
		PlusFront.LOGGER.info("Registered custom game types!");
	}
}
