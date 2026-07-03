package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.match.ping.AbstractPing;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.client.render.game.element.ClientGameElement;
import com.boehmod.blockfront.client.render.game.element.TeamScoreGameElement;
import com.boehmod.blockfront.client.render.minimap.MinimapWaypoint;
import com.boehmod.blockfront.client.screen.match.summary.MatchSummaryScreen;
import com.boehmod.blockfront.client.settings.BFClientSettings;
import com.boehmod.blockfront.common.net.packet.BFRegularPingRequestPacket;
import com.boehmod.blockfront.common.net.packet.BFRegularPingTriggerRequestPacket;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.tag.client.IAllowsPingsClient;
import com.boehmod.blockfront.registry.BFItems;
import com.boehmod.blockfront.unnamed.BF_552;
import com.boehmod.blockfront.util.BFRes;
import com.boehmod.blockfront.util.CollisionUtils;
import com.boehmod.blockfront.util.PacketUtils;
import com.boehmod.blockfront.util.StringUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.client.def.DefusalTeamGameElement;
import dev.vuis.plusfront.client.def.DefusalTimeGameElement;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

public final class DefusalGameClient extends AbstractGameClient<DefusalGame, DefusalPlayerManager> implements IAllowsPingsClient {
	private static final Component CT_LABEL = Component.literal("CT").withStyle(DefusalPlayerManager.CT_STYLE);
	private static final Component T_LABEL = Component.literal("T").withStyle(DefusalPlayerManager.T_STYLE);

	public static final ResourceLocation BOMB_TEXTURE = PlusFront.res("textures/gui/defusal/bomb.png");
	public static final ResourceLocation BOMB_BLINK_TEXTURE = PlusFront.res("textures/gui/defusal/bomb_blink.png");
	private static final ResourceLocation DEAD_TEXTURE = BFRes.loc("textures/gui/dead.png");
	private static final ResourceLocation INDICATOR_TEXTURE = BFRes.loc("textures/gui/indicator.png");

	private final List<BombSiteClient> bombSites;

	private boolean isRoundFinished = false;
	private int bombBlinkTimer = 0;

	public DefusalGameClient(@NotNull BFClientManager manager, @NotNull DefusalGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);

		bombSites = new ObjectArrayList<>(game.getBombSites().size());
		for (BombSite data : game.getBombSites()) {
			bombSites.add(new BombSiteClient(data));
		}

