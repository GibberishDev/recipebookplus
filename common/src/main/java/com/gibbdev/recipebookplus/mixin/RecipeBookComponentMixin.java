package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.RecipeBookPlus;
import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.compat.FarmersDelight;
import com.gibbdev.recipebookplus.interfaces.IEditBox;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookButton;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookComponent;
import com.gibbdev.recipebookplus.interfaces.accessors.IGhostRecipeAccessor;
import com.gibbdev.recipebookplus.interfaces.accessors.IStateSwitchingButtonAccessor;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import com.gibbdev.recipebookplus.platform.Services;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.slf4j.Logger;
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
    private boolean widthTooNarrow;
    @Shadow @Final
    private static Component ONLY_CRAFTABLES_TOOLTIP;
    @Shadow @Final
    private static Component ALL_RECIPES_TOOLTIP;
    @Shadow @Final
    protected GhostRecipe ghostRecipe;

    @Shadow
    public abstract boolean isVisible();
    @Shadow
    protected abstract void updateCollections(boolean resetPageNumber);
    @Shadow
    protected abstract void updateTabs();
    @Shadow
    protected abstract void sendUpdateSettings();
    @Shadow
    protected abstract boolean isOffsetNextToMainGUI();
    @Shadow
    protected abstract void setVisible(boolean visible);
    @Shadow
    protected abstract boolean toggleFiltering();
    @Shadow
    protected abstract void updateFilterButtonTooltip();
    // endregion

    // region unique variables
    @Unique
    private static final ResourceLocation BACKGROUND_IMAGE_LOCATION =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"textures/gui/sprites/custom_recipe_book/background.png");
    @Unique
    private boolean rbp$isGrouping = true;

    @Unique
    private static final WidgetSprites HELP_BUTTON = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/help"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/help")
    );
    @Unique
    private static final WidgetSprites GROUP_BUTTON = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/group_enabled"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/group_disabled"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/group_enabled_hover"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"recipe_book/group_disabled_hover"));
    @Unique
    private static final WidgetSprites CUSTOM_FILTER_BUTTON = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_filter_craftable_enabled"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_filter_craftable_disabled"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_filter_craftable_enabled"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_filter_craftable_disabled"));
    @Unique
    private static final WidgetSprites CUSTOM_GROUP_BUTTON = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_grouping_enabled"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_grouping_disabled"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_grouping_enabled"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/toggle_button_grouping_disabled"));
    @Unique
    private static final WidgetSprites CUSTOM_HELP_BUTTON = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/hover_widget_help"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/hover_widget_help"));
    @Unique
    private static final WidgetSprites CUSTOM_FULLSCREEN_BUTTON = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/hover_widget_help"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"custom_recipe_book/hover_widget_help"));

    @Unique
    private StateSwitchingButton rbp$groupButton;
    @Unique
    private ImageButton rbp$helpButton;
    @Unique
    private ImageButton rbp$fullscreenButton;

    // endregion

    // region search
    @Inject(method = "updateCollections", at=@At("HEAD"), cancellable = true)
    private void rbp$updateCollections(boolean resetPage, CallbackInfo ci) {
        if (Config.getModEnabled()) {
            Services.PLATFORM.getServerModVersion();
            RecipeDiscovery.giveRecipe("minecraft:stonecutter",minecraft.player.getUUID());
//            Minecraft.getInstance().player.sendSystemMessage(Component.literal(Boolean.toString(Config.Server.SPEC.isLoaded())));
            List<RecipeCollection> list = rbp$getCollections();
            String s = this.searchBox.getValue();
            list = rbp$searchCollectionList(list, s);

            if (this.book.isFiltering(this.menu)) {
                list.removeIf(c -> !c.hasCraftable());
            }
            this.recipeBookPage.updateCollections(list, resetPage);
            ci.cancel();
        }
    }


    @Unique
    private List<RecipeCollection> rbp$searchCollectionList(List<RecipeCollection> list, String searchTerm) {
        if (minecraft.level == null) return list;
        searchTerm = searchTerm.toLowerCase(Locale.ROOT);
        RegistryAccess ra = minecraft.level.registryAccess();
        List<RecipeCollection> tempList = Lists.newArrayList(list);
        rbp$isGrouping = RecipeBookPlus.groupingState;
        if (!rbp$isGrouping) {
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

        if (searchTerm.isEmpty()) return list;


        if (searchTerm.startsWith(Config.getIngredientPrefix()) && !searchTerm.equals(Config.getIngredientPrefix())) {
            searchTerm = searchTerm.replaceFirst(Matcher.quoteReplacement(Config.getIngredientPrefix()), "").strip();
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
                            ItemStack containerItem = FarmersDelight.getRecipeContainer(holder);
                            ingredientFound = !searchItems.stream().filter(item->containerItem.getItem().equals(item.getItem())).toList().isEmpty();
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
        } else if (searchTerm.startsWith(Config.getModidPrefix()) && !searchTerm.equals(Config.getModidPrefix())) {
            String namespace = searchTerm.replaceFirst(Matcher.quoteReplacement(Config.getModidPrefix()), "").strip().toLowerCase(Locale.ROOT);
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

    @Unique
    private List<RecipeCollection> rbp$getCollections() {
        List<RecipeCollection> list = new ArrayList<>();

        RecipeBookCategories category = this.selectedTab.getCategory();
        if (this.book.getCollection(this.selectedTab.getCategory()).getFirst() == null) return list;
        RecipeType<?> recipeType = this.book.getCollection(this.selectedTab.getCategory()).getFirst().getRecipes().getFirst().value().getType();
        RegistryAccess ra = minecraft.level.registryAccess();

        if (!Config.getRecipeDiscovery()) {
            Map<String, List<RecipeHolder<?>>> holderGroups = new HashMap<>();
            for (RecipeHolder<?> holder : minecraft.player.level().getRecipeManager().getRecipes()) {
                if (holder.value().getType() != recipeType) continue;
                if (category != rpb$getRecipeCategory(holder)  && !category.name().endsWith("SEARCH")) continue;
                String group = holder.value().getGroup();
                if (group.isEmpty()) {
                    holderGroups.put(holder.value().toString(), List.of(holder));
                } else {
                    List<RecipeHolder<?>> currentList = new ArrayList<>();
                    if (holderGroups.get(group) != null) {
                        currentList.addAll(holderGroups.get(group));
                    }
                    currentList.add(holder);
                    holderGroups.put(group,currentList);
                }
            }
            holderGroups.forEach((group, holders) -> {
                RecipeCollection newCollection = new RecipeCollection(ra, holders);
                for (RecipeHolder<?> holder : holders) this.book.add(holder);
                list.add(newCollection);
            });
            list.forEach(c -> c.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book));
        } else {
            list.addAll(this.book.getCollection(this.selectedTab.getCategory()));
            list.forEach(c -> c.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book));
            list.removeIf(c -> !c.hasKnownRecipes());
        }
        list.removeIf(c -> !c.hasFitting());
        return list;
    }

    @Unique RecipeBookCategories rpb$getRecipeCategory(RecipeHolder<?> holder) {
        Recipe<?> recipe = holder.value();
        if (recipe instanceof CraftingRecipe) {
            CraftingRecipe craftingrecipe = (CraftingRecipe)recipe;
            RecipeBookCategories cat;
            switch (craftingrecipe.category()) {
                case BUILDING -> cat = RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
                case EQUIPMENT -> cat = RecipeBookCategories.CRAFTING_EQUIPMENT;
                case REDSTONE -> cat = RecipeBookCategories.CRAFTING_REDSTONE;
                case MISC -> cat = RecipeBookCategories.CRAFTING_MISC;
                default -> throw new MatchException((String)null, (Throwable)null);
            }

            return cat;
        } else {
            RecipeType<?> recipetype = recipe.getType();
            if (recipe instanceof AbstractCookingRecipe) {
                AbstractCookingRecipe abstractcookingrecipe = (AbstractCookingRecipe)recipe;
                CookingBookCategory cookingbookcategory = abstractcookingrecipe.category();
                if (recipetype == RecipeType.SMELTING) {
                    RecipeBookCategories cat;
                    switch (cookingbookcategory) {
                        case BLOCKS -> cat = RecipeBookCategories.FURNACE_BLOCKS;
                        case FOOD -> cat = RecipeBookCategories.FURNACE_FOOD;
                        case MISC -> cat = RecipeBookCategories.FURNACE_MISC;
                        default -> throw new MatchException((String)null, (Throwable)null);
                    }

                    return cat;
                }

                if (recipetype == RecipeType.BLASTING) {
                    return cookingbookcategory == CookingBookCategory.BLOCKS ? RecipeBookCategories.BLAST_FURNACE_BLOCKS : RecipeBookCategories.BLAST_FURNACE_MISC;
                }

                if (recipetype == RecipeType.SMOKING) {
                    return RecipeBookCategories.SMOKER_FOOD;
                }

                if (recipetype == RecipeType.CAMPFIRE_COOKING) {
                    return RecipeBookCategories.CAMPFIRE;
                }
            }

            if (recipetype == RecipeType.STONECUTTING) {
                return RecipeBookCategories.STONECUTTER;
            } else if (recipetype == RecipeType.SMITHING) {
                return RecipeBookCategories.SMITHING;
            }

            if (Services.PLATFORM.isModLoaded("farmersdelight")) {
                if (FarmersDelight.INSTANCE.getRecipeCategory(recipe) != null) return FarmersDelight.INSTANCE.getRecipeCategory(recipe);
            }

            Logger LOGGER = Constants.LOG;
            Object category = LogUtils.defer(() -> BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()));
            Objects.requireNonNull(holder);
            LOGGER.warn("Unknown recipe category: {}/{}", category, LogUtils.defer(holder::id));
            return RecipeBookCategories.UNKNOWN;
        }
    }

    // endregion


    //region ui
    @Inject(method = "initVisuals", at = @At("HEAD"),cancellable = true)
    public void initVisuals(CallbackInfo ci) {
        if (Config.getModEnabled() && minecraft.player != null) {
            rbp$isGrouping= RecipeBookPlus.groupingState;
            this.xOffset = this.widthTooNarrow ? 0 : 86;
            int xo = (int) Math.round((this.width - 147) / 2.0) - this.xOffset;
            int yo = (int) Math.round((this.height - 166) / 2.0);
            if (Config.getUseCustomUI()) {
                this.stackedContents.clear();
                this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
                this.menu.fillCraftSlotsStackedContents(this.stackedContents);
                String s = this.searchBox != null ? this.searchBox.getValue() : "";
                this.searchBox = new EditBox(minecraft.font, xo + 27, yo + 16, 84, 10, Component.translatable("itemGroup.search"));
                this.searchBox.setMaxLength(50);
                this.searchBox.setVisible(true);
                this.searchBox.setTextColor(-3439300);
                this.searchBox.setValue(s);
                this.searchBox.setBordered(false);
                this.searchBox.setHint(Component.translatable("gui.recipebook.search_hint").withColor(-1791392));

                this.recipeBookPage.init(this.minecraft, xo, yo);
                this.recipeBookPage.addListener((RecipeBookComponent) (Object) this);

                this.filterButton = new StateSwitchingButton(xo + 135, yo + 4, 7, 18, this.book.isFiltering(this.menu));
                this.filterButton.setTooltip(this.filterButton.isStateTriggered() ? Tooltip.create(ONLY_CRAFTABLES_TOOLTIP) : Tooltip.create(ALL_RECIPES_TOOLTIP));
                this.filterButton.initTextureValues(CUSTOM_FILTER_BUTTON);
                ((IStateSwitchingButtonAccessor) this.filterButton).rbp$setClickSound(SoundEvents.WOOL_PLACE, SoundEvents.WOOL_BREAK);

                this.rbp$groupButton = new StateSwitchingButton(xo + 127, yo + 4, 7, 18, rbp$isGrouping);
                this.rbp$groupButton.setTooltip(rbp$isGrouping?Tooltip.create(Component.translatable("recipebookplus.gui.grouping")):Tooltip.create(Component.translatable("recipebookplus.gui.not_grouping")));
                this.rbp$groupButton.initTextureValues(CUSTOM_GROUP_BUTTON);
                ((IStateSwitchingButtonAccessor) this.rbp$groupButton).rbp$setClickSound(SoundEvents.WOOL_PLACE, SoundEvents.WOOL_BREAK);

                if (Config.getDisplayHelpButton()) {
                    this.rbp$helpButton = new ImageButton(xo + 119, yo + 4, 7, 18, CUSTOM_HELP_BUTTON, button -> {});
                    this.rbp$helpButton.setTooltip(rbp$getHelpButtonTooltip());
                }

                this.tabButtons.clear();

                for (RecipeBookCategories recipebookcategories : RecipeBookCategories.getCategories(this.menu.getRecipeBookType())) {
                    this.tabButtons.add(new RecipeBookTabButton(recipebookcategories));
                }

                if (this.selectedTab != null) {
                    this.selectedTab = this.tabButtons.stream().filter((tabButton) -> tabButton.getCategory().equals(this.selectedTab.getCategory())).findFirst().orElse(null);
                }

                if (this.selectedTab == null) {
                    this.selectedTab = this.tabButtons.getFirst();
                }

                this.selectedTab.setStateTriggered(true);
                this.updateCollections(false);
                this.updateTabs();
                ci.cancel();
            }
            else {
                this.rbp$groupButton = new StateSwitchingButton(xo + 11, yo + 139, 26, 16, rbp$isGrouping);
                this.rbp$groupButton.setTooltip(rbp$isGrouping?Tooltip.create(Component.translatable("recipebookplus.gui.grouping")):Tooltip.create(Component.translatable("recipebookplus.gui.not_grouping")));
                this.rbp$groupButton.initTextureValues(GROUP_BUTTON);

                if (Config.getDisplayHelpButton()) {
                    this.rbp$helpButton = new ImageButton(xo + 110, yo + 139, 26, 16, HELP_BUTTON, button -> {});
                    this.rbp$helpButton.setTooltip(rbp$getHelpButtonTooltip());
                }
            }
            if (Config.getEnableRecipeBrowser()) {
                if (Config.getUseCustomUI()) {
                    this.rbp$fullscreenButton = new ImageButton(xo + 119, yo + 4, 7, 18, CUSTOM_HELP_BUTTON, button -> {});
                    this.rbp$fullscreenButton.setTooltip(rbp$getHelpButtonTooltip());
                }
                else {}
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void rbp$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Config.getModEnabled()) {
            if (this.isVisible()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
                int i = (this.width - 147) / 2 - this.xOffset;
                int j = (this.height - 166) / 2;
                if (Config.getUseCustomUI()) {
                    guiGraphics.blit(BACKGROUND_IMAGE_LOCATION, i, j, 1, 1, 151, 166);
                }else {
                    guiGraphics.blit(RECIPE_BOOK_LOCATION, i, j, 1, 1, 147, 166);
                }
                ((IEditBox) this.searchBox).rbp$renderWidgetButWithoutFknShadow(guiGraphics, mouseX, mouseY, partialTick);

                for(RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
                    recipebooktabbutton.render(guiGraphics, mouseX, mouseY, partialTick);
                }
                if (Config.getDisplayHelpButton()) this.rbp$helpButton.render(guiGraphics, mouseX, mouseY, partialTick);
                this.rbp$groupButton.render(guiGraphics,mouseX,mouseY,partialTick);


                this.filterButton.render(guiGraphics, mouseX, mouseY, partialTick);
                this.recipeBookPage.render(guiGraphics, i, j, mouseX, mouseY, partialTick);
                guiGraphics.pose().popPose();
            }
            ci.cancel();
        }
    }
    //MARK: Mouse Clicked
    @Inject(method="mouseClicked",at=@At("HEAD"),cancellable = true)
    public void rbp$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Config.getModEnabled()) {

            if (this.isVisible() && !this.minecraft.player.isSpectator()) {


                ItemStack ghostItem = ((IGhostRecipeAccessor)this.ghostRecipe).getGhostItem(mouseX, mouseY);
                if (ghostItem != null && minecraft.player.containerMenu.getCarried().getItem() == Items.AIR) {
                    boolean ctrl = Screen.hasControlDown();
                    this.recipeBookPage.setInvisible();
                    switch (button) {
                        case 0: {
                            if (ctrl) {
                                rbp$search(BuiltInRegistries.ITEM.getKey(ghostItem.getItem()).toString());
                            } else {
                                rbp$search(Component.translatable(ghostItem.getDescriptionId()).getString());
                            }
                            cir.setReturnValue(true);
                            break;
                        }
                        case 1: {
                            if (ctrl) {
                                rbp$search(Config.getIngredientPrefix()+BuiltInRegistries.ITEM.getKey(ghostItem.getItem()));
                            } else {
                                rbp$search(Config.getIngredientPrefix()+Component.translatable(ghostItem.getDescriptionId()).getString());
                            }
                            cir.setReturnValue(true);
                            break;
                        }
                        case 2: {
                            rbp$search(Config.getModidPrefix()+BuiltInRegistries.ITEM.getKey(ghostItem.getItem()).getNamespace());
                            cir.setReturnValue(true);
                            break;
                        }
                    }
                } else if (this.recipeBookPage.mouseClicked(mouseX, mouseY, button, (this.width - 147) / 2 - this.xOffset, (this.height - 166) / 2, 147, 166)) {
                    this.searchBox.setFocused(false);
                    this.searchBox.moveCursorToStart(false);
                    RecipeHolder<?> recipeholder = this.recipeBookPage.getLastClickedRecipe();
                    RecipeCollection recipecollection = this.recipeBookPage.getLastClickedRecipeCollection();
                    if (recipeholder != null && recipecollection != null) {
                        if ((!recipecollection.isCraftable(recipeholder)) && this.ghostRecipe.getRecipe() == recipeholder) {
                            cir.setReturnValue(false);
                        }
                        this.ghostRecipe.clear();
                        this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, recipeholder, Screen.hasShiftDown());
                        if (!this.isOffsetNextToMainGUI()) {
                            this.setVisible(false);
                        }
                    }

                    cir.setReturnValue(true);
                } else if (this.searchBox.mouseClicked(mouseX, mouseY, button)) {
                    this.searchBox.setFocused(true);
                    cir.setReturnValue(true);
                } else {
                    this.searchBox.setFocused(false);
                    if (this.rbp$groupButton.mouseClicked(mouseX, mouseY, button)) {
                        rbp$isGrouping = !rbp$isGrouping;
                        RecipeBookPlus.groupingState = rbp$isGrouping;
                        rbp$groupButton.setTooltip(rbp$isGrouping ? Tooltip.create(Component.translatable("recipebookplus.gui.grouping")) : Tooltip.create(Component.translatable("recipebookplus.gui.not_grouping")));
                        rbp$groupButton.initTextureValues(Config.getUseCustomUI() ? CUSTOM_GROUP_BUTTON : GROUP_BUTTON);
                        rbp$groupButton.setStateTriggered(rbp$isGrouping);
                        sendUpdateSettings();
                        rbp$updateCollections(true, new CallbackInfo("updateCollections", true));
                        cir.setReturnValue(true);
                    } else if (this.filterButton.mouseClicked(mouseX, mouseY, button)) {
                        boolean flag = this.toggleFiltering();
                        this.filterButton.setStateTriggered(flag);
                        this.updateFilterButtonTooltip();
                        this.sendUpdateSettings();
                        this.updateCollections(false);
                        cir.setReturnValue(true);
                    } else {
                        for (RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
                            if (recipebooktabbutton.mouseClicked(mouseX, mouseY, button)) {
                                if (this.selectedTab != recipebooktabbutton) {
                                    if (this.selectedTab != null) {
                                        this.selectedTab.setStateTriggered(false);
                                    }

                                    this.selectedTab = recipebooktabbutton;
                                    this.selectedTab.setStateTriggered(true);
                                    this.updateCollections(true);
                                }

                                cir.setReturnValue(true);
                            }
                        }

                        cir.setReturnValue(false);
                    }
                }
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "updateTabs", at = @At("HEAD"),cancellable = true)
    private void rbp$updateTabs(CallbackInfo ci) {
        if (Config.getModEnabled()) {
            int xPosTab;
            int yPosTab;
            int yOffset;
            int index;
            if (Config.getUseCustomUI()) {
                xPosTab = (int) Math.round((this.width - 147) / 2.0) - this.xOffset - 28;
                yPosTab = (int) Math.round((this.height - 166) / 2.0) + 3;
                yOffset = 21;
                index = 0;
            } else {
                xPosTab = (int) Math.round((this.width - 147) / 2.0) - this.xOffset - 30;
                yPosTab = (int) Math.round((this.height - 166) / 2.0) + 3;
                yOffset = 27;
                index = 0;
            }

            for (RecipeBookTabButton tabButton : this.tabButtons) {

                RecipeBookCategories recipebookcategories = tabButton.getCategory();
                if (recipebookcategories != RecipeBookCategories.CRAFTING_SEARCH && recipebookcategories != RecipeBookCategories.FURNACE_SEARCH) {
                    if (tabButton.updateVisibility(this.book)) {
                        tabButton.setPosition(xPosTab, yPosTab + yOffset * index++);
                        tabButton.startAnimation(this.minecraft);
                    } else if (!Config.getRecipeDiscovery()) {
                        tabButton.visible = true;
                        tabButton.setPosition(xPosTab, yPosTab + yOffset * index++);
                        tabButton.startAnimation(this.minecraft);
                    }
                } else {
                    tabButton.visible = true;
                    tabButton.setPosition(xPosTab, yPosTab + yOffset * index++);
                }
                if (Config.getUseCustomUI()) {
                    if (tabButtons.indexOf(tabButton) == 0) ((IRecipeBookButton) tabButton).rbp$setColor(-1);
                    else if (tabButtons.indexOf(tabButton) == 1 /*&& Config.getFavoriteEnabled()*/) ((IRecipeBookButton) tabButton).rbp$setColor(0);
                    else ((IRecipeBookButton) tabButton).rbp$setColor(1);
                }
            }
            ci.cancel();
        }
    }

    @Unique
    private Tooltip rbp$getHelpButtonTooltip() {
        return Tooltip.create(Component.translatable(
                "recipebookplus.gui.help_tooltip",
                Component.literal(Config.getIngredientPrefix()).withStyle(ChatFormatting.GOLD),
                Component.literal(Config.getModidPrefix()).withStyle(ChatFormatting.GOLD),
                Component.keybind("recipebookplus.keymapping.recipe").withStyle(ChatFormatting.GREEN),
                Component.keybind("recipebookplus.keymapping.usage").withStyle(ChatFormatting.GREEN),
                Component.keybind("recipebookplus.keymapping.mod").withStyle(ChatFormatting.GREEN)
        ));
    }

    @Unique
    @Override
    public ItemStack rbp$getGhostItemStack(double mouseX, double mouseY) {
        return ((IGhostRecipeAccessor) this.ghostRecipe).getGhostItem(mouseX, mouseY);
    }

    // endregion
}
