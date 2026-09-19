package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements TraceableEntity {
    @Shadow
    public abstract ItemStack getItem();

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;I)V"),cancellable = false)
    public void playerTouch(Player entity, CallbackInfo ci) {
        if (Config.getModEnabled() && Config.getRecipeDiscovery() &&
                (
                        Config.getRecipeDiscoveryMode() == Config.RECIPE_DISCOVERY_MODE_ENUM.INGREDIENT ||
                                Config.getRecipeDiscoveryMode() == Config.RECIPE_DISCOVERY_MODE_ENUM.INGREDIENT_AND_ITEM ||
                                Config.getRecipeDiscoveryMode() == Config.RECIPE_DISCOVERY_MODE_ENUM.ITEM
                )
        ) {
            RecipeDiscovery.itemPickUp(this.getItem(), entity.getUUID());
        }
    }
}
