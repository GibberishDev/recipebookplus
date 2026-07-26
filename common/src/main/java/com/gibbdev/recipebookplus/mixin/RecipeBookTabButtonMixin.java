package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeBookTabButton.class)
public abstract class RecipeBookTabButtonMixin extends ImageButton implements IRecipeBookButton {
    public RecipeBookTabButtonMixin(int x, int y, int width, int height, WidgetSprites sprites, OnPress onPress) {
        super(x, y, width, height, sprites, onPress);
    }

    @Unique
    private int rbp$stickerColor = 0;

    @Unique
    private final WidgetSprites CUSTOM_TAB_SPRITES_CYAN = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_cyan"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_cyan"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_cyan"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_hover_cyan")
    );
    @Unique
    private final WidgetSprites CUSTOM_TAB_SPRITES_YELLOW = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_yellow"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_yellow"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_yellow"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_hover_yellow")
    );
    @Unique
    private final WidgetSprites CUSTOM_TAB_SPRITES_MAGENTA = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_magenta"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_magenta"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_selected_magenta"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/tab_hover_magenta")
    );

    @Shadow
    private float animationTime;
    @Shadow
    private boolean selected;

    @Shadow
    protected abstract void extractIcon(GuiGraphicsExtractor graphics);

    @Inject(method = "extractContents", at=@At("HEAD"), cancellable = true)
    public void rbp$extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            if (this.animationTime > 0.0F) {
                float squeeze = 1.0F + 0.1F * (float)Math.sin(this.animationTime / 15.0F * (float)Math.PI);
                graphics.pose().pushMatrix();
                graphics.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12));
                graphics.pose().scale(1.0F, squeeze);
                graphics.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)));
            }
            List<WidgetSprites> colors = new ArrayList<>();
            colors.add(CUSTOM_TAB_SPRITES_CYAN);
            colors.add(CUSTOM_TAB_SPRITES_YELLOW);
            colors.add(CUSTOM_TAB_SPRITES_MAGENTA);
            int color = rbp$stickerColor;
            if (rbp$stickerColor == 0) color = 1;
            Identifier sprite = colors.get(color - 1).get(this.selected, this.isHovered());
            int xPos = this.getX();
            int yPos = this.getY() + 3;
            this.height = 20;

            if (!this.selected && this.isHovered()) {
                this.width = 34;
            } else if (this.selected) {
                this.width = 38;
            } else {
                xPos += 3;
                this.width = 31;
            }

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, xPos, yPos, this.width, this.height, (float) (this.selected ? 0.9 : 0.75));
            this.extractIcon(graphics);
            if (this.animationTime > 0.0F) {
                graphics.pose().popMatrix();
                this.animationTime -= a;
            }
            ci.cancel();
        }

    }

    @Unique @Override
    public void rbp$setColor(int color) {
        rbp$stickerColor = color;
    }


}
