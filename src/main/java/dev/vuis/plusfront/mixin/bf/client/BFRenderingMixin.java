package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.render.BFRendering;
import dev.vuis.plusfront.client.PFClientTemp;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BFRendering.class)
public abstract class BFRenderingMixin {
	@Inject(
		method = "fancyRectangle(Lnet/minecraft/client/gui/GuiGraphics;IIIII)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void replaceFancyRectangle(GuiGraphics graphics, int x, int y, int width, int height, int color, CallbackInfo ci) {
		if (PFClientTemp.disableFancyRectangles) {
			BFRendering.rectangle(graphics, x, y, width, height, color);
			ci.cancel();
		}
	}

	@Inject(
		method = "fancyRectangle(Lnet/minecraft/client/gui/GuiGraphics;IIIIIF)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void replaceFancyRectangle(GuiGraphics graphics, int x, int y, int width, int height, int color, float alpha, CallbackInfo ci) {
		if (PFClientTemp.disableFancyRectangles) {
			BFRendering.rectangle(graphics, x, y, width, height, color, alpha);
			ci.cancel();
		}
	}
}
