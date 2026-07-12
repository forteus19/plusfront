package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.update.BFUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BFUpdater.class)
public abstract class BFUpdaterMixin {
	/**
	 * @reason prevent update checking
	 */
	@Overwrite
	public void onUpdate() {
	}
}
