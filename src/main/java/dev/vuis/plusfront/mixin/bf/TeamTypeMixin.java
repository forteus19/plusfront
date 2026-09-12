package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.game.TeamType;
import com.boehmod.blockfront.util.BFRes;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.game.CustomTeamTypes;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeamType.class)
public abstract class TeamTypeMixin {
    @Inject(
        method = "<clinit>",
        at = @At("TAIL")
    )
    private static void registerCustom(CallbackInfo ci) {
        CustomTeamTypes.registerPermanent();
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/boehmod/blockfront/util/BFRes;loc(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;",
            ordinal = 0
        )
    )
    private ResourceLocation overrideNamespace(String path) {
        return CustomTeamTypes.OVERRIDE_NAMESPACE.get() ? PlusFront.res(path) : BFRes.loc(path);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
            ordinal = 0
        )
    )
    private boolean checkIndexDisabled1(List instance, Object e) {
        if (!CustomTeamTypes.DISABLE_INDEX.get()) {
            return instance.add(e);
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            ordinal = 0
        )
    )
    private Object checkIndexDisabled2(Map instance, Object k, Object v) {
        if (!CustomTeamTypes.DISABLE_INDEX.get()) {
            return instance.put(k, v);
        }
        return null;
    }
}
