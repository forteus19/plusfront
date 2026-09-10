package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.common.item.GunItem;
import dev.vuis.plusfront.registry.PFItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GunItem.class)
public abstract class GunItemMixin {
	@Redirect(
		method = "method_3768",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/common/item/GunItem;method_3757()Ljava/lang/String;",
			ordinal = 0
		)
	)
	private String overrideIdForCustomItems(GunItem instance) {
		return PFItems.fixInternalId(instance.method_3757());
	}
}
