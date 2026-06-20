package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.game.GameTypeCodec;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownGame;
import com.boehmod.blockfront.util.CommandUtils;
import com.boehmod.blockfront.util.RandomUtils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.vuis.plusfront.data.PFTroubleTownData;
import dev.vuis.plusfront.ex.TroubleTownCodecEx;
import dev.vuis.plusfront.util.PFAssetCommandValidators;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TroubleTownGame.class)
public abstract class TroubleTownGameMixin {
	@Shadow
	@Final
	private List<DeferredHolder<Item, ? extends GunItem>> itemDrops;
	@Shadow
	@Final
	private AssetCommandBuilder command;

	@Unique
	private final List<ItemStack> pf$itemDrops = new ObjectArrayList<>();

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void addCustomAssetCommands(BFAbstractManager<?, ?, ?> manager, CallbackInfo ci) {
		AssetCommandBuilder pfCommand = new AssetCommandBuilder();

		AssetCommandBuilder dropCommand = new AssetCommandBuilder();

		dropCommand.subCommand(
			"add",
			new AssetCommandBuilder((context, args) -> {
				CommandSource source = context.getSource().source;

				ResourceLocation itemLocation = ResourceLocation.tryParse(args[0]);
				if (itemLocation == null) {
					return;
				}

				if (!BuiltInRegistries.ITEM.containsKey(itemLocation)) {
					CommandUtils.sendBfaWarn(source, Component.literal("Invalid item"));
					return;
				}

				pf$itemDrops.add(new ItemStack(BuiltInRegistries.ITEM.get(itemLocation)));
				CommandUtils.sendBfa(source, Component.literal("Added item to drop pool. (" + pf$itemDrops.size() + ")"));
			}).validator(
				PFAssetCommandValidators.count("item")
			)
		);
		dropCommand.subCommand(
			"clear",
			new AssetCommandBuilder((context, args) -> {
				CommandSource source = context.getSource().source;

				pf$itemDrops.clear();
				CommandUtils.sendBfa(source, Component.literal("Cleared custom drop pool."));
			})
		);
		dropCommand.subCommand(
			"list",
			new AssetCommandBuilder((context, args) -> {
				CommandSource source = context.getSource().source;

				if (pf$itemDrops.isEmpty()) {
					CommandUtils.sendBfa(source, Component.literal("No custom drop pool."));
					return;
				}

				for (ItemStack stack : pf$itemDrops) {
					CommandUtils.sendBfa(source, Component.literal(
						BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()
					));
				}
			})
		);

		pfCommand.subCommand("drop", dropCommand);

		command.subCommand("pf", pfCommand);
	}

	@Redirect(
		method = "spawnRandomDrop",
		at = @At(
			value = "NEW",
			target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;"
		)
	)
	private ItemStack overrideDropStack(ItemLike ignored) {
		ItemStack selectedStack;
		if (pf$itemDrops.isEmpty()) {
			selectedStack = new ItemStack(RandomUtils.randomFromList(itemDrops).get());
		} else {
			selectedStack = RandomUtils.randomFromList(pf$itemDrops).copy();
		}
		return selectedStack;
	}

	@ModifyReturnValue(
		method = "getCodecData",
		at = @At("RETURN")
	)
	private GameTypeCodec addCustomCodecData(GameTypeCodec original) {
		TroubleTownCodecEx.cast((GameTypeCodec.TroubleTown) original).pf$setCustomData(new PFTroubleTownData(
			List.copyOf(pf$itemDrops)
		));

		return original;
	}

	@Inject(
		method = "readCodecData",
		at = @At("TAIL")
	)
	private void readCustomCodecData(GameTypeCodec codec, CallbackInfo ci) {
		if (!(codec instanceof GameTypeCodec.TroubleTown tttCodec)) {
			return;
		}

		PFTroubleTownData data = TroubleTownCodecEx.cast(tttCodec).pf$getCustomData();

		pf$itemDrops.clear();
		pf$itemDrops.addAll(data.droppedItems());
	}
}
