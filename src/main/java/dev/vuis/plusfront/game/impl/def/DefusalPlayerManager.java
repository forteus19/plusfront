package dev.vuis.plusfront.game.impl.def;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.WinningTeamData;
import com.boehmod.blockfront.util.RandomUtils;
import com.boehmod.blockfront.util.math.BFPose;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DefusalPlayerManager extends AbstractGamePlayerManager<DefusalGame> {
	public static final String CT_NAME = "Counter-Terrorists";
	public static final String T_NAME = "Terrorists";

	public DefusalPlayerManager(@NotNull DefusalGame game, @NotNull PlayerDataHandler<?> dataHandler) {
		super(game, dataHandler);

		Style ctStyle = Style.EMPTY.withColor(0x475D82);
		Style ctTextStyle = Style.EMPTY.withColor(0xDCE2EA);
		Style tStyle = Style.EMPTY.withColor(0x7E3831);
		Style tTextStyle = Style.EMPTY.withColor(0xE4D5D4);

		addTeam(new GameTeam(game, CT_NAME, ctStyle, ctTextStyle, 8));
		addTeam(new GameTeam(game, T_NAME, tStyle, tTextStyle, 8));
	}

	@Override
	public @Nullable GameTeam getTeamByName(@NotNull String name) {
		// workaround for hardcoded team names
		return super.getTeamByName(switch (name) {
			case BFStats.ALLIES_TEAM_NAME -> CT_NAME;
			case BFStats.AXIS_TEAM_NAME -> T_NAME;
			default -> name;
		});
	}

	@Override
	public boolean canBreakBlock(@NotNull Player player, @NotNull Block block) {
		return false;
	}

	@Override
	public boolean canPlaceBlock(@NotNull Player player, @NotNull Block block) {
		return false;
	}

	@Override
	public boolean canRightClickBlock(@NotNull PlayerDataHandler<?> dataHandler, @NotNull Level level, @NotNull Player player, @NotNull Block block, @NotNull BlockPos blockPos) {
		return true;
	}

	@Override
	public List<ItemEntity> filterDroppedItems(@NotNull Player player, @NotNull List<ItemEntity> items) {
		return null;
	}

	@Override
	public boolean canPickupItem(@NotNull Player player, @NotNull ItemEntity itemEntity, @NotNull ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable WinningTeamData getWinningTeam(@NotNull ServerLevel level, @NotNull Set<UUID> players, @Nullable GameStageTimer timer) {
		return getWinningTeam();
	}

	public @Nullable WinningTeamData getWinningTeam() {
		if (game.getStageManager().getCurrentStage() instanceof DefusalGameStage gameStage && !gameStage.isFinished) {
			return null;
		}

		GameTeam ctTeam = getTeamByName(DefusalPlayerManager.CT_NAME);
		assert ctTeam != null;
		GameTeam tTeam = getTeamByName(DefusalPlayerManager.T_NAME);
		assert tTeam != null;

		if (ctTeam.getStatInt(BFStats.SCORE) >= DefusalGame.SCORE_TO_WIN) {
			return new WinningTeamData(ctTeam, null);
		}
		if (tTeam.getStatInt(BFStats.SCORE) >= DefusalGame.SCORE_TO_WIN) {
			return new WinningTeamData(tTeam, null);
		}

		return null;
	}

	@Override
	protected boolean shouldPlayerRespawnInGame(@NotNull ServerPlayer player) {
		return false;
	}

	@Override
	public void onInitPlayer(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer player,
		@NotNull UUID uuid,
		@NotNull GameTeam team
	) {
		GameUtils.teleportPlayer(dataHandler, player, team.randomSpawn(game));
	}

	public void teleportPlayersToRandomSpawn(PlayerDataHandler<?> dataHandler) {
		for (GameTeam team : getTeams()) {
			for (UUID playerUuid : team.getPlayers()) {
				GameUtils.teleportPlayerAndSync(dataHandler, playerUuid, team.randomSpawn(game));
			}
		}
	}

	@Override
	public void onRemovePlayer(@NotNull ServerPlayer player) {
	}

	@Override
	public void onTickPlayer(@NotNull ServerPlayer player, @NotNull BFAbstractPlayerData<?, ?, ?, ?> playerData) {
	}

	@Override
	public void onPlayerKilled(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer killedPlayer,
		@NotNull UUID killedUuid,
		@Nullable ServerPlayer sourcePlayer,
		@Nullable UUID sourceUuid,
		@NotNull DamageSource source,
		@NotNull Set<UUID> players
	) {
	}

	@Override
	public void initPlayer(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer player,
		@NotNull UUID uuid
	) {
		GameUtils.initPlayerForGame(dataHandler, level, player);
		player.setGameMode(GameType.SPECTATOR);

		BFPose spawnPos = lobbySpawn;

		Set<UUID> availablePlayers = getAvailablePlayers();
		if (!availablePlayers.isEmpty()) {
			Player randomPlayer = GameUtils.getPlayerByUUID(
				RandomUtils.randomFromSet(availablePlayers)
			);

			if (randomPlayer != null) {
				spawnPos = new BFPose(randomPlayer);
			}
		}

		if (spawnPos != null) {
			GameUtils.teleportPlayer(dataHandler, player, spawnPos);
		}
		GameUtils.unfreezePlayer(dataHandler, player);
	}

	@Override
	public boolean shouldAllowEntityDamage(@NotNull Player player, @NotNull UUID uuid, @NotNull DamageSource source, @Nullable Entity sourceEntity) {
		if (!(sourceEntity instanceof Player sourcePlayer)) {
			return true;
		}

		UUID sourceUuid = sourcePlayer.getUUID();

		if (uuid.equals(sourceUuid)) {
			return source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_FIRE);
		}

		GameTeam damagedTeam = getPlayerTeam(uuid);
		GameTeam sourceTeam = getPlayerTeam(sourceUuid);

		return damagedTeam == null || sourceTeam == null || !damagedTeam.getName().equals(sourceTeam.getName());
	}

	@Override
	public boolean shouldAllowPlayerDamage(@NotNull LivingEntity entity, @NotNull DamageSource source) {
		return true;
	}

	@Override
	public boolean isAcceptingPlayers() {
		return true;
	}
}
