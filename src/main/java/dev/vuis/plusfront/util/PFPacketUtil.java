package dev.vuis.plusfront.util;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.phys.Vec3;

public final class PFPacketUtil {
	private PFPacketUtil() {
		throw new AssertionError();
	}

	public static <B extends ByteBuf, T> void writeList(B buf, List<T> list, StreamEncoder<? super B, T> elementEncoder) {
		VarInt.write(buf, list.size());
		for (T element : list) {
			elementEncoder.encode(buf, element);
		}
	}

	public static <B extends ByteBuf, T> List<T> readList(B buf, StreamDecoder<? super B, T> elementDecoder) {
		int numElements = VarInt.read(buf);
		List<T> list = new ObjectArrayList<>(numElements);
		for (int i = 0; i < numElements; i++) {
			list.add(elementDecoder.decode(buf));
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
