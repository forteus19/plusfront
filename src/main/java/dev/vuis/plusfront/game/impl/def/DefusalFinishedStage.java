package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.TimedStage;
import org.jetbrains.annotations.NotNull;

public final class DefusalFinishedStage extends AbstractGameStage<DefusalGame, DefusalPlayerManager> implements TimedStage<DefusalGame, DefusalPlayerManager> {
	private final GameStageTimer timer = new GameStageTimer(0, 8);

	@Override
	public void onStageEnd(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		DefusalGame game = context.game();
		DefusalPlayerManager playerManager = context.playerHandler();

		GameUtils.discardMatchEntities(context.serverLevel(), game, playerManager);

		game.onRoundReset();
		playerManager.onRoundReset();
	}

	@Override
	public void onSecond(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		timer.update(context.players());
	}

	@Override
	public boolean canAdvanceStage(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		return timer.isDone();
	}

	@Override
	public @NotNull AbstractGameStage<DefusalGame, DefusalPlayerManager> createNextStage(@NotNull DefusalGame game) {
		return new DefusalWaitingStage();
	}

	@Override
	public @NotNull GameStatus getStatus() {
		return GameStatus.GAME;
	}

	@Override
	public @NotNull GameStageTimer getStageTimer(@NotNull DefusalGame game) {
		return timer;
	}
}
