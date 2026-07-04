package dev.vuis.plusfront.player;

import com.boehmod.bflib.cloud.common.item.CloudItem;
import com.boehmod.blockfront.registry.BFDataComponents;
import com.boehmod.blockfront.util.BFRes;
import com.boehmod.blockfront.util.CloudItemUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.data.PFCodecs;
import dev.vuis.plusfront.fetch.BfApi;
import dev.vuis.plusfront.util.index.CloudRegistryIndex;
import dev.vuis.plusfront.util.index.ItemIndex;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PFCustomArmory {
	public static final Codec<PFCustomArmory> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), CustomStack.CODEC).fieldOf("equippedWeapons").forGetter(PFCustomArmory::getEquippedWeapons)
		).apply(
			instance, PFCustomArmory::new
		));

	private final Map<Item, CustomStack> equippedWeapons;

	private PFCustomArmory(Map<Item, CustomStack> equippedWeapons) {
		this.equippedWeapons = new IdentityHashMap<>(equippedWeapons);
	}

	public PFCustomArmory() {
		this.equippedWeapons = new IdentityHashMap<>();
	}

	public Map<Item, CustomStack> getEquippedWeapons() {
		return equippedWeapons;
	}

	public @Nullable CustomStack getEquippedWeapon(Item itemHolder) {
		return equippedWeapons.get(itemHolder);
	}

	public void equipWeapon(Item item, CustomStack customStack) {
		equippedWeapons.put(item, customStack);
	}

	public void clearWeapon(Item item) {
		equippedWeapons.remove(item);
	}

	public void clearWeapons() {
		equippedWeapons.clear();
	}

	public void fetchWeapons(UUID playerUuid, Executor mainThreadExecutor, @Nullable Consumer<PFCustomArmory> onFinished) {
		PlusFront.LOGGER.info("Fetching player inventory for {}", playerUuid);

		BfApi.fetchPlayerInventory(playerUuid).thenAccept(
			inventory -> mainThreadExecutor.execute(() -> {
				processFetchedInventory(playerUuid, inventory);
				if (onFinished != null) {
					onFinished.accept(this);
				}
			})
		);
	}

	private void processFetchedInventory(UUID playerUuid, BfApi.Inventory inventory) {
		PlusFront.LOGGER.info("Processing fetched inventory for {}", playerUuid);

		clearWeapons();

		for (BfApi.Inventory.Stack stack : inventory.inventory()) {
			CloudItem<?> cloudItem = CloudRegistryIndex.REGISTRY.getItem(stack.id());
			if (cloudItem == null) {
				PlusFront.LOGGER.warn("Found unknown item ID ({})", stack.id());
				continue;
			}

			ResourceLocation minecraftId = BFRes.fromCloud(cloudItem.getMinecraftItem());

			if (!ItemIndex.WEAPONS.contains(minecraftId)) {
				continue;
			}

			Item item = BuiltInRegistries.ITEM.get(minecraftId);

			equipWeapon(item, new CustomStack(cloudItem, stack.mint()));
		}
	}

	public int numWeapons() {
		return equippedWeapons.size();
	}

	public boolean hasData() {
		return !equippedWeapons.isEmpty();
	}

	public record CustomStack(
		CloudItem<?> cloudItem,
		double mint
	) {
		public static final Codec<CustomStack> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				PFCodecs.CLOUD_ITEM.fieldOf("item").forGetter(CustomStack::cloudItem),
				Codec.DOUBLE.fieldOf("mint").forGetter(CustomStack::mint)
			).apply(
				instance, CustomStack::new
			));

		public void setComponents(ItemStack itemStack) {
			CloudItemUtils.setItemComponents(cloudItem, itemStack);
			itemStack.set(BFDataComponents.MINT, mint);
		}
	}
}
