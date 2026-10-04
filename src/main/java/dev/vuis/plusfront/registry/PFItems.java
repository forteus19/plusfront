package dev.vuis.plusfront.registry;

import com.boehmod.blockfront.common.item.BFCommonItem;
import com.boehmod.blockfront.common.item.GunItem;
import dev.vuis.plusfront.PlusFront;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public final class PFItems {
	private static final DeferredRegister.Items DR = DeferredRegister.createItems(PlusFront.MOD_ID);

	public static final ThreadLocal<Boolean> GUN_OVERRIDE_NAMESPACE = ThreadLocal.withInitial(() -> false);

	public static final DeferredItem<GunItem> GUN_COWBOY_REVOLVER = DR.registerItem(
		"gun_cowboy_revolver",
		properties -> gun("gun_cowboy_revolver", properties),
		new Item.Properties()
			.stacksTo(1)
			.component(DataComponents.TOOL, BFCommonItem.getTool())
	);

	private PFItems() {
		throw new AssertionError();
	}

	private static GunItem gun(String id, Item.Properties properties) {
		GUN_OVERRIDE_NAMESPACE.set(true);
		GunItem item = new GunItem(id, properties);
		GUN_OVERRIDE_NAMESPACE.set(false);
		return item;
	}

	public static String fixInternalId(String originalId) {
		return switch (originalId) {
			case "gun_cowboy_revolver" -> "gun_webley_mk6";
			default -> originalId;
		};
	}

	public static void register(IEventBus modBus) {
		DR.register(modBus);
	}
}
