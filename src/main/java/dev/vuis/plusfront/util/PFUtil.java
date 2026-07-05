package dev.vuis.plusfront.util;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.net.payload.PFFeatureFlagsPayload;
import dev.vuis.plusfront.world.PFSavedData;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
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

	public static void updateFeatureFlags(Map<String, Boolean> featureFlags) {
		blockfrontManager()
			.getConnectionManager()
			.getRequester()
			.getFeatureFlagManager()
			.setFeatureFlags(featureFlags);
	}

	public static @Nullable Object2BooleanMap<String> getFeatureFlags(MinecraftServer server) {
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			PlusFront.LOGGER.error("Overworld not found while getting feature flags");
			return null;
		}

		return PFSavedData.get(overworld).getFeatureFlags();
	}

	public static Optional<Boolean> getFeatureFlag(MinecraftServer server, String featureFlag) {
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			PlusFront.LOGGER.error("Overworld not found while getting feature flag");
			return Optional.empty();
		}

		var featureFlags = PFSavedData.get(overworld).getFeatureFlags();

		if (!featureFlags.containsKey(featureFlag)) {
			return Optional.empty();
		}

		return Optional.of(featureFlags.getBoolean(featureFlag));
	}

	public static boolean setFeatureFlag(MinecraftServer server, String featureFlag, boolean value) {
		PlusFront.LOGGER.info("Setting feature flag \"{}\" to {}", featureFlag, value);

		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			PlusFront.LOGGER.error("Overworld not found while setting feature flag");
			return false;
		}

		PFSavedData savedData = PFSavedData.get(overworld);

		var featureFlags = savedData.getFeatureFlags();

		featureFlags.put(featureFlag, value);
		savedData.setDirty();

		updateFeatureFlags(featureFlags);

		PacketDistributor.sendToAllPlayers(
			new PFFeatureFlagsPayload(featureFlags)
		);

		return true;
	}
}
