package dev.vuis.plusfront.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.world.phys.Vec3;

public record Vec2d(
	double x,
	double y
) {
	public static Vec2d ofYPlane(Vec3 vec3) {
		return new Vec2d(vec3.x, vec3.z);
	}

	public static final Codec<Vec2d> CODEC = Codec.DOUBLE.listOf().comapFlatMap(
		list -> Util.fixedSize(list, 2).map(values -> new Vec2d(values.get(0), values.get(1))),
		vec -> List.of(vec.x, vec.y)
	);

	public static void write(ByteBuf buf, Vec2d vec) {
		buf.writeDouble(vec.x).writeDouble(vec.y);
	}

	public static Vec2d read(ByteBuf buf) {
		double x = buf.readDouble();
		double y = buf.readDouble();
		return new Vec2d(x, y);
	}
}
