package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.interfaces.accessors.IGhostRecipeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe.GhostIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;

@Mixin(GhostRecipe.class)
public class GhostRecipeMixin implements IGhostRecipeAccessor {
    @Shadow @Final
    private List<GhostIngredient> ingredients;
    @Shadow
    float time;

    @Unique
    private HashMap<GhostIngredient, Rect2i> recipebookplus$ingredientRects = new HashMap<>();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void recipebookplus$render$ingredientRectSetter(GuiGraphics guiGraphics, Minecraft minecraft, int leftPos, int topPos, boolean offset, float partialTick, CallbackInfo ci) {
        if (Config.getModEnabled()) {
            if (!Screen.hasControlDown()) {
                this.time += partialTick;
            }
            recipebookplus$ingredientRects.clear();
            for (int i = 0; i < this.ingredients.size(); ++i) {
                GhostIngredient ghostrecipe$ghostingredient = this.ingredients.get(i);
                int j = ghostrecipe$ghostingredient.getX() + leftPos;
                int k = ghostrecipe$ghostingredient.getY() + topPos;
                if (i == 0 && offset) {
                    guiGraphics.fill(j - 4, k - 4, j + 20, k + 20, 822018048);
                } else {
                    guiGraphics.fill(j, k, j + 16, k + 16, 822018048);
                }

                ItemStack itemstack = ghostrecipe$ghostingredient.getItem();
                guiGraphics.renderFakeItem(itemstack, j, k);

                recipebookplus$ingredientRects.put(ghostrecipe$ghostingredient, new Rect2i(j,k,16,16));

                guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), j, k, j + 16, k + 16, 822083583);
                if (i == 0) {
                    guiGraphics.renderItemDecorations(minecraft.font, itemstack, j, k);
                }
            }
            ci.cancel();
        }
    }


    @Override
    public ItemStack recipebookplus$getGhostItem(double mouseX, double mouseY) {
        if (recipebookplus$ingredientRects.isEmpty()) return null;
        final ItemStack[] item = {null};
        recipebookplus$ingredientRects.forEach((ingredient, rect) -> {
            if (rect.contains((int) mouseX, (int) mouseY)) item[0] = ingredient.getItem();
        });
        return item[0];
    }
}
