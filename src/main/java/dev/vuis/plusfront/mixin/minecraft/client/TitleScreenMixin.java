package dev.vuis.plusfront.mixin.minecraft.client;

import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.client.screen.title.LobbyTitleScreen;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
	@Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
	@Expression("builder(translatable('menu.online'), ?)")
	@ModifyExpressionValue(
		method = "createNormalMenuOptions",
		at = @At(
			value = "MIXINEXTRAS:EXPRESSION",
			ordinal = 0
		)
	)
	private Button.Builder replaceRealmsWithBlockfront(Button.Builder original) {
		return Button.builder(BlockFront.DISPLAY_NAME_COMPONENT, b -> minecraft.setScreen(new LobbyTitleScreen()));
	}
}
