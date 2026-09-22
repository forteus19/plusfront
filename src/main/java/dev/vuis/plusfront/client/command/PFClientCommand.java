package dev.vuis.plusfront.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.vuis.plusfront.client.screen.PFConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public final class PFClientCommand {
	private PFClientCommand() {
		throw new AssertionError();
	}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("pf").then(literal("client").then(
			literal("config").executes(PFClientCommand::runConfig)
		)));
    }

	private static int runConfig(CommandContext<CommandSourceStack> context) {
		Minecraft.getInstance().setScreen(new PFConfigScreen(null));
		return 1;
	}
}
