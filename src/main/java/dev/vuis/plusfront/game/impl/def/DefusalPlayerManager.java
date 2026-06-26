package dev.vuis.plusfront.game.impl.def;

import com.boehmod.bflib.cloud.packet.IPacket;
import com.boehmod.blockfront.BlockFront;
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
import dev.vuis.plusfront.util.PFUtil;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

	private UUID bombPlayer = null;

	public DefusalPlayerManager(@NotNull DefusalGame game, @NotNull PlayerDataHandler<?> dataHandler) {
		super(game, dataHandler);

		addTeam(new GameTeam(game, CT_NAME, CT_STYLE, CT_ICON_STYLE, 8));
		addTeam(new GameTeam(game, T_NAME, T_STYLE, T_ICON_STYLE, 8));
	}

	public void clearBombPlayer() {
		bombPlayer = null;
	}

	public boolean isBombPlayer(UUID uuid) {
		return uuid.equals(bombPlayer);
	}

	@Override
	public void reset() {
		super.reset();

		clearBombPlayer();
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

		BFAbstractManager<?, ?, ?> manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			return;
		}
		PlayerDataHandler<?> dataHandler = manager.getPlayerDataHandler();

		if (!(game.getStageManager().getCurrentStage() instanceof DefusalGameStage gameStage)) {
			return;
		}
		if (gameStage.isFinished) {
			return;
		}

		GameTeam ctTeam = getTeamByName(DefusalPlayerManager.CT_NAME);
		assert ctTeam != null;
		GameTeam tTeam = getTeamByName(DefusalPlayerManager.T_NAME);
		assert tTeam != null;

		int ctDead = PFUtil.getNumUnavailable(dataHandler, ctTeam.getPlayers());
		int tDead = PFUtil.getNumUnavailable(dataHandler, tTeam.getPlayers());

		boolean ctOut = ctDead >= ctTeam.numPlayers();
		boolean tOut = tDead >= tTeam.numPlayers();

		if (ctOut && tOut) {
			if (!game.isBombPlanted()) {
				game.onRoundDraw(players);
			} else {
				game.onRoundWin(players, false);
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

		buf.writeBoolean(bombPlayer != null);
		if (bombPlayer != null) {
			IPacket.writeUUID(buf, bombPlayer);
		}
	}

	@Override
	protected void read(@NotNull ByteBuf buf) throws IOException {
		super.read(buf);

		bombPlayer = null;
		if (buf.readBoolean()) {
			bombPlayer = IPacket.readUUID(buf);
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
		GameTeam team = getPlayerTeam(player.getUUID());
		return team != null && team.numPlayers() <= 1;
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
		if (player.getUUID().equals(bombPlayer)) {
			refreshTerroristBomb();
		}
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
		if (killedUuid.equals(bombPlayer)) {
			clearBombPlayer();

			GameTeam terroristsTeam = getTeamByName(T_NAME);
			assert terroristsTeam != null;

			Set<UUID> terrorists = terroristsTeam.getPlayers();

			GameUtils.sendNotification(
				terrorists,
				Component.translatable(
					"pf.message.gamemode.notification.bomb.drop",
					Component.literal(killedPlayer.getScoreboardName())
						.withStyle(terroristsTeam.getStyleIcon())
				).withStyle(terroristsTeam.getStyleText()),
				100,
				"bomb.transfer"
			);
			GameUtils.playSound(
				terrorists,
				BFSounds.ITEM_BOMB_PLANT.value(),
				SoundSource.NEUTRAL
			);
		}
	}

	public void onItemPickup(Player player, ItemEntity item) {
		if (item.getItem().getItem() != BFItems.BOMB.value()) {
			return;
		}

		UUID playerUuid = player.getUUID();

		GameTeam terroristsTeam = getTeamByName(T_NAME);
		assert terroristsTeam != null;

		if (!terroristsTeam.hasPlayer(playerUuid)) {
			return;
		}

		bombPlayer = playerUuid;

		Set<UUID> terrorists = terroristsTeam.getPlayers();

		GameUtils.sendNotification(
			terrorists,
			Component.translatable(
				"pf.message.gamemode.notification.bomb.pickup",
				Component.literal(player.getScoreboardName())
					.withStyle(terroristsTeam.getStyleIcon())
			).withStyle(terroristsTeam.getStyleText()),
			100,
			"bomb.transfer"
		);
		GameUtils.playSound(
			terrorists,
			BFSounds.ITEM_BOMB_PLANT.value(),
			SoundSource.NEUTRAL
		);
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

	public void refreshTerroristBomb() {
		if (game.isBombPlanted()) {
			return;
		}

		GameTeam terroristsTeam = getTeamByName(DefusalPlayerManager.T_NAME);
		assert terroristsTeam != null;

		if (terroristsTeam.numPlayers() == 0) {
			bombPlayer = null;
			return;
		}

		Set<UUID> terrorists = terroristsTeam.getPlayers();

		UUID randomUuid = RandomUtils.randomFromSet(terrorists);
		ServerPlayer randomPlayer = GameUtils.getPlayerByUUID(randomUuid);

		if (randomPlayer == null) {
			return;
		}

		bombPlayer = randomUuid;
		giveAndSync(randomPlayer, BFItems.BOMB.value());

		GameUtils.sendNotification(
			terrorists,
			Component.translatable(
				"pf.message.gamemode.notification.bomb.give",
				Component.literal(randomPlayer.getScoreboardName()).withStyle(terroristsTeam.getStyleIcon())
			).withStyle(terroristsTeam.getStyleText()),
			100,
			"bomb.transfer"
		);
		GameUtils.playSound(
			terrorists,
			BFSounds.ITEM_BOMB_PLANT.value(),
			SoundSource.NEUTRAL
		);
	}

	public void onGiveLoadout(ServerPlayer player) {
		GameTeam team = getPlayerTeam(player.getUUID());

		if (team != null && team.getName().equals(CT_NAME)) {
			giveSingletonItem(player, BFItems.BOMB_DEFUSE_KIT.value());
		}

		if (player.getUUID().equals(bombPlayer)) {
			giveSingletonItem(player, BFItems.BOMB.value());
		}
	}

	private static void giveAndSync(Player player, Item item) {
		player.getInventory().add(new ItemStack(item));
		player.containerMenu.broadcastChanges();
	}

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

		if (!game.checkBombSiteDistance(position)) {
			return;
		}

		Item item = stack.getItem();

		if (item == BFItems.BOMB.value()) {
			level.playSound(
				player,
				position.x, position.y, position.z,
				BFSounds.ITEM_BOMB_PLANT.value(),
				SoundSource.NEUTRAL,
				1.75f, 0.75f
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
				1.75f, 1.5f
			);
		}
	}
}
