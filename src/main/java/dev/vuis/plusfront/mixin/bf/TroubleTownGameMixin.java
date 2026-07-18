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
import dev.vuis.plusfront.ex.TroubleTownGameEx;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
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

import static dev.vuis.plusfront.util.AssetCommandUtil.addExecutor;

@Mixin(TroubleTownGame.class)
public abstract class TroubleTownGameMixin implements TroubleTownGameEx {
	@Shadow
	@Final
	private List<DeferredHolder<Item, ? extends GunItem>> itemDrops;
	@Shadow
	@Final
	private AssetCommandBuilder command;

	@Unique
	private List<ItemStack> pf$itemDrops;
	@Unique
	private Optional<Float> pf$playerInfoDistance;

	@Override
	public Optional<Float> pf$getPlayerInfoDistance() {
		return pf$playerInfoDistance;
	}

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void addCustomAssetCommands(BFAbstractManager<?, ?, ?> manager, CallbackInfo ci) {
		AssetCommandBuilder pfCommand = new AssetCommandBuilder();

		AssetCommandBuilder dropCommand = new AssetCommandBuilder();

		addExecutor(
			dropCommand, "add",
			new String[]{"item"}, (context, source, args) -> {
				ResourceLocation itemLocation = ResourceLocation.tryParse(args[0]);
				if (itemLocation == null) {
					return;
				}

				if (!BuiltInRegistries.ITEM.containsKey(itemLocation)) {
					CommandUtils.sendBfaWarn(source, Component.literal("Invalid item."));
					return;
				}

				pf$itemDrops.add(new ItemStack(BuiltInRegistries.ITEM.get(itemLocation)));

				CommandUtils.sendBfa(source, Component.literal("Added item to drop pool. (" + pf$itemDrops.size() + ")"));
			}
		);
		addExecutor(
			dropCommand, "clear",
			(context, source, args) -> {
				pf$itemDrops.clear();
				CommandUtils.sendBfa(source, Component.literal("Cleared custom drop pool."));
			}
		);
		addExecutor(
			dropCommand, "list",
			(context, source, args) -> {
				if (pf$itemDrops.isEmpty()) {
					CommandUtils.sendBfa(source, Component.literal("No custom drop pool."));
					return;
				}

				for (ItemStack stack : pf$itemDrops) {
					CommandUtils.sendBfa(source, Component.literal(
						BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()
					));
				}
			}
		);

		pfCommand.subCommand("drop", dropCommand);

		AssetCommandBuilder pidCommand = new AssetCommandBuilder();

		addExecutor(
			pidCommand, "set",
			new String[]{"distance"}, (context, source, args) -> {
				float value;
				try {
					value = Float.parseFloat(args[0]);
				} catch (NumberFormatException e) {
					CommandUtils.sendBfaWarn(source, Component.literal("Invalid value."));
					return;
				}

				pf$playerInfoDistance = Optional.of(value);

				CommandUtils.sendBfa(source, Component.literal(
					String.format("Set player info distance to %.1f.", value)
				));
			}
		);
		addExecutor(
			pidCommand, "clear",
			(context, source, args) -> {
				pf$playerInfoDistance = Optional.empty();

				CommandUtils.sendBfa(source, Component.literal("Cleared custom player info distance."));
			}
		);

		pfCommand.subCommand("pid", pidCommand);

		command.subCommand("pf", pfCommand);
	}

	@Inject(
		method = "writeAll",
		at = @At("TAIL")
	)
	private void writeCustom(ByteBuf buf, boolean writeMap, CallbackInfo ci) {
		boolean pidExists = pf$playerInfoDistance != null && pf$playerInfoDistance.isPresent();
		buf.writeBoolean(pidExists);
		if (pidExists) {
			buf.writeFloat(pf$playerInfoDistance.orElseThrow());
		}
	}

	@Inject(
		method = "readAll",
		at = @At("TAIL")
	)
	private void readCustom(ByteBuf buf, CallbackInfo ci) {
		pf$playerInfoDistance = buf.readBoolean() ? Optional.of(buf.readFloat()) : Optional.empty();
	}

	@Redirect(
		method = "spawnRandomDrops",
		at = @At(
			value = "NEW",
			target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;",
			ordinal = 0
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
			List.copyOf(pf$itemDrops),
			pf$playerInfoDistance
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

		pf$itemDrops = new ObjectArrayList<>(data.droppedItems());
		pf$playerInfoDistance = data.playerInfoDistance();
	}
}
