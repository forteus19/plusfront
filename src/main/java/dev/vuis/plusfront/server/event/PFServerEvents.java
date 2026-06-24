package dev.vuis.plusfront.server.event;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.AbstractGame;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(Dist.DEDICATED_SERVER)
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

		AbstractGame<?, ?, ?> game = manager.getPlayerGame(event.getPlayer());
		if (game == null) {
			return;
		}

		if (game instanceof DefusalGame defusalGame) {
			defusalGame.getPlayerManager().onItemPickup(
				event.getPlayer(), event.getItemEntity()
			);
		}
	}
}
