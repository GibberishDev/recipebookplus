package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin <I extends RecipeInput, R extends Recipe<I>> {

    @Shadow
    protected final StackedContents stackedContents = new StackedContents();
    @Shadow
    protected Inventory inventory;
    @Shadow
    protected RecipeBookMenu<I, R> menu;

    @Shadow
    protected abstract void clearGrid();
    @Shadow
    protected abstract boolean testClearGrid();
    @Shadow
    protected abstract void handleRecipeClicked(RecipeHolder<R> recipe, boolean placeAll);

    @Inject(method = "recipeClicked", at = @At("HEAD"), cancellable = true)
    public void rbp$recipeClicked(ServerPlayer player, @Nullable RecipeHolder<R> recipe, boolean placeAll, CallbackInfo ci) {
        if (Config.getModEnabled() && !Config.getRecipeDiscovery()) {
            if (recipe != null) {
                this.inventory = player.getInventory();
                if (this.testClearGrid() || player.isCreative()) {
                    this.stackedContents.clear();
                    player.getInventory().fillStackedContents(this.stackedContents);
                    this.menu.fillCraftSlotsStackedContents(this.stackedContents);
                    if (this.stackedContents.canCraft(recipe.value(), null)) {
                        this.handleRecipeClicked(recipe, placeAll);
                    } else {
                        this.clearGrid();
                        player.connection.send(new ClientboundPlaceGhostRecipePacket(player.containerMenu.containerId, recipe));
                    }

                    player.getInventory().setChanged();
                }
            }
            ci.cancel();
        }

    }

}
