package dev.vuis.plusfront.command;

import com.boehmod.blockfront.assets.AssetStore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.util.PFZipUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import static net.minecraft.commands.Commands.literal;

public final class PFCommand {
	private PFCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("pf").then(
			literal("assets").then(
				literal("backup").requires(stack -> stack.hasPermission(3)).executes(PFCommand::runAssetsBackup)
			)
		));
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
