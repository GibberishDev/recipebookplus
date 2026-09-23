package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {

    @Shadow
    @Final
    private RecipeManager recipeManager;

    protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(method = "handleAddOrRemoveRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundRecipePacket;getState()Lnet/minecraft/network/protocol/game/ClientboundRecipePacket$State;",shift = At.Shift.AFTER),cancellable = true)
    public void recipebookplus$handleAddOrRemoveRecipes(ClientboundRecipePacket packet, CallbackInfo ci) {
        if (Config.getModEnabled() && packet.getState() == ClientboundRecipePacket.State.ADD && Config.getRecipeOverlayAnchor()!= Config.HUDOverlayAnchor.NONE) {
            assert this.minecraft.player != null;
            ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();
            for(ResourceLocation resourcelocation : packet.getRecipes()) {
                this.recipeManager.byKey(resourcelocation).ifPresent((holder) -> {
                    clientrecipebook.add(holder);
                    clientrecipebook.addHighlight(holder);
                });
            }
            clientrecipebook.getCollections().forEach((p_205540_) -> p_205540_.updateKnownRecipes(clientrecipebook));
            if (this.minecraft.screen instanceof RecipeUpdateListener) {
                ((RecipeUpdateListener)this.minecraft.screen).recipesUpdated();
            }
            ci.cancel();
        }
    }

}
