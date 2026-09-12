package dev.vuis.plusfront.game.impl.oitc;

import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameStatus;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.WinningTeamData;
import com.boehmod.blockfront.registry.BFDataComponents;
import com.boehmod.blockfront.util.math.BFPose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChamberPlayerManager extends AbstractGamePlayerManager<ChamberGame> {
	private final GameTeam allies = new GameTeam(
		game,
		BFStats.ALLIES_TEAM_NAME,
		Style.EMPTY.withColor(0x7C8148),
		Style.EMPTY.withColor(0xE5EADC),
		10
	);

	public ChamberPlayerManager(@NotNull ChamberGame game, @NotNull PlayerDataHandler<?> dataHandler) {
		super(game, dataHandler);

		addTeam(allies);
	}

	public GameTeam allies() {
		return allies;
	}

	@Override
	public List<ItemEntity> filterDroppedItems(@NotNull Player player, @NotNull List<ItemEntity> items) {
		return List.of();
	}

	@Override
	public boolean canDropItem(@NotNull Player player, @NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean canPickupItem(@NotNull Player player, @NotNull ItemEntity itemEntity, @NotNull ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable GameTeam getNextJoiningTeam() {
		return allies;
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
	public @Nullable WinningTeamData getWinningTeam(@NotNull ServerLevel level, @NotNull Set<UUID> players, @Nullable GameStageTimer timer) {
		return getWinningTeam(players, timer);
	}

	public @Nullable WinningTeamData getWinningTeam(@NotNull Set<UUID> players, @Nullable GameStageTimer timer) {
		if (timer != null && !timer.isDone()) {
			return null;
		}

		SortedSet<UUID> topPlayers = GameUtils.topPlayers(game, 5, BFStats.KILLS, players);
		return new WinningTeamData(null, !topPlayers.isEmpty() ? Set.of(topPlayers.getFirst()) : null);
	}

	@Override
	protected boolean shouldPlayerJoinInGame(@NotNull ServerPlayer player) {
		return true;
	}

	@Override
	public void onPlayerJoin(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer player,
		@NotNull UUID uuid,
		@NotNull GameTeam team
	) {
		GameUtils.teleportPlayer(dataHandler, player, allies.randomSpawn(game));
		game.giveLoadout(level, player);
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
		if (sourcePlayer == null || sourceUuid == null || game.getStatus() != GameStatus.GAME) {
			return;
		}

		ItemStack mainStack = sourcePlayer.getInventory().getItem(0);
		if (mainStack.getItem() instanceof GunItem) {
			mainStack.set(BFDataComponents.AMMO_LOADED, mainStack.getOrDefault(BFDataComponents.AMMO_LOADED, 0) + 1);
		}

		sourcePlayer.heal(5f);
	}

	@Override
	public void onPlayerRespawn(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull PlayerDataHandler<?> dataHandler,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer player,
		@NotNull UUID uuid
	) {
		GameUtils.resetPlayer(dataHandler, level, player);
		GameUtils.teleportPlayer(dataHandler, player, getRandomSpawn(level));
		game.giveLoadout(level, player);
	}

	@Override
	public boolean shouldAllowEntityDamage(@NotNull Player player, @NotNull UUID uuid, @NotNull DamageSource source, @Nullable Entity sourceEntity) {
		if (sourceEntity instanceof Player sourcePlayer) {
			if (player.equals(sourcePlayer)) {
				return source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_FIRE);
			}
		}

		return true;
	}

	@Override
	public boolean shouldAllowPlayerDamage(@NotNull LivingEntity entity, @NotNull DamageSource source) {
		return true;
	}

	@Override
	public boolean isAcceptingPlayers() {
		return true;
	}

	public int addPlayerSpawn(BFPose pose) {
		allies.addPlayerSpawn(pose);
		return allies.getPlayerSpawns().size();
	}

	public BFPose getRandomSpawn(ServerLevel level) {
		List<BFPose> originalSpawns = allies.getPlayerSpawns();
		if (originalSpawns.isEmpty()) {
			return lobbySpawn;
		}

		if (getPlayers().size() == 1) {
			return originalSpawns.get(level.getRandom().nextInt(originalSpawns.size()));
		}

		List<BFPose> possibleSpawns = new ObjectArrayList<>(originalSpawns);
		Collections.shuffle(possibleSpawns);

		for (int radius = 50; radius > 0; radius -= 10) {
			for (BFPose possibleSpawn : possibleSpawns) {
				if (possibleSpawn.getNearestPlayer(level, radius) == null) {
					return possibleSpawn.copy();
				}
			}
		}

		return originalSpawns.get(level.getRandom().nextInt(originalSpawns.size()));
	}
}
