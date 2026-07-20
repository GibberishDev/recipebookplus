package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.CommonClass;
import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookComponentMixin;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin implements IRecipeBookComponentMixin {

    @Unique
    private boolean rbp$isGrouping = true;

    @Unique
    private static final WidgetSprites HELP_BUTTON = new WidgetSprites(
            Identifier.fromNamespaceAndPath("recipebookplus","recipe_book/help"),
            Identifier.fromNamespaceAndPath("recipebookplus","recipe_book/help"),
            Identifier.fromNamespaceAndPath("recipebookplus","recipe_book/help"),
            Identifier.fromNamespaceAndPath("recipebookplus","recipe_book/help"));
    @Unique
    private static final WidgetSprites GROUP_BUTTON = new WidgetSprites(
            Identifier.fromNamespaceAndPath("recipebookplus","recipe_book/group_enabled"),
            Identifier.fromNamespaceAndPath("recipebookplus","recipe_book/group_disabled"),
            Identifier.fromNamespaceAndPath("recipebookplus","recipe_book/group_enabled_hover"),
            Identifier.fromNamespaceAndPath("recipebookplus","recipe_book/group_disabled_hover"));

    @Unique
    private AbstractWidget rbp$helpButton;
    @Unique
    private CycleButton<Boolean> rbp$groupButton;


    @Shadow
    protected Minecraft minecraft;
    @Shadow
    private EditBox searchBox;
    @Shadow
    private ClientRecipeBook book;
    @Shadow @Nullable
    private RecipeBookTabButton selectedTab;
    @Shadow @Final
    private RecipeBookPage recipeBookPage;
    @Shadow @Final
    protected RecipeBookMenu menu;
    @Shadow
    @Final
    private StackedItemContents stackedContents;

    @Shadow
    protected void sendUpdateSettings(){}

    @Shadow
    protected abstract void updateCollections(boolean resetPage, boolean isFiltering);
    @Shadow
    protected abstract int getXOrigin();
    @Shadow
    protected abstract int getYOrigin();

    @Shadow
    protected abstract boolean isFiltering();

    /**
     * {@code @Mixin} {@link RecipeBookComponent#updateCollections(boolean, boolean)}
     */
    @Inject(at = @At("HEAD"), method = "updateCollections", cancellable = true)
    public void rbp$updateCollections(boolean resetPage, boolean isFiltering, CallbackInfo ci) {
        if (!Config.INSTANCE.getModEnabled()) {
            return;
        }
        ci.cancel();
        if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) return;
        assert this.selectedTab != null;
        List<RecipeCollection> tabCollection = this.book.getCollection(this.selectedTab.getCategory());
        List<RecipeCollection> collection = Lists.newArrayList(tabCollection);
        collection.removeIf((c) -> !c.hasAnySelected());
        String searchTerm = this.searchBox.getValue().toLowerCase(Locale.ROOT);

        List<RecipeCollection> tempCollectionList = List.copyOf(collection);

        if (!rbp$isGrouping) {
            for (RecipeCollection c : tempCollectionList) {
                if (c.getSelectedRecipes(RecipeCollection.CraftableStatus.ANY).size() >= 2) {
                    for (RecipeDisplayEntry recipe : c.getSelectedRecipes(RecipeCollection.CraftableStatus.ANY)) {
                        RecipeCollection newCollection = new RecipeCollection(List.of(recipe));
                        newCollection.selectRecipes(stackedContents, _ -> true);
                        collection.add(collection.indexOf(c),newCollection);
                    }
                    collection.remove(c);
                }
            }
        }

        if (searchTerm.startsWith(Config.INSTANCE.getIngredientPrefix()) && !searchTerm.equals(Config.INSTANCE.getIngredientPrefix())) {
            searchTerm = searchTerm.replaceFirst(Matcher.quoteReplacement(Config.INSTANCE.getIngredientPrefix()),"");
            if (searchTerm.isEmpty()) return;
            List<ItemStack> searchItems = rbp$searchItems(searchTerm);
            tempCollectionList = List.copyOf(collection);
            for (RecipeCollection c : tempCollectionList) {
                List<RecipeDisplayEntry> recipes = c.getSelectedRecipes(RecipeCollection.CraftableStatus.ANY);
                List<RecipeDisplayEntry> validRecipes = new ArrayList<>();
                if (recipes.size() > 1) {
                    for (RecipeDisplayEntry recipe : recipes) {
                        Optional<List<Ingredient>> ingredients = recipe.craftingRequirements();
                        boolean ingredientFound = false;
                        if (ingredients.isPresent()) {
                            for (Ingredient ingredient : ingredients.get()) {
                                for (ItemStack item : searchItems) {
                                    if (ingredient.test(item)) {
                                        ingredientFound = true;
                                        break;
                                    }
                                }
                                if (ingredientFound) break;
                            }

                        }
                        if (ingredientFound) {
                            validRecipes.add(recipe);
                        }
                    }
                    if (!validRecipes.isEmpty()) {
                        RecipeCollection newCollection = new RecipeCollection(validRecipes);
                        newCollection.selectRecipes(stackedContents, _ -> true);
                        collection.add(collection.indexOf(c), newCollection);
                    }
                    collection.remove(c);
                } else if (recipes.stream().findFirst().isPresent()) {
                    boolean recipeIsValid = false;
                    Optional<List<Ingredient>> ingredients = recipes.stream().findFirst().get().craftingRequirements();
                    if (ingredients.isPresent()) {
                        for (Ingredient ingredient : ingredients.get()) {
                            for (ItemStack item : searchItems) {
                                if (ingredient.test(item)) {
                                    recipeIsValid = true;
                                    break;
                                }
                            }
                            if (recipeIsValid) break;
                        }
                    }
                    if (!recipeIsValid) collection.remove(c);
                }
            }
        } else if (searchTerm.startsWith(Config.INSTANCE.getModidPrefix()) && !searchTerm.equals(Config.INSTANCE.getModidPrefix())) {
            searchTerm = searchTerm.replaceFirst(Matcher.quoteReplacement(Config.INSTANCE.getModidPrefix()),"");
            if (searchTerm.isEmpty()) return;
            tempCollectionList = List.copyOf(collection);
            for (RecipeCollection c : tempCollectionList) {
                List<RecipeDisplayEntry> recipes = c.getSelectedRecipes(RecipeCollection.CraftableStatus.ANY);
                if (recipes.size() > 1) {
                    List<RecipeDisplayEntry> validRecipes = new ArrayList<>();
                    for (RecipeDisplayEntry recipe : recipes) {
                        for (ItemStack item : recipe.resultItems(SlotDisplayContext.fromLevel(minecraft.level))) {
                            if (BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace().startsWith(searchTerm)) {
                                validRecipes.add(recipe);
                                break;
                            }
                        }
                    }
                    if (!validRecipes.isEmpty()) {
                        RecipeCollection newCollection = new RecipeCollection(validRecipes);
                        newCollection.selectRecipes(stackedContents,_ -> true);
                        collection.add(collection.indexOf(c),newCollection);
                    }
                    collection.remove(c);
                } else if (recipes.stream().findFirst().isPresent()) {
                    boolean isValid = false;
                    for (ItemStack item : recipes.stream().findFirst().get().resultItems(SlotDisplayContext.fromLevel(minecraft.level))) {
                        if (BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace().startsWith(searchTerm)) {
                            isValid = true;
                            break;
                        }
                    }
                    if (!isValid) collection.remove(c);
                }
            }

        } else if (!searchTerm.isEmpty()) {
            String ns = rbp$getNamespace(searchTerm);
            tempCollectionList = List.copyOf(collection);
            for (RecipeCollection c : tempCollectionList) {
                List<RecipeDisplayEntry> recipes = c.getSelectedRecipes(RecipeCollection.CraftableStatus.ANY);
                if (recipes.size() > 1) {
                    List<RecipeDisplayEntry> validRecipes = new ArrayList<>();
                    for (RecipeDisplayEntry recipe : recipes) {
                        for (ItemStack item : recipe.resultItems(SlotDisplayContext.fromLevel(minecraft.level))) {
                            if (ns.isEmpty()) {
                                String name = Component.translatable(item.getItem().getDescriptionId()).getString().toLowerCase(Locale.ROOT);
                                if (name.contains(searchTerm)) {
                                    validRecipes.add(recipe);
                                    break;
                                }
                            } else {
                                String id = BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
                                if (id.equals(searchTerm)) {
                                    validRecipes.add(recipe);
                                    break;
                                }
                            }
                        }
                    }
                    if (!validRecipes.isEmpty()) {
                        RecipeCollection newCollection = new RecipeCollection(validRecipes);
                        newCollection.selectRecipes(stackedContents, _ -> true);
                        collection.add(collection.indexOf(c), newCollection);
                    }
                    collection.remove(c);
                } else if (recipes.stream().findFirst().isPresent()) {
                    boolean isValid = false;
                    for (ItemStack item : recipes.stream().findFirst().get().resultItems(SlotDisplayContext.fromLevel(minecraft.level))) {
                        if (!ns.isEmpty() && BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(searchTerm)) {
                            isValid = true;
                            break;
                        } else if (Component.translatable(item.getItem().getDescriptionId()).getString().toLowerCase(Locale.ROOT).contains(searchTerm)) {
                            isValid = true;
                            break;
                        }
                    }
                    if (!isValid) collection.remove(c);
                }
            }

        }
        if (isFiltering) {
            collection.removeIf((c) -> !c.hasCraftable());
        }

        this.recipeBookPage.updateCollections(collection, resetPage, isFiltering);
    }

    /**
     * Initializes additional two ui elements - Help button (usage tooltip) and Group button
     * {@code @Mixin} {@link RecipeBookComponent#initVisuals()}
     */
    @Inject(method = "initVisuals",at = @At("TAIL"))
    public void recipebookplus_initVisuals(CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled()) {
            rbp$isGrouping = CommonClass.groupingState;
            int i = getXOrigin();
            int j = getYOrigin();
            //I DONT CARE THAT I USE BUTTON FOR THE STATIC TEXTURE. SUE ME
            this.rbp$helpButton = new ImageButton(i + 110,j + 139,26,16,HELP_BUTTON, _ -> rbp$helpButton());
            this.rbp$helpButton.setTooltip(Tooltip.create(Component.translatable(
                    "recipebookplus.gui.help_tooltip",
                    Component.literal("$").withStyle(ChatFormatting.GOLD),
                    Component.literal("@").withStyle(ChatFormatting.GOLD),
                    Component.keybind("recipebookplus.keymapping.recipe").withStyle(ChatFormatting.GREEN),
                    Component.keybind("recipebookplus.keymapping.usage").withStyle(ChatFormatting.GREEN),
                    Component.keybind("recipebookplus.keymapping.mod").withStyle(ChatFormatting.GREEN)
            )));
            this.rbp$groupButton = CycleButton.booleanBuilder(Component.translatable("recipebookplus.gui.grouping"),Component.translatable("recipebookplus.gui.not_grouping"),rbp$isGrouping).withTooltip((rbp$isGrouping)->rbp$isGrouping?Tooltip.create(Component.translatable("recipebookplus.gui.grouping")):Tooltip.create(Component.translatable("recipebookplus.gui.not_grouping"))).withSprite((cycleButton, rbp$isGrouping) -> GROUP_BUTTON.get(rbp$isGrouping, cycleButton.isHoveredOrFocused())).displayState(CycleButton.DisplayState.HIDE).create(
                i+11,j+139,26,16,
                CommonComponents.EMPTY,
                (_, _) -> {
                    rbp$isGrouping = !rbp$isGrouping;
                    CommonClass.groupingState = rbp$isGrouping;
                    sendUpdateSettings();
                    updateCollections(true, book.isFiltering(menu.getRecipeBookType()));
            });
        }

    }

    /**
     * Injects right before rendering craftable filter toggle button to render 2 new widgets
     * {@code @Mixin} {@link RecipeBookComponent#extractRenderState(GuiGraphicsExtractor, int, int, float)}
     */
    @Inject(method="extractRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/CycleButton;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    public void recipebookplus_render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tick, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled()) {
            this.rbp$helpButton.extractRenderState(graphics,mouseX,mouseY,tick);
            this.rbp$groupButton.extractRenderState(graphics,mouseX,mouseY,tick);
        }
    }

    /**
     * Injects right before processing the input of recipe book elements and hooks in processing for the grouping button
     * {@code @Mixin} {@link RecipeBookComponent#mouseClicked(MouseButtonEvent, boolean)}
     */
    @Inject(method="mouseClicked",at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/components/EditBox;setFocused(Z)V"),cancellable = true)
    public void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.getModEnabled()) {
            if (this.rbp$groupButton.mouseClicked(event, doubleClick)) {
                cir.setReturnValue(true);
            }
        }
    }


    @Unique
    private  List<ItemStack> rbp$searchItems(String term) {
        String namespace = rbp$getNamespace(term);
        if (!namespace.isEmpty()) {
            return BuiltInRegistries.ITEM.stream().filter(item -> BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT).equals(term)).map(ItemStack::new).toList();
        } else {
            return BuiltInRegistries.ITEM.stream().filter(item -> new ItemStack(item).getDisplayName().getString().toLowerCase(Locale.ROOT).contains(term)).map(ItemStack::new).toList();
        }
    }

    @Unique
    private String rbp$getNamespace(String str) {
        if (str.indexOf(":", str.indexOf(":")+1) != -1 || !str.contains(":")) return "";
        String testStr = str.split(":")[0];
        Set<String> namespaces = BuiltInRegistries.ITEM.keySet().stream().map(Identifier::getNamespace).collect(Collectors.toSet());
        if (!namespaces.contains(testStr)) return "";
        return String.valueOf((namespaces.stream().filter(ns->ns.equals(testStr)).findFirst()));
    }

    @Unique
    private void rbp$helpButton(){}

    @Unique @Override
    public EditBox rbp$getSearchBox() {return searchBox;}

    @Unique @Override
    public void rbp$resetSearch() {
        updateCollections(true,isFiltering());
    }
}
