package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.RegEx;
import java.io.Console;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Mixin(RecipeBookComponent.class)
public class UpdateCollectionsMixin {

    @Shadow private EditBox searchBox;
    @Shadow private ClientRecipeBook book;
    @Shadow private RecipeBookPage recipeBookPage;
    @Shadow private RecipeBookTabButton selectedTab;
    @Shadow private StackedContents stackedContents;
    @Shadow private RecipeBookMenu<?, ?> menu;
    @Shadow private Minecraft minecraft;

    /**
     * Changes recipe collection list depending on advanced search terms
     * @Mixin {@link RecipeBookComponent#updateCollections(boolean)}
     *
     *
     * @param resetPageNumber If true forces page reset to first page
     * @param ci {@link CallbackInfo}
     */
    @Inject(method = "updateCollections",at = @At("HEAD"),cancellable = true)
    private void updateCollections(boolean resetPageNumber,CallbackInfo ci) {
        if (!Config.MOD_ENABLED.getAsBoolean()) {return;}
        ci.cancel();
        List<RecipeCollection> list = this.book.getCollection(this.selectedTab.getCategory());
        list.forEach((collection) -> collection.canCraft(stackedContents, menu.getGridWidth(), menu.getGridHeight(), this.book));
        List<RecipeCollection> list1 = Lists.newArrayList(list);
        list1.removeIf((collection) -> !collection.hasKnownRecipes());
        list1.removeIf((collection) -> !collection.hasFitting());
        String s = searchBox.getValue().toLowerCase(Locale.ROOT);


        List<RecipeCollection> tempList = Lists.newArrayList(list1);
        if (Config.SEARCH_UNGROUP.getAsBoolean() && !s.isEmpty()) {
            for (RecipeCollection collection : tempList) {
                if (collection.getRecipes().size() >= 2) {
                    for (RecipeHolder<?> holder : collection.getRecipes()) {
                        RecipeCollection newCollection = new RecipeCollection(minecraft.player.registryAccess(), List.of(holder));
                        newCollection.canCraft(stackedContents, menu.getGridWidth(), menu.getGridHeight(), this.book);
                        list1.add(list1.indexOf(collection), newCollection);
                    }
                    list1.remove(collection);
                }
            }
        }
        if (s.startsWith(Config.INGREDIENT_PREFIX.get()) && s != Config.INGREDIENT_PREFIX.get()) {
            String searchTerm = s.replaceFirst(Matcher.quoteReplacement(Config.INGREDIENT_PREFIX.get()), "").strip();
            if (searchTerm != "") {
                List<ItemStack> itemStacks = searchItems(searchTerm);
                tempList = Lists.newArrayList(list1);
                for (RecipeCollection collection : tempList) {
                    boolean ingredientFound = false;
                    for (RecipeHolder<?> holder : collection.getRecipes()) {
                        Recipe<?> recipe = holder.value();
                        for (Ingredient ingredient : recipe.getIngredients()) {
                            for (ItemStack item : itemStacks) {
                                if (ingredient.test(item)) {
                                    ingredientFound = true;
                                    break;
                                }
                            }
                            if (ingredientFound) break;
                        }
                        if (ingredientFound) break;
                    }
                    if (!ingredientFound) list1.remove(collection);
                }
            }
        } else if (s.startsWith(Config.MODID_PREFIX.get()) && s != Config.MODID_PREFIX.get()) {
            String searchTerm = s.replaceFirst(Matcher.quoteReplacement(Config.MODID_PREFIX.get()), "").strip();
            if (searchTerm != "") {
                tempList = Lists.newArrayList(list1);
                for (RecipeCollection collection : tempList) {
                    for (RecipeHolder<?> holder : collection.getRecipes()) {
                        Item resultItem = holder.value().getResultItem(minecraft.player.registryAccess()).getItem();
                        String namespace = BuiltInRegistries.ITEM.getKey(resultItem).getNamespace();
                        if (!namespace.contains(searchTerm)) list1.remove(collection);
                    }
                }
            }

        } else {
            tempList = Lists.newArrayList(list1);
            for (RecipeCollection collection : tempList) {
                boolean recipeFound = false;
                for (RecipeHolder<?> holder : collection.getRecipes()) {
                    String resultItemName = holder.value().getResultItem(minecraft.player.registryAccess()).getDisplayName().getString().toLowerCase(Locale.ROOT);
                    if (resultItemName.contains(s)) recipeFound = true;
                }
                if (!recipeFound) list1.remove(collection);
            }
        }


        if (book.isFiltering(this.menu)) {
            list1.removeIf((collection) -> !collection.hasCraftable());
        }

        this.recipeBookPage.updateCollections(list1, resetPageNumber);
//        if (lastSearch.equalsIgnoreCase(s)) {
//            return;
//        }
//        lastSearch = s;
//        if (!s.startsWith("u:") || s.equalsIgnoreCase("u:")) {
//            return;
//        }
//        ci.cancel();
//        String searchTerm = s.substring(2).trim();
//        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(searchTerm));
//
//        for (RecipeCollection collection : book.getCollection(selectedTab.getCategory())) {
//            for (RecipeHolder<?> holder : collection.getRecipes()) {
//                Recipe<?> recipe = holder.value();
//                for (Ingredient ingredient : recipe.getIngredients()) {
//                    item.ifPresent(i->{
//                        if (ingredient.test(new ItemStack(i))) {
//                            Minecraft.getInstance().player.displayClientMessage(Component.literal(holder.id().toString()),false);
//                        }
//                    });
//                }
//            }
//        }
    }

    private static List<ItemStack> searchItems(String term) {
        String namespace = getNamespace(term);
        if (!namespace.isEmpty()) {
            return BuiltInRegistries.ITEM.stream().filter(item ->{
                return BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT).contains(term);
            }).map(ItemStack::new).toList();
        } else {
            return BuiltInRegistries.ITEM.stream().filter(item ->{
                return new ItemStack(item).getDisplayName().getString().toLowerCase(Locale.ROOT).contains(term);
            }).map(ItemStack::new).toList();
        }
    }

    private static String getNamespace(String str) {
        if (str.indexOf(":", str.indexOf(":")+1) != -1 || str.indexOf(":")==-1) return "";
        String testStr = str.split(":")[0];
        Set<String> namespaces = BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet());
        if (!namespaces.contains(testStr)) return "";
        return String.valueOf((namespaces.stream().filter(ns->ns.equals(testStr)).findFirst()));
    }
}
