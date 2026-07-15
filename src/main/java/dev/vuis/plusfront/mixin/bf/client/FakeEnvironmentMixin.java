package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.env.FakeEnvironment;
import com.boehmod.blockfront.client.player.FakePlayer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FakeEnvironment.class)
public abstract class FakeEnvironmentMixin {
	@Redirect(
		method = "update",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/client/env/FakeEnvironment;checkEnvironment(Lnet/minecraft/client/Minecraft;)V",
			ordinal = 0
		)
	)
	private void skipCheckInUpdate(FakeEnvironment instance, Minecraft minecraft) {
	}

	@Redirect(
		method = "update",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/client/player/FakePlayer;tick()V",
			ordinal = 0
		)
	)
	private void checkPlayerExists(FakePlayer instance) {
		if (instance != null) {
			instance.tick();
		}
	}
}
