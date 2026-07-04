package dev.vuis.plusfront.util.index;

import com.boehmod.blockfront.common.item.BFWeaponItem;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class ItemIndex {
	public static final Set<ResourceLocation> WEAPONS = new ObjectOpenHashSet<>();

	private ItemIndex() {
		throw new AssertionError();
	}

	public static void init() {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof BFWeaponItem) {
				WEAPONS.add(BuiltInRegistries.ITEM.getKey(item));
			}
		}
	}

	public static SuggestionProvider<CommandSourceStack> suggestWeapons() {
		return (context, builder) -> SharedSuggestionProvider.suggestResource(WEAPONS, builder);
	}
}
