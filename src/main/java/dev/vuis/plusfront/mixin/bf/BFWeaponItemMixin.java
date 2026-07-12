package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.item.BFWeaponItem;
import com.boehmod.blockfront.registry.BFDataComponents;
import dev.vuis.plusfront.player.PFArmory;
import dev.vuis.plusfront.registry.PFAttachmentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BFWeaponItem.class)
public abstract class BFWeaponItemMixin {
	@Shadow
	public static void setOriginalOwner(@NotNull ItemStack var0, @NotNull String var1) {
	}

	@Inject(
		method = "loadComponents",
		at = @At("HEAD"),
		cancellable = true
	)
	private void customComponentHandling(ItemStack stack, Entity entity, CallbackInfo ci) {
		ci.cancel();

		stack.set(BFDataComponents.HAS_TAG, true);

		if (!(entity instanceof Player player)) {
			return;
		}

		setOriginalOwner(stack, player.getScoreboardName());

		PFArmory.Weapons.Stack customStack = player.getData(PFAttachmentTypes.ARMORY_WEAPONS).getEquippedWeapon(stack.getItem());
		if (customStack != null) {
			customStack.setComponents(stack);
		}
	}
}
