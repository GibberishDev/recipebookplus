package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    public InventoryScreenMixin(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Shadow @Final
    private RecipeBookComponent recipeBookComponent;
    @Shadow
    private boolean widthTooNarrow;
    @Shadow
    private float xMouse;
    @Shadow
    private float yMouse;

    @Shadow @Override
    protected abstract void renderBg(@NotNull GuiGraphics guiGraphics, float v, int i, int i1);

    @Unique
    private StateSwitchingButton rbp$toggleRecipeBookButton;
    @Unique @Final
    public final WidgetSprites CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_closed"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_open"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_closed_highlight"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_open_highlight"));

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    protected void rbp$init(CallbackInfo ci) {
        if (Config.getModEnabled() && Config.getUseCustomUI() && this.minecraft != null && this.minecraft.gameMode != null && this.minecraft.player != null) {
            if (this.minecraft.gameMode.hasInfiniteItems()) {
                this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
            } else {
                super.init();
                this.widthTooNarrow = this.width < 379;
                this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                rbp$toggleRecipeBookButton = new StateSwitchingButton(this.leftPos + 104, this.height / 2 - 22, 23, 21, !this.recipeBookComponent.isVisible());
                rbp$toggleRecipeBookButton.initTextureValues(CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES);
                rbp$toggleRecipeBookButton.setTooltip(Tooltip.create(this.recipeBookComponent.isVisible() ? Component.translatable("recipebookplus.gui.recipe_book_button_hide") : Component.translatable("recipebookplus.gui.recipe_book_button_show")));

                this.addWidget(this.recipeBookComponent);
            }
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void rbp$render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Config.getModEnabled() && Config.getUseCustomUI()) {
            if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
                this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
                this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
            } else {
                super.render(guiGraphics, mouseX, mouseY, partialTick);
                this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
                this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, false, partialTick);
            }
            rbp$toggleRecipeBookButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.renderTooltip(guiGraphics, mouseX, mouseY);
            this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
            this.xMouse = (float) mouseX;
            this.yMouse = (float) mouseY;
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void rbp$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Config.getModEnabled() && Config.getUseCustomUI()) {
            if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(this.recipeBookComponent);
                cir.setReturnValue(true);
            } else if (rbp$toggleRecipeBookButton.mouseClicked(mouseX, mouseY, button)) {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                this.rbp$toggleRecipeBookButton.setPosition(this.leftPos + 104, this.height / 2 - 22);
                rbp$toggleRecipeBookButton.setStateTriggered(!this.recipeBookComponent.isVisible());
                rbp$toggleRecipeBookButton.initTextureValues(CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES);
                rbp$toggleRecipeBookButton.setTooltip(Tooltip.create(this.recipeBookComponent.isVisible() ? Component.translatable("recipebookplus.gui.recipe_book_button_hide") : Component.translatable("recipebookplus.gui.recipe_book_button_show")));
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue((!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.mouseClicked(mouseX, mouseY, button));
            }
        }
    }

}
