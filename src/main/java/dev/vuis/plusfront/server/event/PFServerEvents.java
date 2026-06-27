package dev.vuis.plusfront.server.event;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameUtils;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import net.minecraft.world.entity.player.Player;
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
		BFAbstractManager<?, ?, ?> manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			return;
		}

		Player player = event.getPlayer();

		if (GameUtils.isPlayerUnavailable(player, manager.getPlayerDataHandler().getPlayerData(player))) {
			return;
		}

		AbstractGame<?, ?, ?> game = manager.getPlayerGame(player);
		if (game == null) {
			return;
		}

		if (game instanceof DefusalGame defusalGame) {
			defusalGame.getPlayerManager().onItemPickup(
				player, event.getItemEntity()
			);
		}
	}
}
