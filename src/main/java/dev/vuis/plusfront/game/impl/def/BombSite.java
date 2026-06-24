package dev.vuis.plusfront.game.impl.def;

import com.boehmod.bflib.cloud.packet.IPacket;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.util.math.BFPose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record BombSite(
	BFPose position,
	String name
) {
	public static final Codec<BombSite> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			BFPose.BASE_CODEC.fieldOf("position").forGetter(BombSite::position),
			Codec.STRING.fieldOf("name").forGetter(BombSite::name)
		).apply(
			instance, BombSite::new
		));

	public void render(
		LocalPlayer player,
		PoseStack poseStack,
		Font font,
		GuiGraphics graphics,
		Camera camera
	) {
		Vec3 posePosition = position.position;

		BFRendering.component(
			poseStack, font, camera, graphics,
			Component.literal(name),
			posePosition.x, posePosition.y + 2.4f, posePosition.z,
			2.0f, 0xFFE4D5D4, true
		);
		BFRendering.component(
			poseStack, font, camera, graphics,
			Component.literal(String.format("%.2fm", Mth.sqrt((float) player.distanceToSqr(posePosition)))),
			posePosition.x, posePosition.y + 1.65f, posePosition.z,
			1.0f, 0xFFE4D5D4, true
		);
	}

	public void write(ByteBuf buf) throws IOException {
		position.write(buf);
		IPacket.writeString(buf, name);
	}

	public static BombSite read(ByteBuf buf) throws IOException {
		BFPose position = new BFPose();
		position.read(buf);
		String name = IPacket.readString(buf);

		return new BombSite(position, name);
	}
}
