package dev.vuis.plusfront.ex;

import dev.vuis.plusfront.data.PFAbstractGameData;
import java.util.Optional;

public interface GameAssetCodecEx {
	Optional<PFAbstractGameData> pf$getCustomData();

	void pf$setCustomData(Optional<PFAbstractGameData> data);
}
