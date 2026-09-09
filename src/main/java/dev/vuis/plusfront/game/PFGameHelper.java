package dev.vuis.plusfront.game;

import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.common.stat.BFStat;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import dev.vuis.plusfront.util.PFUtil;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PFGameHelper {
	private PFGameHelper() {
		throw new AssertionError();
	}

	public static boolean isPlayerUnavailable(@NotNull Player player) {
		return GameUtils.isPlayerUnavailable(player, PFUtil.getPlayerData(player));
	}

	public static int getNumUnavailable(
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull Set<UUID> players
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
}
