package dev.vuis.plusfront.game.impl.oitc;

import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGameStage;
import com.boehmod.blockfront.game.GameStageContext;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.PreGameStage;
import dev.vuis.plusfront.game.PFGameHelper;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public final class ChamberPreStage extends PreGameStage<ChamberGame, ChamberPlayerManager> {
	public ChamberPreStage() {
		super(20);
	}

	@Override
	public void onStageEnd(@NotNull GameStageContext<ChamberGame, ChamberPlayerManager> context) {
		ChamberGame game = context.game();
		ChamberPlayerManager playerManager = context.playerHandler();
		PlayerDataHandler<?> dataHandler = context.playerDataHandler();
		ServerLevel level = context.serverLevel();
		Set<UUID> players = context.players();

		GameUtils.unfreezePlayers(dataHandler, players);
		PFGameHelper.forUuids(players, player -> {
			GameUtils.teleportPlayer(dataHandler, player, playerManager.getRandomSpawn(level));
			game.giveLoadout(level, player);
		});
		GameUtils.resetPlayers(level, players, dataHandler);
	}

	@Override
	public @NotNull AbstractGameStage<ChamberGame, ChamberPlayerManager> createNextStage(@NotNull ChamberGame game) {
		return new ChamberGameStage();
	}
}
