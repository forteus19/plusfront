package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.util.BFStyles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Stores the client state of a defusal bomb site.
 *
 * @see DefusalGame
 * @see DefusalGameClient
 */
public class BombSiteClient {
	private final Vec3 position;
	private final float radius;

	private final Component nameComponent;
	private Component distanceComponent = null;

	/**
	 * @param data the bomb site data given by {@link DefusalGame}
	 */
	public BombSiteClient(BombSite data) {
		this.position = data.position();
		this.radius = data.radius();

		this.nameComponent = Component.literal(data.name()).withStyle(BFStyles.BOLD);
	}

	/**
	 * Called every client tick to update state used when rendering the waypoint.
	 *
	 * @param player the client player
	 * @param highlightInRadius whether to highlight the distance component if within radius of the bomb site
	 */
	public void update(LocalPlayer player, boolean highlightInRadius) {
		float siteDistance = Mth.sqrt((float) player.distanceToSqr(position));
		MutableComponent mutableDistance = Component.literal(String.format("%.2fm", siteDistance));

		if (highlightInRadius && siteDistance <= radius) {
			mutableDistance.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
		}

		distanceComponent = mutableDistance;
	}

	/**
	 * Renders a waypoint showing info about this bomb site into the world.
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
		BFRendering.component(
			poseStack, font, camera, graphics,
			nameComponent,
			position.x, position.y + 2.5f, position.z,
			2.0f, 0xFFFFFFFF, true
		);
		if (distanceComponent != null) {
			BFRendering.component(
				poseStack, font, camera, graphics,
				distanceComponent,
				position.x, position.y + 1.65f, position.z,
				1.0f, 0xFFFFFFFF, true
			);
		}
	}
}
