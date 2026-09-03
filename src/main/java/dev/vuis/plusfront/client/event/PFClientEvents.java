package dev.vuis.plusfront.client.event;

import com.mojang.brigadier.CommandDispatcher;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.client.PFClientTemp;
import dev.vuis.plusfront.client.PFKeyMappings;
import dev.vuis.plusfront.client.command.PFClientCommand;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(
	value = Dist.CLIENT,
	modid = PlusFront.MOD_ID
)
public final class PFClientEvents {
	private PFClientEvents() {
		throw new AssertionError();
	}

	@SubscribeEvent
	public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		PlusFront.LOGGER.info("Registering client commands...");

		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

		PFClientCommand.register(dispatcher);
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		PlusFront.LOGGER.info("Registering key mappings...");

		PFKeyMappings.register(event::register);
	}

	@SubscribeEvent
	public static void onRenderFramePre(RenderFrameEvent.Pre event) {
		PFClientTemp.frameMillis = Util.getMillis();
	}
}
