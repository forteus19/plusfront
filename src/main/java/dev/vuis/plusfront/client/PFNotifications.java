package dev.vuis.plusfront.client;

import com.boehmod.blockfront.client.gui.notification.BFNotification;
import com.boehmod.blockfront.client.gui.notification.BFNotificationTone;
import dev.vuis.plusfront.client.config.GameGuiStyle;
import dev.vuis.plusfront.client.config.PFClientConfig;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class PFNotifications {
	private static final Component GUI_STYLE_TITLE = Component.translatable("pf.notification.guistyle.title");

	private static final Component ACTION_SWITCH = Component.translatable("pf.message.action.switch");

	private PFNotifications() {
		throw new AssertionError();
	}

	public static @NotNull BFNotification guiStyleRecommend(
		@NotNull GameGuiStyle guiStyle,
		@NotNull String gameType
	) {
		return BFNotification.builder()
			.tone(BFNotificationTone.INFO)
			.title(GUI_STYLE_TITLE)
			.message(Component.translatable(
				"pf.notification.guistyle.recommend",
				guiStyle.getDisplayName(),
				gameType.toUpperCase(Locale.ROOT)
			))
			.acceptAction(ACTION_SWITCH, () -> {
				PFClientConfig.setGameGuiStyle(guiStyle);
				PFClientConfig.save();
			})
			.build();
	}

	public static @NotNull BFNotification guiStyleUnsupported(
		@NotNull GameGuiStyle guiStyleFrom,
		@NotNull GameGuiStyle guiStyleTo,
		@NotNull String gameType
	) {
		return BFNotification.builder()
			.tone(BFNotificationTone.WARNING)
			.title(GUI_STYLE_TITLE)
			.message(Component.translatable(
				"pf.notification.guistyle.unsupported",
				guiStyleFrom.getDisplayName(),
				gameType.toUpperCase(Locale.ROOT),
				guiStyleTo.getDisplayName()
			))
			.build();
	}
}
