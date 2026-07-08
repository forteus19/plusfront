package dev.vuis.plusfront.player;

import com.boehmod.bflib.cloud.common.item.CloudItem;
import com.boehmod.bflib.cloud.common.item.CloudItemStack;
import com.boehmod.bflib.cloud.common.item.types.AbstractCloudItemCoin;
import com.boehmod.bflib.cloud.common.item.types.CloudItemCallingCard;
import com.boehmod.blockfront.registry.BFDataComponents;
import com.boehmod.blockfront.util.BFRes;
import com.boehmod.blockfront.util.CloudItemUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.fetch.BfApi;
import dev.vuis.plusfront.util.index.CloudRegistryIndex;
import dev.vuis.plusfront.util.index.ItemIndex;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PFArmory {
	private static final String CLOUD_ID_ERROR = "Invalid cloud item ID";

	private PFArmory() {
		throw new AssertionError();
	}

	public static void fetch(
		Weapons weapons,
		Extra extra,
		UUID playerUuid,
		Executor mainThreadExecutor,
		@Nullable Runnable onFinished)
	{
		PlusFront.LOGGER.info("Fetching player inventory for {}", playerUuid);

		BfApi.fetchPlayerInventory(playerUuid).thenAccept(
			inventory -> mainThreadExecutor.execute(() -> {
				try {
					PlusFront.LOGGER.info("Processing fetched inventory for {}", playerUuid);

					processFetchedInventory(weapons, extra, inventory);

					if (onFinished != null) {
						onFinished.run();
					}
				} catch (Exception e) {
					PlusFront.LOGGER.error("Error while processing player inventory", e);
				}
			})
		).exceptionally(
			e -> {
				PlusFront.LOGGER.error("Error while fetching player inventory", e);
				return null;
			}
		);
	}

	private static void processFetchedInventory(
		Weapons weapons,
		Extra extra,
		BfApi.Inventory inventory
	) {
		weapons.clearWeapons();
		extra.clearAll();

		for (BfApi.Inventory.Stack stack : inventory.inventory()) {
			CloudItem<?> cloudItem = CloudRegistryIndex.REGISTRY.getItem(stack.id());
			if (cloudItem == null) {
				PlusFront.LOGGER.warn("Found unknown item ID ({})", stack.id());
				continue;
			}

			switch (cloudItem.getItemType()) {
				case GUN, MELEE -> {
					ResourceLocation minecraftId = BFRes.fromCloud(cloudItem.getMinecraftItem());

					if (!ItemIndex.WEAPONS.contains(minecraftId)) {
						continue;
					}

					Item item = BuiltInRegistries.ITEM.get(minecraftId);

					if (weapons.hasEquippedWeapon(item)) {
						PlusFront.LOGGER.warn("Duplicate weapon {}", cloudItem.getDisplayName());
					}

					weapons.equipWeapon(item, new Weapons.Stack(cloudItem, stack.mint()));
				}
				case CARD -> {
					if (extra.hasEquippedCard()) {
						PlusFront.LOGGER.warn("Duplicate card {}", cloudItem.getDisplayName());
					}

					extra.equipCard((CloudItemCallingCard) cloudItem);
				}
				case COIN -> {
					if (extra.hasEquippedCoin()) {
						PlusFront.LOGGER.warn("Duplicate coin {}", cloudItem.getDisplayName());
					}

					extra.equipCoin((AbstractCloudItemCoin<?>) cloudItem);
				}
			}
		}
	}

	public static final class Weapons {
		public static final Codec<Weapons> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Stack.CODEC).fieldOf("weapons").forGetter(Weapons::getEquippedWeapons)
			).apply(
				instance, Weapons::new
			));

		private final Map<Item, Stack> equippedWeapons;

		private Weapons(
			Map<Item, Stack> equippedWeapons
		) {
			this.equippedWeapons = new IdentityHashMap<>(equippedWeapons);
		}

		public Weapons() {
			this.equippedWeapons = new IdentityHashMap<>();
		}

		public Map<Item, Stack> getEquippedWeapons() {
			return equippedWeapons;
		}

		public @Nullable PFArmory.Weapons.Stack getEquippedWeapon(Item item) {
			return equippedWeapons.get(item);
		}

		public boolean hasEquippedWeapon(Item item) {
			return equippedWeapons.containsKey(item);
		}

		public void equipWeapon(Item item, Stack customStack) {
			equippedWeapons.put(item, customStack);
		}

		public void clearWeapon(Item item) {
			equippedWeapons.remove(item);
		}

		public void clearWeapons() {
			equippedWeapons.clear();
		}

		public int numWeapons() {
			return equippedWeapons.size();
		}

		public boolean hasData() {
			return !equippedWeapons.isEmpty();
		}

		public record Stack(
			CloudItem<?> cloudItem,
			double mint
		) {
			private static final Codec<CloudItem<?>> ITEM_CODEC = Codec.INT.comapFlatMap(
				id -> {
					CloudItem<?> item = CloudRegistryIndex.REGISTRY.getItem(id);
					return item != null ?
						DataResult.success(item) :
						DataResult.error(() -> CLOUD_ID_ERROR);
				},
				CloudItem::getId
			);

			public static final Codec<Stack> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
					ITEM_CODEC.fieldOf("item").forGetter(Stack::cloudItem),
					Codec.DOUBLE.fieldOf("mint").forGetter(Stack::mint)
				).apply(
					instance, Stack::new
				));

			public void setComponents(ItemStack itemStack) {
				CloudItemUtils.setItemComponents(cloudItem, itemStack);
				itemStack.set(BFDataComponents.MINT, mint);
			}
		}
	}

	public static final class Extra {
		private static final Codec<CloudItemCallingCard> CARD_CODEC = Codec.INT.comapFlatMap(
			id -> {
				CloudItem<?> item = CloudRegistryIndex.REGISTRY.getItem(id);
				return item instanceof CloudItemCallingCard cardItem ?
					DataResult.success(cardItem) :
					DataResult.error(() -> CLOUD_ID_ERROR);
			},
			CloudItem::getId
		);
		private static final StreamCodec<ByteBuf, CloudItemCallingCard> CARD_STREAM_CODEC = new StreamCodec<>() {
			@Override
			public @NotNull CloudItemCallingCard decode(@NotNull ByteBuf buf) {
				int itemId = VarInt.read(buf);
				CloudItem<?> item = CloudRegistryIndex.REGISTRY.getItem(itemId);

				if (item instanceof CloudItemCallingCard cardItem) {
					return cardItem;
				} else {
					throw new DecoderException(CLOUD_ID_ERROR);
				}
			}

			@Override
			public void encode(@NotNull ByteBuf buf, @NotNull CloudItemCallingCard item) {
				VarInt.write(buf, item.getId());
			}
		};
		private static final Codec<AbstractCloudItemCoin<?>> COIN_CODEC = Codec.INT.comapFlatMap(
			id -> {
				CloudItem<?> item = CloudRegistryIndex.REGISTRY.getItem(id);
				return item instanceof AbstractCloudItemCoin<?> cardItem ?
					DataResult.success(cardItem) :
					DataResult.error(() -> CLOUD_ID_ERROR);
			},
			CloudItem::getId
		);
		private static final StreamCodec<ByteBuf, AbstractCloudItemCoin<?>> COIN_STREAM_CODEC = new StreamCodec<>() {
			@Override
			public @NotNull AbstractCloudItemCoin<?> decode(@NotNull ByteBuf buf) {
				int itemId = VarInt.read(buf);
				CloudItem<?> item = CloudRegistryIndex.REGISTRY.getItem(itemId);

				if (item instanceof AbstractCloudItemCoin<?> coinItem) {
					return coinItem;
				} else {
					throw new DecoderException(CLOUD_ID_ERROR);
				}
			}

			@Override
			public void encode(@NotNull ByteBuf buf, @NotNull AbstractCloudItemCoin<?> item) {
				VarInt.write(buf, item.getId());
			}
		};

		public static final Codec<Extra> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				CARD_CODEC.optionalFieldOf("card").forGetter(Extra::getEquippedCard),
				COIN_CODEC.optionalFieldOf("coin").forGetter(Extra::getEquippedCoin)
			).apply(
				instance, Extra::new
			));
		public static final StreamCodec<ByteBuf, Extra> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(CARD_STREAM_CODEC), Extra::getEquippedCard,
			ByteBufCodecs.optional(COIN_STREAM_CODEC), Extra::getEquippedCoin,
			Extra::new
		);

		private static final UUID DUMMY_UUID = new UUID(0L, 0L);

		private @Nullable CloudItemCallingCard equippedCard = null;
		private @Nullable AbstractCloudItemCoin<?> equippedCoin = null;

		private @Nullable CloudItemStack cachedCardStack = null;
		private @Nullable CloudItemStack cachedCoinStack = null;

		private Extra(
			Optional<CloudItemCallingCard> equippedCard,
			Optional<AbstractCloudItemCoin<?>> equippedCoin
		) {
			this.equippedCard = equippedCard.orElse(null);
			this.equippedCoin = equippedCoin.orElse(null);
		}

		public Extra() {
		}

		public Optional<CloudItemCallingCard> getEquippedCard() {
			return Optional.ofNullable(equippedCard);
		}

		public boolean hasEquippedCard() {
			return equippedCard != null;
		}

		private void updateCardStack() {
			if (equippedCard == null || (cachedCardStack != null && equippedCard.getId() == cachedCardStack.getItemId())) {
				return;
			}
			cachedCardStack = new CloudItemStack(DUMMY_UUID, equippedCard.getId(), 0.0);
		}

		public @Nullable CloudItemStack getEquippedCardStack() {
			updateCardStack();
			return cachedCardStack;
		}

		public void equipCard(CloudItemCallingCard card) {
			equippedCard = card;
			updateCardStack();
		}

		public void clearCard() {
			equippedCard = null;
			cachedCardStack = null;
		}

		public Optional<AbstractCloudItemCoin<?>> getEquippedCoin() {
			return Optional.ofNullable(equippedCoin);
		}

		public boolean hasEquippedCoin() {
			return equippedCoin != null;
		}

		private void updateCoinStack() {
			if (equippedCoin == null || (cachedCoinStack != null && equippedCoin.getId() == cachedCoinStack.getItemId())) {
				return;
			}
			cachedCoinStack = new CloudItemStack(DUMMY_UUID, equippedCoin.getId(), 0.0);
		}

		public @Nullable CloudItemStack getEquippedCoinStack() {
			updateCoinStack();
			return cachedCoinStack;
		}

		public void equipCoin(AbstractCloudItemCoin<?> coin) {
			equippedCoin = coin;
			updateCoinStack();
		}

		public void clearCoin() {
			equippedCoin = null;
			cachedCoinStack = null;
		}

		public void clearAll() {
			clearCard();
			clearCoin();
		}

		public boolean hasData() {
			return equippedCard != null || equippedCoin != null;
		}
	}
}
