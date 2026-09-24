package dev.vuis.plusfront.client.render;

import com.boehmod.blockfront.client.render.BFRendering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.vuis.plusfront.mixin.minecraft.client.GuiGraphicsAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;

public final class PFGuiRenderUtil {
	private PFGuiRenderUtil() {
		throw new AssertionError();
	}

	public static void flushIfUnmanaged(GuiGraphics graphics) {
		((GuiGraphicsAccessor) graphics).invokeFlushIfUnmanaged();
	}

	public static void rectangle(
		GuiGraphics graphics,
		PoseStack poseStack,
		float x,
		float y,
		float width,
		float height,
		int color
	) {
		float x2 = x + width;
		float y2 = y + height;

		Matrix4f matrix = poseStack.last().pose();
		VertexConsumer vertices = graphics.bufferSource().getBuffer(RenderType.gui());

		vertices
			.addVertex(matrix, x, y, 0f).setColor(color)
			.addVertex(matrix, x, y2, 0f).setColor(color)
			.addVertex(matrix, x2, y2, 0f).setColor(color)
			.addVertex(matrix, x2, y, 0f).setColor(color);

		flushIfUnmanaged(graphics);
	}

	public static void gradient(
		GuiGraphics graphics,
		PoseStack poseStack,
		float x,
		float y,
		float width,
		float height,
		int ltColor,
		int rbColor,
		boolean vertical
	) {
		float x2 = x + width;
		float y2 = y + height;

		Matrix4f matrix = poseStack.last().pose();
		VertexConsumer vertices = graphics.bufferSource().getBuffer(RenderType.gui());

		vertices
			.addVertex(matrix, x, y, 0f).setColor(ltColor)
			.addVertex(matrix, x, y2, 0f).setColor(vertical ? rbColor : ltColor)
			.addVertex(matrix, x2, y2, 0f).setColor(rbColor)
			.addVertex(matrix, x2, y, 0f).setColor(vertical ? ltColor : rbColor);

		flushIfUnmanaged(graphics);
	}

	public static void texture(
		PoseStack poseStack,
		ResourceLocation texture,
		float x,
		float y,
		float width,
		float height,
		int color
	) {
		float x2 = x + width;
		float y2 = y + height;

		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		builder
			.addVertex(matrix, x, y, 0f).setUv(0f, 0f)
			.addVertex(matrix, x, y2, 0f).setUv(0f, 1f)
			.addVertex(matrix, x2, y2, 0f).setUv(1f, 1f)
			.addVertex(matrix, x2, y, 0f).setUv(1f, 0f);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texture);
		BFRendering.shaderColor(color);
		BufferUploader.drawWithShader(builder.buildOrThrow());
		BFRendering.resetShaderColor();
	}

	public static void centeredTexture(
		PoseStack poseStack,
		ResourceLocation texture,
		float x,
		float y,
		float width,
		float height,
		int color
	) {
		texture(poseStack, texture, x - width / 2f, y - height / 2f, width, height, color);
	}

	public static void textureWithShadow(
		PoseStack poseStack,
		ResourceLocation texture,
		float x,
		float y,
		float width,
		float height,
		int color,
		float shadowOffset
	) {
		texture(
			poseStack, texture, x + shadowOffset, y + shadowOffset, width, height,
			FastColor.ARGB32.lerp(0.25f, 0xFF000000, FastColor.ARGB32.opaque(color))
		);
		texture(poseStack, texture, x, y, width, height, color);
	}

	public static void text(
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		Object text,
		float x,
		float y,
		float scale,
		int color,
		boolean shadow
	) {
		poseStack.pushPose();
		poseStack.translate(x, y, 0f);
		if (scale != 1f) {
			poseStack.scale(scale, scale, 1f);
		}

		switch (text) {
			case String string -> {
				font.drawInBatch(
					string,
					0f, 0f,
					color,
					shadow,
					poseStack.last().pose(),
					graphics.bufferSource(),
					Font.DisplayMode.NORMAL,
					0x00000000,
					0xF000F0
				);
			}
			case Component component -> {
				font.drawInBatch(
					component,
					0f, 0f,
					color,
					shadow,
					poseStack.last().pose(),
					graphics.bufferSource(),
					Font.DisplayMode.NORMAL,
					0x00000000,
					0xF000F0
				);
			}
			default -> throw new IllegalArgumentException("Unsupported text class");
		}

		poseStack.popPose();

		flushIfUnmanaged(graphics);
	}

	public static void centeredText(
		GuiGraphics graphics,
		PoseStack poseStack,
		Font font,
		Object text,
		float x,
		float y,
		float scale,
		int color,
		boolean shadow
	) {
		float width = switch (text) {
			case String string -> font.width(string);
			case Component component -> font.width(component);
			default -> throw new IllegalArgumentException("Unsupported text class");
		};
		text(graphics, poseStack, font, text, x - (width * scale) / 2f, y, scale, color, shadow);
	}
}
