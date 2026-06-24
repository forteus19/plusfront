package dev.vuis.plusfront.util;

import net.minecraft.world.phys.Vec3;

public final class PFUtil {
	private PFUtil() {
		throw new AssertionError();
	}

	public static Vec3 copyVec3(Vec3 original) {
		return new Vec3(original.x, original.y, original.z);
	}
}
