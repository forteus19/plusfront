package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.match.BFRace;
import com.boehmod.blockfront.game.ClassType;
import com.boehmod.blockfront.game.Loadout;
import com.boehmod.blockfront.game.TeamType;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TeamType.class)
public interface TeamTypeAccessor {
    @Accessor("races")
    List<BFRace> getRaces();

    @Accessor("loadouts")
    Map<ClassType, ObjectList<Loadout>> getRawLoadouts();
}
