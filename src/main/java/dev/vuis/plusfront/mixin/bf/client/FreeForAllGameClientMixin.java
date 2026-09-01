package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.impl.ffa.FreeForAllGame;
import com.boehmod.blockfront.game.impl.ffa.FreeForAllGameClient;
import com.boehmod.blockfront.game.impl.ffa.FreeForAllPlayerManager;
import com.mojang.blaze3d.vertex.PoseStack;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FreeForAllGameClient.class)
public abstract class FreeForAllGameClientMixin extends AbstractGameClient<FreeForAllGame, FreeForAllPlayerManager> {
	public FreeForAllGameClientMixin(@NotNull BFClientManager manager, @NotNull FreeForAllGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);
	}

	@Inject(
		method = "onRenderGui",
		at = @At("HEAD")
	)
	private void customRenderGui(
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
		switch (PFClientConfig.getGameGuiStyle()) {
			case OLD -> {
				PFGameGuiRendering.oldTopElements(
					minecraft, dataHandler,
					graphics, font,
					getStageTimer(), game.getPlayerManager(),
					midX
				);
			}
		}
	}
}
