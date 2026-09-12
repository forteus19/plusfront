package dev.vuis.plusfront.game.impl.oitc;

import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.TeamJoinType;
import com.boehmod.blockfront.game.TimedStage;
import dev.vuis.plusfront.game.PFGameHelper;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChamberGameStage extends AbstractGameStage<ChamberGame, ChamberPlayerManager> implements TimedStage<ChamberGame, ChamberPlayerManager> {
	private final GameStageTimer timer = new GameStageTimer(10, 0).warningTime(20);

	@Override
	public void onStageStart(@NotNull GameStageContext<ChamberGame, ChamberPlayerManager> context) {
		GameUtils.queueMusic(context.players(), PFGameHelper.genericStartMusic());
	}

	@Override
	public void onSecond(@NotNull GameStageContext<ChamberGame, ChamberPlayerManager> context) {
		timer.update(context.players());
	}

	@Override
	public boolean canAdvanceStage(@NotNull GameStageContext<ChamberGame, ChamberPlayerManager> context) {
		return context.playerHandler().getWinningTeam(context.players(), timer) != null;
	}

	@Override
	public @NotNull AbstractGameStage<ChamberGame, ChamberPlayerManager> createNextStage(@NotNull ChamberGame game) {
		return new ChamberPostStage();
	}

	@Override
	public @NotNull GameStatus getStatus() {
		return GameStatus.GAME;
	}

	@Override
	public void onPlayerInit(@NotNull GameStageContext<ChamberGame, ChamberPlayerManager> context, @NotNull TeamJoinType joinType, @NotNull ServerPlayer player) {
		GameUtils.queueMusic(player.getUUID(), PFGameHelper.genericStartMusic());
	}

	@Override
	public @Nullable GameStageTimer getStageTimer(@NotNull ChamberGame game) {
		return timer;
	}
}
