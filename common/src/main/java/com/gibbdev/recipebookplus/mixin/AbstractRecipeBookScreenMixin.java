package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.interfaces.IAbstractContainerScreenMixin;
import com.gibbdev.recipebookplus.interfaces.IAbstractRecipeBookScreenMixin;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin<T extends RecipeBookMenu> extends AbstractContainerScreen<T> implements IAbstractRecipeBookScreenMixin {

    @Unique @Final
    public final WidgetSprites CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_closed"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_open"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_closed_highlight"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "custom_recipe_book/toggle_button_open_highlight"));
    @Unique
    private CycleButton<Boolean> customMenuToggleButton;

    @Shadow
    protected abstract ScreenPosition getRecipeBookButtonPosition();
    @Shadow
    protected abstract void onRecipeBookButtonClick();
    @Shadow @Final
    private RecipeBookComponent<?> recipeBookComponent;

    public AbstractRecipeBookScreenMixin(T menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
        super(menu, inventory, title, imageWidth, imageHeight);
    }

    /**
     * {@code @Mixin} {@link AbstractRecipeBookScreen#initButton()}
     */
    @Inject(method = "initButton", at = @At("HEAD"), cancellable = true)
    private void rbp$initButton(CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled()) {
            ci.cancel();
            ScreenPosition buttonPos = this.getRecipeBookButtonPosition();
            customMenuToggleButton = CycleButton.booleanBuilder(Component.empty(), Component.empty(), !recipeBookComponent.isVisible())
                    .withSprite((button, _) -> CUSTOM_RECIPE_BOOK_TOGGLE_BUTTON_SPRITES.get(!recipeBookComponent.isVisible(), button.isHovered()))
                    .withTooltip((_) -> !recipeBookComponent.isVisible() ? Tooltip.create(Component.translatable("recipebookplus.gui.recipe_book_button_show")) : Tooltip.create(Component.translatable("recipebookplus.gui.recipe_book_button_hide")))
                    .create(buttonPos.x(), buttonPos.y(), 23, 21, CommonComponents.EMPTY, (button, _) -> rbp$toggleRecipeBookViaButton(button));
            customMenuToggleButton.setMessage(CommonComponents.EMPTY);
            this.addRenderableWidget(customMenuToggleButton);
            this.addWidget(this.recipeBookComponent);
        }
    }


    @Unique @Override
    public RecipeBookComponent<?> rbp$getRecipeBookComponent() {
        return recipeBookComponent;
    }

    @Unique @Override
    public ItemStack rbp$getSlotUnderCursor() {
        Slot slot = ((IAbstractContainerScreenMixin) (Object) this).rbp$getHoveredSlot();
        if (slot == null) return null;
        if (slot.hasItem()) return slot.getItem();
        return null;
    }

    @Unique @Override
    public void rbp$openRecipeBook() {
        this.recipeBookComponent.toggleVisibility();
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        ScreenPosition updatedButtonPos = this.getRecipeBookButtonPosition();
        customMenuToggleButton.setPosition(updatedButtonPos.x(), updatedButtonPos.y());
        customMenuToggleButton.setValue(!recipeBookComponent.isVisible());
        this.onRecipeBookButtonClick();
        customMenuToggleButton.setMessage(CommonComponents.EMPTY);
    }

    @Unique
    private void rbp$toggleRecipeBookViaButton(CycleButton<Boolean> button) {
        this.recipeBookComponent.toggleVisibility();
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        ScreenPosition updatedButtonPos = this.getRecipeBookButtonPosition();
        button.setPosition(updatedButtonPos.x(), updatedButtonPos.y());
        button.setValue(!recipeBookComponent.isVisible());
        this.onRecipeBookButtonClick();
        button.setMessage(CommonComponents.EMPTY);
    }

}
