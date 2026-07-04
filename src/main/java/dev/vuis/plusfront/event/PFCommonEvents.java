package dev.vuis.plusfront.event;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.mojang.brigadier.CommandDispatcher;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.command.PFCommand;
import dev.vuis.plusfront.net.payload.PFStartConsumablePayload;
import dev.vuis.plusfront.net.payload.PFStopMusicPayload;
import dev.vuis.plusfront.util.PFUtil;
import dev.vuis.plusfront.util.index.ItemIndex;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(
	modid = PlusFront.MOD_ID
)
public final class PFCommonEvents {
	private PFCommonEvents() {
		throw new AssertionError();
	}

	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		PlusFront.LOGGER.info("Doing common setup...");

		ItemIndex.init();
	}

	@SubscribeEvent
	public static void onLoadComplete(FMLLoadCompleteEvent event) {
		PlusFront.LOGGER.info("Doing post-load setup...");

		BFAbstractManager<?, ?, ?> manager = PFUtil.blockfrontManager();

		PlusFront.LOGGER.info(
			"Overriding feature flags:\n{}",
			PlusFront.FEATURE_FLAGS.entrySet().stream()
				.map(flag -> "    " + flag.getKey() + " = " + flag.getValue())
				.collect(Collectors.joining("\n"))
		);

		event.enqueueWork(() -> {
			manager.getConnectionManager()
				.getRequester()
				.getFeatureFlagManager()
				.setFeatureFlags(PlusFront.FEATURE_FLAGS);
		});
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		PlusFront.LOGGER.info("Registering commands...");

		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

		PFCommand.register(dispatcher);
	}

	@SubscribeEvent
	public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
		PlusFront.LOGGER.info("Registering payload handlers...");

		PayloadRegistrar registrar = event.registrar("1");

		PFStartConsumablePayload.register(registrar);
		PFStopMusicPayload.register(registrar);
	}
}
