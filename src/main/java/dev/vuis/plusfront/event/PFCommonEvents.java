package dev.vuis.plusfront.event;

import dev.vuis.plusfront.net.payload.PFStartConsumablePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public final class PFCommonEvents {
	private PFCommonEvents() {
		throw new AssertionError();
	}

	@SubscribeEvent
	public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");

		PFStartConsumablePayload.register(registrar);
	}
}
