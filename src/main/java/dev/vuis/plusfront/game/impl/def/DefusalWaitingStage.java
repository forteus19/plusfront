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
import com.boehmod.blockfront.registry.BFSounds;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

public final class DefusalWaitingStage extends AbstractGameStage<DefusalGame, DefusalPlayerManager> implements TimedStage<DefusalGame, DefusalPlayerManager> {
	private final GameStageTimer timer = new GameStageTimer(0, 5).warningTime(5);

	@Override
	public void onPlayerInit(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context, @NotNull TeamJoinType joinType, @NotNull ServerPlayer player) {
		GameTeam joinedTeam = context.playerHandler().getPlayerTeam(player.getUUID());

		if (joinedTeam != null && joinedTeam.numPlayers() <= 1) {
			player.setGameMode(GameType.ADVENTURE);
			GameUtils.freezePlayer(context.playerDataHandler(), player);

			timer.setSecondsRemaining(10);
		} else {
			player.setGameMode(GameType.SPECTATOR);
		}
	}

	@Override
	public void onStageStart(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		DefusalGame game = context.game();
		DefusalPlayerManager playerManager = context.playerHandler();
		PlayerDataHandler<?> dataHandler = context.playerDataHandler();
		Set<UUID> players = context.players();
		ServerLevel level = context.serverLevel();

		GameUtils.clearClientRagdolls(players);
		GameUtils.resetPlayers(level, players, dataHandler);
		playerManager.teleportPlayersToRandomSpawn(dataHandler);
		GameUtils.freezePlayers(dataHandler, players);

		for (UUID playerUuid : context.players()) {
			dataHandler.getPlayerData(playerUuid).clearPlayerDamage();
		}

		for (GameTeam team : playerManager.getTeams()) {
			GameUtils.giveClassLoadout(level, game, team);
		}

		GameUtils.playSound(
			players,
			BFSounds.MATCH_CLASSES_CHANGE.value(),
			SoundSource.NEUTRAL,
			1f, 1f
		);

		playerManager.refreshTerroristBomb();
	}

	@Override
	public void onSecond(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		DefusalPlayerManager playerManager = context.playerHandler();
		Set<UUID> players = context.players();

		GameTeam ctTeam = playerManager.getTeamByName(DefusalPlayerManager.CT_NAME);
		assert ctTeam != null;
		GameTeam tTeam = playerManager.getTeamByName(DefusalPlayerManager.T_NAME);
		assert tTeam != null;

		if (ctTeam.numPlayers() > 0 && tTeam.numPlayers() > 0) {
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
	public @NotNull GameStageTimer getStageTimer(@NotNull DefusalGame game) {
		return timer;
	}
}
