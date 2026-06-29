package dev.vuis.plusfront.game.impl.def;

import com.boehmod.bflib.cloud.packet.IPacket;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.util.PFPacketUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a bomb site in a defusal game.
 *
 * @param position the position of the bomb site
 * @param name the name of the bomb site
 * @param radius the radius a player must be within in order to plant the bomb
 *
 * @see DefusalGame
 */
public record BombSite(
	Vec3 position,
	String name,
	float radius
) {
	/**
	 * Codec used for serialization.
	 */
	public static final Codec<BombSite> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Vec3.CODEC.fieldOf("position").forGetter(BombSite::position),
			Codec.STRING.fieldOf("name").forGetter(BombSite::name),
			Codec.FLOAT.fieldOf("radius").forGetter(BombSite::radius)
		).apply(
			instance, BombSite::new
		));

	/**
	 * Writes this bomb site's data to the given {@link ByteBuf}.
	 *
	 * @param buf the buf to write the data to
	 */
	public void write(ByteBuf buf) {
		PFPacketUtil.writeVec3(buf, position);
		IPacket.writeString(buf, name);
		buf.writeFloat(radius);
	}

	/**
	 * Reads bomb site data into a new record from the given {@link ByteBuf}.
	 *
	 * @param buf the buf to read the data from
	 * @return a new bomb site record from buf data
	 */
	public static BombSite read(ByteBuf buf) {
		Vec3 position = PFPacketUtil.readVec3(buf);
		String name = IPacket.readString(buf);
		float radius = buf.readFloat();

		return new BombSite(position, name, radius);
	}
}
