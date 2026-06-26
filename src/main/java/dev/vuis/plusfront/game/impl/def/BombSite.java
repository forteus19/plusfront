package dev.vuis.plusfront.game.impl.def;

import com.boehmod.bflib.cloud.packet.IPacket;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.util.PFPacketUtil;
import io.netty.buffer.ByteBuf;
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
