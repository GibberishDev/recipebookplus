package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.CommonClass;
import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookComponent;
import com.gibbdev.recipebookplus.platform.Services;
import com.google.common.collect.Lists;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.recipebook.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin implements IRecipeBookComponent, PlaceRecipe<Ingredient>, Renderable, GuiEventListener, NarratableEntry, RecipeShownListener {

    // region shadows
    @Shadow
    private ClientRecipeBook book;
    @Shadow
    private RecipeBookTabButton selectedTab;
    @Shadow
    private final RecipeBookPage recipeBookPage = new RecipeBookPage();
    @Shadow
    private final StackedContents stackedContents = new StackedContents();
    @Shadow
    protected RecipeBookMenu<?, ?> menu;
    @Shadow
    protected Minecraft minecraft;
    @Shadow
    private EditBox searchBox;
    @Shadow @Final
    protected static ResourceLocation RECIPE_BOOK_LOCATION;
    @Shadow
    private int xOffset;
    @Shadow
    private int width;
    @Shadow
    private int height;
    @Shadow @Final
    private List<RecipeBookTabButton> tabButtons;
    @Shadow
    protected StateSwitchingButton filterButton;

    @Shadow
    public abstract boolean isVisible();
    @Shadow
    protected abstract void updateCollections(boolean resetPageNumber);
    // endregion

    // region unique variables
    @Unique
    private static final ResourceLocation BACKGROUND_IMAGE_LOCATION =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"textures/gui/sprites/custom_recipe_book/background.png");

    // endregion

    // region search
    @Inject(method = "updateCollections", at=@At("HEAD"), cancellable = true)
    private void rbp$updateCollections(boolean resetPage, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled()) {
            List<RecipeCollection> list = this.book.getCollection(this.selectedTab.getCategory());
            list.forEach(c -> c.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book));
            List<RecipeCollection> list1 = Lists.newArrayList(list);
            list1.removeIf(c -> !c.hasKnownRecipes());
            list1.removeIf(c -> !c.hasFitting());
            String s = this.searchBox.getValue();

            list1 = rbp$searchCollectionList(list1, s);

