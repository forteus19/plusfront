package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.stat.BFStat;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameCombatManager;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameUtils;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameCombatManager.class)
public abstract class GameCombatManagerMixin {
	@Redirect(
		method = "addKillFeedEntry",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/GameUtils;incrementPlayerStat(Lcom/boehmod/blockfront/common/BFAbstractManager;Lcom/boehmod/blockfront/game/AbstractGame;Ljava/util/UUID;Lcom/boehmod/blockfront/common/stat/BFStat;)V"
		)
	)
	private void onlyCountStatDuringGame(BFAbstractManager<?, ?, ?> manager, @NotNull AbstractGame<?, ?, ?> game, @NotNull UUID player, @NotNull BFStat stat) {
		if (game.getStatus() == GameStatus.GAME) {
			GameUtils.incrementPlayerStat(manager, game, player, stat);
		}
	}
}
