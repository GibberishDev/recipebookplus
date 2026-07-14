package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Mixin(RecipeBookComponent.class)
public abstract class UpdateCollectionsMixin {

    private static boolean grouping = true;

    private static final WidgetSprites HELP_BUTTON = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath("recipebookplus","recipe_book/help"),
            ResourceLocation.fromNamespaceAndPath("recipebookplus","recipe_book/help"),
            ResourceLocation.fromNamespaceAndPath("recipebookplus","recipe_book/help"),
            ResourceLocation.fromNamespaceAndPath("recipebookplus","recipe_book/help"));
    private static final WidgetSprites GROUP_BUTTON = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath("recipebookplus","recipe_book/group_enabled"),
            ResourceLocation.fromNamespaceAndPath("recipebookplus","recipe_book/group_disabled"),
            ResourceLocation.fromNamespaceAndPath("recipebookplus","recipe_book/group_enabled_hover"),
            ResourceLocation.fromNamespaceAndPath("recipebookplus","recipe_book/group_disabled_hover"));

    private AbstractWidget helpButton;
    private StateSwitchingButton groupButton;

    @Shadow private EditBox searchBox;
    @Shadow private ClientRecipeBook book;
    @Shadow private RecipeBookPage recipeBookPage;
    @Shadow private RecipeBookTabButton selectedTab;
    @Shadow private StackedContents stackedContents;
    @Shadow private RecipeBookMenu<?, ?> menu;
    @Shadow private Minecraft minecraft;
    @Shadow private int width;
    @Shadow private int height;
    @Shadow private int xOffset;
    @Shadow private boolean widthTooNarrow;

