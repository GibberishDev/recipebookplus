package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {

    protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }
    @Inject(method = "handleTakeItemEntity", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;getItem()Lnet/minecraft/world/item/ItemStack;"),cancellable = false)
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {

        if (Config.getModEnabled() && Config.getRecipeDiscovery() &&
                (
                        Config.getRecipeDiscoveryMode() == Config.RECIPE_DISCOVERY_MODE_ENUM.INGREDIENT ||
                                Config.getRecipeDiscoveryMode() == Config.RECIPE_DISCOVERY_MODE_ENUM.INGREDIENT_AND_ITEM ||
                                Config.getRecipeDiscoveryMode() == Config.RECIPE_DISCOVERY_MODE_ENUM.ITEM
                )
        ) {
            ItemStack item = ((ItemEntity) minecraft.level.getEntity(packet.getItemId())).getItem();
            RecipeDiscovery.itemPickUp(item,minecraft.player.getUUID());
        }
    }
}
