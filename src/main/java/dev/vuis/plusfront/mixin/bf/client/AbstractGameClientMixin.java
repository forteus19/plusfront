package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameType;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.client.config.PFClientConfig;
import dev.vuis.plusfront.client.render.game.PFGameGuiRendering;
import dev.vuis.plusfront.util.PFUtil;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractGameClient.class)
public abstract class AbstractGameClientMixin<G extends AbstractGame<G, P, ?>, P extends AbstractGamePlayerManager<G>> {
	@Shadow
	@Final
	@NotNull
	protected G game;

	@Inject(
		method = "renderGui",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/game/AbstractGameClient;onRenderGui(Lnet/minecraft/client/Minecraft;Lcom/boehmod/blockfront/client/BFClientManager;Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/multiplayer/ClientLevel;Lcom/boehmod/blockfront/client/player/BFClientPlayerData;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Ljava/util/Set;IIIIFF)V",
			ordinal = 0
		)
	)
	private void customPreRenderGui(
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
		float delta,
		CallbackInfo ci
	) {
		if (!PFClientConfig.getGameGuiStyle().showOldWaitingMessage()) {
			return;
		}

		Component waitingMessage = switch (game.getStatus()) {
			case PRE_GAME -> PFGameGuiRendering.WAITING_MESSAGE.copy()
				.append(" ")
				.append(Component.literal("(" + players.size() + "/" + game.getMinimumPlayers() + ")").withStyle(ChatFormatting.GRAY));
			case POST_GAME -> PFGameGuiRendering.GAME_OVER_MESSAGE;
			default -> null;
		};

		if (waitingMessage != null) {
			PFGameGuiRendering.oldWaitingMessage(
				game.getPlayerManager(),
				graphics, poseStack, font,
				waitingMessage,
				midX, midY
			);
		}
	}

	@Inject(
		method = "renderNotifications",
		at = @At("HEAD"),
		cancellable = true
	)
	private void hideNotificationsForGuiStyle(GuiGraphics graphics, Font font, PoseStack poseStack, int width, float delta, CallbackInfo ci) {
		if (PFClientConfig.getGameGuiStyle().showNotificationsInChat()) {
			ci.cancel();
		}
	}

	@ModifyVariable(
		method = "renderNotifications",
		ordinal = 1,
		at = @At(
			value = "STORE",
			ordinal = 0
		)
	)
	private int setNotificationOffset(int original) {
		return PFClientConfig.getGameGuiStyle().getNotificationOffset();
	}

	@Inject(
		method = "renderGameElements",
		at = @At("HEAD"),
		cancellable = true
	)
	private void hideGameElementsForGuiStyle(GuiGraphics graphics, Font font, PoseStack poseStack, int x, float delta, CallbackInfo ci) {
		GameType gameType = PFUtil.getGameType(game);
		if (gameType == null) {
			return;
		}

		if (PFClientConfig.getGameGuiStyle().shouldHideGameElements(gameType)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "addNotification(Ljava/util/List;ILjava/lang/String;Lnet/minecraft/world/phys/Vec3;II)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void replaceNotificationWithChat(
		@NotNull List<Component> lines,
		int time,
		@NotNull String key,
		Vec3 position,
		int compassColor,
		int pointerColor,
		CallbackInfo ci
	) {
		if (PFClientConfig.getGameGuiStyle().showNotificationsInChat()) {
			ChatComponent chat = Minecraft.getInstance().gui.getChat();
			for (Component line : lines) {
				chat.addMessage(BFStats.NOTICE_PREFIX.copy().append(line));
			}

			ci.cancel();
		}
	}
}
