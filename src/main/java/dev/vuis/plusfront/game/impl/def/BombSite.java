package dev.vuis.plusfront.game.impl.def;

import com.boehmod.bflib.cloud.packet.IPacket;
import com.boehmod.blockfront.client.render.BFRendering;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.util.PFPacketUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record BombSite(
	Vec3 position,
	String name,
	float radius
) {
	public static final Codec<BombSite> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Vec3.CODEC.fieldOf("position").forGetter(BombSite::position),
			Codec.STRING.fieldOf("name").forGetter(BombSite::name),
			Codec.FLOAT.fieldOf("radius").forGetter(BombSite::radius)
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
		BFRendering.component(
			poseStack, font, camera, graphics,
			Component.literal(name),
			position.x, position.y + 2.4f, position.z,
			2.0f, 0xFFFFFFFF, true
		);
		BFRendering.component(
			poseStack, font, camera, graphics,
			Component.literal(String.format("%.2fm", Mth.sqrt((float) player.distanceToSqr(position)))),
			position.x, position.y + 1.65f, position.z,
			1.0f, 0xFFFFFFFF, true
		);
	}

	public void write(ByteBuf buf) {
		PFPacketUtil.writeVec3(buf, position);
		IPacket.writeString(buf, name);
		buf.writeFloat(radius);
	}

	public static BombSite read(ByteBuf buf) {
		Vec3 position = PFPacketUtil.readVec3(buf);
		String name = IPacket.readString(buf);
		float radius = buf.readFloat();

		return new BombSite(position, name, radius);
	}
}
