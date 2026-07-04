package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.bflib.cloud.common.item.CloudItemStack;
import com.boehmod.bflib.cloud.common.item.CloudItemType;
import com.boehmod.bflib.cloud.common.player.AbstractCloudInventory;
import com.boehmod.blockfront.cloud.PlayerCloudInventory;
import com.boehmod.blockfront.common.player.PlayerCloudData;
import dev.vuis.plusfront.player.PFArmory;
import dev.vuis.plusfront.registry.PFAttachmentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerCloudInventory.class)
public abstract class PlayerCloudInventoryMixin extends AbstractCloudInventory<PlayerCloudData> {
	public PlayerCloudInventoryMixin(@NotNull PlayerCloudData playerData) {
		super(playerData);
	}

	@Inject(
		method = "method_1674",
		at = @At("HEAD"),
		cancellable = true
	)
	private void overrideWithCustomArmory(CloudItemType itemType, String key, CallbackInfoReturnable<CloudItemStack> cir) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null || !(itemType == CloudItemType.CARD || itemType == CloudItemType.COIN)) {
			return;
		}

		Player player = level.getPlayerByUUID(playerData.getUUID());
		if (player == null) {
			return;
		}

		PFArmory.Extra extra = player.getData(PFAttachmentTypes.ARMORY_EXTRA);

		if (itemType == CloudItemType.CARD) {
			cir.setReturnValue(extra.getEquippedCardStack());
		} else {
			cir.setReturnValue(extra.getEquippedCoinStack());
		}
	}
}
