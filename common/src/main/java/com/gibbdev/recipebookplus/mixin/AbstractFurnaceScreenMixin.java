package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin<T extends AbstractFurnaceMenu> extends AbstractContainerScreen<T>  {
    @Shadow @Final
    public AbstractFurnaceRecipeBookComponent recipeBookComponent;
    @Shadow
    private boolean widthTooNarrow;

    @Unique
    private StateSwitchingButton rbp$toggleRecipeBookButton;
    @Unique @Final
    public final WidgetSprites CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_closed"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_open"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_closed_highlight"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_open_highlight"));

    public AbstractFurnaceScreenMixin(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "init", at=@At("HEAD"),cancellable = true)
    public void rbp$init(CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI() && this.minecraft != null) {
            super.init();
            this.widthTooNarrow = this.width < 379;
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            rbp$toggleRecipeBookButton = new StateSwitchingButton(this.leftPos + 20, this.height / 2 - 49, 23, 21, !this.recipeBookComponent.isVisible());
            rbp$toggleRecipeBookButton.initTextureValues(CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES);
            rbp$toggleRecipeBookButton.setTooltip(Tooltip.create(this.recipeBookComponent.isVisible() ? Component.translatable("recipebookplus.gui.recipe_book_button_hide") : Component.translatable("recipebookplus.gui.recipe_book_button_show")));
            this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void rbp$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
                this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
                this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
            } else {
                super.render(guiGraphics, mouseX, mouseY, partialTick);
                this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
                this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, true, partialTick);
            }
            this.rbp$toggleRecipeBookButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.renderTooltip(guiGraphics, mouseX, mouseY);
            this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void rbp$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
            } else if (this.rbp$toggleRecipeBookButton.mouseClicked(mouseX, mouseY, button)) {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                this.rbp$toggleRecipeBookButton.setPosition(this.leftPos + 20, this.height / 2 - 49);
                rbp$toggleRecipeBookButton.setStateTriggered(!this.recipeBookComponent.isVisible());
                rbp$toggleRecipeBookButton.initTextureValues(CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES);
                rbp$toggleRecipeBookButton.setTooltip(Tooltip.create(this.recipeBookComponent.isVisible() ? Component.translatable("recipebookplus.gui.recipe_book_button_hide") : Component.translatable("recipebookplus.gui.recipe_book_button_show")));
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(this.widthTooNarrow && this.recipeBookComponent.isVisible() || super.mouseClicked(mouseX, mouseY, button));
            }
        }
    }
}
