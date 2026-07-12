package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.stat.BFStat;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameCombatManager;
import com.boehmod.blockfront.game.GameUtils;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.vuis.plusfront.util.PFUtil;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameCombatManager.class)
public abstract class GameCombatManagerMixin {
	@Shadow
	@Final
	private @NotNull AbstractGame<?, ?, ?> game;

	@Definition(id = "GAME", field = "Lcom/boehmod/blockfront/game/GameStatus;GAME:Lcom/boehmod/blockfront/game/GameStatus;")
	@Expression("? == GAME")
	@ModifyExpressionValue(
		method = "method_2808",
		at = @At(
			value = "MIXINEXTRAS:EXPRESSION",
			ordinal = 0
		)
	)
	private boolean checkStatusForStats(boolean original) {
		return PFUtil.allowStatChanges(game);
	}

	@Redirect(
		method = "addKillFeedEntry",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/GameUtils;incrementPlayerStat(Lcom/boehmod/blockfront/common/BFAbstractManager;Lcom/boehmod/blockfront/game/AbstractGame;Ljava/util/UUID;Lcom/boehmod/blockfront/common/stat/BFStat;)V"
		)
	)
	private void checkKfeMethodStats(BFAbstractManager<?, ?, ?> manager, @NotNull AbstractGame<?, ?, ?> game, @NotNull UUID player, @NotNull BFStat stat) {
		if (PFUtil.allowStatChanges(game)) {
			GameUtils.incrementPlayerStat(manager, game, player, stat);
		}
	}
}
