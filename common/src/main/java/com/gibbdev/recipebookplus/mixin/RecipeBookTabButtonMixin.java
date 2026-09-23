package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeBookTabButton.class)
public abstract class RecipeBookTabButtonMixin extends StateSwitchingButton implements IRecipeBookButton {

    public RecipeBookTabButtonMixin(int x, int y, int width, int height, boolean initialState) { super(x, y, width, height, initialState); }

    @Unique
    private int recipebookplus$stickerColor = 0;
    @Unique
    private WidgetSprites recipebookplus$buttonTexture = null;
    @Shadow
    private float animationTime;
    @Shadow
    protected abstract void renderIcon(GuiGraphics guiGraphics, ItemRenderer itemRenderer);
    @Shadow @Final
    private RecipeBookCategories category;

    @Unique
    private final WidgetSprites CUSTOM_TAB_SPRITES_CYAN = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_cyan"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_cyan"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_cyan"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_hover_cyan")
    );
    @Unique
    private final WidgetSprites CUSTOM_TAB_SPRITES_YELLOW = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_yellow"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_yellow"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_yellow"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_hover_yellow")
    );
    @Unique
    private final WidgetSprites CUSTOM_TAB_SPRITES_MAGENTA = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_magenta"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_magenta"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_magenta"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_hover_magenta")
    );
    @Unique
    private final WidgetSprites CUSTOM_TAB_SPRITES_GREEN = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_green"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_green"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_green"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_hover_green")
    );
    @Unique
    private final WidgetSprites CUSTOM_TAB_SPRITES_RED = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_red"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_red"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_red"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_hover_red")
    );

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Config.getModEnabled() && Config.getUseCustomUI()) {
            if (this.animationTime > 0.0F) {
                float f = 1.0F + 0.1F * (float) Math.sin(this.animationTime / 15.0F * (float) Math.PI);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float) (this.getX() + 8), (float) (this.getY() + 12), 0.0F);
                guiGraphics.pose().scale(1.0F, f, 1.0F);
                guiGraphics.pose().translate((float) (-(this.getX() + 8)), (float) (-(this.getY() + 12)), 0.0F);
            }

            Minecraft minecraft = Minecraft.getInstance();
            RenderSystem.disableDepthTest();
            ResourceLocation resourcelocation = recipebookplus$buttonTexture.get(this.isStateTriggered, this.isHovered());
            int xPos = this.getX();
            int yPos = this.getY() + 3;
            this.height = 20;
            if (!this.isStateTriggered && this.isHovered()) {
                this.width = 34;
            } else if (this.isStateTriggered) {
                this.width = 38;
            } else {
                xPos += 3;
                this.width = 31;
            }
            guiGraphics.setColor(1.0F,1.0F,1.0F,(float) (this.isStateTriggered ? 0.9 : 0.75));
            guiGraphics.blitSprite(resourcelocation, xPos, yPos, this.width, this.height);
            guiGraphics.setColor(1.0F,1.0F,1.0F,1.0F);
            RenderSystem.enableDepthTest();
            this.renderIcon(guiGraphics, minecraft.getItemRenderer());
            if (this.animationTime > 0.0F) {
                guiGraphics.pose().popPose();
                this.animationTime -= partialTick;
            }
            ci.cancel();
        }
    }

    @Inject(method = "renderIcon", at = @At("HEAD"),cancellable = true)
    private void renderIcon(GuiGraphics guiGraphics, ItemRenderer itemRenderer, CallbackInfo ci) {
        if (Config.getModEnabled() && Config.getUseCustomUI()) {
            List<ItemStack> list = this.category.getIconItems();
            int i = (this.isHovered || this.isStateTriggered) ? 1 : 4;
            if (list.size() == 1) {
                guiGraphics.renderFakeItem((ItemStack) list.get(0), this.getX() + 9 + i, this.getY() + 5);
            } else if (list.size() == 2) {
                guiGraphics.renderFakeItem((ItemStack) list.get(0), this.getX() + 3 + i, this.getY() + 5);
                guiGraphics.renderFakeItem((ItemStack) list.get(1), this.getX() + 14 + i, this.getY() + 5);
            }
            ci.cancel();
        }
    }


    @Unique
    @Override
    public void recipebookplus$setColor(int color) {
        if (color == -1) recipebookplus$buttonTexture = CUSTOM_TAB_SPRITES_RED;
        else if (color == 0) recipebookplus$buttonTexture = CUSTOM_TAB_SPRITES_YELLOW;
        else {
            List<WidgetSprites> colors = new ArrayList<>();
            colors.add(CUSTOM_TAB_SPRITES_CYAN);
            colors.add(CUSTOM_TAB_SPRITES_MAGENTA);
            colors.add(CUSTOM_TAB_SPRITES_GREEN);
            color = (int) Math.floor(Math.random() * ((double) colors.size() - 0.01));
            recipebookplus$buttonTexture = colors.get(color);
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (this.isStateTriggered) return;
        if (Config.getUseCustomUI() && Config.getModEnabled()) handler.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        else handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}


