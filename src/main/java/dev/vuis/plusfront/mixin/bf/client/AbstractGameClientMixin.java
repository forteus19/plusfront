package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.GameTeam;
import dev.vuis.plusfront.game.impl.def.DefusalPlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractGameClient.class)
public abstract class AbstractGameClientMixin {
	@Redirect(
		method = "method_2696",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/GameTeam;getName()Ljava/lang/String;",
			ordinal = 0
		)
	)
	private String fixTeamNameForUniform(GameTeam instance) {
		String originalName = instance.getName();

		return switch (originalName) {
			case DefusalPlayerManager.CT_NAME -> BFStats.ALLIES_TEAM_NAME;
			case DefusalPlayerManager.T_NAME -> BFStats.AXIS_TEAM_NAME;
			default -> originalName;
		};
	}
}
