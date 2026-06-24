package dev.vuis.plusfront.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.phys.Vec3;

public final class PFPacketUtil {
	private PFPacketUtil() {
		throw new AssertionError();
	}

	public static void writeVec3(ByteBuf buf, Vec3 vec) {
		buf.writeDouble(vec.x).writeDouble(vec.y).writeDouble(vec.z);
	}

	public static Vec3 readVec3(ByteBuf buf) {
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		return new Vec3(x, y, z);
	}
}
