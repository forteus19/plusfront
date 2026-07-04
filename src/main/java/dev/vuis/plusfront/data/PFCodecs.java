package dev.vuis.plusfront.data;

import com.boehmod.bflib.cloud.common.item.CloudItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.vuis.plusfront.util.index.CloudRegistryIndex;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.world.phys.Vec2;

public final class PFCodecs {
	public static final Codec<Vec2> VEC2 = Codec.FLOAT.listOf().comapFlatMap(
		list -> Util.fixedSize(list, 2).map(values -> new Vec2(values.get(0), values.get(1))),
		vec -> List.of(vec.x, vec.y)
	);
	public static final Codec<CloudItem<?>> CLOUD_ITEM = Codec.INT.comapFlatMap(
		PFCodecs::cloudItem,
		CloudItem::getId
	);

	private PFCodecs() {
		throw new AssertionError();
	}

	private static DataResult<CloudItem<?>> cloudItem(int id) {
		CloudItem<?> item = CloudRegistryIndex.REGISTRY.getItem(id);
		return item != null ? DataResult.success(item) : DataResult.error(() -> "Invalid cloud item ID");
	}
}
