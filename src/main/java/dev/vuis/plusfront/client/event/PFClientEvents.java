package dev.vuis.plusfront.client.event;

import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.client.PFKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(
	value = Dist.CLIENT,
	modid = PlusFront.MOD_ID
)
public final class PFClientEvents {
	private PFClientEvents() {
		throw new AssertionError();
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		PlusFront.LOGGER.info("Registering key mappings...");

		PFKeyMappings.register(event::register);
	}
}
