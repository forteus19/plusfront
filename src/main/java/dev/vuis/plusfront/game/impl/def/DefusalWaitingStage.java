package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.TeamJoinType;
import com.boehmod.blockfront.game.TimedStage;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefusalWaitingStage extends AbstractGameStage<DefusalGame, DefusalPlayerManager> implements TimedStage<DefusalGame, DefusalPlayerManager> {
	private final GameStageTimer timer = new GameStageTimer(0, 5).warningTime(5);

	@Override
	public void onPlayerInit(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context, @NotNull TeamJoinType joinTyoe, @NotNull ServerPlayer player) {
		player.setGameMode(GameType.SPECTATOR);
	}

	@Override
	public void onStageStart(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		DefusalGame game = context.game();
		DefusalPlayerManager playerManager = context.playerHandler();
		PlayerDataHandler<?> dataHandler = context.playerDataHandler();
		Set<UUID> players = context.players();
		ServerLevel level = context.serverLevel();

		GameUtils.clearClientRagdolls(players);
		GameUtils.freezePlayers(dataHandler, players);
		playerManager.teleportPlayersToRandomSpawn(dataHandler);
		GameUtils.initPlayersForGame(level, players, dataHandler);

		for (GameTeam team : playerManager.getTeams()) {
			GameUtils.giveClassLoadout(level, game, team);

			if (team.isAllies()) {
				playerManager.giveDefuseKits(team.getPlayers());
			}
		}

		playerManager.refreshTerroristBomb();
	}

	@Override
	public void onSecond(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		DefusalGame game = context.game();
		Set<UUID> players = context.players();

		if (game.hasMinimumPlayers(players)) {
			timer.update(players);
		} else {
			timer.restart();
		}
	}

	@Override
	public boolean canAdvanceStage(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		return !timer.isRunning();
	}

	@Override
	public @NotNull AbstractGameStage<DefusalGame, DefusalPlayerManager> createNextStage(@NotNull DefusalGame game) {
		return new DefusalGameStage();
	}

	@Override
	public @NotNull GameStatus getStatus() {
		return GameStatus.GAME;
	}

	@Override
	public @Nullable GameStageTimer getStageTimer(@NotNull DefusalGame game) {
		return timer;
	}
}
