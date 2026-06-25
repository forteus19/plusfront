package dev.vuis.plusfront.client.def;

import com.boehmod.blockfront.client.render.game.element.TeamGameElement;
import com.boehmod.blockfront.client.render.game.element.TextWithIconGameElement;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.util.BFRes;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.game.impl.def.DefusalGame;
import dev.vuis.plusfront.game.impl.def.DefusalPlayerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DefusalTeamGameElement extends TextWithIconGameElement<DefusalGame, DefusalPlayerManager> {
	private static final ResourceLocation CT_ICON = PlusFront.res("textures/text/team_ct.png");
	private static final ResourceLocation T_ICON = BFRes.loc("textures/text/team_axis.png");

	@Override
	public void update(
		@NotNull Minecraft minecraft,
		@NotNull DefusalGame game,
		@NotNull DefusalPlayerManager playerManager,
		@NotNull AbstractGameClient<DefusalGame, DefusalPlayerManager> gameClient,
		@NotNull LocalPlayer player
	) {
		GameTeam team = game.getPlayerManager().getPlayerTeam(player.getUUID());

		if (team != null) {
			String teamName = team.getName();

			setText(
				Component.literal(teamName).withStyle(team.getStyleText())
			);
			setIconTexture(
				switch (teamName) {
					case DefusalPlayerManager.CT_NAME -> CT_ICON;
					case DefusalPlayerManager.T_NAME -> T_ICON;
					default -> null;
				}
			);
		} else {
			setText(TeamGameElement.NO_TEAM_COMPONENT);
			setIconTexture(null);
		}
	}
}
