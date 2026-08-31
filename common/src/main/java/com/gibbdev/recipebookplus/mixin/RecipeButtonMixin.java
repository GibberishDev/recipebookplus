package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeButton.class)
public abstract class RecipeButtonMixin extends AbstractWidget {
    public RecipeButtonMixin(int x, int y, int width, int height, Component message) {super(x, y, width, height, message);}
    @Shadow @Override public abstract void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v);
    @Shadow @Override public abstract void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput);


    @Unique
    private static final ResourceLocation CUSTOM_SLOT_MANY_CRAFTABLE_SPRITE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/slot_many_craftable");
    @Unique
    private static final ResourceLocation CUSTOM_SLOT_CRAFTABLE_SPRITE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/slot_craftable");
    @Unique
    private static final ResourceLocation CUSTOM_SLOT_MANY_UNCRAFTABLE_SPRITE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/slot_many_uncraftable");
    @Unique
    private static final ResourceLocation CUSTOM_SLOT_UNCRAFTABLE_SPRITE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/slot_uncraftable");

    @Shadow
    private RecipeCollection collection;
    @Shadow
    private float animationTime;
    @Shadow
    private RecipeBookMenu<?, ?> menu;
    @Shadow
    private RecipeBook book;
    @Shadow
    private float time;
    @Shadow
    private int currentIndex;

    @Shadow
    protected abstract List<RecipeHolder<?>> getOrderedRecipes();

    @Inject(method = "renderWidget", at = @At("HEAD"),cancellable = true)
    public void rbp$renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Config.getModEnabled() && Config.getUseCustomUI()) {
            if (!Screen.hasControlDown()) {
                this.time += partialTick;
            }

            ResourceLocation resourcelocation;
            if (this.collection.hasCraftable()) {
                if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
                    resourcelocation = CUSTOM_SLOT_MANY_CRAFTABLE_SPRITE;
                } else {
                    resourcelocation = CUSTOM_SLOT_CRAFTABLE_SPRITE;
                }
            } else if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
                resourcelocation = CUSTOM_SLOT_MANY_UNCRAFTABLE_SPRITE;
            } else {
                resourcelocation = CUSTOM_SLOT_UNCRAFTABLE_SPRITE;
            }

            boolean flag = this.animationTime > 0.0F;
            if (flag) {
                float f = 1.0F + 0.1F * (float) Math.sin(this.animationTime / 15.0F * (float) Math.PI);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float) (this.getX() + 8), (float) (this.getY() + 12), 0.0F);
                guiGraphics.pose().scale(f, f, 1.0F);
                guiGraphics.pose().translate((float) (-(this.getX() + 8)), (float) (-(this.getY() + 12)), 0.0F);
                this.animationTime -= partialTick;
            }

            guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.width, this.height);
            List<RecipeHolder<?>> list = this.getOrderedRecipes();
            this.currentIndex = Mth.floor(this.time / 30.0F) % list.size();
            ItemStack itemstack = list.get(this.currentIndex).value().getResultItem(this.collection.registryAccess());
            int i = 4;
            if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
                guiGraphics.renderItem(itemstack, this.getX() + i + 1, this.getY() + i + 1, 0, 10);
                --i;
            }

            guiGraphics.renderFakeItem(itemstack, this.getX() + i, this.getY() + i);
            if (flag) {
                guiGraphics.pose().popPose();
            }

            ci.cancel();
        }
    }


}
