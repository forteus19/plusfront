package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.gui.layer.MatchGuiLayer;
import com.boehmod.blockfront.game.AbstractGame;
import com.llamalad7.mixinextras.sugar.Local;
import dev.vuis.plusfront.client.config.PFClientConfig;
import dev.vuis.plusfront.game.tag.IModifyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MatchGuiLayer.class)
public abstract class MatchGuiLayerMixin {
	@ModifyConstant(
		method = "method_9183",
		constant = @Constant(
			intValue = 16,
			ordinal = 0
		)
	)
	private static int modifyKillFeedSpacing(int constant) {
		return PFClientConfig.getGameGuiStyle().showOldKillFeed() ? 12 : constant;
	}

	@ModifyArg(
		method = "method_503",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/client/gui/layer/MatchGuiLayer;renderKillFeed(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lcom/boehmod/blockfront/game/AbstractGame;Lcom/boehmod/blockfront/game/AbstractGameClient;ZIF)V",
			ordinal = 0
		),
		index = 6
	)
	private int shiftKillFeedInDefusal(int y, @Local(argsOnly = true) AbstractGame<?, ?, ?> game) {
		return y + (game instanceof IModifyRendering modifyRendering ? modifyRendering.getKillFeedOffset() : 0);
	}
}
