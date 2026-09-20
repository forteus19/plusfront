package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.gui.layer.MatchGuiLayer;
import com.boehmod.blockfront.client.render.minimap.MinimapRendering;
import com.boehmod.blockfront.client.render.minimap.MinimapWaypoint;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.client.config.GameGuiStyle;
import dev.vuis.plusfront.client.config.PFClientConfig;
import dev.vuis.plusfront.game.tag.IModifyRendering;
import java.util.Collection;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MatchGuiLayer.class)
public abstract class MatchGuiLayerMixin {
	@Redirect(
		method = "renderMinimap",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/client/render/minimap/MinimapRendering;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/player/LocalPlayer;Ljava/util/Collection;IIFII)V",
			ordinal = 0
		)
	)
	private void modifyMinimapPosition(
		@NotNull PoseStack poseStack,
		@NotNull GuiGraphics graphics,
		@NotNull Font font,
		@NotNull LocalPlayer player,
		@NotNull Collection<MinimapWaypoint> waypoints,
		int x,
		int y,
		float zoom,
		int width,
		int height
	) {
		if (PFClientConfig.getGameGuiStyle() == GameGuiStyle.OLD) {
			x = y = 6;
		}
		MinimapRendering.render(poseStack, graphics, font, player, waypoints, x, y, zoom, width, height);
	}

	@Redirect(
		method = "method_9183",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
			ordinal = 0
		)
	)
	private static void modifyKillFeedSpacing(PoseStack instance, float x, float y, float z, @Local(argsOnly = true) int baseY, @Local(ordinal = 2) int entryIndex) {
		GameGuiStyle guiStyle = PFClientConfig.getGameGuiStyle();
		if (guiStyle.showOldKillFeed()) {
			y = baseY + entryIndex * 12f;
		}
		if (guiStyle == GameGuiStyle.OLD) {
			x = 6f;
		}
		instance.translate(x, y, z);
	}

	@ModifyConstant(
		method = "method_503",
		constant = @Constant(
			intValue = 25,
			ordinal = 0
		)
	)
	private int shiftKillFeedInDefusal(int y, @Local(argsOnly = true) AbstractGameClient<?, ?> gameClient) {
		if (gameClient instanceof IModifyRendering modifyRendering) {
			y += modifyRendering.getKillFeedOffset();
		}
		if (PFClientConfig.getGameGuiStyle() == GameGuiStyle.OLD) {
			y -= 17;
		}
		return y;
	}
}