//region Injects

    /**
     * Changes recipe collection list depending on advanced search terms
     * @Mixin {@link RecipeBookComponent#updateCollections(boolean)}
     */
    @Inject(method = "updateCollections",at = @At("HEAD"),cancellable = true)
    private void recipebookplus_updateCollections(boolean resetPageNumber,CallbackInfo ci) {
        if (!Config.MOD_ENABLED.getAsBoolean()) {return;}
        ci.cancel();
        List<RecipeCollection> list = book.getCollection(selectedTab.getCategory());
        list.forEach((collection) -> collection.canCraft(stackedContents, menu.getGridWidth(), menu.getGridHeight(), book));
        List<RecipeCollection> list1 = Lists.newArrayList(list);
        list1.removeIf((collection) -> !collection.hasKnownRecipes());
        list1.removeIf((collection) -> !collection.hasFitting());
        String s = searchBox.getValue().toLowerCase(Locale.ROOT);


        List<RecipeCollection> tempList = Lists.newArrayList(list1);
        if (!grouping) {
            for (RecipeCollection collection : tempList) {
                if (collection.getRecipes().size() >= 2) {
                    List<RecipeHolder<?>> holders = new ArrayList<>();
                    holders.addAll(collection.getDisplayRecipes(true));
                    holders.addAll(collection.getDisplayRecipes(false));
                    for (RecipeHolder<?> holder : holders) {
                        RecipeCollection newCollection = new RecipeCollection(minecraft.level.registryAccess(), List.of(holder));
                        newCollection.canCraft(stackedContents, menu.getGridWidth(), menu.getGridHeight(), book);
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
                        Item resultItem = holder.value().getResultItem(minecraft.level.registryAccess()).getItem();
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
                    String resultItemName = holder.value().getResultItem(minecraft.level.registryAccess()).getDisplayName().getString().toLowerCase(Locale.ROOT);
                    if (resultItemName.contains(s)) recipeFound = true;
                }
                if (!recipeFound) list1.remove(collection);
            }
        }

        if (book.isFiltering(menu)) {
            list1.removeIf((collection) -> !collection.hasCraftable());
        }
        recipeBookPage.updateCollections(list1, resetPageNumber);
    }

    /**
     * Initialises aditional two ui elements - Help button (usage tooltip) and Group button
     * @Mixin {@link RecipeBookComponent#initVisuals()}
     */
    @Inject(method = "initVisuals",at = @At("TAIL"),cancellable = false)
    public void recipebookplus_initVisuals(CallbackInfo ci) {
        if (Config.MOD_ENABLED.get()) {
            xOffset = widthTooNarrow ? 0 : 86;
            int i = (width - 147) / 2 - xOffset;
            int j = (height - 166) / 2;
            //I DONT CARE THAT I USE BUTTON FOR THE STATIC TEXTURE. SUE ME
            helpButton = new ImageButton(i + 110,j + 139,26,16,HELP_BUTTON, fuck -> helpButton());
            helpButton.setTooltip(Tooltip.create(Component.translatable(
                    "recipebookplus.gui.help_tooltip",
                    Component.literal(Config.INGREDIENT_PREFIX.get()).withStyle(ChatFormatting.GOLD),
                    Component.literal(Config.MODID_PREFIX.get()).withStyle(ChatFormatting.GOLD),
                    Component.keybind("recipebookplus.keymapping.recipe").withStyle(ChatFormatting.GREEN),
                    Component.keybind("recipebookplus.keymapping.usage").withStyle(ChatFormatting.GREEN),
                    Component.keybind("recipebookplus.keymapping.mod").withStyle(ChatFormatting.GREEN)
            )));
            groupButton = new StateSwitchingButton(i+11,j+139,26,16,true);
            groupButton.initTextureValues(GROUP_BUTTON);
        }

    }

    /**
     * Injects right before rendering craftable filter toggle button to render 2 new windgets
     * @Mixin {@link RecipeBookComponent#render(GuiGraphics, int, int, float)}
     */
    @Inject(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/StateSwitchingButton;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"),cancellable=false)
    public void recipebookplus_render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Config.MOD_ENABLED.get()) {
            helpButton.render(guiGraphics,mouseX,mouseY,partialTick);
            groupButton.render(guiGraphics,mouseX,mouseY,partialTick);
        }
    }


    /**
     * Injects right before procesing the input of recipe book elements and hooks in processiong for the grouping button
     * @Mixin {@link RecipeBookComponent#mouseClicked(double, double, int)}
     */
    @Inject(method="mouseClicked",at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/components/EditBox;setFocused(Z)V"),cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Config.MOD_ENABLED.get()) {
            if (this.groupButton.mouseClicked(mouseX, mouseY, button)) {
                this.toggleGrouping();
                this.groupButton.setStateTriggered(grouping);
                recipebookplus_updateCollections(true, new CallbackInfo("updateCollections", true));
                cir.setReturnValue(true);
            }
        }
    }

//endregion

//region misc methods


    /**
     *
     * @param term
     * @return
     */
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

    /**
     * Processes input string to determine if it contains a valid namespace
     * @param str {@link String} input string
     * @return {@link  String} outputs valid namespace or empty string ("") if no fitting namesspaces were found
     */
    private static String getNamespace(String str) {
        if (str.indexOf(":", str.indexOf(":")+1) != -1 || str.indexOf(":")==-1) return "";
        String testStr = str.split(":")[0];
        Set<String> namespaces = BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet());
        if (!namespaces.contains(testStr)) return "";
        return String.valueOf((namespaces.stream().filter(ns->ns.equals(testStr)).findFirst()));
    }

    /**Dud method to plug in the requirement for callback for {@link ImageButton} {@link UpdateCollectionsMixin#helpButton}*/
    private void helpButton() {return;}

    /**Updates grouping of Recipe Collections and updates visuals for {@link UpdateCollectionsMixin#groupButton}*/
    private void toggleGrouping() {
        grouping = !grouping;
        groupButton.setTooltip(grouping ? Tooltip.create(Component.translatable("recipebookplus.gui.grouping")) : Tooltip.create(Component.translatable("recipebookplus.gui.not_grouping")));
        groupButton.initTextureValues(GROUP_BUTTON);
    }

    /**@ToDo: implement keybinds for entering search temrs based on the hovered item JEI style*/
    public void search(String searchTerm) {
        searchBox.setValue(searchTerm);
        recipebookplus_updateCollections(true, new CallbackInfo("updateCollections", true));
    }

//endreion

}