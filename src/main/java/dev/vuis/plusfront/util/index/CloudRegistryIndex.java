package dev.vuis.plusfront.util.index;

import com.boehmod.bflib.cloud.common.CloudRegistry;
import com.boehmod.bflib.cloud.common.item.CloudItem;
import com.boehmod.bflib.cloud.common.item.CloudItems;
import com.boehmod.bflib.cloud.common.item.types.AbstractCloudItemCoin;
import com.boehmod.bflib.cloud.common.item.types.CloudItemCallingCard;
import com.boehmod.bflib.cloud.common.item.types.CloudItemTrophy;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.vuis.plusfront.PlusFront;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
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

		ITEMS = new Items(REGISTRY.getItems());
	}

	private CloudRegistryIndex() {
		throw new AssertionError();
	}

	public static final class Items {
		private final Map<ResourceLocation, Map<String, CloudItem<?>>> weaponSkins = new Object2ObjectOpenHashMap<>();
		private final Map<String, CloudItemCallingCard> cards = new Object2ObjectOpenHashMap<>();
		private final Map<String, AbstractCloudItemCoin<?>> coins = new Object2ObjectOpenHashMap<>();

		private Items(Collection<CloudItem<?>> items) {
			for (CloudItem<?> item : items) {
				if (item.isDefault() || item.isDeprecated()) {
					continue;
				}

				String formattedSuffix = item.getSuffix().toLowerCase(Locale.ROOT).replace(' ', '_');

				switch (item.getItemType()) {
					case GUN, MELEE -> {
						weaponSkins.computeIfAbsent(BFRes.fromCloud(item.getMinecraftItem()), k -> new Object2ObjectOpenHashMap<>())
							.put(formattedSuffix, item);
					}
					case CARD -> {
						cards.put(formattedSuffix, (CloudItemCallingCard) item);
					}
					case COIN -> {
						coins.put(
							item instanceof CloudItemTrophy ? "trophy_" + formattedSuffix : formattedSuffix,
							(AbstractCloudItemCoin<?>) item
						);
					}
				}
			}
		}

		public @Nullable CloudItem<?> getWeaponSkin(ResourceLocation item, String skin) {
			Map<String, CloudItem<?>> skinsForItem = weaponSkins.get(item);
			if (skinsForItem == null) {
				return null;
			}
			return skinsForItem.get(skin);
		}

		public SuggestionProvider<CommandSourceStack> suggestWeaponSkins(String itemArgumentName) {
			return (context, builder) -> {
				ResourceLocation itemLocation = ResourceLocationArgument.getId(context, itemArgumentName);

				Map<String, CloudItem<?>> skinsForItem = weaponSkins.get(itemLocation);
				if (skinsForItem == null) {
					return builder.buildFuture();
				}

				return SharedSuggestionProvider.suggest(skinsForItem.keySet(), builder);
			};
		}

		public @Nullable CloudItemCallingCard getCard(String name) {
			return cards.get(name);
		}

		public SuggestionProvider<CommandSourceStack> suggestCards() {
			return (context, builder) -> SharedSuggestionProvider.suggest(cards.keySet(), builder);
		}

		public @Nullable AbstractCloudItemCoin<?> getCoin(String name) {
			return coins.get(name);
		}

		public SuggestionProvider<CommandSourceStack> suggestCoins() {
			return (context, builder) -> SharedSuggestionProvider.suggest(coins.keySet(), builder);
		}
	}
}
