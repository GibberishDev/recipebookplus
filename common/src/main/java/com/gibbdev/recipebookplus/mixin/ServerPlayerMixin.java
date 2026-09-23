package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import com.gibbdev.recipebookplus.ui.hud.HUDOverlays;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "awardRecipes", at = @At("HEAD"))
    public void awardRecipes(Collection<RecipeHolder<?>> recipes, CallbackInfoReturnable<Integer> cir) {
        if (Config.getRecipeDiscovery() && Config.getModEnabled() && Config.getRecipeDiscoveryAdvancement()) {
            Set<String> list = new HashSet<>();
            for (RecipeHolder<?> holder : recipes) {
                if (!RecipeDiscovery.isKnown(holder.id().toString(), this.uuid)) list.add(holder.id().toString());
            }
            if (Config.getRecipeOverlayAnchor()!= Config.HUDOverlayAnchor.NONE) {
                HUDOverlays.RecipeAcquiredPopup.queueRecipeNotification(list);
            }
        }

    }

}
