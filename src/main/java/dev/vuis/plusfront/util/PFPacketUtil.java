package dev.vuis.plusfront.util;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.VarInt;
import net.minecraft.world.phys.Vec3;

public final class PFPacketUtil {
	private PFPacketUtil() {
		throw new AssertionError();
	}

	public static <T> void writeList(ByteBuf buf, List<T> list, BiConsumer<ByteBuf, T> elementEncoder) {
		VarInt.write(buf, list.size());
		for (T element : list) {
			elementEncoder.accept(buf, element);
		}
	}

	public static <T> List<T> readList(ByteBuf buf, Function<ByteBuf, T> elementDecoder) {
		int numElements = VarInt.read(buf);
		List<T> list = new ObjectArrayList<>(numElements);
		for (int i = 0; i < numElements; i++) {
			list.add(elementDecoder.apply(buf));
		}
		return list;
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
