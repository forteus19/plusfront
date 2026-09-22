package dev.vuis.plusfront.client.screen;

import com.boehmod.blockfront.client.gui.widget.BFButton;
import com.boehmod.blockfront.client.screen.BFMenuScreen;
import dev.vuis.plusfront.client.config.GameGuiStyle;
import dev.vuis.plusfront.client.config.PFClientConfig;
import dev.vuis.plusfront.util.PFUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PFConfigScreen extends BFMenuScreen {
	private static final Component TITLE = Component.translatable("pf.screen.config.title");

	private static final Component GAME_GUI_STYLE = Component.translatable("pf.config.gameGuiStyle");

	private final @Nullable Screen parent;

	public PFConfigScreen(@Nullable Screen parent) {
		super(TITLE);
		this.parent = parent;
	}

	@Override
	public void widgetInit() {
		super.widgetInit();

		BFButton nextButton;

		nextButton = new BFButton(
			5, 18, 20, 20,
			Component.empty(),
			button -> onClose()
		);
		nextButton.icon(RETURN_ICON).iconSize(20);
		nextButton.background(BFButton.Background.NONE);
		nextButton.tip(CommonComponents.GUI_BACK);
		addRenderableWidget(nextButton);

		nextButton = new BFButton(
			width / 2 + 20, 30, 80, 20,
			PFClientConfig.getGameGuiStyle().getDisplayName(),
			button -> {
				GameGuiStyle newValue = PFUtil.nextEnum(PFClientConfig.getGameGuiStyle());
				PFClientConfig.setGameGuiStyle(newValue);
				button.setMessage(newValue.getDisplayName());
			}
		);
		nextButton.background(BFButton.Background.SHADOW);
		addRenderableWidget(nextButton);
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		graphics.drawCenteredString(font, TITLE, width / 2, 10, 0xFFFFFF);

		graphics.drawCenteredString(font, GAME_GUI_STYLE, width / 2 - 60, 40 - font.lineHeight / 2, 0xFFFFFF);
	}

	@Override
	public void onClose() {
		PFClientConfig.save();
		minecraft.setScreen(parent);
	}
}
