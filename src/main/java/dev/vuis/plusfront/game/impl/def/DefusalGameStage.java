package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.TimedStage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DefusalGameStage extends AbstractGameStage<DefusalGame, DefusalPlayerManager> implements TimedStage<DefusalGame, DefusalPlayerManager> {
	private final GameStageTimer timer = new GameStageTimer(1, 50).warningTime(15);

	@Override
	public void onStageStart(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		GameUtils.unfreezePlayers(context.playerDataHandler(), context.players());
	}

	@Override
	public void onStageEnd(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		DefusalGame game = context.game();

		if (!game.finishedRound()) {
			game.onRoundWin(context.players(), true, false);
		}
	}

	@Override
	public void onSecond(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		GameStageTimer currentTimer = getStageTimer(context.game());
		if (currentTimer != null) {
			currentTimer.update(context.players());
		}
	}

	@Override
	public boolean canAdvanceStage(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		return timer.isDone() && !context.game().isBombPlanted();
	}

	@Override
	public @NotNull AbstractGameStage<DefusalGame, DefusalPlayerManager> createNextStage(@NotNull DefusalGame game) {
		return game.getPlayerManager().getWinningTeam() == null ? new DefusalFinishedStage() : new DefusalPostStage();
	}

	@Override
	public @NotNull GameStatus getStatus() {
		return GameStatus.GAME;
	}

	@Override
	public @Nullable GameStageTimer getStageTimer(@NotNull DefusalGame game) {
		return game.isBombPlanted() ? null : timer;
	}
}
