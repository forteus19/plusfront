package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.game.GameBoundary;
import dev.vuis.plusfront.ex.GameBoundaryEx;
import dev.vuis.plusfront.util.PFPacketUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.Utf8String;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BombSite {
	public static final int NAME_MAX_LENGTH = 16;
	public static final double VISIBLE_HEIGHT = 1.0;

	public final @NotNull String name;
	public @NotNull GameBoundary boundary = new GameBoundary();
	public double minY;
	public double maxY;
	public double visibleY;
	public @NotNull List<Vec3> waypoints = new ObjectArrayList<>();

	public BombSite(@NotNull String name) {
		this.name = name;
	}

	public BombSiteCodec toCodec() {
		return new BombSiteCodec(
			name,
			boundary,
			minY,
			maxY,
			visibleY,
			List.copyOf(waypoints)
		);
	}

	public static BombSite fromCodec(BombSiteCodec codec) {
		BombSite bombSite = new BombSite(codec.name());
		bombSite.boundary = codec.boundary();
		bombSite.minY = codec.minY();
		bombSite.maxY = codec.maxY();
		bombSite.visibleY = codec.visibleY();
		bombSite.waypoints.clear();
		bombSite.waypoints.addAll(codec.waypoints());

		return bombSite;
	}

	/**
	 * Writes this bomb site's data to the given {@link ByteBuf}.
	 *
	 * @param buf the buf to write the data to
	 */
	public void write(ByteBuf buf) throws IOException {
		Utf8String.write(buf, name, NAME_MAX_LENGTH);
		boundary.write(buf);
		buf.writeDouble(minY);
		buf.writeDouble(maxY);
		buf.writeDouble(visibleY);
		PFPacketUtil.writeList(buf, waypoints, PFPacketUtil::writeVec3);
	}

	/**
	 * Reads bomb site data into a new record from the given {@link ByteBuf}.
	 *
	 * @param buf the buf to read the data from
	 * @return a new bomb site record from buf data
	 */
	public static BombSite read(ByteBuf buf) throws IOException {
		String name = Utf8String.read(buf, NAME_MAX_LENGTH);

		BombSite bombSite = new BombSite(name);
		bombSite.boundary.read(buf);
		bombSite.minY = buf.readDouble();
		bombSite.maxY = buf.readDouble();
		bombSite.visibleY = buf.readDouble();
		bombSite.waypoints = PFPacketUtil.readList(buf, PFPacketUtil::readVec3);

		return bombSite;
	}

	public boolean isWithinArea(Vec3 position) {
		if (boundary.isEmpty()) {
			return false;
		}

		return position.y >= minY
			&& position.y <= maxY
			&& boundary.method_3128((float) position.x, (float) position.z);
	}

	public @Nullable AABB getBoundaryAABB() {
		if (boundary.isEmpty()) {
			return null;
		}

		float minX = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxZ = Float.MIN_VALUE;

		List<Vec2> points = ((GameBoundaryEx) boundary).pf$getPoints();

		for (Vec2 point : points) {
			if (point.x < minX) {
				minX = point.x;
			}
			if (point.x > maxX) {
				maxX = point.x;
			}
			if (point.y < minZ) {
				minZ = point.y;
			}
			if (point.y > maxZ) {
				maxZ = point.y;
			}
		}

		return new AABB(
			minX, visibleY, minZ,
			maxX, visibleY + VISIBLE_HEIGHT, maxZ
		);
	}
}
