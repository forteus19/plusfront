package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.render.game.element.TimeGameElement;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStageTimer;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.client.render.IconRenderer;
import dev.vuis.plusfront.client.render.IconRenderers;
import dev.vuis.plusfront.ex.GameStageTimerEx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TimeGameElement.class)
public abstract class TimeGameElementMixin<G extends AbstractGame<G, P, ?>, P extends AbstractGamePlayerManager<G>> {
	@Shadow
	public abstract int getWidth(@NotNull Font font);

	@Unique
	private GameStageTimer pf$timer;

	@Inject(
		method = "update",
		at = @At("HEAD")
	)
	private void updateTimer(
		@NotNull Minecraft minecraft,
		@NotNull G game,
		@NotNull P playerManager,
		@NotNull AbstractGameClient<G, P> gameClient,
		@NotNull LocalPlayer player,
		CallbackInfo ci
	) {
		pf$timer = gameClient.getStageTimer();
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/client/render/game/element/ClientGameElement;render(Lnet/minecraft/client/gui/GuiGraphics;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;IIF)V",
			shift = At.Shift.AFTER,
			ordinal = 0
		),
		cancellable = true
	)
	private void handleIconRendering(GuiGraphics graphics, PoseStack poseStack, Font font, int x, int y, float delta, CallbackInfo ci) {
		if (pf$timer == null) {
			return;
		}
		IconRenderer iconRenderer = ((GameStageTimerEx) (Object) pf$timer).pf$getIconRenderer();
		if (iconRenderer == null) {
			return;
		}

		IconRenderers.renderAt(graphics, poseStack, iconRenderer, x + getWidth(font) / 2f, y + 7.5f);

		ci.cancel();
	}
}
