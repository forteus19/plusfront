package dev.vuis.plusfront.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class PFGuiRenderUtil {
	private PFGuiRenderUtil() {
		throw new AssertionError();
	}

	public static void texture(
		PoseStack poseStack,
		ResourceLocation texture,
		float x,
		float y,
		float width,
		float height
	) {
		float x2 = x + width;
		float y2 = y + height;

		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		builder.addVertex(matrix, x, y, 0f).setUv(0f, 0f);
		builder.addVertex(matrix, x, y2, 0f).setUv(0f, 1f);
		builder.addVertex(matrix, x2, y2, 0f).setUv(1f, 1f);
		builder.addVertex(matrix, x2, y, 0f).setUv(1f, 0f);

		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		BufferUploader.drawWithShader(builder.buildOrThrow());
	}

	public static void centeredTexture(
		PoseStack poseStack,
		ResourceLocation texture,
		float x,
		float y,
		float width,
		float height
	) {
		texture(poseStack, texture, x - width / 2f, y - width / 2f, width, height);
	}
}
