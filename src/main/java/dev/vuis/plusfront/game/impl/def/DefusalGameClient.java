package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.gui.toast.BFToast;
import com.boehmod.blockfront.client.gui.toast.BFToasts;
import com.boehmod.blockfront.client.gui.toast.ToastType;
import com.boehmod.blockfront.client.match.ping.AbstractPing;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.client.render.game.element.ClientGameElement;
import com.boehmod.blockfront.client.render.game.element.TeamScoreGameElement;
import com.boehmod.blockfront.client.render.game.element.TimeGameElement;
import com.boehmod.blockfront.client.render.minimap.MinimapWaypoint;
import com.boehmod.blockfront.client.screen.match.summary.MatchSummaryScreen;
import com.boehmod.blockfront.client.settings.BFClientSettings;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameNotification;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.tag.client.IAllowsPingsClient;
import com.boehmod.blockfront.registry.BFItems;
import com.boehmod.blockfront.unnamed.BF_552;
import com.boehmod.blockfront.util.BFRes;
import com.boehmod.blockfront.util.BFStyles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.client.PFClientTemp;
import dev.vuis.plusfront.client.PFKeyMappings;
import dev.vuis.plusfront.client.config.GameGuiStyle;
import dev.vuis.plusfront.client.config.PFClientConfig;
import dev.vuis.plusfront.client.def.DefusalTeamGameElement;
import dev.vuis.plusfront.client.render.IconRenderers;
import dev.vuis.plusfront.client.render.game.PFGameGuiRendering;
import dev.vuis.plusfront.ex.GameStageTimerEx;
import dev.vuis.plusfront.game.PFGameClientHelper;
import dev.vuis.plusfront.game.PFGameHelper;
import dev.vuis.plusfront.game.ScoreboardFormats;
import dev.vuis.plusfront.game.tag.IExtraPlayerInfo;
import dev.vuis.plusfront.game.tag.IModifyRendering;
import dev.vuis.plusfront.util.PFUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
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
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DefusalGameClient extends AbstractGameClient<DefusalGame, DefusalPlayerManager>
	implements
	IAllowsPingsClient,
	IExtraPlayerInfo,
	IModifyRendering {

	private static final Component GUI_STYLE_TITLE = Component.translatable("pf.notification.guistyle.title");
	private static final Component GUI_STYLE_MESSAGE = Component.translatable("pf.notification.guistyle.defusal");
	private static final Component GUI_STYLE_SWITCH = Component.translatable("pf.message.action.switch");

	private static final Component CT_LABEL = Component.literal("CT").withStyle(DefusalPlayerManager.CT_STYLE);
	private static final Component T_LABEL = Component.literal("T").withStyle(DefusalPlayerManager.T_STYLE);

	private static final Component BOMB_PLANT_REMINDER =
		Component.translatable("pf.message.gamemode.notification.bomb.reminder").withStyle(DefusalPlayerManager.T_STYLE);

	private static final ResourceLocation DEAD_TEXTURE = BFRes.loc("textures/gui/dead.png");
	private static final ResourceLocation INDICATOR_TEXTURE = BFRes.loc("textures/gui/indicator.png");

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
	private final Object2FloatMap<UUID> playerHealth = new Object2FloatOpenHashMap<>();

	private boolean isGameStage = false;
	@Getter
	private boolean finishedRound = false;

	public DefusalGameClient(@NotNull BFClientManager manager, @NotNull DefusalGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);

		manager.getCinematics().method_2205(new BF_552(game));

		if (PFClientConfig.getGameGuiStyle() != GameGuiStyle.CS2) {
			BFToasts.showToast(
				BFToast.builder()
					.type(ToastType.INFO)
					.title(GUI_STYLE_TITLE)
					.message(GUI_STYLE_MESSAGE)
					.acceptAction(GUI_STYLE_SWITCH, () -> {
						PFClientConfig.setGameGuiStyle(GameGuiStyle.CS2);
						PFClientConfig.save();
					})
					.build()
			);
		}
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
			new TimeGameElement<>()
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

		if (player.getMainHandItem().getItem() == BFItems.BOMB.value()) {
			renderBombSiteCages(poseStack, frustum, camera);
		}
	}

	private void renderBombSiteCages(
		PoseStack poseStack,
		Frustum frustum,
		Camera camera
	) {
		List<BombSite> bombSites = game.getBombSites();
		int numBombSites = bombSites.size();

		if (numBombSites != bombSiteBoxes.size()) {
			PlusFront.LOGGER.warn("Mismatched bomb sites and boundary AABBs!");
		}

		for (int i = 0; i < numBombSites; i++) {
			BombSite bombSite = bombSites.get(i);
			AABB bombSiteBox = bombSiteBoxes.get(i);

			if (frustum.isVisible(bombSiteBox)) {
				BFRendering.cageGameBoundary(
					camera, poseStack,
					bombSite.boundary,
					bombSite.visibleY, bombSite.visibleY + BombSite.VISIBLE_HEIGHT,
					BOMBSITE_CAGE_SETTINGS
				);
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

		GameGuiStyle guiStyle = PFClientConfig.getGameGuiStyle();

		switch (guiStyle) {
			case OLD -> {
				PFGameGuiRendering.oldScoreOnly(
					minecraft, dataHandler,
					graphics, poseStack, font,
					getStageTimer(), game.getPlayerManager(),
					midX
				);
			}
			case CS2 -> {
				PFGameGuiRendering.cs2ScoreOnly(
					minecraft, dataHandler, this,
					graphics, poseStack, font,
					getStageTimer(), game.getPlayerManager(),
					midX
				);
			}
		}

		if (PFKeyMappings.showWaypoints.isDown()) {
			Camera camera = minecraft.gameRenderer.getMainCamera();

			for (BombSite bombSite : game.getBombSites()) {
				if (bombSite.waypoints.isEmpty()) {
					continue;
				}

				for (Vec3 siteWaypoint : bombSite.waypoints) {
					renderBombSiteWaypoint(
						poseStack, graphics, font, camera, width, height, partialTick,
						siteWaypoint.add(0.0, 1.5, 0.0), bombSite.name
					);
				}
			}

			if (playerManager.terrorists().hasPlayer(player.getUUID())) {
				ItemEntity bombItem = game.getBombItem(level);

				if (bombItem != null) {
					renderBombItemWaypoint(
						poseStack, graphics, camera, width, height, partialTick,
						bombItem.getPosition(partialTick).add(0.0, 0.5, 0.0)
					);
				}
			}
		}

		ClientPacketListener connection = minecraft.getConnection();

		if (connection != null && !guiStyle.hasBuiltInPlayerHeads()) {
			int playerHeadsY = 25;
			if (BFClientSettings.UI_RENDER_GAME_MINIMAP.isEnabled()) {
				playerHeadsY += 104;
			}

			renderPlayerHeadLists(
				connection, playerManager, player,
				poseStack, graphics, font,
				playerHeadsY
			);
		}
	}

	private void renderBombSiteWaypoint(
		PoseStack poseStack,
		GuiGraphics graphics,
		Font font,
		Camera camera,
		int width,
		int height,
		float partialTick,
		Vec3 position,
		String name
	) {
		BFRendering.ScreenClampData screenClampData = BFRendering.screenClamp(position, camera, width, height, 48, partialTick);

		TextColor textColor = PFClientTemp.frameMillis % 1000 < 500 ? DefusalPlayerManager.T_TEXT_COLOR : null;

		poseStack.pushPose();
		poseStack.translate(screenClampData.screenX(), screenClampData.screenY(), 0f);

		{
			poseStack.pushPose();
			poseStack.scale(2f, 2f, 1f);

			Component nameText = Component.literal(name)
				.withStyle(BFStyles.BOLD.withColor(textColor));

			graphics.drawString(
				font,
				nameText,
				-font.width(nameText) / 2, -9,
				0xFFFFFFFF, true
			);

			poseStack.popPose();
		}

		Component distanceText = Component.literal(Mth.floor(camera.getPosition().distanceTo(position)) + "m")
			.withStyle(Style.EMPTY.withColor(textColor));

		graphics.drawString(
			font,
			distanceText,
			-font.width(distanceText) / 2, 3,
			0xFFFFFFFF, true
		);

		poseStack.popPose();
	}

	private void renderBombItemWaypoint(
		PoseStack poseStack,
		GuiGraphics graphics,
		Camera camera,
		int width,
		int height,
		float partialTick,
		Vec3 bombPosition
	) {
		BFRendering.ScreenClampData screenClampData = BFRendering.screenClamp(bombPosition, camera, width, height, 32, partialTick);

		IconRenderers.renderAt(
			graphics, poseStack,
			IconRenderers.BOMB,
			screenClampData.screenX(), screenClampData.screenY()
		);
	}

	private void renderPlayerHeadLists(
		ClientPacketListener connection,
		DefusalPlayerManager playerManager,
		LocalPlayer player,
		PoseStack poseStack,
		GuiGraphics graphics,
		Font font,
		int y
	) {
		GameTeam counterTerrorists = playerManager.counterTerrorists();
		GameTeam terrorists = playerManager.terrorists();

		poseStack.pushPose();
		poseStack.translate(12, y, 0);

		graphics.drawString(font, CT_LABEL, -font.width(CT_LABEL) / 2, 4, 0xFFFFFFFF, true);

		{
			poseStack.pushPose();
			poseStack.translate(8, 0, 0);

			renderPlayerHeadList(connection, playerManager, counterTerrorists.getPlayers(), graphics, false);

			poseStack.popPose();
		}

		poseStack.translate(0, 17, 0);

		graphics.drawString(font, T_LABEL, -font.width(T_LABEL) / 2, 4, 0xFFFFFFFF, true);

		{
			poseStack.pushPose();
			poseStack.translate(8, 0, 0);

			renderPlayerHeadList(connection, playerManager, terrorists.getPlayers(), graphics, terrorists.hasPlayer(player.getUUID()));

			poseStack.popPose();
		}

		poseStack.popPose();
	}

	private void renderPlayerHeadList(
		ClientPacketListener connection,
		DefusalPlayerManager playerManager,
		Set<UUID> players,
		GuiGraphics graphics,
		boolean showBombIndicator
	) {
		int numPlayers = players.size();

		int headsWidth = Math.max(1, numPlayers) * 11 + Math.max(0, numPlayers - 1);

		graphics.fill(
			0, 0, headsWidth + 4, 15,
			0x4C000000
		);
		graphics.fill(
			1, 1, headsWidth + 3, 14,
			0x7F000000
		);

		int headX = 2;
		int headY = 2;

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

		playerHealth.clear();
		int playerHealthSize = VarInt.read(buf);
		for (int i = 0; i < playerHealthSize; i++) {
			UUID playerUuid = UUIDUtil.STREAM_CODEC.decode(buf);
			float health = buf.readFloat();
			playerHealth.put(playerUuid, health);
		}

		onGamePacket();
	}

	private void onGamePacket() {
		bombSiteBoxes.clear();
		for (BombSite bombSite : game.getBombSites()) {
			bombSiteBoxes.add(bombSite.getBoundaryAABB());
		}

		((GameStageTimerEx) (Object) getStageTimer()).pf$setIconRenderer(
			game.isBombPlanted() ? IconRenderers.BOMB : null
		);
	}

	@Override
	public boolean canChangePerspective(@NotNull Player player) {
		return PFGameClientHelper.canChangePerspective(game, player);
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

		return PFGameHelper.isSameTeam(game.getPlayerManager(), localPlayer.getUUID(), targetPlayer.getUUID());
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
		return PFGameClientHelper.getPlayerWaypoints(this, localPlayer.getUUID(), level);
	}

	@Override
	public void setNameTagState(
		@NotNull Minecraft minecraft,
		@NotNull RenderNameTagEvent event,
		@NotNull Player player,
		@NotNull ClientLevel level
	) {
		event.setCanRender(PFUtil.triState(player.hasLineOfSight(event.getEntity())));
	}

	@Override
	public @Nullable ResourceLocation getUniformTexture(@NotNull UUID uuid, @Nullable String classKey, @NotNull Set<UUID> players) {
		return PFGameClientHelper.getUniformTexture(game, uuid, classKey, players);
	}

	@Override
	public @NotNull Collection<? extends MatchSummaryScreen> getSummaryScreens(boolean onlyVote) {
		return getSummaryScreens(game, onlyVote);
	}

	@Override
	public void onPing(@NotNull Minecraft minecraft, @NotNull BFClientManager manager) {
		PFGameClientHelper.sendPingRequest(this, minecraft, manager);
	}

	@Override
	public boolean shouldMovePing(@NotNull AbstractPing ping, @NotNull UUID playerUuid, @NotNull Vec3 newPosition) {
		return PFGameClientHelper.shouldMovePing(ping, playerUuid, newPosition);
	}

	@Override
	public float getPlayerHealth(UUID playerUuid) {
		return playerHealth.getFloat(playerUuid);
	}

	@Override
	public int getKillFeedOffset() {
		return !PFClientConfig.getGameGuiStyle().hasBuiltInPlayerHeads() ? 38 : 0;
	}
}
