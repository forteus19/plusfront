package dev.vuis.plusfront.util;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.function.BiFunction;
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
		parent.subCommand(name, executor(requiredArgs, executor));
	}

	public static AssetCommandBuilder executor(Executor executor) {
		return new AssetCommandBuilder((context, args) -> executor.execute(context, context.getSource().source, args));
	}

	public static AssetCommandBuilder executor(String[] requiredArgs, Executor executor) {
		return executor(executor).validator(AssetCommandValidators.count(requiredArgs));
	}

	public static AssetCommandBuilder executorPlayers(Executor executor) {
		return executor(executor).validator(AssetCommandValidators.ONLY_PLAYERS);
	}

	public static AssetCommandBuilder executorPlayers(String[] requiredArgs, Executor executor) {
		return executor(requiredArgs, executor).validator(AssetCommandValidators.ONLY_PLAYERS);
	}

	public static AssetCommandBuilder executorPlayers(
		String[] requiredArgs,
		BiFunction<CommandContext<CommandSourceStack>, String[], Collection<String>> suggestor,
		Executor executor
	) {
		return executorPlayers(requiredArgs, executor).suggest(suggestor);
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
