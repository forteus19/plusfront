package dev.vuis.plusfront.client.render;

import com.boehmod.blockfront.util.BFRes;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.client.PFClientTemp;
import dev.vuis.plusfront.client.config.PFClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class IconRenderers {
	private static final ResourceLocation BOMB_TEXTURE = PlusFront.res("textures/gui/defusal/bomb.png");
	private static final ResourceLocation BOMB_BLINK_TEXTURE = PlusFront.res("textures/gui/defusal/bomb_blink.png");
	private static final ResourceLocation OLD_BOMB_TEXTURE = BFRes.loc("textures/gui/game/defusal/bomb_planted.png");
	private static final ResourceLocation OLD_BOMB_BLINK_TEXTURE = BFRes.loc("textures/gui/game/defusal/bomb_planted_blink.png");

	public static final IconRenderer BOMB = (graphics, poseStack) -> {
		ResourceLocation texture, blinkTexture;
		float offset = 0f, scale = 1f;

		switch (PFClientConfig.getGameGuiStyle()) {
			case MODERN, CS2 -> {
				texture = BOMB_TEXTURE;
				blinkTexture = BOMB_BLINK_TEXTURE;
				offset = 0.5f;
			}
			case OLD -> {
				texture = OLD_BOMB_TEXTURE;
				blinkTexture = OLD_BOMB_BLINK_TEXTURE;
				scale = 11f / 16f;
			}
			default -> {
				return;
			}
		}

		float width = 32f * scale;
		float height = 16f * scale;
		boolean blink = PFClientTemp.frameMillis % 1000 < 500;

		PFGuiRenderUtil.centeredTexture(poseStack, texture, 0f, offset, width, height, 0xFFFFFFFF);
		if (blink) {
			PFGuiRenderUtil.centeredTexture(poseStack, blinkTexture, 0f, offset, width, height, 0xFFFFFFFF);
		}
	};

	private IconRenderers() {
		throw new AssertionError();
	}

	public static void renderAt(
		GuiGraphics graphics,
		PoseStack poseStack,
		IconRenderer renderer,
		float x,
		float y
	) {
		poseStack.pushPose();
		poseStack.translate(x, y, 0f);

		renderer.render(graphics, poseStack);

		poseStack.popPose();
	}
}