		manager.getCinematics().method_2205(new BF_552(game));
	}

	@Override
	protected @NotNull List<Component> getTips() {
		return List.of();
	}

	@Override
	protected @NotNull List<ClientGameElement<DefusalGame, DefusalPlayerManager>> getGameElements() {
		return List.of(
			new DefusalTeamGameElement(),
			new TeamScoreGameElement<>(),
			new DefusalTimeGameElement()
		);
	}

	@Override
	public boolean shouldRenderBackpack(@NotNull AbstractClientPlayer player) {
		return true;
	}

	@Override
	public void update(
		@NotNull Minecraft minecraft,
		@NotNull Random random,
		@NotNull RandomSource randomSource,
		@NotNull LocalPlayer player,
		@NotNull ClientLevel level,
		@NotNull BFClientManager manager,
		@NotNull BFClientPlayerData playerData,
		@NotNull Set<UUID> players,
		float renderTime,
		@NotNull Vec3 cameraPos,
		@NotNull BlockPos cameraBlockPos
	) {
		super.update(minecraft, random, randomSource, player, level, manager, playerData, players, renderTime, cameraPos, cameraBlockPos);

		bombBlinkTimer = ++bombBlinkTimer % 20;

		GameTeam terroristTeam = game.getPlayerManager().getTeamByName(DefusalPlayerManager.T_NAME);
		assert terroristTeam != null;

		boolean highlightInRadius = player.getMainHandItem().getItem() == BFItems.BOMB.value();

		if (BFClientSettings.UI_RENDER_WAYPOINTS.isEnabled()) {
			for (BombSiteClient site : bombSites) {
				site.update(player, highlightInRadius);
			}
		}
	}

	@Override
	public void renderWorld(
		@NotNull AbstractGamePlayerManager<?> playerManager,
		@NotNull Minecraft minecraft,
		@NotNull ClientLevel level,
		@NotNull LocalPlayer player,
		@NotNull RenderLevelStageEvent renderEvent,
		@NotNull MultiBufferSource.BufferSource bufferSource,
		@NotNull PoseStack poseStack,
		@NotNull Frustum frustum,
		@NotNull Font font,
		@NotNull GuiGraphics graphics,
		@NotNull Camera camera,
		boolean debug,
		float renderTime,
		float partialTick
	) {
		super.renderWorld(playerManager, minecraft, level, player, renderEvent, bufferSource, poseStack, frustum, font, graphics, camera, debug, renderTime, partialTick);

		if (BFClientSettings.UI_RENDER_WAYPOINTS.isEnabled()) {
			for (BombSiteClient site : bombSites) {
				if (debug) {
					site.renderDebug(bufferSource, poseStack);
				}

				site.render(poseStack, font, graphics, camera);
			}
		}
	}

	@Override
	public void onRenderGui(
		@NotNull Minecraft minecraft,
		@NotNull BFClientManager manager,
		@NotNull LocalPlayer player,
		@NotNull ClientLevel level,
		@NotNull BFClientPlayerData playerData,
		@NotNull GuiGraphics graphics,
		@NotNull Font font,
		@NotNull PoseStack poseStack,
		@NotNull MultiBufferSource bufferSource,
		@NotNull Set<UUID> players,
		int width,
		int height,
		int midX,
		int midY,
		float renderTime,
		float partialTick
	) {
		DefusalPlayerManager playerManager = game.getPlayerManager();

		GameTeam ctTeam = playerManager.getTeamByName(DefusalPlayerManager.CT_NAME);
		assert ctTeam != null;
		GameTeam tTeam = playerManager.getTeamByName(DefusalPlayerManager.T_NAME);
		assert tTeam != null;

		boolean isTerrorist = tTeam.hasPlayer(player.getUUID());

		if (isTerrorist) {
			ItemEntity bombItem = game.getBombItem(level);

			if (bombItem != null) {
				renderBombItemWaypoint(
					graphics, minecraft.gameRenderer.getMainCamera(), width, height, partialTick,
					bombItem.getPosition(partialTick).add(0.0, 0.5, 0.0)
				);
			}
		}

		ClientPacketListener connection = minecraft.getConnection();
		if (connection == null) {
			return;
		}

		int playerHeadsY = 25;
		if (BFClientSettings.UI_RENDER_GAME_MINIMAP.isEnabled()) {
			playerHeadsY += 104;
		}

		graphics.drawString(font, CT_LABEL, 12 - font.width(CT_LABEL) / 2, playerHeadsY + 4, 0xFFFFFFFF, true);
		renderPlayerHeadList(minecraft, connection, manager, graphics, playerManager, ctTeam.getPlayers(), 20, playerHeadsY, false);

		playerHeadsY += 17;

		graphics.drawString(font, T_LABEL, 12 - font.width(T_LABEL) / 2, playerHeadsY + 4, 0xFFFFFFFF, true);
		renderPlayerHeadList(minecraft, connection, manager, graphics, playerManager, tTeam.getPlayers(), 20, playerHeadsY, isTerrorist);
	}

	private void renderBombItemWaypoint(
		GuiGraphics graphics,
		Camera camera,
		int width,
		int height,
		float partialTick,
		Vec3 bombPosition
	) {
		BFRendering.ScreenClampData screenClampData = BFRendering.screenClamp(bombPosition, camera, width, height, 32, partialTick);

		graphics.blit(
			bombBlinkTimer < 10 ? BOMB_TEXTURE : BOMB_BLINK_TEXTURE,
			(int) (screenClampData.screenX() - 16f), (int) (screenClampData.screenY() - 8f),
			0f, 0f,
			32, 16, 32, 16
		);
	}

	private void renderPlayerHeadList(
		Minecraft minecraft,
		ClientPacketListener connection,
		BFClientManager manager,
		GuiGraphics graphics,
		DefusalPlayerManager playerManager,
		Set<UUID> players,
		int x,
		int y,
		boolean showBombIndicator
	) {
		int numPlayers = players.size();

		int headsWidth = Math.max(1, numPlayers) * 11 + Math.max(0, numPlayers - 1);

		graphics.fill(
			x, y, x + headsWidth + 4, y + 15,
			0x4C000000
		);
		graphics.fill(
			x + 1, y + 1, x + headsWidth + 3, y + 14,
			0x7F000000
		);

		int headX = x + 2;
		int headY = y + 2;

		for (UUID playerUuid : players) {
			PlayerInfo playerInfo = connection.getPlayerInfo(playerUuid);
			if (playerInfo == null) {
				headX += 12;
				continue;
			}

			BFClientPlayerData playerData = dataHandler.getPlayerData(playerUuid);

			if (playerData.isOutOfGame() || playerInfo.getGameMode() == GameType.SPECTATOR) {
				graphics.blit(
					DEAD_TEXTURE,
					headX, headY, 11, 11,
					0f, 0f,
					8, 8, 8, 8
				);
			} else {
				ResourceLocation skinTexture = BFRendering.getSkinTexture(minecraft, manager, playerUuid);

				graphics.blit(
					skinTexture,
					headX, headY, 11, 11,
					8f, 8f,
					8, 8, 64, 64
				);
				graphics.blit(
					skinTexture,
					headX, headY, 11, 11,
					40f, 8f,
					8, 8, 64, 64
				);

				if (showBombIndicator && playerManager.isBombPlayer(playerUuid)) {
					RenderSystem.setShaderColor(1f, 0f, 0f, 1f);
					graphics.blit(
						INDICATOR_TEXTURE,
						headX - 1, headY - 1,
						0f, 0f,
						3, 3, 3, 3
					);
					BFRendering.resetShaderColor();
				}
			}

			headX += 12;
		}
	}

	@Override
	public void read(@NotNull ByteBuf buf) throws IOException {
		super.read(buf);

		isRoundFinished = buf.readBoolean();
	}

	public boolean isRoundFinished() {
		return isRoundFinished;
	}

	@Override
	public boolean canChangePerspective(@NotNull Player player) {
		return game.getStatus() != GameStatus.GAME
			|| game.getStageManager().getCurrentStage().getClass() == DefusalWaitingStage.class
			|| player.getVehicle() != null;
	}

	@Override
	public @NotNull Scoreboard getScoreboard() {
		return new Scoreboard()
			.column("PING", (data, info, tag) -> info != null ? StringUtils.formatLong(info.getLatency()) : "???")
			.column("K", (data, info, tag) -> StringUtils.formatLong(tag.getInt(BFStats.KILLS.getKey())))
			.column("A", (data, info, tag) -> StringUtils.formatLong(tag.getInt(BFStats.ASSISTS.getKey())))
			.column("D", (data, info, tag) -> StringUtils.formatLong(tag.getInt(BFStats.DEATHS.getKey())))
			.column("SCORE", (data, info, tag) -> StringUtils.formatLong(tag.getInt(BFStats.SCORE.getKey())));
	}

	@Override
	public boolean canOpenInventory() {
		return false;
	}

	@Override
	public boolean shouldRenderNameTag(
		@NotNull Minecraft minecraft,
		@NotNull LivingEntity target,
		@NotNull Player localPlayer,
		@NotNull ClientLevel level
	) {
		if (!(target instanceof Player targetPlayer)) {
			return false;
		}

		DefusalPlayerManager playerManager = game.getPlayerManager();

		GameTeam targetTeam = playerManager.getPlayerTeam(targetPlayer.getUUID());
		if (targetTeam == null) {
			return false;
		}

		GameTeam localTeam = playerManager.getPlayerTeam(localPlayer.getUUID());
		if (localTeam == null) {
			return false;
		}

		return localTeam.getName().equalsIgnoreCase(targetTeam.getName());
	}

	@Override
	public boolean canSwitchItem() {
		return true;
	}

	@Override
	public @NotNull List<MinimapWaypoint> getSpecificMinimapWaypoints(
		@NotNull Minecraft minecraft,
		@NotNull Set<UUID> players,
		@NotNull LocalPlayer localPlayer,
		@NotNull ClientLevel level
	) {
		DefusalPlayerManager playerManager = game.getPlayerManager();

		UUID localUuid = localPlayer.getUUID();

		GameTeam localTeam = playerManager.getPlayerTeam(localUuid);
		if (localTeam == null) {
			return List.of();
		}
		Set<UUID> localTeamPlayers = localTeam.getPlayers();

		List<MinimapWaypoint> waypoints = new ObjectArrayList<>();

		for (Player player : level.players()) {
			UUID playerUuid = player.getUUID();

			if (localTeamPlayers.contains(playerUuid) && !playerUuid.equals(localUuid)) {
				waypoints.add(
					new MinimapWaypoint(MinimapWaypoint.TEXTURE_PLAYER, player.position())
						.setRotation(player.getYRot() - 180f)
				);
			}
		}

		return waypoints;
	}

	@Override
	public void setNameTagState(
		@NotNull Minecraft minecraft,
		@NotNull RenderNameTagEvent event,
		@NotNull Player player,
		@NotNull ClientLevel level
	) {
		event.setCanRender(player.hasLineOfSight(event.getEntity()) ? TriState.TRUE : TriState.FALSE);
	}

	@Override
	public @NotNull Collection<? extends MatchSummaryScreen> getSummaryScreens(boolean onlyVote) {
		return getSummaryScreens(game, onlyVote);
	}

	@Override
	public void onPing(@NotNull Minecraft minecraft, @NotNull BFClientManager manager) {
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

		AbstractPing existingPing = getNearestPing(hitPosition, 2.0);
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

	@Override
	public boolean shouldMovePing(@NotNull AbstractPing ping, @NotNull UUID playerUuid, @NotNull Vec3 newPosition) {
		return ping.getPlayerUuid().equals(playerUuid) && ping.getPosition().distanceToSqr(newPosition) <= (2.0 * 2.0);
	}
}
