package dev.vuis.plusfront.util;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.net.payload.PFFeatureFlagsPayload;
import dev.vuis.plusfront.world.PFSavedData;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class PFUtil {
	private PFUtil() {
		throw new AssertionError();
	}

	public static TriState triState(boolean value) {
		return value ? TriState.TRUE : TriState.FALSE;
	}

	public static BFAbstractManager<?, ?, ?> blockfrontManager() {
		BFAbstractManager<?, ?, ?> manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			throw new IllegalStateException("BlockFront manager is null!");
		}
		return manager;
	}

	public static @Nullable AbstractGame<?, ?, ?> playerGame(Player player) {
		return blockfrontManager().getPlayerGame(player.getUUID());
	}

	public static PlayerDataHandler<?> playerDataHandler() {
		return blockfrontManager().getPlayerDataHandler();
	}

	public static BFAbstractPlayerData<?, ?, ?, ?> getPlayerData(Player player) {
		return playerDataHandler().getPlayerData(player);
	}

	public static void forceJoinGame(BFAbstractManager<?, ?, ?> manager, ServerPlayer player, AbstractGame<?, ?, ?> targetGame) {
		AbstractGame<?, ?, ?> currentGame = manager.getPlayerGame(player.getUUID());

		if (currentGame == targetGame) {
			return;
		}

		if (currentGame != null) {
			currentGame.getPlayerManager().removePlayer(manager, player.serverLevel(), player);
		}

		manager.assignPlayerToGame(player.serverLevel(), player, targetGame);
	}

	public static SuggestionProvider<CommandSourceStack> suggestGames() {
		return (context, builder) -> SharedSuggestionProvider.suggest(blockfrontManager().getGames().keySet(), builder);
	}

	public static @Nullable GameType getGameType(AbstractGame<?, ?, ?> game) {
		return GameType.getByName(game.getType());
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
