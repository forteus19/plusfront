package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.game.AbstractCapturePoint;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.CapturePointGameClient;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.client.config.PFClientConfig;
import dev.vuis.plusfront.client.render.game.PFGameGuiRendering;
import dev.vuis.plusfront.game.PFGameHelper;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapturePointGameClient.class)
public abstract class CapturePointGameClientMixin<G extends AbstractGame<G, P, ?>, P extends AbstractGamePlayerManager<G>> extends AbstractGameClient<G, P> {
	@Shadow
	@Nullable
	protected AbstractCapturePoint<?> currentCapturePoint;
	@Shadow
	@Nullable
	private Component currentStatusMessage;

	public CapturePointGameClientMixin(@NotNull BFClientManager manager, @NotNull G game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);
	}

	@Inject(
		method = "onRenderGui",
		at = @At("HEAD"),
		cancellable = true
	)
	private void overrideStatusRender(
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
		if (PFClientConfig.getGameGuiStyle().showOldCapturingStatus()) {
			ci.cancel();

			AbstractCapturePoint<?> capturePoint = currentCapturePoint;
			Component statusMessage = currentStatusMessage;
			if (capturePoint == null || statusMessage == null) {
				return;
			}

			if (PFGameHelper.isSameTeam(capturePoint.cbTeam, game.getPlayerManager().getPlayerTeam(player.getUUID()))) {
				return;
			}

			PFGameGuiRendering.oldCapturingStatus(
				graphics, poseStack, font,
				capturePoint, statusMessage,
				height, midX, delta
			);
		}
	}
}
