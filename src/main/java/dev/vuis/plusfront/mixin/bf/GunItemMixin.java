package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.util.BFRes;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.registry.PFItems;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GunItem.class)
public abstract class GunItemMixin {
	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/util/BFRes;loc(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;",
			ordinal = 0
		)
	)
	private ResourceLocation overrideNamespace(String path) {
		return PFItems.GUN_OVERRIDE_NAMESPACE.get() ? PlusFront.res(path) : BFRes.loc(path);
	}
}
