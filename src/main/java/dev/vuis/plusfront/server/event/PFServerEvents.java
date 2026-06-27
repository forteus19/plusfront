package dev.vuis.plusfront.server.event;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.registry.BFItems;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import dev.vuis.plusfront.util.PFUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(
	value = Dist.DEDICATED_SERVER,
	modid = PlusFront.MOD_ID
)
public final class PFServerEvents {
	private PFServerEvents() {
		throw new AssertionError();
	}

	@SubscribeEvent
	public static void onItemEntityPickupPost(ItemEntityPickupEvent.Post event) {
		BFAbstractManager<?, ?, ?> manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			return;
		}

		Player player = event.getPlayer();

		if (GameUtils.isPlayerUnavailable(player, manager.getPlayerDataHandler().getPlayerData(player))) {
			return;
		}

		AbstractGame<?, ?, ?> game = manager.getPlayerGame(player);
		if (!(game instanceof DefusalGame defusalGame)) {
			return;
		}

		if (event.getOriginalStack().getItem() == BFItems.BOMB.value()) {
			defusalGame.getPlayerManager().onBombPickup(player);
		}
	}

	@SubscribeEvent
	public static void onItemToss(ItemTossEvent event) {
		Player player = event.getPlayer();

		AbstractGame<?, ?, ?> game = PFUtil.getPlayerGame(player);
		if (!(game instanceof DefusalGame defusalGame)) {
			return;
		}

		ItemEntity itemEntity = event.getEntity();

		if (itemEntity.getItem().getItem() == BFItems.BOMB.value()) {
			defusalGame.setBombItem(itemEntity);
			defusalGame.getPlayerManager().onBombDrop(player);
		}
	}

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		AbstractGame<?, ?, ?> game = PFUtil.getPlayerGame(player);
		if (!(game instanceof DefusalGame defusalGame)) {
			return;
		}

		Item targetItem = BFItems.BOMB.value();

		for (ItemEntity itemEntity : event.getDrops()) {
			Item droppedItem = itemEntity.getItem().getItem();

			if (droppedItem == targetItem) {
				defusalGame.setBombItem(itemEntity);
				break;
			}
		}
	}
}
