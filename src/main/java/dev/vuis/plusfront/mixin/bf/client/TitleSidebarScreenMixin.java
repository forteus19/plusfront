package dev.vuis.plusfront.mixin.bf.client;

import com.boehmod.blockfront.client.gui.widget.BFButton;
import com.boehmod.blockfront.client.screen.title.sidebar.TitleSidebarScreen;
import com.boehmod.blockfront.util.BFStyles;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TitleSidebarScreen.class)
public abstract class TitleSidebarScreenMixin extends Screen {
	protected TitleSidebarScreenMixin(Component title) {
		super(title);
	}

	@Definition(id = "BFButton", type = BFButton.class)
	@Definition(id = "PLAY_OFFLINE_MESSAGE", field = "Lcom/boehmod/blockfront/client/screen/title/sidebar/TitleSidebarScreen;field_1130:Lnet/minecraft/network/chat/Component;")
	@Expression("new BFButton(?, ?, ?, ?, PLAY_OFFLINE_MESSAGE, ?)")
	@ModifyExpressionValue(
		method = "widgetInit",
		at = @At(
			value = "MIXINEXTRAS:EXPRESSION",
			ordinal = 0
		)
	)
	private BFButton replaceWithMinecraftButton(BFButton original) {
		return new BFButton(
			original.getX(), original.getY(), original.getWidth(), original.getHeight(),
			Component.literal("Minecraft").withStyle(BFStyles.BOLD),
			b -> minecraft.setScreen(new TitleScreen())
		);
	}
}
