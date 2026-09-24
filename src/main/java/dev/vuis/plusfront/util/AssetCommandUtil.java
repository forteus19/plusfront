package dev.vuis.plusfront.util;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.function.BiFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public final class AssetCommandUtil {
	private AssetCommandUtil() {
		throw new AssertionError();
	}

	public static void addExecutor(AssetCommandBuilder parent, String name, Executor<CommandSource> executor) {
		parent.subCommand(name, executor(executor));
	}

	public static void addExecutor(AssetCommandBuilder parent, String name, String[] requiredArgs, Executor<CommandSource> executor) {
		parent.subCommand(name, executor(requiredArgs, executor));
	}

	public static AssetCommandBuilder executor(Executor<CommandSource> executor) {
		return new AssetCommandBuilder((context, args) -> executor.execute(context, context.getSource().source, args));
	}

	public static AssetCommandBuilder executor(String[] requiredArgs, Executor<CommandSource> executor) {
		return executor(executor).validator(AssetCommandValidators.count(requiredArgs));
	}

	public static AssetCommandBuilder executorPlayers(Executor<ServerPlayer> executor) {
		return new AssetCommandBuilder((context, args) -> executor.execute(context, (ServerPlayer) context.getSource().source, args))
			.validator(AssetCommandValidators.ONLY_PLAYERS);
	}

	public static AssetCommandBuilder executorPlayers(String[] requiredArgs, Executor<ServerPlayer> executor) {
		return executorPlayers(executor).validator(AssetCommandValidators.count(requiredArgs));
	}

	public static AssetCommandBuilder executorPlayers(
		String[] requiredArgs,
		BiFunction<CommandContext<CommandSourceStack>, String[], Collection<String>> suggestor,
		Executor<ServerPlayer> executor
	) {
		return executorPlayers(requiredArgs, executor).suggest(suggestor);
	}

	@FunctionalInterface
	public interface Executor<S extends CommandSource> {
		void execute(
			@NotNull CommandContext<CommandSourceStack> context,
			@NotNull S source,
			@NotNull String[] args
		);
	}
}
