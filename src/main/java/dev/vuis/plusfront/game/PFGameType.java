package dev.vuis.plusfront.game;

import com.boehmod.bflib.cloud.common.mm.SearchGame;
import com.boehmod.blockfront.game.GameType;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import dev.vuis.plusfront.game.impl.oitc.ChamberGame;

@SuppressWarnings("unused")
public final class PFGameType {
	public static final GameType DEFUSAL = new GameType(GameType.Category.VERSUS, "pf.gamemode.def", "def", SearchGame.DEFUSAL, DefusalGame.class)
		.experimental().hidden();
	public static final GameType CHAMBER = new GameType(GameType.Category.VERSUS, "pf.gamemode.oitc", "oitc", SearchGame.FREE_FOR_ALL, ChamberGame.class)
		.experimental().hidden();

	private PFGameType() {
		throw new AssertionError();
	}

	public static void init() {
	}
}