//            if (list1.isEmpty()) return;

            this.recipeBookPage.updateCollections(list1, resetPage);
            ci.cancel();
        }
    }


    @Unique
    private List<RecipeCollection> rbp$searchCollectionList(List<RecipeCollection> list, String searchTerm) {
        if (searchTerm.isEmpty() || minecraft.level == null) return list;
        searchTerm = searchTerm.toLowerCase(Locale.ROOT);
        List<RecipeCollection> tempList = Lists.newArrayList(list);
        RegistryAccess ra = minecraft.level.registryAccess();

        if (!CommonClass.groupingState) {
            for (RecipeCollection collection : tempList ) {
                if (collection.getRecipes().size() > 1) {

                    List<RecipeHolder<?>> holders = collection.getDisplayRecipes(true);
                    holders.addAll(collection.getDisplayRecipes(false));

                    for (RecipeHolder<?> holder : holders) {
                        RecipeCollection newCollection = new RecipeCollection(ra, List.of(holder));
                        newCollection.canCraft(stackedContents, menu.getGridWidth(), menu.getGridHeight(), book);
                        list.add(list.indexOf(collection),newCollection);
                    }
                    list.remove(collection);
                }
            }
        }

        if (searchTerm.startsWith(Config.INSTANCE.getIngredientPrefix()) && !searchTerm.equals(Config.INSTANCE.getIngredientPrefix())) {
            searchTerm = searchTerm.replaceFirst(Matcher.quoteReplacement(Config.INSTANCE.getIngredientPrefix()), "").strip();
            List<ItemStack> searchItems = rbp$getSearchItems(searchTerm);
            if (searchItems.isEmpty()) return new ArrayList<>();
            tempList = Lists.newArrayList(list);
            for (RecipeCollection collection : tempList ) {

                List<RecipeHolder<?>> holders = collection.getDisplayRecipes(true);
                holders.addAll(collection.getDisplayRecipes(false));
                List<RecipeHolder<?>> relevantHolders = holders.stream().filter(
                    holder -> {
                        boolean ingredientFound = !holder.value().getIngredients().stream().filter(
                                ingredient -> !(searchItems.stream().filter(ingredient).toList().isEmpty())
                        ).toList().isEmpty();
                        if (Services.PLATFORM.isModLoaded("farmersdelight") && !ingredientFound) {
                            if (holder.value().getType() == ModRecipeTypes.COOKING.get()) {
                                ItemStack containerItem = ((CookingPotRecipe) holder.value()).getOutputContainer();
                                ingredientFound = !searchItems.stream().filter(item->containerItem.getItem().equals(item.getItem())).toList().isEmpty();
                            }
                        }
                        return ingredientFound;
                    }
                ).toList();
                if (!relevantHolders.isEmpty()) {
                    RecipeCollection newCollection = new RecipeCollection(ra, relevantHolders);
                    newCollection.canCraft(stackedContents, menu.getGridWidth(), menu.getGridHeight(), book);
                    list.add(list.indexOf(collection),newCollection);
                }
                list.remove(collection);
            }
        } else if (searchTerm.startsWith(Config.INSTANCE.getModidPrefix()) && !searchTerm.equals(Config.INSTANCE.getModidPrefix())) {
            String namespace = searchTerm.replaceFirst(Matcher.quoteReplacement(Config.INSTANCE.getModidPrefix()), "").strip().toLowerCase(Locale.ROOT);
            if (namespace.isEmpty()) return new ArrayList<>();
            tempList = Lists.newArrayList(list);
            for (RecipeCollection collection : tempList ) {
                List<RecipeHolder<?>> holders = collection.getDisplayRecipes(true);
                holders.addAll(collection.getDisplayRecipes(false));
                List<RecipeHolder<?>> relevantHolders = holders.stream().filter(
                        holder -> BuiltInRegistries.ITEM.getKey(holder.value().getResultItem(ra).getItem()).getNamespace().startsWith(namespace)
                ).toList();
                if (!relevantHolders.isEmpty()) {
                    RecipeCollection newCollection = new RecipeCollection(ra, relevantHolders);
                    newCollection.canCraft(stackedContents, menu.getGridWidth(), menu.getGridHeight(), book);
                    list.add(list.indexOf(collection),newCollection);
                }
                list.remove(collection);
            }
        } else {
            String namespace = rbp$getNamespace(searchTerm);
            String s = searchTerm;
            tempList = Lists.newArrayList(list);
            for (RecipeCollection collection : tempList ) {
                List<RecipeHolder<?>> holders = collection.getDisplayRecipes(true);
                holders.addAll(collection.getDisplayRecipes(false));
                List<RecipeHolder<?>> relevantHolders = holders.stream().filter(
                    holder -> {
                        if (namespace.isEmpty()) {
                            return holder.value().getResultItem(ra).getDisplayName().getString().toLowerCase(Locale.ROOT).contains(s);
                        } else {
                            return BuiltInRegistries.ITEM.getKey(holder.value().getResultItem(ra).getItem()).toString().toLowerCase(Locale.ROOT).startsWith(s);
                        }
                    }
                ).toList();
                if (!relevantHolders.isEmpty()) {
                    RecipeCollection newCollection = new RecipeCollection(ra, relevantHolders);
                    newCollection.canCraft(stackedContents, menu.getGridWidth(), menu.getGridHeight(), book);
                    list.add(list.indexOf(collection),newCollection);
                }
                list.remove(collection);
            }
        }


        if (this.book.isFiltering(this.menu)) {
            list.removeIf(c -> !c.hasCraftable());
        }

        return list;
    }

    @Unique
    private List<ItemStack> rbp$getSearchItems(String searchTerm) {
        if (rbp$getNamespace(searchTerm).isEmpty())
        { return BuiltInRegistries.ITEM.stream().filter(item -> new ItemStack(item).getDisplayName().getString().toLowerCase(Locale.ROOT).contains(searchTerm)).map(ItemStack::new).toList(); }
        else
        { return BuiltInRegistries.ITEM.stream().filter(item -> BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT).equals(searchTerm)).map(ItemStack::new).toList(); }
    }

    @Unique
    private static String rbp$getNamespace(String searchTerm) {
        if (searchTerm.indexOf(":", searchTerm.indexOf(":")+1) != -1 || !searchTerm.contains(":")) return "";
        String testStr = searchTerm.split(":")[0];
        Set<String> namespaces = BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet());
        if (!namespaces.contains(testStr)) return "";
        return String.valueOf((namespaces.stream().filter(ns->ns.equals(testStr)).findFirst()));
    }

    @Override
    public void rbp$search(String searchTerm) {
        searchBox.setFocused(false);
        searchBox.setValue(searchTerm);
        searchBox.moveCursorToStart(false);
        searchBox.setFocused(false);
        updateCollections(true);
    }
    // endregion

    // region UI
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void rbp$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled()) {
            if (this.isVisible()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
                int i = (this.width - 147) / 2 - this.xOffset;
                int j = (this.height - 166) / 2;
                if (Config.INSTANCE.getUseCustomUI()) {
                    guiGraphics.blit(BACKGROUND_IMAGE_LOCATION, i, j, 1, 1, 151, 166);
                }else {
                    guiGraphics.blit(RECIPE_BOOK_LOCATION, i, j, 1, 1, 147, 166);
                }
                this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);

                for(RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
                    recipebooktabbutton.render(guiGraphics, mouseX, mouseY, partialTick);
                }

                this.filterButton.render(guiGraphics, mouseX, mouseY, partialTick);
                this.recipeBookPage.render(guiGraphics, i, j, mouseX, mouseY, partialTick);
                guiGraphics.pose().popPose();
            }
            ci.cancel();
        }
    }

    // endregion
}
