package dev.vuis.plusfront.event;

import com.mojang.brigadier.CommandDispatcher;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.command.PFCommand;
import dev.vuis.plusfront.net.payload.PFFeatureFlagsPayload;
import dev.vuis.plusfront.net.payload.PFStartConsumablePayload;
import dev.vuis.plusfront.net.payload.PFStopMusicPayload;
import dev.vuis.plusfront.player.PFArmory;
import dev.vuis.plusfront.registry.PFAttachmentTypes;
import dev.vuis.plusfront.server.config.PFServerConfig;
import dev.vuis.plusfront.util.PFUtil;
import dev.vuis.plusfront.util.index.ItemIndex;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
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
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}

		var featureFlags = PFUtil.getFeatureFlags(server);
		if (featureFlags != null) {
			PacketDistributor.sendToAllPlayers(new PFFeatureFlagsPayload(featureFlags));
		}

		if (PFServerConfig.INSTANCE.getAutoFetchArmory()) {
			PFArmory.Weapons weapons = player.getData(PFAttachmentTypes.ARMORY_WEAPONS);
			PFArmory.Extra extra = player.getData(PFAttachmentTypes.ARMORY_EXTRA);

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

		PFFeatureFlagsPayload.register(registrar);
		PFStartConsumablePayload.register(registrar);
		PFStopMusicPayload.register(registrar);
	}

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		PlusFront.LOGGER.info("Initializing feature flags...");

		var featureFlags = PFUtil.getFeatureFlags(event.getServer());
		if (featureFlags != null) {
			PFUtil.updateFeatureFlags(featureFlags);
		}
	}
}
