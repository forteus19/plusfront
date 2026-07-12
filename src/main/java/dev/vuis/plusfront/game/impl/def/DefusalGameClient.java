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
import com.boehmod.blockfront.game.GameNotification;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.tag.client.IAllowsPingsClient;
import com.boehmod.blockfront.registry.BFItems;
import com.boehmod.blockfront.unnamed.BF_552;
import com.boehmod.blockfront.util.BFRes;
import com.boehmod.blockfront.util.BFStyles;
import com.boehmod.blockfront.util.CollisionUtils;
import com.boehmod.blockfront.util.PacketUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.client.def.DefusalTeamGameElement;
import dev.vuis.plusfront.client.def.DefusalTimeGameElement;
import dev.vuis.plusfront.client.render.PFGuiRenderUtil;
import dev.vuis.plusfront.game.ScoreboardFormats;
import dev.vuis.plusfront.util.PFUtil;
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
import net.minecraft.client.gui.components.PlayerFaceRenderer;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DefusalGameClient extends AbstractGameClient<DefusalGame, DefusalPlayerManager> implements IAllowsPingsClient {
	private static final Component CT_LABEL = Component.literal("CT").withStyle(DefusalPlayerManager.CT_STYLE);
	private static final Component T_LABEL = Component.literal("T").withStyle(DefusalPlayerManager.T_STYLE);

	private static final Component BOMB_PLANT_REMINDER =
		Component.translatable("pf.message.gamemode.notification.bomb.reminder").withStyle(DefusalPlayerManager.T_STYLE);

	public static final ResourceLocation BOMB_TEXTURE = PlusFront.res("textures/gui/defusal/bomb.png");
	public static final ResourceLocation BOMB_BLINK_TEXTURE = PlusFront.res("textures/gui/defusal/bomb_blink.png");
	private static final ResourceLocation DEAD_TEXTURE = BFRes.loc("textures/gui/dead.png");
	private static final ResourceLocation INDICATOR_TEXTURE = BFRes.loc("textures/gui/indicator.png");

	private static final Component BOMBSITE_LABEL = Component.literal("BOMBSITE");

	private static final int BOMBSITE_CAGE_COLOR = 0xFFFC4141;
	private static final BFRendering.CageSettings BOMBSITE_CAGE_SETTINGS =
		BFRendering.CageSettings.create()
			.fill(BOMBSITE_CAGE_COLOR, 0.25f)
			.line(BOMBSITE_CAGE_COLOR, 1.0f, 1.5f)
			.gridSpacing(1.0)
			.sides(true, false, false)
			.verticalFade(BFRendering.BoundaryFadeDirection.TOP)
			.occludedAlpha(0.25f);

	private final List<AABB> bombSiteBoxes = new ObjectArrayList<>();

	private boolean isGameStage = false;
	private boolean finishedRound = false;
	private int bombItemBlinkTimer = 0;

	public DefusalGameClient(@NotNull BFClientManager manager, @NotNull DefusalGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);

		manager.getCinematics().method_2205(new BF_552(game));
	}

	public boolean isFinishedRound() {
		return finishedRound;
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
	protected void addLocalNotifications(@NotNull Minecraft minecraft, @NotNull LocalPlayer player, @NotNull List<GameNotification> target) {
		super.addLocalNotifications(minecraft, player, target);

		if (!isGameStage) {
			return;
		}

		DefusalPlayerManager playerManager = game.getPlayerManager();
		UUID playerUuid = player.getUUID();

		GameTeam team = playerManager.getPlayerTeam(playerUuid);

		if (team != null &&
			team.getName().equals(DefusalPlayerManager.T_NAME) &&
			playerManager.isBombHolder(playerUuid) &&
			game.checkBombSiteArea(player.position())
		) {
			addNotification(target, "bomb.reminder", BOMB_PLANT_REMINDER);
		}
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

		bombItemBlinkTimer = ++bombItemBlinkTimer % 20;
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
			renderBombSites(player, poseStack, frustum, font, graphics, camera);
		}
	}

	private void renderBombSites(
		LocalPlayer player,
		PoseStack poseStack,
		Frustum frustum,
		Font font,
		GuiGraphics graphics,
		Camera camera
	) {
		List<BombSite> bombSites = game.getBombSites();
		int numBombSites = bombSites.size();

		if (numBombSites != bombSiteBoxes.size()) {
			PlusFront.LOGGER.warn("Mismatched bomb sites and boundary AABBs!");
		}

		boolean showBoundary = player.getMainHandItem().getItem() == BFItems.BOMB.value();

		for (int i = 0; i < numBombSites; i++) {
			BombSite bombSite = bombSites.get(i);
			AABB bombSiteAABB = bombSiteBoxes.get(i);

			renderBombSite(
				poseStack, frustum, font, graphics, camera,
				bombSite, showBoundary, bombSiteAABB
			);
		}
	}

	private static void renderBombSite(
		PoseStack poseStack,
		Frustum frustum,
		Font font,
		GuiGraphics graphics,
		Camera camera,
		BombSite bombSite,
		boolean showBoundary,
		AABB boundaryAABB
	) {
		if (showBoundary && frustum.isVisible(boundaryAABB)) {
			BFRendering.cageGameBoundary(
				camera, poseStack,
				bombSite.boundary,
				bombSite.visibleY, bombSite.visibleY + BombSite.VISIBLE_HEIGHT,
				BOMBSITE_CAGE_SETTINGS
			);
		}

		for (Vec3 waypoint : bombSite.waypoints) {
			BFRendering.component(
				poseStack, font, camera, graphics,
				Component.literal(bombSite.name).withStyle(BFStyles.BOLD),
				waypoint.x, waypoint.y + 2.5f, waypoint.z,
				2.0f, 0xFFFFFFFF, true
			);
			BFRendering.component(
				poseStack, font, camera, graphics,
				BOMBSITE_LABEL,
				waypoint.x, waypoint.y + 1.75f, waypoint.z,
				1.0f, 0xFFFFFFFF, true
			);
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

		GameTeam counterTerrorists = playerManager.counterTerrorists();
		GameTeam terrorists = playerManager.terrorists();

		boolean isTerrorist = terrorists.hasPlayer(player.getUUID());

		if (isTerrorist) {
			ItemEntity bombItem = game.getBombItem(level);

			if (bombItem != null) {
				renderBombItemWaypoint(
					poseStack, minecraft.gameRenderer.getMainCamera(), width, height, partialTick,
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
		renderPlayerHeadList(connection, playerManager, counterTerrorists.getPlayers(), graphics, 20, playerHeadsY, false);

		playerHeadsY += 17;

		graphics.drawString(font, T_LABEL, 12 - font.width(T_LABEL) / 2, playerHeadsY + 4, 0xFFFFFFFF, true);
		renderPlayerHeadList(connection, playerManager, terrorists.getPlayers(), graphics, 20, playerHeadsY, isTerrorist);
	}

	private void renderBombItemWaypoint(
		PoseStack poseStack,
		Camera camera,
		int width,
		int height,
		float partialTick,
		Vec3 bombPosition
	) {
		BFRendering.ScreenClampData screenClampData = BFRendering.screenClamp(bombPosition, camera, width, height, 32, partialTick);

		PFGuiRenderUtil.centeredTexture(
			poseStack,
			bombItemBlinkTimer < 10 ? BOMB_TEXTURE : BOMB_BLINK_TEXTURE,
			screenClampData.screenX(), screenClampData.screenY(),
			32f, 16f
		);
	}

	private void renderPlayerHeadList(
		ClientPacketListener connection,
		DefusalPlayerManager playerManager,
		Set<UUID> players,
		GuiGraphics graphics,
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

			if (shouldShowPlayerDead(playerInfo, playerUuid)) {
				graphics.blit(
					DEAD_TEXTURE,
					headX, headY, 11, 11,
					0f, 0f,
					8, 8, 8, 8
				);
			} else {
				PlayerFaceRenderer.draw(
					graphics,
					playerInfo.getSkin(),
					headX, headY, 11
				);

				if (showBombIndicator && playerManager.isBombHolder(playerUuid)) {
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

	private boolean shouldShowPlayerDead(@Nullable PlayerInfo playerInfo, @NotNull UUID playerUuid) {
		if (playerInfo == null || playerInfo.getGameMode() == GameType.SPECTATOR) {
			return true;
		}

		BFClientPlayerData playerData = dataHandler.getPlayerData(playerUuid);
		return playerData.isOutOfGame();
	}

	@Override
	public void read(@NotNull ByteBuf buf) throws IOException {
		super.read(buf);

		isGameStage = buf.readBoolean();
		finishedRound = buf.readBoolean();

		onGamePacket();
	}

	private void onGamePacket() {
		bombSiteBoxes.clear();
		for (BombSite bombSite : game.getBombSites()) {
			bombSiteBoxes.add(bombSite.getBoundaryAABB());
		}
	}

	@Override
	public boolean canChangePerspective(@NotNull Player player) {
		return game.getStatus() != GameStatus.GAME || player.getVehicle() != null;
	}

	@Override
	public @NotNull Scoreboard getScoreboard() {
		return new Scoreboard()
			.column("PING", ScoreboardFormats.ping())
			.column("K", ScoreboardFormats.tagInt(BFStats.KILLS))
			.column("D", ScoreboardFormats.tagInt(BFStats.DEATHS))
			.column("A", ScoreboardFormats.tagInt(BFStats.ASSISTS))
			.column("HS%", ScoreboardFormats.tagPercent(BFStats.HEAD_SHOTS, BFStats.KILLS))
			.column("SCORE", ScoreboardFormats.tagInt(BFStats.SCORE));
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

		GameTeam localTeam = playerManager.getPlayerTeam(localPlayer.getUUID());
		GameTeam targetTeam = playerManager.getPlayerTeam(targetPlayer.getUUID());

		return PFUtil.isSameTeam(localTeam, targetTeam);
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
