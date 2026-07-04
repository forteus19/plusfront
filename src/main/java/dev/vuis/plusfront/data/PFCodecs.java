package dev.vuis.plusfront.data;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.world.phys.Vec2;

public final class PFCodecs {
	public static final Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap(
		list -> Util.fixedSize(list, 2).map(values -> new Vec2(values.get(0), values.get(1))),
		vec -> List.of(vec.x, vec.y)
	);

	private PFCodecs() {
		throw new AssertionError();
	}
}
