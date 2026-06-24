package dev.vuis.plusfront.mixin.bf;

import com.boehmod.bflib.cloud.common.mm.SearchGame;
import com.boehmod.blockfront.game.GameType;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
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
		new GameType(
			GameType.Category.VERSUS,
			"pf.gamemode.def",
			"def",
			SearchGame.DEFUSAL,
			DefusalGame.class
		)
			.experimental()
			.hidden();
	}
}
