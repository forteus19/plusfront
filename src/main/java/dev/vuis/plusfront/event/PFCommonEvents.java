package dev.vuis.plusfront.event;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.mojang.brigadier.CommandDispatcher;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.command.PFCommand;
import dev.vuis.plusfront.net.payload.PFStartConsumablePayload;
import dev.vuis.plusfront.net.payload.PFStopMusicPayload;
import dev.vuis.plusfront.player.PFArmory;
import dev.vuis.plusfront.registry.PFAttachmentTypes;
import dev.vuis.plusfront.server.config.PFServerConfig;
import dev.vuis.plusfront.util.PFUtil;
import dev.vuis.plusfront.util.index.ItemIndex;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}

		PFArmory.Weapons weapons = player.getData(PFAttachmentTypes.ARMORY_WEAPONS);
		PFArmory.Extra extra = player.getData(PFAttachmentTypes.ARMORY_EXTRA);

		if (PFServerConfig.INSTANCE.getAutoFetchArmory()) {
			PFArmory.fetch(
				weapons, extra,
				player.getUUID(),
				server,
				() -> PlusFront.LOGGER.info(
					"Auto-fetched armory for {} ({} weapons)",
					player.getScoreboardName(),
					weapons.numWeapons()
				)
			);
		}
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
