package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.GameTeam;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.vuis.plusfront.game.impl.def.DefusalPlayerManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameTeam.class)
public abstract class GameTeamMixin {
	@Shadow
	@Final
	private @NotNull String name;

	@ModifyReturnValue(
		method = "isAllies",
		at = @At("TAIL")
	)
	private boolean includeCounterTerrorists(boolean original) {
		return original || name.equals(DefusalPlayerManager.CT_NAME);
	}

	@ModifyReturnValue(
		method = "isAxis",
		at = @At("TAIL")
	)
	private boolean includeTerrorists(boolean original) {
		return original || name.equals(DefusalPlayerManager.T_NAME);
	}
}
