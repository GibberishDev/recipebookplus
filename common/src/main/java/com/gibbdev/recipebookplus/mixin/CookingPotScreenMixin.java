package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.client.gui.CookingPotRecipeBookComponent;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

@Mixin(CookingPotScreen.class)
public abstract class CookingPotScreenMixin extends AbstractContainerScreen<CookingPotMenu> {
    public CookingPotScreenMixin(CookingPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }


    @Shadow
    private boolean widthTooNarrow;
    @Shadow @Final
    private CookingPotRecipeBookComponent recipeBookComponent;

    @Shadow @Override
    protected abstract void renderBg(@NotNull GuiGraphics gui, float partialTicks, int mouseX, int mouseY);
    @Shadow
    protected abstract void renderMealDisplayTooltip(GuiGraphics gui, int mouseX, int mouseY);
    @Shadow
    protected abstract void renderHeatIndicatorTooltip(GuiGraphics gui, int mouseX, int mouseY);


    @Unique
    private StateSwitchingButton rbp$toggleRecipeBookButton;
    @Unique @Final
    public final WidgetSprites CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_closed"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_open"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_closed_highlight"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_open_highlight"));

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void rbp$init(CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI() && this.minecraft !=null) {
            super.init();
            this.widthTooNarrow = this.width < 379;
            this.titleLabelX = 28;
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            if (Configuration.ENABLE_COOKING_POT_RECIPE_BOOK.get()) {
                rbp$toggleRecipeBookButton = new StateSwitchingButton(this.leftPos + 5, this.height / 2 - 49, 23, 21, !this.recipeBookComponent.isVisible());
                rbp$toggleRecipeBookButton.initTextureValues(CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES);
                rbp$toggleRecipeBookButton.setTooltip(Tooltip.create(this.recipeBookComponent.isVisible() ? Component.translatable("recipebookplus.gui.recipe_book_button_hide") : Component.translatable("recipebookplus.gui.recipe_book_button_show")));
            } else {
                this.recipeBookComponent.hide();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            }

            this.addWidget(this.recipeBookComponent);
            this.setInitialFocus(this.recipeBookComponent);
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void rbp$render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
                this.renderBackground(gui, mouseX, mouseY, partialTicks);
                this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
            } else {
                super.render(gui, mouseX, mouseY, partialTicks);
                this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
                this.recipeBookComponent.renderGhostRecipe(gui, this.leftPos, this.topPos, false, partialTicks);
            }
            rbp$toggleRecipeBookButton.render(gui, mouseX, mouseY, partialTicks);
            this.renderMealDisplayTooltip(gui, mouseX, mouseY);
            this.renderHeatIndicatorTooltip(gui, mouseX, mouseY);
            this.recipeBookComponent.renderTooltip(gui, this.leftPos, this.topPos, mouseX, mouseY);
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int buttonId, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, buttonId)) {
                this.setFocused(this.recipeBookComponent);
                cir.setReturnValue(true);
            } else if (rbp$toggleRecipeBookButton.mouseClicked(mouseX, mouseY, buttonId)) {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                this.rbp$toggleRecipeBookButton.setPosition(this.leftPos + 5, this.height / 2 - 49);
                rbp$toggleRecipeBookButton.setStateTriggered(!this.recipeBookComponent.isVisible());
                rbp$toggleRecipeBookButton.initTextureValues(CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES);
                rbp$toggleRecipeBookButton.setTooltip(Tooltip.create(this.recipeBookComponent.isVisible() ? Component.translatable("recipebookplus.gui.recipe_book_button_hide") : Component.translatable("recipebookplus.gui.recipe_book_button_show")));
                cir.setReturnValue(true);
            } else
            cir.setReturnValue(this.widthTooNarrow && this.recipeBookComponent.isVisible() || super.mouseClicked(mouseX, mouseY, buttonId));
        }
    }

}
