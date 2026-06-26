package dev.vuis.plusfront.server.event;

import com.boehmod.blockfront.game.AbstractGame;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import dev.vuis.plusfront.util.PFUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
	public static void onItemEntityPickupPre(ItemEntityPickupEvent.Pre event) {
		AbstractGame<?, ?, ?> game = PFUtil.getPlayerGame(event.getPlayer());
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
