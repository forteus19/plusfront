package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.entity.BombEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BombEntity.class)
public abstract class BombEntityMixin {
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/common/entity/BombEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
			ordinal = 0
		)
	)
	private void preventBombSliding(BombEntity instance, Vec3 deltaMovement) {
		instance.setDeltaMovement(0.0, deltaMovement.y, 0.0);
	}
}
