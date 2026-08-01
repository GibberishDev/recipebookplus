package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.interfaces.IEditBox;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

@Mixin(EditBox.class)
public abstract class EditBoxMixin extends AbstractWidget implements Renderable, IEditBox {
    public EditBoxMixin(int x, int y, int width, int height, Component message) {super(x, y, width, height, message);}
    @Shadow @Override
    public abstract void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v);
    @Shadow @Override
    public abstract void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput);

    @Shadow @Final
    private static WidgetSprites SPRITES;
    @Shadow @Final
    private Font font;
    @Shadow
    private String value;
    @Shadow
    private boolean bordered;
    @Shadow
    private boolean isEditable;
    @Shadow
    private int displayPos;
    @Shadow
    private int cursorPos;
    @Shadow
    private int highlightPos;
    @Shadow
    private int textColor;
    @Shadow
    private int textColorUneditable;
    @Shadow @Nullable
    private String suggestion;
    @Shadow
    private BiFunction<String, Integer, FormattedCharSequence> formatter;
    @Shadow @Nullable
    private Component hint;
    @Shadow
    private long focusedTime;

    @Shadow
    public abstract boolean isVisible();
    @Shadow
    public abstract boolean isBordered();
    @Shadow
    public abstract int getInnerWidth();
    @Shadow
    protected abstract int getMaxLength();
    @Shadow
    protected abstract void renderHighlight(GuiGraphics guiGraphics, int minX, int minY, int maxX, int maxY);


    @Override @Unique
    public void rbp$renderWidgetButWithoutFknShadow(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                ResourceLocation resourcelocation = SPRITES.get(this.isActive(), this.isFocused());
                guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }

            int l1 = this.isEditable ? this.textColor : this.textColorUneditable;
            int i = this.cursorPos - this.displayPos;
            String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean flag = i >= 0 && i <= s.length();
            boolean flag1 = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L && flag;
            int j = this.bordered ? this.getX() + 4 : this.getX();
            int k = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
            int l = j;
            int i1 = Mth.clamp(this.highlightPos - this.displayPos, 0, s.length());
            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, i) : s;
                l = guiGraphics.drawString(this.font, this.formatter.apply(s1, this.displayPos), j, k, l1, false);
            }

            boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int j1 = l;
            if (!flag) {
                j1 = i > 0 ? j + this.width : j;
            } else if (flag2) {
                j1 = l - 1;
                --l;
            }

            if (!s.isEmpty() && flag && i < s.length()) {
                guiGraphics.drawString(this.font, this.formatter.apply(s.substring(i), this.cursorPos), l, k, l1, false);
            }

            if (this.hint != null && s.isEmpty() && !this.isFocused()) {
                guiGraphics.drawString(this.font, this.hint, l, k, l1, false);
            }

            if (!flag2 && this.suggestion != null) {
                guiGraphics.drawString(this.font, this.suggestion, j1 - 1, k, -8355712);
            }

            if (flag1) {
                if (flag2) {
                    guiGraphics.fill(RenderType.guiOverlay(), j1, k - 1, j1 + 1, k + 1 + 9, -3092272);
                } else {
                    guiGraphics.drawString(this.font, "_", j1, k, l1, false);
                }
            }

            if (i1 != i) {
                int k1 = j + this.font.width(s.substring(0, i1));
                this.renderHighlight(guiGraphics, j1, k - 1, k1 - 1, k + 1 + 9);
            }
        }

    }

}
