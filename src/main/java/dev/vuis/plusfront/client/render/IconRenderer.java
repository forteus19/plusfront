package dev.vuis.plusfront.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface IconRenderer {
	void render(GuiGraphics graphics, PoseStack poseStack);
}
