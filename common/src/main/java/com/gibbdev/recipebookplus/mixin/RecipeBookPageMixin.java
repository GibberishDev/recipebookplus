package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeBookPage.class)
public class RecipeBookPageMixin {

    @Shadow
    private Minecraft minecraft;
    @Shadow
    private RecipeBook recipeBook;
    @Shadow
    private final List<RecipeButton> buttons = Lists.newArrayListWithCapacity(20);
    @Shadow
    private StateSwitchingButton forwardButton;
    @Shadow
    private StateSwitchingButton backButton;
    @Shadow
    private int totalPages;
    @Shadow
    private int currentPage;
    @Shadow
    private RecipeButton hoveredButton;
    @Shadow @Final
    private OverlayRecipeComponent overlay;
    @Unique
    private static final WidgetSprites CUSTOM_PAGE_FORWARD_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/page_forward"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/page_forward_highlight")
    );
    @Unique
    private static final WidgetSprites CUSTOM_PAGE_BACKWARD_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/page_backward"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/page_backward_highlight")
    );
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void rbp$init(Minecraft minecraft, int x, int y, CallbackInfo ci) {
        if (Config.getModEnabled() && Config.getUseCustomUI() && minecraft.player != null) {
            this.minecraft = minecraft;
            this.recipeBook = minecraft.player.getRecipeBook();
            for (int i = 0; i < this.buttons.size(); ++i) {
                (this.buttons.get(i)).setPosition(x + 11 + 25 * (i % 5), y + 31 + 25 * (i / 5));
            }

            this.forwardButton = new StateSwitchingButton(x + 95, y + 144, 12, 8, false);
            this.forwardButton.initTextureValues(CUSTOM_PAGE_FORWARD_SPRITES);
            this.backButton = new StateSwitchingButton(x + 38, y + 144, 12, 8, true);
            this.backButton.initTextureValues(CUSTOM_PAGE_BACKWARD_SPRITES);
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void rbp$render(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Config.getModEnabled() && Config.getUseCustomUI()) {
            if (this.totalPages > 1) {
                Component component = Component.translatable("gui.recipebook.page", this.currentPage + 1, this.totalPages);
                int i = this.minecraft.font.width(component);
                guiGraphics.drawString(this.minecraft.font, component, x - (int) Math.round(i / 2.0) + 73, y + 145, -3439300, false);
            }

            this.hoveredButton = null;

            for (RecipeButton recipebutton : this.buttons) {
                recipebutton.render(guiGraphics, mouseX, mouseY, partialTick);
                if (recipebutton.visible && recipebutton.isHoveredOrFocused()) {
                    this.hoveredButton = recipebutton;
                }
            }
            if (this.backButton!=null) this.backButton.render(guiGraphics, mouseX, mouseY, partialTick);
            if (this.forwardButton!=null) this.forwardButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.overlay.render(guiGraphics, mouseX, mouseY, partialTick);
            ci.cancel();
        }
    }
}
