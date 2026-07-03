package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.util.BFStyles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.vuis.plusfront.util.Vec2d;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Stores the client state of a defusal bomb site.
 *
 * @see DefusalGame
 * @see DefusalGameClient
 */
public class BombSiteClient {
	private static final Component BOMBSITE_COMPONENT = Component.literal("BOMBSITE");
	private static final Component PLANT_COMPONENT = Component.literal("PLANT").withStyle(ChatFormatting.GOLD);

	private final BombSite data;
	private final Component nameComponent;

	private boolean inPlantArea = false;

	/**
	 * @param data the bomb site data given by {@link DefusalGame}
	 */
	public BombSiteClient(BombSite data) {
		this.data = data;
		this.nameComponent = Component.literal(data.name()).withStyle(BFStyles.BOLD);
	}

	/**
	 * Called every client tick to update state used when rendering the waypoint.
	 *
	 * @param player the local player
	 * @param showPlantStatus whether to show whether the player can plant at the bombsite
	 */
	public void update(Player player, boolean showPlantStatus) {
		inPlantArea = showPlantStatus && data.isWithinArea(player.position());
	}

	/**
	 * Renders waypoints showing info about this bomb site into the world.
	 *
	 * @param poseStack the current pose stack
	 * @param font used when rendering components
	 * @param graphics the current graphics context
	 * @param camera the current camera
	 */
	public void render(
		PoseStack poseStack,
		Font font,
		GuiGraphics graphics,
		Camera camera
	) {
		for (Vec3 waypoint : data.waypoints()) {
			BFRendering.component(
				poseStack, font, camera, graphics,
				nameComponent,
				waypoint.x, waypoint.y + 2.5f, waypoint.z,
				2.0f, 0xFFFFFFFF, true
			);
			BFRendering.component(
				poseStack, font, camera, graphics,
				inPlantArea ? PLANT_COMPONENT : BOMBSITE_COMPONENT,
				waypoint.x, waypoint.y + 1.75f, waypoint.z,
				1.0f, 0xFFFFFFFF, true
			);
		}
	}

	public void renderDebug(
		MultiBufferSource.BufferSource bufferSource,
		PoseStack poseStack
	) {
		List<Vec2d> areaPolygon = data.areaPolygon();
		int points = areaPolygon.size();
		float minY = (float) data.minY();
		float maxY = (float) data.maxY();

		Matrix4f matrix = poseStack.last().pose();
		VertexConsumer vertices = bufferSource.getBuffer(RenderType.debugQuads());

		for (int i = 0, j = points - 1; i < points; j = i++) {
			Vec2d p1 = areaPolygon.get(i);
			Vec2d p2 = areaPolygon.get(j);

			float x1 = (float) p1.x();
			float z1 = (float) p1.y();
			float x2 = (float) p2.x();
			float z2 = (float) p2.y();

			vertices.addVertex(matrix, x1, minY, z1).setColor(255, 0, 0, 127);
			vertices.addVertex(matrix, x1, maxY, z1).setColor(255, 0, 0, 127);
			vertices.addVertex(matrix, x2, maxY, z2).setColor(255, 0, 0, 127);
			vertices.addVertex(matrix, x2, minY, z2).setColor(255, 0, 0, 127);
		}
	}
}
