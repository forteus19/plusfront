package dev.vuis.plusfront.world;

import com.boehmod.blockfront.common.world.damage.BFDamageSource;
import com.boehmod.blockfront.registry.BFItems;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BombDamageSource extends BFDamageSource {
	public BombDamageSource(@NotNull Level level, @Nullable Entity entity) {
		super(
			level.damageSources().damageTypes.getHolderOrThrow(DamageTypes.EXPLOSION),
			entity,
			new ItemStack(BFItems.BOMB.value())
		);
	}
}
