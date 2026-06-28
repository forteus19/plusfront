package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.TimedStage;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefusalGameStage extends AbstractGameStage<DefusalGame, DefusalPlayerManager> implements TimedStage<DefusalGame, DefusalPlayerManager> {
	private final GameStageTimer playingTimer = new GameStageTimer(1, 50).warningTime(15);
	private final GameStageTimer finishedTimer = new GameStageTimer(0, 8).warningTime(5);

	boolean isFinished = false;

	@Override
	public void onStageStart(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		GameUtils.unfreezePlayers(context.playerDataHandler(), context.players());
	}

	@Override
	public void onStageEnd(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		DefusalGame game = context.game();
		DefusalPlayerManager playerManager = context.playerHandler();
		PlayerDataHandler<?> dataHandler = context.playerDataHandler();

		game.resetBombPlanted();
		game.setBombItem(null);
		playerManager.clearBombPlayer();

		for (UUID playerUuid : context.players()) {
			dataHandler.getPlayerData(playerUuid).clearPlayerDamage();
		}

		GameUtils.discardMatchEntities(context.serverLevel(), game, context.playerHandler());
	}

	@Override
	public void onSecond(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		DefusalGame game = context.game();
		Set<UUID> players = context.players();

		if (isFinished) {
			finishedTimer.update(players);
		} else if (!game.isBombPlanted()) {
			playingTimer.update(players);

			if (playingTimer.isDone()) {
				game.onRoundWin(players, true);
			}
		}
	}

	@Override
	public boolean canAdvanceStage(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		return isFinished && (finishedTimer.isDone() || context.playerHandler().getWinningTeam() != null);
	}

	@Override
	public @NotNull AbstractGameStage<DefusalGame, DefusalPlayerManager> createNextStage(@NotNull DefusalGame game) {
		return game.getPlayerManager().getWinningTeam() == null ? new DefusalWaitingStage() : new DefusalPostStage();
	}

	@Override
	public @NotNull GameStatus getStatus() {
		return GameStatus.GAME;
	}

	@Override
	public @Nullable GameStageTimer getStageTimer(@NotNull DefusalGame game) {
		if (isFinished) {
			return finishedTimer;
		} else {
			return !game.isBombPlanted() ? playingTimer : null;
		}
	}
}
