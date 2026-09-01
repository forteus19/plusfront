package dev.vuis.plusfront.game;

import com.boehmod.bflib.cloud.common.mm.SearchGame;
import com.boehmod.blockfront.game.GameType;
import dev.vuis.plusfront.game.impl.def.DefusalGame;

public final class PFGameType {
	public static final GameType DEFUSAL = new GameType(GameType.Category.VERSUS, "pf.gamemode.def", "def", SearchGame.DEFUSAL, DefusalGame.class)
		.experimental().hidden();

	private PFGameType() {
		throw new AssertionError();
	}

	public static void init() {
	}
}
