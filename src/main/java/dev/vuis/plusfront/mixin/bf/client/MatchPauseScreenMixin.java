package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.gui.widget.BFButton;
import com.boehmod.blockfront.client.screen.BFMenuScreen;
import com.boehmod.blockfront.client.screen.match.MatchPauseScreen;
import dev.vuis.plusfront.client.screen.PFConfigScreen;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MatchPauseScreen.class)
public abstract class MatchPauseScreenMixin extends BFMenuScreen {
	public MatchPauseScreenMixin(@NotNull Component component) {
		super(component);
	}

	@Redirect(
		method = "widgetInit",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/client/screen/match/MatchPauseScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
			ordinal = 5
		)
	)
	private GuiEventListener replacePlusFrontSettingsButton(MatchPauseScreen instance, GuiEventListener guiEventListener) {
		BFButton original = (BFButton) guiEventListener;

		BFButton nextButton = new BFButton(
			original.getX(),
			original.getY(),
			original.getWidth(),
			original.getHeight(),
			Component.translatable("pf.message.ingame.settings"),
			button -> minecraft.setScreen(new PFConfigScreen((MatchPauseScreen) (Object) this))
		);
		nextButton.tip(Component.translatable("pf.message.ingame.settings.tip"));
		nextButton.alignment(BFButton.Alignment.LEFT);
		nextButton.background(BFButton.Background.NONE);

		return addRenderableWidget(nextButton);
	}
}
