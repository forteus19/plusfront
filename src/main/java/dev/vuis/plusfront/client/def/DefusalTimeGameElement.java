package dev.vuis.plusfront.client.def;

import com.boehmod.blockfront.client.render.game.element.ClientGameElement;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.util.BFRes;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import dev.vuis.plusfront.game.impl.def.DefusalGameClient;
import dev.vuis.plusfront.game.impl.def.DefusalPlayerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

public class DefusalTimeGameElement extends ClientGameElement<DefusalGame, DefusalPlayerManager> {
	private static final ResourceLocation STOPWATCH_TEXTURE = BFRes.loc("textures/gui/stopwatch.png");

	private FormattedCharSequence timerText = FormattedCharSequence.EMPTY;
	private boolean isBombPlanted = false;
	private int blinkTimer = 0;

	public DefusalTimeGameElement() {
		super(44);
	}

	@Override
	public void update(
		@NotNull Minecraft minecraft,
		@NotNull DefusalGame game,
		@NotNull DefusalPlayerManager playerManager,
		@NotNull AbstractGameClient<DefusalGame, DefusalPlayerManager> gameClient,
		@NotNull LocalPlayer player
	) {
		if (game.isBombPlanted()) {
			if (!isBombPlanted) {
				blinkTimer = 0;
			}
			isBombPlanted = true;
			blinkTimer = ++blinkTimer % 20;
		} else {
			isBombPlanted = false;
			timerText = gameClient.method_2678().getComponent().getVisualOrderText();
		}
	}

	@Override
	public void render(
		@NotNull GuiGraphics graphics,
		@NotNull PoseStack poseStack,
		@NotNull Font font,
		int x,
		int y,
		float delta
	) {
		super.render(graphics, poseStack, font, x, y, delta);

		int baseX = x + 3;
		int baseY = y + 2;

		if (isBombPlanted) {
			graphics.blit(
				blinkTimer < 10 ? DefusalGameClient.BOMB_TEXTURE : DefusalGameClient.BOMB_BLINK_TEXTURE,
				x + 22 - 16, y, 0f, 0f,
				32, 16, 32, 16
			);
		} else {
			graphics.blit(
				STOPWATCH_TEXTURE,
				baseX, baseY, 0f, 0f,
				11, 11, 11, 11
			);
			graphics.drawString(
				font,
				timerText,
				baseX + 13, y + 4,
				0xFFFFFFFF, true
			);
		}
	}
}
