package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.PreGameStage;
import org.jetbrains.annotations.NotNull;

public class DefusalPreStage extends PreGameStage<DefusalGame, DefusalPlayerManager> {
	public DefusalPreStage() {
		super(20);
	}

	@Override
	public void onStageEnd(@NotNull GameStageContext<DefusalGame, DefusalPlayerManager> context) {
		GameUtils.discardMatchEntities(context.serverLevel(), context.game(), context.playerHandler());
	}

	@Override
	public @NotNull AbstractGameStage<DefusalGame, DefusalPlayerManager> createNextStage(@NotNull DefusalGame game) {
		return new DefusalWaitingStage();
	}
}
