package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.match.TeamType;
import dev.vuis.plusfront.game.TransformedTeamTypes;
import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TeamType.class)
public abstract class TeamTypeMixin {
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
        if (!TransformedTeamTypes.DISABLE_INDEX.get()) {
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
        if (!TransformedTeamTypes.DISABLE_INDEX.get()) {
            return instance.put(k, v);
        }
        return null;
    }
}
