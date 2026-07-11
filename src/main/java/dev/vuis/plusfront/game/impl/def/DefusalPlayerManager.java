package dev.vuis.plusfront.game.impl.def;

import com.boehmod.bflib.cloud.common.ChatGraphic;
import com.boehmod.bflib.cloud.packet.IPacket;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.player.BFAbstractPlayerData;
import com.boehmod.blockfront.common.player.PlayerDataHandler;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.GameUtils;
import com.boehmod.blockfront.game.WinningTeamData;
import com.boehmod.blockfront.registry.BFItems;
import com.boehmod.blockfront.registry.BFSounds;
import com.boehmod.blockfront.util.RandomUtils;
import com.boehmod.blockfront.util.math.BFPose;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.util.PFUtil;
import dev.vuis.plusfront.world.BombDamageSource;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DefusalPlayerManager extends AbstractGamePlayerManager<DefusalGame> {
	public static final String CT_NAME = "Counter-Terrorists";
	public static final String T_NAME = "Terrorists";

	public static final Style CT_STYLE = Style.EMPTY.withColor(0x546A91);
	public static final Style CT_ICON_STYLE = Style.EMPTY.withColor(0xDCE2EA);
	public static final Style T_STYLE = Style.EMPTY.withColor(0x7E3831);
	public static final Style T_ICON_STYLE = Style.EMPTY.withColor(0xE4D5D4);

	private final GameTeam counterTerrorists = new GameTeam(game, CT_NAME, CT_STYLE, CT_ICON_STYLE, 8);
	private final GameTeam terrorists = new GameTeam(game, T_NAME, T_STYLE, T_ICON_STYLE, 8);

	private boolean preventEliminationWins = false;
	private @Nullable UUID bombHolder = null;
	private @Nullable Player bombPlanter = null;

	public DefusalPlayerManager(@NotNull DefusalGame game, @NotNull PlayerDataHandler<?> dataHandler) {
		super(game, dataHandler);

		addTeam(counterTerrorists);
		addTeam(terrorists);
	}

	public GameTeam counterTerrorists() {
		return counterTerrorists;
	}

	public GameTeam terrorists() {
		return terrorists;
	}

	public void preventEliminationWins() {
		preventEliminationWins = true;
	}

	public void clearBombHolder() {
		bombHolder = null;
	}

	public boolean isBombHolder(UUID uuid) {
		return uuid.equals(bombHolder);
	}

	public @Nullable Player getBombPlanter() {
		return bombPlanter;
	}

	public void setBombPlanter(@Nullable Player uuid) {
		bombPlanter = uuid;
	}

	public void onRoundFinished() {
		preventEliminationWins = false;
		clearBombHolder();
		setBombPlanter(null);
	}

	@Override
	public void reset() {
		super.reset();

		onRoundFinished();
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
	public void update(@NotNull Set<UUID> players) {
		super.update(players);

		if (game.isRoundInProgress() && !preventEliminationWins) {
			handleEliminations(
				PFUtil.playerDataHandler(),
				players
			);
		}
	}

	private void handleEliminations(PlayerDataHandler<?> dataHandler, Set<UUID> players) {
		int ctDead = PFUtil.getNumUnavailable(dataHandler, counterTerrorists.getPlayers());
		int tDead = PFUtil.getNumUnavailable(dataHandler, terrorists.getPlayers());

		boolean ctOut = ctDead >= counterTerrorists.numPlayers();
		boolean tOut = tDead >= terrorists.numPlayers();

		if (ctOut && tOut) {
			if (game.isBombPlanted()) {
				game.onRoundWin(players, false);
			} else {
				game.onRoundDraw(players);
			}
			return;
		}

		if (ctOut) {
			game.onRoundWin(players, false);
		}
		if (tOut && !game.isBombPlanted()) {
			game.onRoundWin(players, true);
		}
	}

	@Override
	protected void write(@NotNull ByteBuf buf) throws IOException {
		super.write(buf);

		buf.writeBoolean(bombHolder != null);
		if (bombHolder != null) {
			IPacket.writeUUID(buf, bombHolder);
		}
	}

	@Override
	protected void read(@NotNull ByteBuf buf) throws IOException {
		super.read(buf);

		bombHolder = null;
		if (buf.readBoolean()) {
			bombHolder = IPacket.readUUID(buf);
		}
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
		for (ItemEntity item : items) {
			if (item.getItem().getItem() == BFItems.BOMB.value()) {
				return List.of(item);
			}
		}

		return null;
	}

	@Override
	public boolean canDropItem(@NotNull Player player, @NotNull ItemStack stack) {
		return stack.getItem() == BFItems.BOMB.value();
	}

	@Override
	public boolean canPickupItem(@NotNull Player player, @NotNull ItemEntity itemEntity, @NotNull ItemStack stack) {
		if (stack.getItem() != BFItems.BOMB.value()) {
			return false;
		}

		GameTeam team = getPlayerTeam(player.getUUID());

		return team != null && team.getName().equals(T_NAME);
	}

	@Override
	public @Nullable WinningTeamData getWinningTeam(@NotNull ServerLevel level, @NotNull Set<UUID> players, @Nullable GameStageTimer timer) {
		return getWinningTeam();
	}

	public @Nullable WinningTeamData getWinningTeam() {
		if (counterTerrorists.getStatInt(BFStats.SCORE) >= DefusalGame.SCORE_TO_WIN) {
			return new WinningTeamData(counterTerrorists, null);
		}
		if (terrorists.getStatInt(BFStats.SCORE) >= DefusalGame.SCORE_TO_WIN) {
			return new WinningTeamData(terrorists, null);
		}

		return null;
	}

	@Override
	protected boolean shouldPlayerJoinInGame(@NotNull ServerPlayer player) {
		GameTeam team = getPlayerTeam(player.getUUID());
		return team != null && team.numPlayers() <= 1;
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
		GameUtils.teleportPlayer(dataHandler, player, team.randomSpawn(game));
	}

	/**
	 * Teleports all players to random spawns for their respective team.
	 *
	 * @param dataHandler used to set freeze positions if players are currently frozen
	 */
	public void teleportPlayersToRandomSpawn(PlayerDataHandler<?> dataHandler) {
		Random random = ThreadLocalRandom.current();

		for (GameTeam team : getTeams()) {
			List<BFPose> originalSpawns = team.getPlayerSpawns();
			if (originalSpawns.isEmpty()) {
				if (lobbySpawn != null) {
					originalSpawns = List.of(lobbySpawn);
				} else {
					throw new IllegalStateException("Missing team spawns and lobby spawn");
				}
			}
			List<BFPose> spawns = new ObjectArrayList<>(originalSpawns);

			for (UUID playerUuid : team.getPlayers()) {
				if (spawns.isEmpty()) {
					spawns.addAll(originalSpawns);
				}

				GameUtils.teleportPlayerAndSync(
					dataHandler,
					playerUuid,
					spawns.remove(random.nextInt(spawns.size()))
				);
			}
		}
	}

	@Override
	public void onRemovePlayer(@NotNull ServerPlayer player) {
		if (player.getUUID().equals(bombHolder) && !PFUtil.isPlayerUnavailable(player)) {
			refreshTerroristBomb();
		}
	}

	@Override
	public void onTickPlayer(@NotNull ServerPlayer player, @NotNull BFAbstractPlayerData<?, ?, ?, ?> playerData) {
	}

	@Override
	public void handlePlayerDeath(
		@NotNull BFAbstractManager<?, ?, ?> manager,
		@NotNull ServerLevel level,
		@NotNull ServerPlayer killedPlayer,
		@NotNull UUID killedUuid,
		@Nullable ServerPlayer sourcePlayer,
		@Nullable UUID sourceUuid,
		@NotNull DamageSource source,
		@NotNull Set<UUID> players
	) {
		if (!game.isRoundInProgress()) {
			return;
		}

		if (isBombHolder(killedUuid)) {
			onBombDrop(killedPlayer);
		}

		if (sourcePlayer != null &&
			sourceUuid != null &&
			!killedUuid.equals(sourceUuid) &&
			isFriendlyKill(killedUuid, sourceUuid, source)
		) {
			onFriendlyKill(manager, sourcePlayer, sourceUuid, players);
		} else {
			super.handlePlayerDeath(manager, level, killedPlayer, killedUuid, sourcePlayer, sourceUuid, source, players);
		}
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
		if (sourceUuid != null && !sourceUuid.equals(killedUuid)) {
			GameUtils.incrementPlayerStat(manager, game, sourceUuid, BFStats.SCORE);
		}
	}

	private boolean isFriendlyKill(UUID killedUuid, UUID sourceUuid, DamageSource source) {
		if (source instanceof BombDamageSource) {
			return false;
		}

		GameTeam killedTeam = getPlayerTeam(killedUuid);
		GameTeam sourceTeam = getPlayerTeam(sourceUuid);

		return PFUtil.isSameTeam(killedTeam, sourceTeam);
	}

	private void onFriendlyKill(BFAbstractManager<?, ?, ?> manager, ServerPlayer sourcePlayer, UUID sourceUuid, Set<UUID> players) {
		GameUtils.changePlayerStat(manager, game, sourceUuid, BFStats.SCORE, -1);

		GameUtils.sendNotification(
			players,
			Component.translatable(
				"pf.message.gamemode.notification.kill.friendly",
				Component.literal(sourcePlayer.getScoreboardName())
			).withStyle(ChatFormatting.DARK_RED),
			80
		);

		GameUtils.sendChatGraphic(
			ChatGraphic.WARNING,
			sourcePlayer,
			Component.translatable("pf.message.gamemode.message.kill.friendly")
				.withStyle(ChatFormatting.RED)
		);
		GameUtils.playSound(
			sourcePlayer,
			SoundEvents.NOTE_BLOCK_HARP.value(),
			SoundSource.MASTER,
			1f, 2f
		);
		GameUtils.playSound(
			sourcePlayer,
			SoundEvents.NOTE_BLOCK_BASS.value(),
			SoundSource.MASTER,
			1f, 2f
		);
	}

	public void onBombDrop(Player previousPlayer) {
		clearBombHolder();

		Set<UUID> terroristPlayers = terrorists.getPlayers();

		GameUtils.sendNotification(
			terroristPlayers,
			Component.translatable(
				"pf.message.gamemode.notification.bomb.drop",
				Component.literal(previousPlayer.getScoreboardName())
					.withStyle(T_ICON_STYLE)
			).withStyle(T_STYLE),
			100,
			"bomb.transfer"
		);
		GameUtils.playSound(
			terroristPlayers,
			BFSounds.ITEM_BOMB_PLANT.value(),
			SoundSource.NEUTRAL
		);
	}

	public void onBombPickup(Player player) {
		UUID playerUuid = player.getUUID();

		if (!terrorists.hasPlayer(playerUuid)) {
			PlusFront.LOGGER.warn("Non-terrorist player picked up the bomb! ({})", player.getScoreboardName());
		}

		bombHolder = playerUuid;
		game.setBombItem(null);

		Set<UUID> terroristPlayers = terrorists.getPlayers();

		GameUtils.sendNotification(
			terroristPlayers,
			Component.translatable(
				"pf.message.gamemode.notification.bomb.pickup",
				Component.literal(player.getScoreboardName())
					.withStyle(T_ICON_STYLE)
			).withStyle(T_STYLE),
			100,
			"bomb.transfer"
		);
		GameUtils.playSound(
			terroristPlayers,
			BFSounds.ITEM_BOMB_PLANT.value(),
			SoundSource.NEUTRAL
		);
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
		GameUtils.unfreezePlayer(dataHandler, player);

		if (game.isPreStage()) {
			GameTeam team = getPlayerTeam(player.getUUID());

			if (team != null) {
				GameUtils.teleportPlayer(dataHandler, player, team.randomSpawn(game));
				GameUtils.giveClassLoadout(level, player, game, team);
				player.containerMenu.broadcastChanges();
			}
		} else {
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
		}
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

		if (game.isRoundInProgress()) {
			GameStageTimer timer = game.getInProgressTimer();
			if (timer == null) {
				return true;
			}

			GameTeam damagedTeam = getPlayerTeam(uuid);
			GameTeam sourceTeam = getPlayerTeam(sourceUuid);

			if (PFUtil.isSameTeam(damagedTeam, sourceTeam)) {
				return timer.secondsPassed() >= 5;
			} else {
				return true;
			}
		}

		if (game.isRoundWaiting()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean shouldAllowPlayerDamage(@NotNull LivingEntity entity, @NotNull DamageSource source) {
		return true;
	}

	public boolean shouldAddAssistDamage(@NotNull UUID sourceUuid, @NotNull UUID targetUuid) {
		GameTeam sourceTeam = getPlayerTeam(sourceUuid);
		GameTeam targetTeam = getPlayerTeam(targetUuid);

		return !PFUtil.isSameTeam(sourceTeam, targetTeam);
	}

	@Override
	public boolean isAcceptingPlayers() {
		return true;
	}

	/**
	 * Selects a random player from the Terrorists as the "bomb player", gives them a bomb item if they do not already have one, and notifies the players on their team.
	 * Does nothing if the bomb is planted.
	 */
	public void refreshTerroristBomb() {
		if (game.isBombPlanted()) {
			return;
		}

		if (terrorists.numPlayers() == 0) {
			bombHolder = null;
			return;
		}

		Set<UUID> terroristPlayers = terrorists.getPlayers();

		UUID randomUuid = RandomUtils.randomFromSet(terroristPlayers);
		ServerPlayer randomPlayer = GameUtils.getPlayerByUUID(randomUuid);

		if (randomPlayer == null) {
			return;
		}

		bombHolder = randomUuid;
		giveAndSync(randomPlayer, BFItems.BOMB.value());

		GameUtils.sendNotification(
			terroristPlayers,
			Component.translatable(
				"pf.message.gamemode.notification.bomb.give",
				Component.literal(randomPlayer.getScoreboardName())
					.withStyle(T_ICON_STYLE)
			).withStyle(T_STYLE),
			100,
			"bomb.transfer"
		);
		GameUtils.playSound(
			terroristPlayers,
			BFSounds.ITEM_BOMB_PLANT.value(),
			SoundSource.NEUTRAL
		);
	}

	public void onGiveLoadout(ServerPlayer player) {
		GameTeam team = getPlayerTeam(player.getUUID());

		if (team != null && team.getName().equals(CT_NAME)) {
			giveSingletonItem(player, BFItems.BOMB_DEFUSE_KIT.value());
		}

		if (player.getUUID().equals(bombHolder)) {
			giveSingletonItem(player, BFItems.BOMB.value());
		}
	}

	/**
	 * Adds an item stack of size 1 to the given player's inventory and broadcasts the changes.
	 *
	 * @param player the player to give the item to
	 * @param item   the item to give
	 */
	private static void giveAndSync(Player player, Item item) {
		player.getInventory().add(new ItemStack(item));
		player.containerMenu.broadcastChanges();
	}

	/**
	 * Checks that the given player's inventory does not already have an item of the given item instance, then adds an item stack of size 1 and broadcasts the changes.
	 *
	 * @param player the player to give the item to
	 * @param item   the item to give
	 */
	private static void giveSingletonItem(Player player, Item item) {
		for (ItemStack stack : player.getInventory().items) {
			if (stack.getItem() == item) {
				return;
			}
		}

		giveAndSync(player, item);
	}

	public void onStartConsumable(Level level, Player player, ItemStack stack) {
		Vec3 position = player.position();

		if (!game.checkBombSiteArea(position)) {
			return;
		}

		Item item = stack.getItem();

		if (item == BFItems.BOMB.value()) {
			level.playSound(
				player,
				position.x, position.y, position.z,
				BFSounds.ITEM_BOMB_PLANT.value(),
				SoundSource.NEUTRAL,
				1.75f, 1f
			);
			level.playSound(
				player,
				position.x, position.y, position.z,
				SoundEvents.ENDER_DRAGON_FLAP,
				SoundSource.NEUTRAL,
				1.5f, 2f
			);
		} else if (item == BFItems.BOMB_DEFUSE_KIT.value()) {
			level.playSound(
				player,
				position.x, position.y, position.z,
				SoundEvents.ENDER_DRAGON_FLAP,
				SoundSource.NEUTRAL,
				1.75f, 2f
			);
		}
	}
}
