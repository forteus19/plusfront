package dev.vuis.plusfront.compat.voicechat;

import com.boehmod.blockfront.assets.AssetStore;
import com.boehmod.blockfront.assets.impl.GameAsset;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.tag.IAllowsRespawning;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartingEvent;
import dev.vuis.plusfront.PFTemp;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.util.PFUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@ForgeVoicechatPlugin
public final class PFVoicechat implements VoicechatPlugin {
	private static @Nullable PFVoicechat instance = null;

	private @Nullable VoicechatServerApi serverApi = null;

	// only games that do *not* implement IAllowsRespawning will get dead groups
	// games that *do* implement IAllowsRespawning will simply mute players that are in dead state
	private final Map<UUID, Group> deadGroups = new Object2ObjectOpenHashMap<>();

	public PFVoicechat() {
		instance = this;
		PFTemp.voicechatLoaded = true;

		PlusFront.LOGGER.info("Voicechat plugin initialized!");
	}

	/**
	 * Returns the current voicechat plugin instance. Check {@link PFTemp#voicechatLoaded} before calling.
	 *
	 * @return the voicechat plugin instance
	 *
	 * @see PFTemp#voicechatLoaded
	 */
	public static PFVoicechat getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Voicechat not loaded");
		}

		return instance;
	}

	@Override
	public String getPluginId() {
		return PlusFront.MOD_ID;
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(
			VoicechatServerStartingEvent.class,
			event -> serverApi = event.getVoicechat()
		);
		registration.registerEvent(
			MicrophonePacketEvent.class,
			PFVoicechat::onMicrophonePacket
		);
	}

	private static void onMicrophonePacket(MicrophonePacketEvent event) {
		BFAbstractManager<?, ?, ?> manager = PFUtil.blockfrontManager();

		VoicechatConnection connection = event.getSenderConnection();
		if (connection == null) {
			return;
		}

		if (!(connection.getPlayer().getPlayer() instanceof ServerPlayer player)) {
			return;
		}

		AbstractGame<?, ?, ?> game = manager.getPlayerGame(player.getUUID());
		if (!(game instanceof IAllowsRespawning)) {
			return;
		}

		BFAbstractPlayerData<?, ?, ?, ?> playerData = manager.getPlayerDataHandler().getPlayerData(player);
		if (playerData.isOutOfGame()) {
			event.cancel();
		}
	}

	public void update(MinecraftServer server) {
		if (serverApi == null) {
			return;
		}

		Set<UUID> remainingGroups = new ObjectOpenHashSet<>(deadGroups.keySet());
		List<ServerPlayer> remainingPlayers = new ObjectArrayList<>(server.overworld().players());

		for (GameAsset gameAsset : AssetStore.getInstance().getRegistry(GameAsset.class).getEntries().values()) {
			AbstractGame<?, ?, ?> game = gameAsset.getGame();
			if (game == null || game instanceof IAllowsRespawning || game.getStatus() == GameStatus.IDLE) {
				continue;
			}

			UUID gameUuid = game.getUUID();

			Group deadGroup = deadGroups.get(gameUuid);
			if (deadGroup == null) {
				deadGroup = createDeadGroup(game);
				deadGroups.put(gameUuid, deadGroup);
			}

			remainingGroups.remove(gameUuid);

			List<ServerPlayer> handledPlayers = handlePlayers(game, deadGroup);
			remainingPlayers.removeAll(handledPlayers);
		}

		cleanup(remainingGroups, remainingPlayers);
	}

	private Group createDeadGroup(AbstractGame<?, ?, ?> game) {
		assert serverApi != null;

		PlusFront.LOGGER.info("[VC] Creating dead group for {}", game.getName());

		return serverApi.groupBuilder()
			.setName("Dead")
			.setType(Group.Type.NORMAL)
			.setHidden(true)
			.setPersistent(true)
			.build();
	}

	private List<ServerPlayer> handlePlayers(AbstractGame<?, ?, ?> game, Group deadGroup) {
		assert serverApi != null;

		AbstractGamePlayerManager<?> playerManager = game.getPlayerManager();
		PlayerDataHandler<?> dataHandler = PFUtil.blockfrontManager().getPlayerDataHandler();

		Set<UUID> players = playerManager.getPlayers();
		Set<UUID> spectators = playerManager.getSpectators();

		List<ServerPlayer> handledPlayers = new ObjectArrayList<>(players.size() + spectators.size());

		for (UUID playerUuid : players) {
			VoicechatConnection vcConnection = serverApi.getConnectionOf(playerUuid);
			if (vcConnection == null) {
				continue;
			}

			ServerPlayer player = GameUtils.getPlayerByUUID(playerUuid);
			if (player == null) {
				continue;
			}

			BFAbstractPlayerData<?, ?, ?, ?> playerData = dataHandler.getPlayerData(playerUuid);

			boolean dead = playerData.isOutOfGame() || player.isSpectator();
			Group group = vcConnection.getGroup();

			if (dead && group == null) {
				vcConnection.setGroup(deadGroup);
			} else if (!dead && deadGroup.equals(group)) {
				vcConnection.setGroup(null);
			}

			handledPlayers.add(player);
		}

		for (UUID spectatorUuid : spectators) {
			VoicechatConnection vcConnection = serverApi.getConnectionOf(spectatorUuid);
			if (vcConnection == null) {
				continue;
			}

			ServerPlayer player = GameUtils.getPlayerByUUID(spectatorUuid);
			if (player == null) {
				continue;
			}

			if (vcConnection.getGroup() == null) {
				vcConnection.setGroup(deadGroup);
			}

			handledPlayers.add(player);
		}

		return handledPlayers;
	}

	private void cleanup(Iterable<UUID> groups, Iterable<ServerPlayer> players) {
		assert serverApi != null;

		int numRemovedGroups = 0;
		for (UUID group : groups) {
			Group prevGroup = deadGroups.remove(group);
			if (prevGroup != null) {
				serverApi.removeGroup(prevGroup.getId());
				numRemovedGroups++;
			}
		}
		if (numRemovedGroups > 0) {
			PlusFront.LOGGER.info("[VC] Removed {} stale dead groups", numRemovedGroups);
		}

		int numClearedPlayers = 0;
		for (ServerPlayer player : players) {
			VoicechatConnection vcConnection = serverApi.getConnectionOf(player.getUUID());
			if (vcConnection != null && deadGroups.containsValue(vcConnection.getGroup())) {
				vcConnection.setGroup(null);
				numClearedPlayers++;
			}
		}
		if (numClearedPlayers > 0) {
			PlusFront.LOGGER.info("[VC] Cleared {} stale assigned groups", numClearedPlayers);
		}
	}
}
