package dev.vuis.plusfront.compat.voicechat;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.game.AbstractGame;
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
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@ForgeVoicechatPlugin
public final class PFVoicechat implements VoicechatPlugin {
	private static @Nullable PFVoicechat instance = null;

	private @Nullable VoicechatServerApi serverApi = null;

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

		AbstractGame<?, ?, ?> game = manager.getPlayerGame(player);
		if (!(game instanceof IAllowsRespawning)) {
			return;
		}

		BFAbstractPlayerData<?, ?, ?, ?> playerData = manager.getPlayerDataHandler().getPlayerData(player);
		if (playerData.isOutOfGame()) {
			event.cancel();
		}
	}

	/**
	 * Creates a new "Dead" voicechat group, removing the old group for the given game if it exists.
	 *
	 * @param gameUuid the uuid of the game
	 */
	public void onGameStart(UUID gameUuid) {
		assert serverApi != null;

		Group prevGroup = deadGroups.put(
			gameUuid,
			serverApi.groupBuilder()
				.setName("Dead")
				.setType(Group.Type.NORMAL)
				.setHidden(true)
				.setPersistent(true)
				.build()
		);
		if (prevGroup != null) {
			serverApi.removeGroup(prevGroup.getId());
		}
	}

	/**
	 * Removes the old "Dead" voicechat group for the given game if it exists.
	 *
	 * @param gameUuid the uuid of the game
	 */
	public void onGameEnd(UUID gameUuid) {
		assert serverApi != null;

		Group prevGroup = deadGroups.remove(gameUuid);
		if (prevGroup != null) {
			serverApi.removeGroup(prevGroup.getId());
		}
	}

	/**
	 * Adds the given player to the game's dead voicechat group.
	 *
	 * @param gameUuid the uuid of the player's game
	 * @param playerUuid the player to add to the "Dead" group
	 */
	public void addToDeadGroup(UUID gameUuid, UUID playerUuid) {
		assert serverApi != null;

		VoicechatConnection connection = serverApi.getConnectionOf(playerUuid);
		if (connection == null) {
			return;
		}

		Group deadGroup = deadGroups.get(gameUuid);
		if (deadGroup == null) {
			return;
		}

		connection.setGroup(deadGroup);
	}

	/**
	 * Removes the given player from the group they are currently in.
	 *
	 * @param playerUuid the player to add to the "Dead" group
	 */
	public void removeFromGroup(UUID playerUuid) {
		assert serverApi != null;

		VoicechatConnection connection = serverApi.getConnectionOf(playerUuid);
		if (connection == null) {
			return;
		}

		connection.setGroup(null);
	}
}
