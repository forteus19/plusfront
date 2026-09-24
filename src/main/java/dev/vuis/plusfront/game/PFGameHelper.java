package dev.vuis.plusfront.game;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.client.sound.BFMusicType;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.common.stat.BFStat;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameMusic;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.util.CommandUtils;
import com.boehmod.blockfront.util.math.BFPose;
import dev.vuis.plusfront.util.PFUtil;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.vuis.plusfront.util.AssetCommandUtil.executor;
import static dev.vuis.plusfront.util.AssetCommandUtil.executorPlayers;

public final class PFGameHelper {
	private PFGameHelper() {
		throw new AssertionError();
	}

	public static void forUuids(@NotNull Set<UUID> players, @NotNull Consumer<ServerPlayer> consumer) {
		for (UUID uuid : players) {
			ServerPlayer player = GameUtils.getPlayerByUUID(uuid);
			if (player != null) {
				consumer.accept(player);
			}
		}
	}

	public static void forUuids(@NotNull Set<UUID> players, @NotNull BiConsumer<UUID, ServerPlayer> consumer) {
		for (UUID uuid : players) {
			ServerPlayer player = GameUtils.getPlayerByUUID(uuid);
			if (player != null) {
				consumer.accept(uuid, player);
			}
		}
	}

	public static boolean isPlayerUnavailable(@NotNull Player player) {
		return GameUtils.isPlayerUnavailable(player, PFUtil.getPlayerData(player));
	}

	public static int getNumUnavailable(
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull Iterable<UUID> players
	) {
		int count = 0;

		for (UUID playerUuid : players) {
			ServerPlayer player = GameUtils.getPlayerByUUID(playerUuid);
			if (player == null) {
				continue;
			}

			if (GameUtils.isPlayerUnavailable(player, dataHandler.getPlayerData(player))) {
				count++;
			}
		}

		return count;
	}

	public static boolean isSameTeam(
		@Nullable GameTeam team1,
		@Nullable GameTeam team2
	) {
		return team1 != null && team2 != null && team1.getName().equals(team2.getName());
	}

	public static boolean isSameTeam(
		@NotNull AbstractGamePlayerManager<?> playerManager,
		@NotNull UUID playerUuid1,
		@NotNull UUID playerUuid2
	) {
		return isSameTeam(
			playerManager.getPlayerTeam(playerUuid1),
			playerManager.getPlayerTeam(playerUuid2)
		);
	}

	public static void incrementTeamStat(
		@NotNull GameTeam team,
		@NotNull BFStat stat
	) {
		team.putStatInt(stat, team.getStatInt(stat, 0) + 1);
	}

	public static @NotNull GameMusic genericStartMusic() {
		return GameMusic.create().method_1540(BFMusicType.START_GENERIC).method_1536(5);
	}

	public static void addTeamSpawnCommands(
		@NotNull AssetCommandBuilder command,
		@NotNull AbstractGame<?, ?, ?> game
	) {
		command.subCommand("spawn", new AssetCommandBuilder()
			.subCommand("add", executorPlayers(new String[]{"team"}, (context, source, args) -> {
				String teamName = args[0];

				GameTeam team = game.getPlayerManager().getTeamByName(teamName);
				if (team == null) {
					CommandUtils.sendBfa(source, Component.literal("Team " + teamName + " was not found!"));
					return;
				}

				team.addPlayerSpawn(new BFPose(source));

				CommandUtils.sendBfa(source, Component.literal(teamName + " team spawn added. (" + team.getPlayerSpawns().size() + ")"));
			}))
			.subCommand("clear", executor((context, source, args) -> {
				String teamName = args[0];

				GameTeam team = game.getPlayerManager().getTeamByName(teamName);
				if (team == null) {
					CommandUtils.sendBfa(source, Component.literal("Team " + teamName + " was not found!"));
					return;
				}

				team.clearPlayerSpawns();

				CommandUtils.sendBfa(source, Component.literal(teamName + " team's spawns cleared."));
			})));
	}
}
