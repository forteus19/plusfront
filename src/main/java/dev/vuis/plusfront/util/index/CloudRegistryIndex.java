package dev.vuis.plusfront.util.index;

import com.boehmod.bflib.cloud.common.CloudRegistry;
import com.boehmod.bflib.cloud.common.item.CloudItem;
import com.boehmod.bflib.cloud.common.item.CloudItemType;
import com.boehmod.bflib.cloud.common.item.CloudItems;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.vuis.plusfront.PlusFront;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class CloudRegistryIndex {
	public static final CloudRegistry REGISTRY = new CloudRegistry();

	public static final Items ITEMS;

	static {
		PlusFront.LOGGER.info("Initializing cloud registry index");

		CloudItems.registerItems(REGISTRY);

		ITEMS = new Items(REGISTRY);
	}

	private CloudRegistryIndex() {
		throw new AssertionError();
	}

	public static final class Items {
		private final Map<ResourceLocation, Map<String, CloudItem<?>>> skins = new Object2ObjectOpenHashMap<>();

		private Items(CloudRegistry registry) {
			for (CloudItem<?> item : registry.getItems()) {
				if (item.isDefault() || item.isDeprecated() || item.getItemType() != CloudItemType.GUN) {
					continue;
				}

				skins.computeIfAbsent(BFRes.fromCloud(item.getMinecraftItem()), k -> new Object2ObjectOpenHashMap<>())
					.put(item.getSuffix().toLowerCase(Locale.ROOT).replace(' ', '_'), item);
			}
		}

		public @Nullable CloudItem<?> getSkin(ResourceLocation item, String skin) {
			Map<String, CloudItem<?>> skinsForItem = skins.get(item);
			if (skinsForItem == null) {
				return null;
			}
			return skinsForItem.get(skin);
		}

		public SuggestionProvider<CommandSourceStack> suggestSkins(String itemArgumentName) {
			return (context, builder) -> {
				ResourceLocation itemLocation = ResourceLocationArgument.getId(context, itemArgumentName);

				Map<String, CloudItem<?>> skinsForItem = skins.get(itemLocation);
				if (skinsForItem == null) {
					return builder.buildFuture();
				}

				return SharedSuggestionProvider.suggest(skinsForItem.keySet(), builder);
			};
		}
	}
}
