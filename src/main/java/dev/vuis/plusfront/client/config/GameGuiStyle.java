package dev.vuis.plusfront.client.config;

import com.boehmod.blockfront.game.GameType;
import dev.vuis.plusfront.game.PFGameType;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public enum GameGuiStyle {
	MODERN(
		Component.translatable("pf.config.gameGuiStyle.modern"),
		new Settings()
	),
	OLD(
		Component.translatable("pf.config.gameGuiStyle.old"),
		new Settings()
			.disableFancyRectangles()
			.hiddenGameElementTypes(
				GameType.DOMINATION,
				GameType.TEAM_DEATHMATCH,
				GameType.GUN_GAME,
				GameType.FREE_FOR_ALL,
				PFGameType.DEFUSAL,
				PFGameType.CHAMBER
			)
			.showNotificationsInChat()
			.builtInPlayerHeads()
			.oldKillFeed()
			.oldWaitingMessage()
			.oldCapturingStatus()
	),
	CS2(
		Component.translatable("pf.config.gameGuiStyle.cs2"),
		new Settings()
			.supportedGameTypes(
				PFGameType.DEFUSAL
			)
			.hiddenGameElementTypes(
				PFGameType.DEFUSAL
			)
			.notificationOffset(18)
			.builtInPlayerHeads()
	);

	@Getter
	private final @NotNull Component displayName;
	private final @NotNull Settings settings;

	public boolean isGameTypeSupported(GameType gameType) {
		return settings.supportedGameTypes == null || settings.supportedGameTypes.contains(gameType);
	}

	public boolean disableFancyRectangles() {
		return settings.disableFancyRectangles;
	}

	public boolean shouldHideGameElements(GameType gameType) {
		return settings.hiddenGameElementTypes.contains(gameType);
	}

	public int getNotificationOffset() {
		return settings.notificationOffset;
	}

	public boolean showNotificationsInChat() {
		return settings.showNotificationsInChat;
	}

	public boolean hasBuiltInPlayerHeads() {
		return settings.builtInPlayerHeads;
	}

	public boolean showOldKillFeed() {
		return settings.oldKillFeed;
	}

	public boolean showOldWaitingMessage() {
		return settings.oldWaitingMessage;
	}

	public boolean showOldCapturingStatus() {
		return settings.oldCapturingStatus;
	}

	public static final class Settings {
		private @Nullable Set<GameType> supportedGameTypes = null;
		private boolean disableFancyRectangles = false;
		private Set<GameType> hiddenGameElementTypes = Set.of();
		private int notificationOffset = 0;
		private boolean showNotificationsInChat = false;
		private boolean builtInPlayerHeads = false;
		private boolean oldKillFeed = false;
		private boolean oldWaitingMessage = false;
		private boolean oldCapturingStatus = false;

		private Settings() {
		}

		private Settings supportedGameTypes(@Nullable GameType... supportedGameTypes) {
			this.supportedGameTypes = supportedGameTypes != null ? Set.of(supportedGameTypes) : null;
			return this;
		}

		private Settings disableFancyRectangles() {
			disableFancyRectangles = true;
			return this;
		}

		private Settings hiddenGameElementTypes(GameType... hiddenGameElementTypes) {
			this.hiddenGameElementTypes = Set.of(hiddenGameElementTypes);
			return this;
		}

		private Settings notificationOffset(int notificationOffset) {
			this.notificationOffset = notificationOffset;
			return this;
		}

		private Settings showNotificationsInChat() {
			showNotificationsInChat = true;
			return this;
		}

		private Settings builtInPlayerHeads() {
			builtInPlayerHeads = true;
			return this;
		}

		private Settings oldKillFeed() {
			oldKillFeed = true;
			return this;
		}

		private Settings oldWaitingMessage() {
			oldWaitingMessage = true;
			return this;
		}

		private Settings oldCapturingStatus() {
			oldCapturingStatus = true;
			return this;
		}
	}
}
