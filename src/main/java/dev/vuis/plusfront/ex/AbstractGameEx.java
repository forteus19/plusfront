package dev.vuis.plusfront.ex;

import com.boehmod.blockfront.game.TeamType;

public interface AbstractGameEx {
	TeamType pf$getAlliesTeamOverride();

	void pf$setAlliesTeamOverride(TeamType teamType);

	TeamType pf$getAxisTeamOverride();

	void pf$setAxisTeamOverride(TeamType teamType);
}
