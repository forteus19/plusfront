package dev.vuis.plusfront.client.render.game;

import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.mixin.bf.GameStageTimerAccessor;
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
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public final class PFGameGuiRendering {
	@SuppressWarnings("NoTranslation")
	public static final Component WAITING_MESSAGE = Component.translatable("bf.message.match.title.waiting");
	@SuppressWarnings("NoTranslation")
	public static final Component GAME_OVER_MESSAGE = Component.translatable("bf.message.match.title.gameover");

	private static final ResourceLocation DEAD_TEXTURE = BFRes.loc("textures/gui/dead.png");

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
		Font font,
		int midX,
		int y,
		GameStageTimer timer
	) {
		BFRendering.rectangle(graphics, midX - 19, y, 38, 13, BFRendering.translucentBlack());
		BFRendering.centeredString(font, graphics, oldTimerComponent(timer), midX, y + 3);
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

	public static void oldTopElements(
		Minecraft minecraft,
		PlayerDataHandler<?> dataHandler,
		GuiGraphics graphics,
		Font font,
		GameStageTimer timer,
		@Nullable GameTeam axisTeam,
		@Nullable GameTeam alliesTeam,
		int midX
	) {
		oldTimer(graphics, font, midX, 1, timer);

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
		Font font,
		GameStageTimer timer,
		AbstractGamePlayerManager<?> playerManager,
		int midX
	) {
		oldTopElements(
			minecraft, dataHandler,
			graphics, font,
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
			graphics, font,
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
				lineColor = FastColor.ARGB32.color(0xFF, team.getColor());
			}
		}

		BFRendering.rectangleWithDarkShadow(poseStack, graphics, bgX, bgY, bgWidth, 15f, BFRendering.translucentBlack());
		BFRendering.centeredComponent2d(poseStack, font, graphics, message, midX, baseY - 4f, 1f);
		BFRendering.rectangle(poseStack, graphics, bgX, bgY + 14f, bgWidth, 1f, lineColor);
	}
}
