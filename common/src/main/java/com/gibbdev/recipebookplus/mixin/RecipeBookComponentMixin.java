package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.CommonClass;
import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
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
import net.minecraft.client.renderer.RenderPipelines;
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
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/help")
    );
    @Unique
    private static final WidgetSprites GROUP_BUTTON = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/group_enabled"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/group_disabled"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/group_enabled_hover"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/group_disabled_hover"));
    @Unique
    private static final WidgetSprites CUSTOM_FILTER_BUTTON = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_filter_craftable_enabled"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_filter_craftable_disabled"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_filter_craftable_enabled"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_filter_craftable_disabled"));
    @Unique
    private static final WidgetSprites CUSTOM_GROUP_BUTTON = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_grouping_enabled"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_grouping_disabled"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_grouping_enabled"),
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_grouping_disabled"));
    @Unique
    private static final WidgetSprites CUSTOM_HELP_BUTTON = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/hover_widget_help")
    );
    @Unique
    private static final Identifier BACKGROUND_IMAGE_LOCATION =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID,"textures/gui/sprites/custom_recipe_book/background.png");

    @Unique
    private AbstractWidget rbp$helpButton;
    @Unique
    private CycleButton<Boolean> rbp$groupButton;

    @Unique
    Component rbp$ALL_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.all");


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
    @Shadow @Final
    private StackedItemContents stackedContents;
    @Shadow
    private float time;
    @Shadow @Final
    private List<RecipeBookTabButton> tabButtons;
    @Shadow
    protected CycleButton<Boolean> filterButton;
    @Shadow
    private int xOffset;
    @Shadow
    private int width;
    @Shadow
    private boolean widthTooNarrow;
    @Shadow @Final
    private List<RecipeBookComponent.TabInfo> tabInfos;

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
    @Shadow
    public abstract boolean isVisible();
    @Shadow
    protected abstract Component getRecipeFilterName();
    @Shadow
    protected abstract void toggleFiltering();
    @Shadow
    protected abstract void onTabButtonPress(Button button);
    @Shadow
    protected abstract void selectMatchingRecipes();
    @Shadow
    protected abstract  void updateTabs(boolean isFiltering);

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
    @Inject(method = "initVisuals",at = @At("HEAD"),cancellable = true)
    public void rbp$initVisuals(CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && minecraft.player != null) {
            if (Config.INSTANCE.getUseCustomUI()) {
                // region variables init
                boolean isFiltering = this.isFiltering();
                rbp$isGrouping = CommonClass.groupingState;
                this.xOffset = this.widthTooNarrow ? 0 : 86;
                int xo = this.getXOrigin();
                int yo = this.getYOrigin();
                this.stackedContents.clear();
                this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
                this.menu.fillCraftSlotsStackedContents(this.stackedContents);
                // endregion
                // region searchBox
                String oldEdit = this.searchBox != null ? this.searchBox.getValue() : "";
                this.searchBox = new EditBox(this.minecraft.font, getXOrigin() + 27, getYOrigin() + 17, 84, 10, Component.translatable("itemGroup.search"));
                this.searchBox.setMaxLength(50);
                this.searchBox.setVisible(true);
                this.searchBox.setTextColor(-3439300);
                this.searchBox.setTextShadow(false);
                this.searchBox.setValue(oldEdit);
                this.searchBox.setBordered(false);
                this.searchBox.setInvertHighlightedTextColor(false);
                this.searchBox.setHint(Component.translatable("gui.recipebook.search_hint").withColor(-1791392).withoutShadow());
                // endregion
                this.recipeBookPage.init(this.minecraft, xo, yo);
                // region filterButton
                this.filterButton = CycleButton.booleanBuilder(this.getRecipeFilterName(), rbp$ALL_RECIPES_TOOLTIP, isFiltering)
                    .withTooltip((filtering) -> filtering ? Tooltip.create(this.getRecipeFilterName()) : Tooltip.create(rbp$ALL_RECIPES_TOOLTIP))
                    .withSprite((cycleButton, filtering) -> CUSTOM_FILTER_BUTTON.get(filtering, cycleButton.isHoveredOrFocused()))
                    .displayState(CycleButton.DisplayState.HIDE)
                    .create(
                        xo + 135, yo + 4, 7, 18,
                        CommonComponents.EMPTY,
                        (_, value) -> {
                            this.toggleFiltering();
                            this.sendUpdateSettings();
                            this.updateCollections(false, value);
                });
                // endregion
                // region groupButton
                this.rbp$groupButton = CycleButton.booleanBuilder(Component.translatable("recipebookplus.gui.grouping"),Component.translatable("recipebookplus.gui.not_grouping"),rbp$isGrouping)
                    .withTooltip((rbp$isGrouping)->rbp$isGrouping?Tooltip.create(Component.translatable("recipebookplus.gui.grouping")):Tooltip.create(Component.translatable("recipebookplus.gui.not_grouping")))
                    .withSprite((cycleButton, rbp$isGrouping) -> CUSTOM_GROUP_BUTTON.get(rbp$isGrouping, cycleButton.isHoveredOrFocused()))
                    .displayState(CycleButton.DisplayState.HIDE)
                    .create(
                        xo+127,yo+4,7,18,
                        CommonComponents.EMPTY,
                        (_, _) -> {
                            rbp$isGrouping = !rbp$isGrouping;
                            CommonClass.groupingState = rbp$isGrouping;
                            sendUpdateSettings();
                            updateCollections(true, book.isFiltering(menu.getRecipeBookType()));
                        });
                // endregion
                // region helpWidget
                if (Config.INSTANCE.getDisplayHelpButton()) {
                    this.rbp$helpButton = new ImageButton(xo + 119, yo + 4, 7, 18, CUSTOM_HELP_BUTTON, _ -> rbp$helpButton());
                    this.rbp$helpButton.setTooltip(rbp$getHelpButtonTooltip());
                }
                // endregion

                this.tabButtons.clear();
                for(RecipeBookComponent.TabInfo tabInfo : this.tabInfos) {
                    this.tabButtons.add(new RecipeBookTabButton(0, 0, tabInfo, this::onTabButtonPress));
                }
                if (this.selectedTab != null) {
                    this.selectedTab = this.tabButtons.stream().filter((o) -> o.getCategory().equals(this.selectedTab.getCategory())).findFirst().orElse(null);
                }
                if (this.selectedTab == null) {
                    this.selectedTab = this.tabButtons.getFirst();
                }
                this.selectedTab.select();
                this.selectMatchingRecipes();
                this.updateTabs(isFiltering);

                this.updateCollections(false, isFiltering);

                ci.cancel();

            } else {
                this.xOffset = this.widthTooNarrow ? 0 : 86;
                rbp$isGrouping = CommonClass.groupingState;
                int xo = getXOrigin();
                int yo = getYOrigin();
                if (Config.INSTANCE.getDisplayHelpButton()) {
                    //I DONT CARE THAT I USE BUTTON FOR THE STATIC TEXTURE. SUE ME
                    this.rbp$helpButton = new ImageButton(xo + 110, yo + 139, 26, 16, HELP_BUTTON, _ -> rbp$helpButton());
                    this.rbp$helpButton.setTooltip(rbp$getHelpButtonTooltip());
                }
                this.rbp$groupButton = CycleButton.booleanBuilder(Component.translatable("recipebookplus.gui.grouping"),Component.translatable("recipebookplus.gui.not_grouping"),rbp$isGrouping)
                        .withTooltip((rbp$isGrouping)->rbp$isGrouping?Tooltip.create(Component.translatable("recipebookplus.gui.grouping")):Tooltip.create(Component.translatable("recipebookplus.gui.not_grouping")))
                        .withSprite((cycleButton, rbp$isGrouping) -> GROUP_BUTTON.get(rbp$isGrouping, cycleButton.isHoveredOrFocused()))
                        .displayState(CycleButton.DisplayState.HIDE)
                        .create(
                                xo+11,yo+139,26,16,
                                CommonComponents.EMPTY,
                                (_, _) -> {
                                    rbp$isGrouping = !rbp$isGrouping;
                                    CommonClass.groupingState = rbp$isGrouping;
                                    sendUpdateSettings();
                                    updateCollections(true, book.isFiltering(menu.getRecipeBookType()));
                                });
            }

        }

    }

    /**
     * Injects right before rendering craftable filter toggle button to render 2 new widgets
     * {@code @Mixin} {@link RecipeBookComponent#extractRenderState(GuiGraphicsExtractor, int, int, float)}
     */
    @Inject(method="extractRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/CycleButton;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    public void rbp$extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tick, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && !Config.INSTANCE.getUseCustomUI()) {
            if (Config.INSTANCE.getDisplayHelpButton()) {
                this.rbp$helpButton.extractRenderState(graphics, mouseX, mouseY, tick);
            }
            this.rbp$groupButton.extractRenderState(graphics,mouseX,mouseY,tick);
        }
    }
    /**
     * Replaces render method to extract render state for custom UI
     * {@code @Mixin} {@link RecipeBookComponent#extractRenderState(GuiGraphicsExtractor, int, int, float)}
     */
    @Inject(method="extractRenderState",at=@At(value="HEAD"),cancellable = true)
    public void rbp$extractRenderStateCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tick, CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI() && this.isVisible()) {
            ci.cancel();
            if (!this.minecraft.hasControlDown()) {
                this.time += tick;
            }

            int xo = this.getXOrigin();
            int yo = this.getYOrigin();
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_IMAGE_LOCATION, xo, yo, 1.0F, 1.0F, 151, 166, 256, 256);
            this.searchBox.extractRenderState(graphics, mouseX, mouseY, tick);

            for(RecipeBookTabButton tabButton : this.tabButtons) {
                tabButton.extractRenderState(graphics, mouseX, mouseY, tick);
            }

            this.filterButton.extractRenderState(graphics, mouseX, mouseY, tick);
            this.rbp$groupButton.extractRenderState(graphics,mouseX,mouseY,tick);
            if (Config.INSTANCE.getDisplayHelpButton()) {
                this.rbp$helpButton.extractRenderState(graphics, mouseX, mouseY, tick);
            }
            this.recipeBookPage.extractRenderState(graphics, xo, yo, mouseX, mouseY, tick);
        }
    }

    /**
     * Injects right before processing the input of recipe book elements and hooks in processing for the grouping button
     * {@code @Mixin} {@link RecipeBookComponent#mouseClicked(MouseButtonEvent, boolean)}
     */
    @Inject(method="mouseClicked",at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/components/EditBox;setFocused(Z)V"),cancellable = true)
    public void rbp$mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.getModEnabled()) {
            if (this.rbp$groupButton.mouseClicked(event, doubleClick)) {
                cir.setReturnValue(true);
            }
        }
    }
    @Inject(method="mouseClicked",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;getLastClickedRecipe()Lnet/minecraft/world/item/crafting/display/RecipeDisplayId;"))
    public void rbp$mouseClickedUnfocusSearchbar(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.getModEnabled()) {
            this.searchBox.setFocused(false);
            this.searchBox.moveCursorToStart(false);
        }
    }

    @Inject(method = "getXOrigin", at=@At("HEAD"), cancellable = true)
    private void getXOrigin(CallbackInfoReturnable<Integer> cir) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            cir.setReturnValue((int) Math.round((this.width - 147) / 2.0) - this.xOffset - 1);
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
    @Unique
    private Tooltip rbp$getHelpButtonTooltip() {
        return Tooltip.create(Component.translatable(
                "recipebookplus.gui.help_tooltip",
                Component.literal(Config.INSTANCE.getIngredientPrefix()).withStyle(ChatFormatting.GOLD),
                Component.literal(Config.INSTANCE.getModidPrefix()).withStyle(ChatFormatting.GOLD),
                Component.keybind("recipebookplus.keymapping.recipe").withStyle(ChatFormatting.GREEN),
                Component.keybind("recipebookplus.keymapping.usage").withStyle(ChatFormatting.GREEN),
                Component.keybind("recipebookplus.keymapping.mod").withStyle(ChatFormatting.GREEN)
        ));
    }

    @Unique @Override
    public EditBox rbp$getSearchBox() {return searchBox;}

    @Unique @Override
    public void rbp$resetSearch() {
        updateCollections(true,isFiltering());
    }
}
