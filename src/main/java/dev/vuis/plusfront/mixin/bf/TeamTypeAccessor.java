package dev.vuis.plusfront.mixin.bf;

import com.boehmod.blockfront.common.match.BFRace;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import com.boehmod.blockfront.common.match.TeamType;
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
    Map<MatchClass, ObjectList<Loadout>> getRawLoadouts();
}
