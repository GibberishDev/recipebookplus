package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.recipediscovery.RecipeLookup;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements Container, Nameable {

    @Shadow
    public abstract boolean contains(ItemStack stack);

    @Shadow
    @Final
    public Player player;

    @Inject(method = "setItem", at = @At("HEAD"))
    public void setItem(int index, ItemStack stack, CallbackInfo ci) {
        if (
                !this.contains(stack)
                && stack.getItem() != Items.AIR
        ) {
            RecipeLookup.checkPickedUpItems(stack, this.player);
        }
    }
}
