package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.client.BFClient;
import com.boehmod.blockfront.common.BFAbstractManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BFClient.class)
public abstract class BFClientMixin {
	@Redirect(
		method = "getManager",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/BlockFront;getManager()Lcom/boehmod/blockfront/common/BFAbstractManager;",
			ordinal = 0
		)
	)
	private static BFAbstractManager<?, ?, ?> checkInitialized(BlockFront instance) {
		return instance != null ? instance.getManager() : null;
	}
}
