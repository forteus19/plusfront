package dev.vuis.plusfront.client.render.game;

import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractCapturePoint;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.tag.IHasCapturePoints;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.client.render.IconRenderer;
import dev.vuis.plusfront.client.render.IconRenderers;
import dev.vuis.plusfront.ex.GameStageTimerEx;
import dev.vuis.plusfront.mixin.bf.GameStageTimerAccessor;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public final class PFGameGuiRendering {
	@SuppressWarnings("NoTranslation")
	public static final Component WAITING_MESSAGE = Component.translatable("bf.message.match.title.waiting");
	@SuppressWarnings("NoTranslation")
	public static final Component GAME_OVER_MESSAGE = Component.translatable("bf.message.match.title.gameover");

	private static final ResourceLocation DEAD_TEXTURE = BFRes.loc("textures/gui/dead.png");
	private static final ResourceLocation ARROW_LEFT_TEXTURE = BFRes.loc("textures/gui/game/domination/cpoint_arrow_left_black.png");
	private static final ResourceLocation ARROW_RIGHT_TEXTURE = BFRes.loc("textures/gui/game/domination/cpoint_arrow_right_black.png");
	private static final ResourceLocation NEUTRAL_ICON_TEXTURE = BFRes.loc("textures/misc/bfneutralicon.png");

	private PFGameGuiRendering() {
		throw new AssertionError();
	}

	private static Component oldTimerComponent(GameStageTimer timer) {
		GameStageTimerAccessor accessor = (GameStageTimerAccessor) (Object) timer;
		int secondsRemaining = timer.getSecondsRemaining();

		return Component.literal(BFRendering.formatTime(secondsRemaining))
			.withStyle(
				secondsRemaining <= accessor.getWarningThreshold() ? ChatFormatting.RED : ChatFormatting.WHITE,
				ChatFormatting.BOLD
			);
	}

	private static void oldTimer(
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		int midX,
		int y,
		GameStageTimer timer
	) {
		GameStageTimerEx timerEx = (GameStageTimerEx) (Object) timer;
		IconRenderer iconRenderer = timerEx.pf$getIconRenderer();

		BFRendering.rectangle(graphics, midX - 19, y, 38, 13, BFRendering.translucentBlack());

		if (iconRenderer == null) {
			BFRendering.centeredString(font, graphics, oldTimerComponent(timer), midX, y + 3);
		} else {
			IconRenderers.renderAt(
				graphics, poseStack,
				iconRenderer,
				midX, y + 6.5f
			);
		}
	}

	private static void oldPlayerHead(
		Minecraft minecraft,
		PlayerDataHandler<?> dataHandler,
		GuiGraphics graphics,
		UUID playerUuid,
		int x,
		int y
	) {
		ClientPacketListener connection = minecraft.getConnection();
		if (connection == null) {
			return;
		}

		PlayerInfo playerInfo = connection.getPlayerInfo(playerUuid);
		BFAbstractPlayerData<?, ?, ?, ?> playerData = dataHandler.getPlayerData(playerUuid);

		BFRendering.rectangle(graphics, x - 1, y - 1, 13, 13, BFRendering.translucentBlack());

		if (playerInfo == null || playerInfo.getGameMode() == GameType.SPECTATOR || playerData.isOutOfGame()) {
			graphics.blit(
				DEAD_TEXTURE,
				x, y, 11, 11,
				0f, 0f,
				8, 8, 8, 8
			);
		} else {
			PlayerFaceRenderer.draw(
				graphics,
				playerInfo.getSkin(),
				x, y, 11
			);
		}
	}

	private static void oldTopElements(
		Minecraft minecraft,
		PlayerDataHandler<?> dataHandler,
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		GameStageTimer timer,
		@Nullable GameTeam axisTeam,
		@Nullable GameTeam alliesTeam,
		int midX
	) {
		oldTimer(graphics, poseStack, font, midX, 1, timer);

		if (axisTeam != null) {
			UUID[] players = axisTeam.getPlayers().toArray(new UUID[0]);

			for (int i = 0; i < players.length; i++) {
				oldPlayerHead(
					minecraft, dataHandler,
					graphics,
					players[i],
					midX - 32 - i * 14, 2
				);
			}
		}

		if (alliesTeam != null) {
			UUID[] players = alliesTeam.getPlayers().toArray(new UUID[0]);

			for (int i = 0; i < players.length; i++) {
				oldPlayerHead(
					minecraft, dataHandler,
					graphics,
					players[i],
					midX + 21 + i * 14, 2
				);
			}
		}
	}

	public static void oldTopElements(
		Minecraft minecraft,
		PlayerDataHandler<?> dataHandler,
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		GameStageTimer timer,
		AbstractGamePlayerManager<?> playerManager,
		int midX
	) {
		oldTopElements(
			minecraft, dataHandler,
			graphics, poseStack, font,
			timer,
			playerManager.getTeamByName(BFStats.AXIS_TEAM_NAME),
			playerManager.getTeamByName(BFStats.ALLIES_TEAM_NAME),
			midX
		);
	}

	private static Component scoreComponent(GameTeam team) {
		return Component.literal(Integer.toString(team.getStatInt(BFStats.SCORE))).withStyle(team.getStyleText());
	}

	@SuppressWarnings("deprecation")
	private static void oldScore(
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		GameTeam team,
		float x
	) {
		BFRendering.rectangle(poseStack, graphics, x, 15f, 18.5f, 10f, BFRendering.translucentBlack());
		BFRendering.centeredComponent2d(poseStack, font, graphics, scoreComponent(team), x + 9.75f, 16.5f);
	}

	private static void oldScoreOnly(
		Minecraft minecraft,
		PlayerDataHandler<?> dataHandler,
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		GameStageTimer timer,
		@Nullable GameTeam axisTeam,
		@Nullable GameTeam alliesTeam,
		int midX
	) {
		oldTopElements(
			minecraft, dataHandler,
			graphics, poseStack, font,
			timer, axisTeam, alliesTeam,
			midX
		);

		if (axisTeam != null) {
			oldScore(graphics, poseStack, font, axisTeam, midX - 19f);
		}

		if (alliesTeam != null) {
			oldScore(graphics, poseStack, font, alliesTeam, midX + 0.5f);
		}
	}

	public static void oldScoreOnly(
		Minecraft minecraft,
		PlayerDataHandler<?> dataHandler,
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		GameStageTimer timer,
		AbstractGamePlayerManager<?> playerManager,
		int midX
	) {
		oldScoreOnly(
			minecraft, dataHandler,
			graphics, poseStack, font,
			timer,
			playerManager.getTeamByName(BFStats.AXIS_TEAM_NAME),
			playerManager.getTeamByName(BFStats.ALLIES_TEAM_NAME),
			midX
		);
	}

	private static void oldScoreBar(
		GuiGraphics graphics,
		PoseStack poseStack,
		GameTeam team,
		int arrows,
		int x,
		int y,
		boolean alignRight,
		float renderTime
	) {
		int width = 78, height = 8;

		int filledWidth = (int) (width * (team.getStatInt(BFStats.SCORE) / 500f));
		int filledX = alignRight ? 78 - filledWidth : 0;
		int color = team.getColor();

		ResourceLocation arrowTexture = alignRight ? ARROW_LEFT_TEXTURE : ARROW_RIGHT_TEXTURE;
		float arrowAlpha = Math.min(0.5f + Mth.sin(renderTime / 15f) / 2f, 0.5f);

		poseStack.pushPose();
		poseStack.translate(x, y, 0f);

		BFRendering.rectangle(graphics, -1, -1, width + 2, height + 2, BFRendering.translucentBlack());
		BFRendering.rectangle(graphics, 0, 0, width, height, 0x59000000);
		BFRendering.rectangle(graphics, 0, 0, width, height, FastColor.ARGB32.color(0x59, color));
		BFRendering.rectangle(graphics, filledX, 0, filledWidth, height, FastColor.ARGB32.opaque(color));

		for (int i = 0; i < arrows; i++) {
			BFRendering.texture(poseStack, graphics, arrowTexture, alignRight ? (width - 7 * i - 7) : (1 + 7 * i), 1, 6, 6, arrowAlpha);
		}

		poseStack.popPose();
	}

	private static void oldCapturePointIcons(
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		List<? extends AbstractCapturePoint<?>> capturePoints,
		int y,
		int midX,
		float renderTime
	) {
		int spacing = 18;

		int numCapturePoints = capturePoints.size();

		int startX = midX - (spacing / 2) * numCapturePoints + 1;
		float capturingAlpha = Math.max(0.5f * Mth.sin(renderTime / 5f), 0.01f);

		for (int i = 0; i < numCapturePoints; i++) {
			AbstractCapturePoint<?> capturePoint = capturePoints.get(i);
			GameTeam cbTeam = capturePoint.getCbTeam();
			ResourceLocation icon = capturePoint.getIcon();
			String name = capturePoint.name;

			int x = startX + i * spacing;
			float alpha = capturePoint.isBeingCaptured ? capturingAlpha : 0.5f;
			int color = cbTeam != null ? cbTeam.getColor() : 0xFFFFFF;

			BFRendering.tintedTexture(poseStack, graphics, NEUTRAL_ICON_TEXTURE, x, y, 14, 14, 0, alpha, color);
			if (icon != null) {
				BFRendering.texture(poseStack, graphics, icon, x, y, 14, 14, alpha);
			}

			graphics.drawString(font, name, x + spacing / 2 - font.width(name) / 2 - 1, y + 17, 0xFFFFFFFF, false);
		}
	}

	private static void oldCapturePointScore(
		Minecraft minecraft,
		PlayerDataHandler<?> dataHandler,
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		GameStageTimer timer,
		@Nullable GameTeam axisTeam,
		@Nullable GameTeam alliesTeam,
		List<? extends AbstractCapturePoint<?>> capturePoints,
		int midX,
		float renderTime
	) {
		int axisArrows = 0;
		int alliesArrows = 0;

		for (AbstractCapturePoint<?> capturePoint : capturePoints) {
			if (capturePoint.cbTeam == axisTeam) {
				axisArrows++;
			} else if (capturePoint.cbTeam == alliesTeam) {
				alliesArrows++;
			}
		}

		oldScoreOnly(
			minecraft, dataHandler,
			graphics, poseStack, font,
			timer, axisTeam, alliesTeam,
			midX
		);

		if (axisTeam != null) {
			oldScoreBar(
				graphics, poseStack,
				axisTeam, axisArrows,
				midX - 99, 16,
				false,
				renderTime
			);
		}

		if (alliesTeam != null) {
			oldScoreBar(
				graphics, poseStack,
				alliesTeam, alliesArrows,
				midX + 21, 16,
				true,
				renderTime
			);
		}

		oldCapturePointIcons(
			graphics, poseStack, font,
			capturePoints,
			27,
			midX, renderTime
		);
	}

	public static <G extends AbstractGame<G, ?, ?> & IHasCapturePoints<?, ?>> void oldCapturePointScore(
		Minecraft minecraft,
		PlayerDataHandler<?> dataHandler,
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		GameStageTimer timer,
		G game,
		int midX,
		float renderTime
	) {
		AbstractGamePlayerManager<?> playerManager = game.getPlayerManager();

		oldCapturePointScore(
			minecraft, dataHandler,
			graphics, poseStack, font,
			timer,
			playerManager.getTeamByName(BFStats.AXIS_TEAM_NAME),
			playerManager.getTeamByName(BFStats.ALLIES_TEAM_NAME),
			game.getCapturePoints(),
			midX, renderTime
		);
	}

	@SuppressWarnings("deprecation")
	public static void oldWaitingMessage(
		AbstractGamePlayerManager<?> playerManager,
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		Component message,
		int midX,
		int midY
	) {
		int baseY = midY / 2 + 2;
		float bgWidth = font.width(message) + 6;
		float bgX = midX - bgWidth / 2f;
		float bgY = baseY - 7.5f;

		int lineColor = 0xFFFFFFFF;

		LocalPlayer localPlayer = Minecraft.getInstance().player;
		if (localPlayer != null) {
			GameTeam team = playerManager.getPlayerTeam(localPlayer.getUUID());
			if (team != null) {
				lineColor = FastColor.ARGB32.opaque(team.getColor());
			}
		}

		BFRendering.rectangleWithDarkShadow(poseStack, graphics, bgX, bgY, bgWidth, 15f, BFRendering.translucentBlack());
		BFRendering.centeredComponent2d(poseStack, font, graphics, message, midX, baseY - 4f, 1f);
		BFRendering.rectangle(poseStack, graphics, bgX, bgY + 14f, bgWidth, 1f, lineColor);
	}
}
