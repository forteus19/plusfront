package dev.vuis.plusfront.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.vuis.plusfront.client.PlusFrontClient;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

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
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new ConfigurationScreen(
			PlusFrontClient.instance().getContainer(), null
		));

		return 1;
	}
}
