package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.gui.layer.MatchGuiLayer;
import com.boehmod.blockfront.game.AbstractGame;
import com.llamalad7.mixinextras.sugar.Local;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MatchGuiLayer.class)
public abstract class MatchGuiLayerMixin {
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
		if (game.getClass() == DefusalGame.class) {
			return y + 38;
		} else {
			return y;
		}
	}
}
