package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.common.item.BFConsumableItem;
import com.llamalad7.mixinextras.sugar.Local;
import dev.vuis.plusfront.net.payload.PFStartConsumablePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BFConsumableItem.class)
public abstract class BFConsumableItemMixin {
	@Shadow
	public static int ticksUntilAction;

	@Redirect(
		method = "inventoryTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/MouseHandler;isRightPressed()Z",
			ordinal = 0
		)
	)
	private boolean replaceRightWithUseKey(MouseHandler instance, @Local Minecraft minecraft) {
		return minecraft.options.keyUse.isDown();
	}

	@Inject(
		method = "inventoryTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V",
			ordinal = 0
		)
	)
	private void addStartListener(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci) {
		if (ticksUntilAction == 0) {
			PacketDistributor.sendToServer(PFStartConsumablePayload.INSTANCE);
		}
	}
}
