package dev.vuis.plusfront.util;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.AssetCommandValidator;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public final class AssetCommandUtil {
	private AssetCommandUtil() {
		throw new AssertionError();
	}

	public static void addExecutor(AssetCommandBuilder parent, String name, Executor executor) {
		parent.subCommand(name, executor(executor));
	}

	public static void addExecutor(AssetCommandBuilder parent, String name, String[] requiredArgs, Executor executor) {
		parent.subCommand(name, executor(executor, requiredArgs));
	}

	public static AssetCommandBuilder executor(Executor executor) {
		return new AssetCommandBuilder((context, args) -> executor.execute(context, context.getSource().source, args));
	}

	public static AssetCommandBuilder executor(Executor executor, String... requiredArgs) {
		return executor(executor).validator(countValidator(requiredArgs));
	}

	public static @NotNull AssetCommandValidator countValidator(String... args) {
		return AssetCommandValidators.count(args);
	}

	@FunctionalInterface
	public interface Executor {
		void execute(
			@NotNull CommandContext<CommandSourceStack> context,
			@NotNull CommandSource source,
			@NotNull String[] args
		);
	}
}
