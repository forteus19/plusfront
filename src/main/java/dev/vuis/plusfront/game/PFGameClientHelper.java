package dev.vuis.plusfront.game;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.match.ping.AbstractPing;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.render.minimap.MinimapWaypoint;
import com.boehmod.blockfront.common.net.packet.BFRegularPingRequestPacket;
import com.boehmod.blockfront.common.net.packet.BFRegularPingTriggerRequestPacket;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.TeamType;
import com.boehmod.blockfront.util.BFRes;
import com.boehmod.blockfront.util.CollisionUtils;
import com.boehmod.blockfront.util.PacketUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PFGameClientHelper {
	public static final Set<ResourceLocation> SPECIALIST_UNIFORMS = Set.of(
		BFRes.loc("ussr_infantry")
	);

	private PFGameClientHelper() {
		throw new AssertionError();
	}

	public static boolean canChangePerspective(
		@NotNull AbstractGame<?, ?, ?> game,
		@NotNull Player player
	) {
		return game.getStatus() != GameStatus.GAME || player.getVehicle() != null;
	}

	public static @NotNull List<MinimapWaypoint> getPlayerWaypoints(
		@NotNull AbstractGameClient<?, ?> gameClient,
		@NotNull UUID localUuid,
		@NotNull ClientLevel level
	) {
		AbstractGamePlayerManager<?> playerManager = gameClient.getGame().getPlayerManager();

		GameTeam localTeam = playerManager.getPlayerTeam(localUuid);
		if (localTeam == null) {
			return List.of();
		}
		Set<UUID> localTeamPlayers = localTeam.getPlayers();

		List<MinimapWaypoint> waypoints = new ObjectArrayList<>(localTeam.numPlayers() - 1);

		for (Player player : level.players()) {
			UUID playerUuid = player.getUUID();

			if (localTeamPlayers.contains(playerUuid) && !playerUuid.equals(localUuid)) {
				waypoints.add(
					new MinimapWaypoint(MinimapWaypoint.TEXTURE_PLAYER, player.position()).setRotation(player.getYRot() - 180f)
				);
			}
		}

		return waypoints;
	}

	public static @Nullable ResourceLocation getUniformTexture(
		@NotNull AbstractGame<?, ?, ?> game,
		@NotNull UUID playerUuid,
		@Nullable String classKey,
		@NotNull Set<UUID> players
	) {
		if (classKey == null || !players.contains(playerUuid)) {
			return null;
		}

		GameTeam team = game.getPlayerManager().getPlayerTeam(playerUuid);
		if (team == null) {
			return null;
		}

		TeamType teamType = team.getDivisionData(game);

		if (classKey.equals("specialist") && !SPECIALIST_UNIFORMS.contains(teamType.getResourceLocation())) {
			classKey = "anti_tank";
		}

		return BFRes.loc("textures/skins/game/nations/" + teamType.getNationType().getTag() + "/" + teamType.getSkin() + "/" + classKey + ".png");
	}

	public static void sendPingRequest(
		@NotNull AbstractGameClient<?, ?> gameClient,
		@NotNull Minecraft minecraft,
		@NotNull BFClientManager manager
	) {
		LocalPlayer localPlayer = minecraft.player;
		BFClientPlayerData localPlayerData = manager.getPlayerDataHandler().getPlayerData(minecraft);

		if (localPlayer == null || GameUtils.isPlayerUnavailable(localPlayer, localPlayerData)) {
			return;
		}

		HitResult hit = CollisionUtils.hitBlock(localPlayer, 64.0, minecraft.getTimer().getGameTimeDeltaPartialTick(false));
		if (hit.getType() == HitResult.Type.MISS) {
			return;
		}
		Vec3 hitPosition = hit.getLocation();

		AbstractPing existingPing = gameClient.getNearestPing(hitPosition, 2.0);
		if (existingPing != null && existingPing.getPlayerUuid().equals(localPlayer.getUUID())) {
			PacketUtils.sendToServer(new BFRegularPingTriggerRequestPacket(
				existingPing.getUuid(), hitPosition
			));
		} else {
			PacketUtils.sendToServer(new BFRegularPingRequestPacket(
				hitPosition
			));
		}
	}

	public static boolean shouldMovePing(
		@NotNull AbstractPing ping,
		@NotNull UUID playerUuid,
		@NotNull Vec3 newPosition
	) {
		return ping.getPlayerUuid().equals(playerUuid) && ping.getPosition().distanceToSqr(newPosition) <= (2.0 * 2.0);
	}
}
