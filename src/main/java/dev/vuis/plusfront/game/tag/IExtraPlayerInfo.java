package dev.vuis.plusfront.game.tag;

import java.util.Optional;
import java.util.UUID;

public interface IExtraPlayerInfo {
	Optional<Integer> getPlayerHighlight(UUID playerUuid);

	float getPlayerHealth(UUID playerUuid);
}
