package dev.vuis.plusfront.command;

import com.boehmod.bflib.cloud.common.item.CloudItem;
import com.boehmod.bflib.cloud.common.item.CloudItemStack;
import com.boehmod.bflib.cloud.common.item.types.AbstractCloudItemCoin;
import com.boehmod.bflib.cloud.common.item.types.CloudItemCallingCard;
import com.boehmod.blockfront.assets.AssetStore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.player.PFArmory;
import dev.vuis.plusfront.registry.PFAttachmentTypes;
import dev.vuis.plusfront.util.PFUtil;
import dev.vuis.plusfront.util.PFZipUtil;
import dev.vuis.plusfront.util.index.CloudRegistryIndex;
import dev.vuis.plusfront.util.index.FeatureFlagIndex;
import dev.vuis.plusfront.util.index.ItemIndex;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class PFCommand {
	private PFCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("pf").then(
			literal("armory").requires(stack -> stack.hasPermission(2)).then(
				argument("players", EntityArgument.players()).then(
					literal("clear").executes(PFCommand::runArmoryClear).then(
						literal("weapon").executes(PFCommand::runArmoryClearWeapon).then(
							argument("item", ResourceLocationArgument.id()).suggests(ItemIndex.suggestWeapons()).executes(PFCommand::runArmoryWeaponClearSpecific)
						)
					).then(
						literal("card").executes(PFCommand::runArmoryClearCard)
					).then(
						literal("coin").executes(PFCommand::runArmoryClearCoin)
					)
				).then(
					literal("fetch").executes(PFCommand::runArmoryFetch)
				).then(
					literal("set").then(
						literal("weapon").then(
							argument("item", ResourceLocationArgument.id()).suggests(ItemIndex.suggestWeapons()).then(
								argument("skin", StringArgumentType.word()).suggests(CloudRegistryIndex.ITEMS.suggestWeaponSkins("item")).executes(
									PFCommand::runArmorySetWeapon
								).then(
									argument("mint", DoubleArgumentType.doubleArg()).executes(context ->
										runArmorySetWeapon(context, DoubleArgumentType.getDouble(context, "mint"))
									)
								)
							)
						)
					).then(
						literal("card").then(
							argument("item", StringArgumentType.word()).suggests(CloudRegistryIndex.ITEMS.suggestCards()).executes(PFCommand::runArmorySetCard)
						)
					).then(
						literal("coin").then(
							argument("item", StringArgumentType.word()).suggests(CloudRegistryIndex.ITEMS.suggestCoins()).executes(PFCommand::runArmorySetCoin)
						)
					)
				)
			)
		).then(
			literal("assets").requires(stack -> stack.hasPermission(3)).then(
				literal("backup").executes(PFCommand::runAssetsBackup)
			)
		).then(
			literal("feature").then(
				literal("list").executes(PFCommand::runFeatureList)
			).then(
				literal("get").then(
					argument("name", StringArgumentType.word()).suggests(FeatureFlagIndex.suggestFeatureFlags()).executes(PFCommand::runFeatureGet)
				)
			).then(
				literal("set").requires(stack -> stack.hasPermission(2)).then(
					argument("name", StringArgumentType.word()).suggests(FeatureFlagIndex.suggestFeatureFlags()).then(
						argument("value", BoolArgumentType.bool()).executes(PFCommand::runFeatureSet)
					)
				)
			)
		));
	}

	private static int runArmoryClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY_WEAPONS).clearWeapons();
			player.getData(PFAttachmentTypes.ARMORY_EXTRA).clearAll();
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.clear.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.clear.success", players.size());
			}
		}, true);

		return players.size();
	}

	private static int runArmoryClearWeapon(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY_WEAPONS).clearWeapons();
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.clear.weapon.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.clear.weapon.success", players.size());
			}
		}, true);

		return players.size();
	}

	private static int runArmoryWeaponClearSpecific(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		ResourceLocation itemLocation = ResourceLocationArgument.getId(context, "item");

		if (!ItemIndex.WEAPONS.contains(itemLocation)) {
			stack.sendFailure(Component.translatable("pf.message.command.armory.error.weapon"));
			return -1;
		}

		Item item = BuiltInRegistries.ITEM.get(itemLocation);

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY_WEAPONS).clearWeapon(item);
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.clear.weapon.specific.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.clear.weapon.specific.success", players.size());
			}
		}, true);

		return players.size();
	}

	private static int runArmoryClearCard(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY_EXTRA).clearCard();
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.clear.card.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.clear.card.success", players.size());
			}
		}, true);

		return players.size();
	}

	private static int runArmoryClearCoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY_EXTRA).clearCoin();
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.clear.coin.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.clear.coin.success", players.size());
			}
		}, true);

		return players.size();
	}

	private static int runArmoryFetch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		if (players.size() > 1) {
			stack.sendFailure(Component.translatable("pf.message.command.armory.fetch.error.multiple"));
			return -1;
		}

		ServerPlayer player = players.iterator().next();

		PFArmory.Weapons weapons = player.getData(PFAttachmentTypes.ARMORY_WEAPONS);
		PFArmory.Extra extra = player.getData(PFAttachmentTypes.ARMORY_EXTRA);

		PFArmory.fetch(
			weapons,
			extra,
			player.getUUID(),
			stack.getServer(),
			() -> stack.sendSuccess(() -> Component.translatable(
				"pf.message.command.armory.fetch.success",
				player.getDisplayName(),
				weapons.numWeapons()
			), true)
		);

		return players.size();
	}

	private static int runArmorySetWeapon(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return runArmorySetWeapon(
			context,
			CloudItemStack.mintStack(ThreadLocalRandom.current())
		);
	}

	private static int runArmorySetWeapon(CommandContext<CommandSourceStack> context, double mint) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		ResourceLocation itemLocation = ResourceLocationArgument.getId(context, "item");

		if (!ItemIndex.WEAPONS.contains(itemLocation)) {
			stack.sendFailure(Component.translatable("pf.message.command.armory.error.weapon"));
			return -1;
		}

		String skin = StringArgumentType.getString(context, "skin");

		CloudItem<?> cloudItem = CloudRegistryIndex.ITEMS.getWeaponSkin(itemLocation, skin);
		if (cloudItem == null) {
			stack.sendFailure(Component.translatable("pf.message.command.armory.error.item"));
			return -1;
		}

		Item item = BuiltInRegistries.ITEM.get(itemLocation);

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY_WEAPONS).equipWeapon(item, new PFArmory.Weapons.Stack(cloudItem, mint));
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.set.weapon.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.set.weapon.success", players.size());
			}
		}, true);

		return players.size();
	}

	private static int runArmorySetCard(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		String cardName = StringArgumentType.getString(context, "item");

		CloudItemCallingCard card = CloudRegistryIndex.ITEMS.getCard(cardName);
		if (card == null) {
			stack.sendFailure(Component.translatable("pf.message.command.armory.error.item"));
			return -1;
		}

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY_EXTRA).equipCard(card);
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.set.card.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.set.card.success", players.size());
			}
		}, true);

		return players.size();
	}

	private static int runArmorySetCoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		String coinName = StringArgumentType.getString(context, "item");

		AbstractCloudItemCoin<?> coin = CloudRegistryIndex.ITEMS.getCoin(coinName);
		if (coin == null) {
			stack.sendFailure(Component.translatable("pf.message.command.armory.error.item"));
			return -1;
		}

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY_EXTRA).equipCoin(coin);
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.set.coin.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.set.coin.success", players.size());
			}
		}, true);

		return players.size();
	}

	private static int runAssetsBackup(CommandContext<CommandSourceStack> context) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		assert server != null;

		PlusFront.LOGGER.info("Assets backup requested...");

		CommandSourceStack stack = context.getSource();

		Path serverDirectory = server.getServerDirectory();

		LocalDateTime currentTime = LocalDateTime.now();
		Path targetPath = serverDirectory.resolve(
			Path.of("plusfront", "backups", String.format(
				"%04d-%02d-%02d_%02d-%02d-%02d.zip",
				currentTime.getYear(),
				currentTime.getMonthValue(),
				currentTime.getDayOfMonth(),
				currentTime.getHour(),
				currentTime.getMinute(),
				currentTime.getSecond()
			))
		);

		if (Files.exists(targetPath)) {
			stack.sendFailure(Component.translatable("pf.message.command.assets.backup.error.exists"));
			return -1;
		}

		PlusFront.LOGGER.info("Zipping assets folder...");

		try {
			Path targetParent = targetPath.getParent();
			if (targetParent != null) {
				Files.createDirectories(targetParent);
			}

			PFZipUtil.saveFolderAsZip(
				serverDirectory.resolve(AssetStore.getInstance().getBasePath()),
				targetPath
			);
		} catch (IOException e) {
			PlusFront.LOGGER.error("Exception while zipping assets folder", e);
			stack.sendFailure(Component.translatable("pf.message.command.assets.backup.error.generic"));

			return -1;
		}

		PlusFront.LOGGER.info("Assets backed up!");
		stack.sendSuccess(() -> Component.translatable("pf.message.command.assets.backup.success"), true);

		return 1;
	}

	private static int runFeatureList(CommandContext<CommandSourceStack> context) {
		CommandSourceStack stack = context.getSource();

		var featureFlags = PFUtil.getFeatureFlags(stack.getServer());
		if (featureFlags == null) {
			stack.sendFailure(Component.translatable("pf.message.command.feature.list.error"));
			return -1;
		}

		stack.sendSystemMessage(Component.translatable("pf.message.command.feature.list.header", featureFlags.size()));

		featureFlags.object2BooleanEntrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(flag -> {
			stack.sendSystemMessage(Component.literal("- " + flag.getKey() + ": " + flag.getBooleanValue()));
		});

		return 0;
	}

	private static int runFeatureGet(CommandContext<CommandSourceStack> context) {
		CommandSourceStack stack = context.getSource();

		String name = StringArgumentType.getString(context, "name");

		Optional<Boolean> value = PFUtil.getFeatureFlag(stack.getServer(), name);

		stack.sendSystemMessage(
			value.isPresent() ?
				Component.translatable("pf.message.command.feature.get.success", name, Boolean.toString(value.orElseThrow())) :
				Component.translatable("pf.message.command.feature.get.empty", name)
		);

		return 0;
	}

	private static int runFeatureSet(CommandContext<CommandSourceStack> context) {
		CommandSourceStack stack = context.getSource();

		String name = StringArgumentType.getString(context, "name");
		boolean value = BoolArgumentType.getBool(context, "value");

		if (!FeatureFlagIndex.isAcknowledged(name)) {
			stack.sendFailure(Component.translatable("pf.message.command.feature.set.error.invalid"));
			return -1;
		}

		if (!PFUtil.setFeatureFlag(stack.getServer(), name, value)) {
			stack.sendFailure(Component.translatable("pf.message.command.feature.set.error.generic"));
			return -1;
		}

		stack.sendSuccess(() -> Component.translatable("pf.message.command.feature.set.success", name, Boolean.toString(value)), true);

		return 1;
	}
}
