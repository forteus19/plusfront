package dev.vuis.plusfront.game.impl.def;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.util.PFMathUtil;
import dev.vuis.plusfront.util.PFPacketUtil;
import dev.vuis.plusfront.util.Vec2d;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.Utf8String;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a bomb site in a defusal game.
 *
 * @param areaPolygon a list of points that makes up the polygon of the plantable area ({@code y} represents {@code z})
 * @param minY the minimum plantable Y position
 * @param maxY the maximum plantable Y position
 * @param waypoints a list of positions to show visible waypoints
 * @param name the name of the bomb site
 *
 * @see DefusalGame
 */
public record BombSite(
	List<Vec2d> areaPolygon,
	double minY,
	double maxY,
	List<Vec3> waypoints,
	String name
) {
	public static final int NAME_MAX_LENGTH = 16;

	public BombSite {
		if (name.length() > NAME_MAX_LENGTH) {
			throw new IllegalArgumentException("Name is too long!");
		}
	}

	/**
	 * Codec used for serialization.
	 */
	public static final Codec<BombSite> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Vec2d.CODEC.listOf().fieldOf("areaPolygon").forGetter(BombSite::areaPolygon),
			Codec.DOUBLE.fieldOf("minY").forGetter(BombSite::minY),
			Codec.DOUBLE.fieldOf("maxY").forGetter(BombSite::maxY),
			Vec3.CODEC.listOf().fieldOf("waypoints").forGetter(BombSite::waypoints),
			Codec.sizeLimitedString(NAME_MAX_LENGTH).fieldOf("name").forGetter(BombSite::name)
		).apply(
			instance, BombSite::new
		));

	/**
	 * Writes this bomb site's data to the given {@link ByteBuf}.
	 *
	 * @param buf the buf to write the data to
	 */
	public void write(ByteBuf buf) {
		PFPacketUtil.writeList(buf, areaPolygon, Vec2d::write);
		buf.writeDouble(minY);
		buf.writeDouble(maxY);
		PFPacketUtil.writeList(buf, waypoints, PFPacketUtil::writeVec3);
		Utf8String.write(buf, name, NAME_MAX_LENGTH);
	}

	/**
	 * Reads bomb site data into a new record from the given {@link ByteBuf}.
	 *
	 * @param buf the buf to read the data from
	 * @return a new bomb site record from buf data
	 */
	public static BombSite read(ByteBuf buf) {
		List<Vec2d> areaPolygon = PFPacketUtil.readList(buf, Vec2d::read);
		double minY = buf.readDouble();
		double maxY = buf.readDouble();
		List<Vec3> waypoints = PFPacketUtil.readList(buf, PFPacketUtil::readVec3);
		String name = Utf8String.read(buf, NAME_MAX_LENGTH);

		return new BombSite(areaPolygon, minY, maxY, waypoints, name);
	}

	public boolean isWithinArea(Vec3 position) {
		if (areaPolygon.size() < 3) {
			return false;
		}

		return position.y >= minY
			&& position.y <= maxY
			&& PFMathUtil.isPointWithinPolygon(areaPolygon, Vec2d.ofYPlane(position));
	}
}
