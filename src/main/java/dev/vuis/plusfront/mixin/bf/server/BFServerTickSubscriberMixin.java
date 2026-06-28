package dev.vuis.plusfront.mixin.bf.server;

import com.boehmod.blockfront.server.event.BFServerTickSubscriber;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BFServerTickSubscriber.class)
public abstract class BFServerTickSubscriberMixin {
	@Redirect(
		method = "onTickPost",
		at = @At(
			value = "FIELD",
			target = "Lcom/boehmod/blockfront/cloud/BFFeatureFlags;serverPlayerVoiceSounds:Z",
			opcode = Opcodes.PUTSTATIC,
			ordinal = 0
		)
	)
	private static void stopFeatureFlagOverriding(boolean value) {
		// why does it do this lmao
	}
}
