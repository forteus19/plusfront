package dev.vuis.plusfront.util;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class PFUtil {
	private PFUtil() {
		throw new AssertionError();
	}

	public static BFAbstractManager<?, ?, ?> blockfrontManager() {
		BFAbstractManager<?, ?, ?> manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			throw new IllegalStateException("BlockFront manager is null!");
		}
		return manager;
	}

	public static PlayerDataHandler<?> playerDataHandler() {
		return blockfrontManager().getPlayerDataHandler();
	}

	public static BFAbstractPlayerData<?, ?, ?, ?> getPlayerData(Player player) {
		return playerDataHandler().getPlayerData(player);
	}

	public static boolean isPlayerUnavailable(Player player) {
		return GameUtils.isPlayerUnavailable(player, getPlayerData(player));
	}

	public static int getNumUnavailable(PlayerDataHandler<?> dataHandler, Set<UUID> players) {
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

	public static boolean isSameTeam(@Nullable GameTeam teamA, @Nullable GameTeam teamB) {
		return teamA != null && teamB != null && teamA.getName().equals(teamB.getName());
	}
}
