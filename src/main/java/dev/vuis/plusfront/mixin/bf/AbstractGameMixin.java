package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.TeamType;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.vuis.plusfront.PFTemp;
import dev.vuis.plusfront.compat.voicechat.PFVoicechat;
import dev.vuis.plusfront.ex.AbstractGameEx;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
		method = "reset",
		at = @At("TAIL")
	)
	private void handleVoicechat(ServerLevel level, CallbackInfo ci) {
		if (PFTemp.voicechatLoaded) {
			PFVoicechat.getInstance().onGameEnd(getUUID());
		}
	}

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
