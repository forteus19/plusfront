package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.common.match.kill.KillSectionPlayer;
import com.boehmod.blockfront.common.match.kill.KillSectionText;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.client.config.PFClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({KillSectionPlayer.class, KillSectionText.class})
public abstract class KillSectionPlayerAndTextMixin {
	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/client/render/BFRendering;centeredCharSequence(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/util/FormattedCharSequence;FF)V"
		)
	)
	private void disableShadowOnText(PoseStack poseStack, Font font, GuiGraphics graphics, FormattedCharSequence text, float x, float y, Operation<Void> original) {
		if (PFClientConfig.getGameGuiStyle().showOldKillFeed()) {
			graphics.drawString(font, text, x - font.width(text) / 2f, y, 0xFFFFFF, false);
		} else {
			original.call(poseStack, font, graphics, text, x, y);
		}
	}
}
