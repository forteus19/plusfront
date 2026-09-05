package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.common.match.kill.KillEntryType;
import com.boehmod.blockfront.common.match.kill.KillFeedEntry;
import dev.vuis.plusfront.client.config.PFClientConfig;
import dev.vuis.plusfront.client.render.game.PFGameGuiRendering;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KillFeedEntry.class)
public abstract class KillFeedEntryMixin {
	@Shadow
	private @NotNull KillEntryType type;

	@Inject(
		method = "method_3214",
		at = @At("HEAD"),
		cancellable = true
	)
	private void renderOldBackground(GuiGraphics graphics, int width, CallbackInfo ci) {
		if (PFClientConfig.getGameGuiStyle().showOldKillFeed()) {
			PFGameGuiRendering.oldKillFeedBackground(graphics, type, width);
			ci.cancel();
		}
	}
}
