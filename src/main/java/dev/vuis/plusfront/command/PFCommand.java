package dev.vuis.plusfront.command;

import com.boehmod.bflib.cloud.common.item.CloudItem;
import com.boehmod.bflib.cloud.common.item.CloudItemStack;
import com.boehmod.blockfront.assets.AssetStore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.player.PFCustomArmory;
import dev.vuis.plusfront.registry.PFAttachmentTypes;
import dev.vuis.plusfront.util.PFZipUtil;
import dev.vuis.plusfront.util.index.CloudRegistryIndex;
import dev.vuis.plusfront.util.index.ItemIndex;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
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
					literal("clear").executes(PFCommand::runClearAll).then(
						argument("item", ResourceLocationArgument.id()).suggests(ItemIndex.suggestWeapons()).executes(PFCommand::runClearSpecific)
					)
				).then(
					literal("set").then(
						argument("item", ResourceLocationArgument.id()).suggests(ItemIndex.suggestWeapons()).then(
							argument("skin", StringArgumentType.word()).suggests(CloudRegistryIndex.ITEMS.suggestSkins("item")).executes(
								PFCommand::runArmorySet
							).then(
								argument("mint", DoubleArgumentType.doubleArg()).executes(context ->
									runArmorySet(context, DoubleArgumentType.getDouble(context, "mint"))
								)
							)
						)
					)
				)
			)
		).then(
			literal("assets").requires(stack -> stack.hasPermission(3)).then(
				literal("backup").executes(PFCommand::runAssetsBackup)
			)
		));
	}

	private static int runClearAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY).clearAll();
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

	private static int runClearSpecific(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		ResourceLocation itemLocation = ResourceLocationArgument.getId(context, "item");

		if (!ItemIndex.WEAPONS.contains(itemLocation)) {
			stack.sendFailure(Component.translatable("pf.message.command.armory.error.weapon"));
			return -1;
		}

		Item item = BuiltInRegistries.ITEM.get(itemLocation);

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY).clearWeapon(item);
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.clear.specific.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.clear.specific.success", players.size());
			}
		}, true);

		return players.size();
	}

	private static int runArmorySet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return runArmorySet(
			context,
			CloudItemStack.mintStack(ThreadLocalRandom.current())
		);
	}

	private static int runArmorySet(CommandContext<CommandSourceStack> context, double mint) throws CommandSyntaxException {
		CommandSourceStack stack = context.getSource();

		ResourceLocation itemLocation = ResourceLocationArgument.getId(context, "item");

		if (!ItemIndex.WEAPONS.contains(itemLocation)) {
			stack.sendFailure(Component.translatable("pf.message.command.armory.error.weapon"));
			return -1;
		}

		String skin = StringArgumentType.getString(context, "skin");

		CloudItem<?> cloudItem = CloudRegistryIndex.ITEMS.getSkin(itemLocation, skin);
		if (cloudItem == null) {
			stack.sendFailure(Component.translatable("pf.message.command.armory.error.skin"));
			return -1;
		}

		Item item = BuiltInRegistries.ITEM.get(itemLocation);

		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			player.getData(PFAttachmentTypes.ARMORY).equipWeapon(item, new PFCustomArmory.CustomStack(cloudItem, mint));
		}

		stack.sendSuccess(() -> {
			if (players.size() == 1) {
				return Component.translatable("pf.message.command.armory.set.success.single", players.iterator().next().getDisplayName());
			} else {
				return Component.translatable("pf.message.command.armory.set.success", players.size());
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
}
