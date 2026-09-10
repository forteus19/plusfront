package dev.vuis.plusfront.game;

import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.common.match.BFRace;
import com.boehmod.blockfront.game.ClassType;
import com.boehmod.blockfront.game.Loadout;
import com.boehmod.blockfront.game.NationType;
import com.boehmod.blockfront.game.TeamType;
import com.boehmod.blockfront.registry.BFItems;
import com.boehmod.blockfront.util.BFRes;
import dev.vuis.plusfront.PlusFront;
import dev.vuis.plusfront.mixin.bf.TeamTypeAccessor;
import dev.vuis.plusfront.registry.PFItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

// hardcoded custom team types until i can find a better solution :P
public final class CustomTeamTypes {
	public static final ThreadLocal<Boolean> DISABLE_INDEX = ThreadLocal.withInitial(() -> false);

	private static final Map<ResourceLocation, TeamType> DEFUSAL = new Object2ObjectOpenHashMap<>();

	private CustomTeamTypes() {
		throw new AssertionError();
	}

	public static void registerPermanent() {
		NationType us = NationType.getByKey("us");
		assert us != null;

		new TeamType(100, us, "cowboy")
			.races(List.of(BFRace.CAUCASIAN))
			.setLoadouts(Map.of(
				BFRes.loc("rifleman"),
				List.of(
					new Loadout(
						PFItems.GUN_COWBOY_REVOLVER.toStack(),
						null,
						BFItems.MELEE_ITEM_KNIFE_M1905.toStack()
					)
				)
			));
	}

	public static TeamType getDefusal(TeamType original) {
		return DEFUSAL.computeIfAbsent(original.getResourceLocation(), k -> transformDefusal(original));
	}

	private static TeamType transformDefusal(TeamType original) {
		PlusFront.LOGGER.info("Transforming team type {} for defusal", original.getResourceLocation());

		TeamTypeAccessor originalAccessor = (TeamTypeAccessor) (Object) original;

		DISABLE_INDEX.set(true);
		TeamType transformed = new TeamType(
			original.getId(), original.getNationType(), original.getSkin()
		);
		DISABLE_INDEX.set(false);

		transformed.races(originalAccessor.getRaces());

		Map<ClassType, ObjectList<Loadout>> loadouts = ((TeamTypeAccessor) (Object) transformed).getRawLoadouts();
		loadouts.putAll(originalAccessor.getRawLoadouts());

		switch (transformed.getNationType().getTag()) {
			case "fr" -> {
				putSpecialist(
					loadouts,
					BFItems.GUN_BATR,
					BFItems.GUN_PISTOLET_AUTOMATIQUE_MODELE_1935A,
					BFItems.GRENADE_AT_NO82_GAMMON_BOMB,
					BFItems.GRENADE_SMOKE_FUMIGENE_MLE_1916
				);
			}
			case "gb" -> {
				putSpecialist(
					loadouts,
					BFItems.GUN_BATR,
					BFItems.GUN_BROWNING_HIPOWER,
					BFItems.GRENADE_AT_NO82_GAMMON_BOMB,
					BFItems.GRENADE_SMOKE_NO77
				);
			}
			case "ger" -> {
				putSpecialist(
					loadouts,
					BFItems.GUN_PANZERBUCHSE39,
					BFItems.GUN_WALTHER_P38,
					BFItems.GRENADE_AT_HAFTHOHLLADUNG,
					BFItems.GRENADE_SMOKE_STIELHANDGRANATE
				);
			}
			case "it" -> {
				putSpecialist(
					loadouts,
					BFItems.GUN_WZ_35,
					BFItems.GUN_BERETTA_M1934,
					BFItems.GRENADE_AT_TYPE_L,
					BFItems.GRENADE_SMOKE_BREDA_MOD_42
				);
			}
			case "jpn" -> {
				putSpecialist(
					loadouts,
					BFItems.GUN_PTRS,
					BFItems.GUN_TYPE14,
					BFItems.GRENADE_AT_TYPE_3,
					BFItems.GRENADE_SMOKE_TYPE94
				);
			}
			case "pol" -> {
				putSpecialist(
					loadouts,
					BFItems.GUN_WZ_35,
					BFItems.GUN_FB_VIS,
					BFItems.GRENADE_AT_ET_WZ_38,
					BFItems.GRENADE_SMOKE_STIELHANDGRANATE
				);
			}
			case "us" -> {
				putSpecialist(
					loadouts,
					BFItems.GUN_BATR,
					BFItems.GUN_COLT,
					BFItems.GRENADE_AT_NO82_GAMMON_BOMB,
					BFItems.GRENADE_SMOKE
				);
			}
			case "ussr" -> {
				putSpecialist(
					loadouts,
					BFItems.GUN_PTRS,
					BFItems.GUN_TOKAREV_TT33,
					BFItems.GRENADE_AT_RPG_43,
					BFItems.GRENADE_SMOKE_RGD2
				);
			}
		}

		return transformed;
	}

	private static void putSpecialist(
		Map<ClassType, ObjectList<Loadout>> loadouts,
		DeferredHolder<Item, ? extends GunItem> primary,
		DeferredHolder<Item, ? extends GunItem> secondary,
		DeferredHolder<Item, ? extends Item> atGrenade,
		DeferredHolder<Item, ? extends Item> smokeGrenade
	) {
		ClassType specialist = ClassType.getByKey("specialist");
		assert specialist != null;

		loadouts.put(
			specialist,
			ObjectList.of(
				new Loadout(
					new ItemStack(primary.value()),
					new ItemStack(secondary.value()),
					BFItems.MELEE_ITEM_WRENCH.toStack()
				).addExtra(
					new ItemStack(atGrenade.value())
				).addExtra(
					new ItemStack(smokeGrenade.value(), 2)
				)
			)
		);
	}
}
