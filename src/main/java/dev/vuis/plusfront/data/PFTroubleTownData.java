package dev.vuis.plusfront.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public record PFTroubleTownData(
	List<ItemStack> droppedItems
) {
	public static final PFTroubleTownData EMPTY = new PFTroubleTownData(
		List.of()
	);

	public static final MapCodec<PFTroubleTownData> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			ItemStack.STRICT_SINGLE_ITEM_CODEC.listOf().optionalFieldOf("droppedItems", List.of()).forGetter(PFTroubleTownData::droppedItems)
		).apply(
			instance, PFTroubleTownData::new
		));
}
