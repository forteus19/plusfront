package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.TeamType;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.vuis.plusfront.ex.AbstractGameEx;
import dev.vuis.plusfront.server.PFCustomMemes;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractGame.class)
public abstract class AbstractGameMixin implements AbstractGameEx {
	@Shadow
	public abstract @NotNull UUID getUUID();

	@Unique
	private TeamType pf$alliesTeamOverride;
	@Unique
	private TeamType pf$axisTeamOverride;

	@Inject(
		method = "write",
		at = @At("TAIL")
	)
	private void writeCustom(ByteBuf buf, CallbackInfo ci) {
		boolean hasAlliesTeamOverride = pf$alliesTeamOverride != null;
		buf.writeBoolean(hasAlliesTeamOverride);
		if (hasAlliesTeamOverride) {
			ResourceLocation.STREAM_CODEC.encode(buf, pf$alliesTeamOverride.getResourceLocation());
		}

		boolean hasAxisTeamOverride = pf$axisTeamOverride != null;
		buf.writeBoolean(hasAxisTeamOverride);
		if (hasAxisTeamOverride) {
			ResourceLocation.STREAM_CODEC.encode(buf, pf$axisTeamOverride.getResourceLocation());
		}
	}

	@Inject(
		method = "read",
		at = @At("TAIL")
	)
	private void readCustom(ByteBuf buf, CallbackInfo ci) {
		if (buf.readBoolean()) {
			pf$alliesTeamOverride = TeamType.getByResourceLocation(ResourceLocation.STREAM_CODEC.decode(buf));
		}

		if (buf.readBoolean()) {
			pf$axisTeamOverride = TeamType.getByResourceLocation(ResourceLocation.STREAM_CODEC.decode(buf));
		}
	}

	@ModifyReturnValue(
		method = "getAlliesDivision",
		at = @At("TAIL")
	)
	private TeamType overrideAlliesTeam(TeamType original) {
		return pf$alliesTeamOverride != null ? pf$alliesTeamOverride : original;
	}

	@ModifyReturnValue(
		method = "getAxisDivision",
		at = @At("TAIL")
	)
	private TeamType overrideAxisTeam(TeamType original) {
		return pf$axisTeamOverride != null ? pf$axisTeamOverride : original;
	}

	@Definition(id = "var11", local = @Local(type = DeferredHolder.class, ordinal = 0))
	@Expression("var11 == null")
	@ModifyExpressionValue(
		method = "onPlayerSoundboard",
		at = @At(
			value = "MIXINEXTRAS:EXPRESSION",
			ordinal = 0
		)
	)
	private boolean preventMemeEarlyReturn(boolean original, @Local(ordinal = 0) String memeName) {
		return original && !PFCustomMemes.LOADED.containsKey(memeName);
	}

	@Redirect(
		method = "onPlayerSoundboard",
		at = @At(
			value = "INVOKE",
			target = "Lnet/neoforged/neoforge/registries/DeferredHolder;get()Ljava/lang/Object;",
			ordinal = 0
		)
	)
	private Object handleCustomMemes(DeferredHolder<SoundEvent, SoundEvent> original, @Local(ordinal = 0) String memeName) {
		Holder<SoundEvent> customMeme = PFCustomMemes.LOADED.get(memeName);
		// if original == null, customMeme != null because of the earlier check
		return customMeme != null ? customMeme.value() : original.value();
	}

	@Override
	public TeamType pf$getAlliesTeamOverride() {
		return pf$alliesTeamOverride;
	}

	@Override
	public void pf$setAlliesTeamOverride(TeamType teamType) {
		pf$alliesTeamOverride = teamType;
	}

	@Override
	public TeamType pf$getAxisTeamOverride() {
		return pf$axisTeamOverride;
	}

	@Override
	public void pf$setAxisTeamOverride(TeamType teamType) {
		pf$axisTeamOverride = teamType;
	}
}
