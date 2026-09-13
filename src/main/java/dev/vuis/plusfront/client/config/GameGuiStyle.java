package dev.vuis.plusfront.client.config;

import com.boehmod.blockfront.game.GameType;
import dev.vuis.plusfront.game.PFGameType;
import java.util.Set;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GameGuiStyle {
	MODERN(
		new Settings()
	),
	OLD(
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
			.showNotificationsAsToasts()
			.builtInPlayerHeads()
			.oldKillFeed()
			.oldWaitingMessage()
			.oldCapturingStatus()
	);

	private final Settings settings;

	public boolean disableFancyRectangles() {
		return settings.disableFancyRectangles;
	}

	public boolean shouldHideGameElements(GameType gameType) {
		return settings.hiddenGameElementTypes.contains(gameType);
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
		private boolean disableFancyRectangles = false;
		private Set<GameType> hiddenGameElementTypes = Set.of();
		private boolean showNotificationsInChat = false;
		private boolean builtInPlayerHeads = false;
		private boolean oldKillFeed = false;
		private boolean oldWaitingMessage = false;
		private boolean oldCapturingStatus = false;

		private Settings() {
		}

		private Settings disableFancyRectangles() {
			disableFancyRectangles = true;
			return this;
		}

		private Settings hiddenGameElementTypes(GameType... hiddenGameElementTypes) {
			this.hiddenGameElementTypes = Set.of(hiddenGameElementTypes);
			return this;
		}

		private Settings showNotificationsAsToasts() {
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
