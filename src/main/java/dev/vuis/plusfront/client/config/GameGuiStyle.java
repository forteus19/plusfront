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
			.hiddenGameElementTypes(
				GameType.DOMINATION,
				GameType.TEAM_DEATHMATCH,
				GameType.FREE_FOR_ALL,
				PFGameType.DEFUSAL
			)
			.showNotificationsAsToasts()
			.builtInPlayerHeads()
	);

	private final Settings settings;

	public boolean shouldHideGameElements(GameType gameType) {
		return settings.hiddenGameElementTypes.contains(gameType);
	}

	public boolean showNotificationsInChat() {
		return settings.showNotificationsInChat;
	}

	public boolean hasBuiltInPlayerHeads() {
		return settings.builtInPlayerHeads;
	}

	public static final class Settings {
		private Set<GameType> hiddenGameElementTypes = Set.of();
		private boolean showNotificationsInChat = false;
		private boolean builtInPlayerHeads = false;

		private Settings() {
		}

		private Settings hiddenGameElementTypes(GameType... disabledGameElementTypes) {
			this.hiddenGameElementTypes = Set.of(disabledGameElementTypes);
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
	}
}
