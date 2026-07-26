package com.gibbdev.recipebookplus.mixin;


import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeButton.class)
public abstract class RecipeButtonMixin extends AbstractWidget {

    @Unique
    private static final Identifier CUSTOM_SLOT_MANY_CRAFTABLE_SPRITE = Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/slot_many_craftable");
    @Unique
    private static final Identifier CUSTOM_SLOT_CRAFTABLE_SPRITE = Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/slot_craftable");
    @Unique
    private static final Identifier CUSTOM_SLOT_MANY_UNCRAFTABLE_SPRITE = Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/slot_many_uncraftable");
    @Unique
    private static final Identifier CUSTOM_SLOT_UNCRAFTABLE_SPRITE = Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/slot_uncraftable");

    @Shadow
    private RecipeCollection collection;
    @Shadow
    private boolean allRecipesHaveSameResultDisplay;
    @Shadow
    private float animationTime;

    public RecipeButtonMixin(int x, int y, int width, int height, Component message) {super(x, y, width, height, message);}

    @Shadow
    protected abstract boolean hasMultipleRecipes();
    @Shadow
    public abstract  ItemStack getDisplayStack();


    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"), cancellable = true)
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            Identifier sprite;
            if (this.collection.hasCraftable()) {
                if (this.hasMultipleRecipes()) {
                    sprite = CUSTOM_SLOT_MANY_CRAFTABLE_SPRITE;
                } else {
                    sprite = CUSTOM_SLOT_CRAFTABLE_SPRITE;
                }
            } else if (this.hasMultipleRecipes()) {
                sprite = CUSTOM_SLOT_MANY_UNCRAFTABLE_SPRITE;
            } else {
                sprite = CUSTOM_SLOT_UNCRAFTABLE_SPRITE;
            }

            boolean shouldAnimate = this.animationTime > 0.0F;
            if (shouldAnimate) {
                float squeeze = 1.0F + 0.1F * (float) Math.sin((this.animationTime / 15.0F * (float) Math.PI));
                graphics.pose().pushMatrix();
                graphics.pose().translate((float) (this.getX() + 8), (float) (this.getY() + 12));
                graphics.pose().scale(squeeze, squeeze);
                graphics.pose().translate((float) (-(this.getX() + 8)), (float) (-(this.getY() + 12)));
                this.animationTime -= a;
            }

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.width, this.height);
            ItemStack currentItemStack = this.getDisplayStack();
            int offset = 4;
            if (this.hasMultipleRecipes() && this.allRecipesHaveSameResultDisplay) {
                graphics.item(currentItemStack, this.getX() + offset + 1, this.getY() + offset + 1, 0);
                --offset;
            }

            graphics.fakeItem(currentItemStack, this.getX() + offset, this.getY() + offset);
            if (shouldAnimate) {
                graphics.pose().popMatrix();
            }
            ci.cancel();
        }
    }
}
