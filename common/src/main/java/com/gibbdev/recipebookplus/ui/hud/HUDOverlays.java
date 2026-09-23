package com.gibbdev.recipebookplus.ui.hud;

import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.RecipeBookPlus;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HUDOverlays {

    // I need to come clean... I basically copied Vectorwing's homework...
    public static abstract class BaseOverlay implements LayeredDraw.Layer {
        public abstract void render(Minecraft mc, Player player, GuiGraphics guiGraphics, int guiTicks);

        @Override
        public final void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
            Minecraft minecraft = Minecraft.getInstance();
            if (
                    minecraft.player == null
                    || !shouldRenderOverlay(minecraft, minecraft.player, guiGraphics, minecraft.gui.getGuiTicks())
            ) return;
            render(minecraft, minecraft.player, guiGraphics, minecraft.gui.getGuiTicks());
        }

        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics guiGraphics, int guiTicks) {
            return !minecraft.options.hideGui && minecraft.gameMode != null && minecraft.gameMode.canHurtPlayer();
        }
    }

    // Recipe Acquired popup
    public static class RecipeAcquiredPopup extends BaseOverlay {
        public static final RecipeAcquiredPopup INSTANCE = new RecipeAcquiredPopup();

        private static ANIMATION_STATE animationState = ANIMATION_STATE.CLOSED;
        private enum ANIMATION_STATE {
            CLOSED,
            OPEN,
            OPENING,
            CLOSING
        }

        private static final List<ResourceLocation> BOOK_ANIMATION_SPRITES = List.of(
                RecipeBookPlus.rl("overlay/book_overlay_frame_0"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_1"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_2"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_3"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_4"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_5"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_6"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_7"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_8"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_9"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_10"),
                RecipeBookPlus.rl("overlay/book_overlay_frame_11")
        );

        private static int posX = 10;
        private static int posY = 10;
        private static int lastProcessedTick = 0;

        private static final Map<Item, Set<RecipeHolder<?>>> recipeQueue = new HashMap<>();
        private static final Map<RecipeHolder<?>, List<Integer>> itemAnimationMap = new HashMap<>();
        private static int        itemAnimationFrames = 20;

        private static int          bookAnimationFrame = 0;
        private static float        bookTransformProgress = 0f;
        private static int          bookTransformProgressFrame = 0;
        private static float        bookTransformProgressMaxFrames = 10f;

        private static int          newRecipesNumber = 0;
        private static float        newRecipesNumberTransformProgress = 0f;
        private static int          newRecipesNumberTransformProgressFrame = 0;
        private static float        newRecipesNumberTransformProgressMaxFrames = 10f;

        private static ItemStack    displayedWorkstation = ItemStack.EMPTY;
        private static float        displayedWorkstationTransformProgress = 0f;
        private static int          displayedWorkstationTransformProgressFrame = 0;
        private static float        displayedWorkstationTransformProgressMaxFrames = 10f;

        @Override
        public void render(Minecraft mc, Player player, GuiGraphics guiGraphics, int guiTicks) {
            if (mc.level == null || !shouldRenderOverlay(mc, player, guiGraphics, guiTicks)) return;
            updateWidgetTransform(guiGraphics);
            animationState = getNewAnimationState();

            displayBook(guiGraphics,guiTicks);

            if (animationState == ANIMATION_STATE.OPEN) {
                displayRecipesNumber(guiGraphics, guiTicks, mc);
                displayRecipeResults(guiGraphics, guiTicks, mc);
                displayWorkstation(guiGraphics, guiTicks);
            }
            lastProcessedTick = guiTicks;
        }

        @Override
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics guiGraphics, int guiTicks) {
            return (!isQueueEmpty() || animationState != ANIMATION_STATE.CLOSED) && !minecraft.options.hideGui && minecraft.gameMode != null && Config.getRecipeOverlayAnchor()!= Config.HUDOverlayAnchor.NONE;
        }

        public static void queueRecipeNotification(Set<String> ids) {
            int newRecipes = 0;
            for (String id : ids) {
                Optional<RecipeHolder<?>> maybeHolder = Minecraft.getInstance().level.getRecipeManager().byKey(ResourceLocation.parse(id));
                if (maybeHolder.isPresent()) {
                    newRecipes++;
                    Item craftingStation = maybeHolder.get().value().getToastSymbol().getItem();
                    if (recipeQueue.containsKey(craftingStation)) {
                        Set<RecipeHolder<?>> newSet = new HashSet<>();
                        newSet.addAll(recipeQueue.get(craftingStation));
                        newSet.add(maybeHolder.get());
                        recipeQueue.put(craftingStation, newSet);
                    } else {
                        recipeQueue.put(craftingStation, Set.of(maybeHolder.get()));
                    }
                }
            }
            newRecipesNumber += newRecipes;
            newRecipesNumberTransformProgressFrame = 0;
        }

        private static void updateWidgetTransform(GuiGraphics guiGraphics) {
            boolean flipX = false;
            boolean flipY = false;
            posX = 0;
            posY = 0;
            switch (Config.getRecipeOverlayAnchor()) {
                case Config.HUDOverlayAnchor.TOP_MIDDLE -> {
                    posX = Math.round(guiGraphics.guiWidth() / 2.0f);
                }
                case TOP_RIGHT -> {
                    posX = guiGraphics.guiWidth();
                    flipX = true;
                }
                case CENTER_LEFT -> posY = Math.round(guiGraphics.guiHeight() / 2.0f);
                case CENTER_MIDDLE -> {
                    posX = Math.round(guiGraphics.guiWidth() / 2.0f);
                    posY = Math.round(guiGraphics.guiHeight() / 2.0f);
                }
                case CENTER_RIGHT -> {
                    posX = guiGraphics.guiWidth();
                    posY = Math.round(guiGraphics.guiHeight() / 2.0f);
                    flipX = true;
                }
                case BOTTOM_LEFT -> {
                    posY = guiGraphics.guiHeight();
                    flipY = true;
                }
                case BOTTOM_MIDDLE -> {
                    posX = Math.round(guiGraphics.guiWidth() / 2.0f);
                    posY = guiGraphics.guiHeight();
                    flipY = true;
                }
                case BOTTOM_RIGHT -> {
                    posX = guiGraphics.guiWidth();
                    posY = guiGraphics.guiHeight();
                    flipX = true;
                    flipY = true;
                }
                case null, default -> {
                    posX = 0;
                    posY = 0;
                }
            }
            if (flipX) posX -= Config.getRecipeOverlayXOffset(); else posX += Config.getRecipeOverlayXOffset();
            if (flipY) posY -= Config.getRecipeOverlayYOffset(); else posY += Config.getRecipeOverlayYOffset();
        }

        private static boolean isQueueEmpty() {
            return recipeQueue.isEmpty() && itemAnimationMap.isEmpty();
        }

        private static ANIMATION_STATE getNewAnimationState() {
            switch (animationState) {
                case OPEN -> {
                    if (    isQueueEmpty()                                  // No queued recipes
                            && displayedWorkstationTransformProgress <= 0   // Finished playing workstation animation
                            && newRecipesNumberTransformProgress <= 0       // Finished playing recipes added number animation
                    ) {
                        return ANIMATION_STATE.CLOSING;                     // Should close the book
                    }
                    return ANIMATION_STATE.OPEN;                            // Keep book open
                }
                case CLOSED -> {
                    if (    !isQueueEmpty()                                 // Some queued recipes
                    ) {
                        return ANIMATION_STATE.OPENING;                     // Should open the book
                    }
                    return ANIMATION_STATE.CLOSED;                          // Keep closed
                }
                case CLOSING -> {
                    if (    !isQueueEmpty()                                 // Some queued recipes
                    ) {
                        return ANIMATION_STATE.OPENING;                     // Reopen the book
                    } else if (
                            bookTransformProgress == 0.0f                   // Book is fully faded/shrunk
                            && bookAnimationFrame == 0                      // Book texture is on fully closed frame
                    ) {
                        newRecipesNumber = 0;
                        displayedWorkstation = ItemStack.EMPTY;
                        return ANIMATION_STATE.CLOSED;                      // Declare fully closed
                    }
                    return ANIMATION_STATE.CLOSING;                         // Keep closing
                }
                case OPENING -> {
                    if (    bookTransformProgress == 1.0f                   // Book is fully visible/expanded
                            && bookAnimationFrame == 6                      // Book texture on fully opened frame
                    ) {
                        return ANIMATION_STATE.OPEN;                        // Declare fully open
                    }
                    return ANIMATION_STATE.OPENING;                         // Keep opening
                }
            }
            //resetAnimation();                                             // EAT SHIT AND DIE (unexpected -> reset)
            return ANIMATION_STATE.CLOSED;
        }

        private static void displayBook(GuiGraphics guiGraphics, int guiTicks) {
            switch (animationState) {
                case OPEN -> {
                    bookAnimationFrame = 6;
                    bookTransformProgress = 1.0f;
                }
                case OPENING -> {
                    if (guiTicks != lastProcessedTick) {
                        if (bookAnimationFrame<6 && guiTicks % 2 == 0 && bookTransformProgress == 1.0f) bookAnimationFrame++;
                        else if (bookTransformProgress < 1.0f) {
                            bookTransformProgress = Math.min(bookTransformProgressFrame / bookTransformProgressMaxFrames, 1.0f);
                            if (bookTransformProgressFrame < bookTransformProgressMaxFrames) bookTransformProgressFrame++;
                        }
                    }
                }
                case CLOSING -> {
                    if (guiTicks != lastProcessedTick) {
                        if (bookAnimationFrame < 11 && bookAnimationFrame > 0 && guiTicks % 2 == 0 && bookTransformProgress == 1.0f) bookAnimationFrame++;
                        else if (bookAnimationFrame == 11 && bookTransformProgress == 1.0f && guiTicks % 2 == 0) bookAnimationFrame = 0;
                        else if (bookTransformProgress > 0.0f && bookAnimationFrame == 0) {
                            bookTransformProgress = Math.max(bookTransformProgressFrame / bookTransformProgressMaxFrames, 0.0f);
                            if (bookTransformProgressFrame > 0) bookTransformProgressFrame--;
                        }
                    }
                }
                case CLOSED -> {
                    return;
                }
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(posX+10,posY+14,0);
            guiGraphics.pose().scale(bookTransformProgress, bookTransformProgress,1f);
            guiGraphics.pose().rotateAround(Axis.ZN.rotationDegrees(180f - (bookTransformProgress * 180f)), 0,0,0);
            guiGraphics.blitSprite(BOOK_ANIMATION_SPRITES.get(bookAnimationFrame),-10,-14,32,32);
            guiGraphics.pose().popPose();
        }

        private static void displayRecipesNumber(GuiGraphics guiGraphics, int guiTicks, Minecraft mc) {
            if (newRecipesNumber != 0) {
                if (guiTicks != lastProcessedTick) {
                    newRecipesNumberTransformProgress = newRecipesNumberTransformProgressFrame / newRecipesNumberTransformProgressMaxFrames;
                    if (newRecipesNumberTransformProgressFrame < newRecipesNumberTransformProgressMaxFrames && !isQueueEmpty()) {
                        newRecipesNumberTransformProgressFrame++;
                    } else if (isQueueEmpty() && newRecipesNumberTransformProgressFrame > 0) {
                        newRecipesNumberTransformProgressFrame--;
                    }
                }
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(posX+16,posY+16,0);
                guiGraphics.pose().scale(0.5f* newRecipesNumberTransformProgress,0.5f* newRecipesNumberTransformProgress,1f);
                guiGraphics.drawString(mc.font, Component.literal("+"+newRecipesNumber),0,0, 16777215, false);
                guiGraphics.pose().popPose();
            }
        }

        private static void displayRecipeResults(GuiGraphics guiGraphics, int guiTicks, Minecraft mc) {
            if (isQueueEmpty()) return;
            if (guiTicks != lastProcessedTick) {
                if (!recipeQueue.isEmpty()) {
                    int queueSize = 0;
                    for (Item key : recipeQueue.keySet()) {
                        queueSize += recipeQueue.get(key).size();
                    }
                    if (
                          (queueSize <  4 && guiTicks % 8 == 0)
                       || (queueSize >= 4 && queueSize < 8 && guiTicks % 6 == 0)
                       || (queueSize >= 8 && queueSize < 16 && guiTicks % 4 == 0)
                       || (queueSize >= 16 && queueSize < 32 && guiTicks % 2 == 0)
                       || (queueSize >= 32)

                    ) {
                        RecipeHolder<?> nextHolder = null;
                        if (recipeQueue.containsKey(displayedWorkstation.getItem())) {
                            nextHolder = recipeQueue.get(displayedWorkstation.getItem()).stream().findFirst().get();
                        } else {
                            Item nextWorkstation = recipeQueue.keySet().stream().findFirst().get();
                            displayedWorkstation = nextWorkstation.getDefaultInstance();
                            nextHolder = recipeQueue.get(nextWorkstation).stream().findFirst().get();
                        }
                        Set<RecipeHolder<?>> newSet = new HashSet<>(recipeQueue.get(displayedWorkstation.getItem()));
                        newSet.remove(nextHolder);
                        if (newSet.isEmpty()) recipeQueue.remove(displayedWorkstation.getItem());
                        else recipeQueue.put(displayedWorkstation.getItem(), newSet);
                        itemAnimationMap.put(nextHolder, List.of(itemAnimationFrames, (int) Math.round((Math.random()*2f-1f) * 45.0f)));
                    }
                }
                if (!itemAnimationMap.isEmpty()) {
                    if (guiTicks != lastProcessedTick) {
                        Map<RecipeHolder<?>, List<Integer>> tempMap = Map.copyOf(itemAnimationMap);
                        for (RecipeHolder<?> holder : tempMap.keySet()) {
                            int framesLeft = itemAnimationMap.get(holder).getFirst();
                            framesLeft--;
                            if (framesLeft <= 0) itemAnimationMap.remove(holder);
                            else itemAnimationMap.put(holder, List.of(framesLeft, tempMap.get(holder).get(1)));
                        }
                        if (itemAnimationMap.isEmpty()) return;
                    }
                }
            }

            if (!itemAnimationMap.isEmpty()) {
            for (RecipeHolder<?> holder : itemAnimationMap.keySet()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(posX + 11, posY+9, 0);
                float progress = itemAnimationMap.get(holder).getFirst() / (float) itemAnimationFrames;
                guiGraphics.pose().scale(progress,progress,progress);
                ItemStack item = holder.value().getResultItem(mc.level.registryAccess());
                guiGraphics.pose().rotateAround(Axis.ZN.rotationDegrees((float)itemAnimationMap.get(holder).get(1)), 0,0,0);
                guiGraphics.renderFakeItem(item, -8, -8-16);
                guiGraphics.pose().popPose();
            }
            }
        }

        private static void displayWorkstation(GuiGraphics guiGraphics, int guiTicks) {
            if (displayedWorkstation != ItemStack.EMPTY) {
                if (guiTicks != lastProcessedTick) {
                    displayedWorkstationTransformProgress = displayedWorkstationTransformProgressFrame / displayedWorkstationTransformProgressMaxFrames;
                    if (displayedWorkstationTransformProgressFrame < displayedWorkstationTransformProgressMaxFrames && !isQueueEmpty()) {
                        displayedWorkstationTransformProgressFrame++;
                    } else if (isQueueEmpty() && displayedWorkstationTransformProgressFrame > 0) {
                        displayedWorkstationTransformProgressFrame--;
                    }
                }
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(posX, posY + 16, 0);
                guiGraphics.pose().scale(0.5f * displayedWorkstationTransformProgress, 0.5f * displayedWorkstationTransformProgress, 0.5f * displayedWorkstationTransformProgress);
//                guiGraphics.pose().rotateAround(Axis.ZN.rotationDegrees((float) Math.sin((float) guiTicks) * 3.0f), 0, 0, 0);
                guiGraphics.renderFakeItem(displayedWorkstation, -8, -8);
                guiGraphics.pose().popPose();
            }
        }

    }

}
