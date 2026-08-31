package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeBookTabButton.class)
public abstract class RecipeBookButtonMixin extends StateSwitchingButton implements IRecipeBookButton {

    public RecipeBookButtonMixin(int x, int y, int width, int height, boolean initialState) { super(x, y, width, height, initialState); }

    @Unique
    private int rbp$stickerColor = 0;
    @Shadow
    private float animationTime;
    @Shadow
    protected abstract void renderIcon(GuiGraphics guiGraphics, ItemRenderer itemRenderer);

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
            List<WidgetSprites> colors = new ArrayList<>();
            colors.add(CUSTOM_TAB_SPRITES_CYAN);
            colors.add(CUSTOM_TAB_SPRITES_YELLOW);
            colors.add(CUSTOM_TAB_SPRITES_MAGENTA);
            int color = rbp$stickerColor;
            if (rbp$stickerColor == 0) color = 1;
            ResourceLocation resourcelocation = colors.get(color - 1).get(this.isStateTriggered, this.isHovered());
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


    @Unique
    @Override
    public void rbp$setColor(int color) {
        rbp$stickerColor = color;
    }
}


