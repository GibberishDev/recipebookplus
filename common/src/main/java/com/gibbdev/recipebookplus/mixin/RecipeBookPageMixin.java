package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.google.common.collect.Lists;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeBookPage.class)
public abstract class RecipeBookPageMixin {

    @Unique
    private static final WidgetSprites CUSTOM_PAGE_FORWARD_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/page_forward"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/page_forward_highlight")
    );
    @Unique
    private static final WidgetSprites CUSTOM_PAGE_BACKWARD_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/page_backward"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/page_backward_highlight")
    );

    @Shadow
    private Minecraft minecraft;
    @Shadow
    private ClientRecipeBook recipeBook;
    @Shadow
    private final List<RecipeButton> buttons = Lists.newArrayListWithCapacity(20);
    @Shadow
    private @Nullable ImageButton forwardButton;
    @Shadow
    private @Nullable ImageButton backButton;
    @Shadow @Final
    private static Component NEXT_PAGE_TEXT;
    @Shadow @Final
    private static Component PREVIOUS_PAGE_TEXT;
    @Shadow
    private int totalPages;
    @Shadow
    private int currentPage;
    @Shadow
    private @Nullable RecipeButton hoveredButton;
    @Shadow @Final
    private OverlayRecipeComponent overlay;

    @Shadow
    protected abstract void updateArrowButtons();

    @Inject(method = "init", at=@At("HEAD"), cancellable = true)
    public void rbp$init(Minecraft minecraft, int xo, int yo, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            this.minecraft = minecraft;
            this.recipeBook = minecraft.player.getRecipeBook();

            for(int i = 0; i < this.buttons.size(); ++i) {
                ((RecipeButton)this.buttons.get(i)).setPosition(xo + 11 + 25 * (i % 5), yo + 31 + 25 * (i / 5));
            }

            this.forwardButton = new ImageButton(xo + 95, yo + 144, 12, 8, CUSTOM_PAGE_FORWARD_SPRITES, (button) -> this.updateArrowButtons(), NEXT_PAGE_TEXT);
            this.forwardButton.setTooltip(Tooltip.create(NEXT_PAGE_TEXT));
            this.backButton = new ImageButton(xo + 38, yo + 144, 12, 8, CUSTOM_PAGE_BACKWARD_SPRITES, (button) -> this.updateArrowButtons(), PREVIOUS_PAGE_TEXT);
            this.backButton.setTooltip(Tooltip.create(PREVIOUS_PAGE_TEXT));
            ci.cancel();
        }
    }

    @Inject(method="extractRenderState", at = @At("HEAD"), cancellable = true)
    public void rbp$extractRenderState(GuiGraphicsExtractor graphics, int xo, int yo, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            if (this.totalPages > 1) {
                Component pageNumbers = Component.translatable("gui.recipebook.page", new Object[]{this.currentPage + 1, this.totalPages});
                int pWidth = this.minecraft.font.width(pageNumbers);
                graphics.text(this.minecraft.font, pageNumbers, xo - (int) Math.round(pWidth / 2.0) + 73, yo + 145, -3439300,false);
            }

            this.hoveredButton = null;

            for (RecipeButton recipeBookButton : this.buttons) {
                recipeBookButton.extractRenderState(graphics, mouseX, mouseY, a);
                if (recipeBookButton.visible && recipeBookButton.isHoveredOrFocused()) {
                    this.hoveredButton = recipeBookButton;
                }
            }

            if (this.forwardButton != null) {
                this.forwardButton.extractRenderState(graphics, mouseX, mouseY, a);
            }

            if (this.backButton != null) {
                this.backButton.extractRenderState(graphics, mouseX, mouseY, a);
            }

            graphics.nextStratum();
            this.overlay.extractRenderState(graphics, mouseX, mouseY, a);
            ci.cancel();
            return;
        }
    }


}
