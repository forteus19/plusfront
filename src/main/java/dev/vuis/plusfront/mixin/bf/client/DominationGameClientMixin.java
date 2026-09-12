package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.game.CapturePointGameClient;
import com.boehmod.blockfront.game.impl.dom.DominationGame;
import com.boehmod.blockfront.game.impl.dom.DominationGameClient;
import com.boehmod.blockfront.game.impl.dom.DominationPlayerManager;
import com.boehmod.blockfront.game.tag.client.IAllowsPingsClient;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.client.config.GameGuiStyle;
import dev.vuis.plusfront.client.config.PFClientConfig;
import dev.vuis.plusfront.client.render.game.PFGameGuiRendering;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DominationGameClient.class)
public abstract class DominationGameClientMixin extends CapturePointGameClient<DominationGame, DominationPlayerManager> implements IAllowsPingsClient {
	public DominationGameClientMixin(@NotNull BFClientManager manager, @NotNull DominationGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);
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
		float delta
	) {
		super.onRenderGui(
			minecraft, manager, player, level, playerData, graphics, font, poseStack, bufferSource, players, width, height, midX, midY, renderTime, delta
		);

		if (PFClientConfig.getGameGuiStyle() == GameGuiStyle.OLD) {
			PFGameGuiRendering.oldCapturePointScore(
				minecraft, dataHandler,
				graphics, poseStack, font,
				getStageTimer(), game,
				midX, renderTime
			);
		}
	}
}
