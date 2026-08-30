package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.CommonClass;
import com.gibbdev.recipebookplus.Config;
import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.compat.FarmersDelight;
import com.gibbdev.recipebookplus.interfaces.IEditBox;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookButton;
import com.gibbdev.recipebookplus.interfaces.IRecipeBookComponent;
import com.gibbdev.recipebookplus.platform.Services;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private boolean widthTooNarrow;
    @Shadow @Final
    private static Component ONLY_CRAFTABLES_TOOLTIP;
    @Shadow @Final
    private static Component ALL_RECIPES_TOOLTIP;

    @Shadow
    public abstract boolean isVisible();
    @Shadow
    protected abstract void updateCollections(boolean resetPageNumber);
    @Shadow
    protected abstract void updateTabs();
    @Shadow
    protected abstract void sendUpdateSettings();
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
        if (Config.INSTANCE.getModEnabled()) {
            minecraft.player.sendSystemMessage(Component.literal(Config.INSTANCE.getRecipeDiscoveryMode().toString()));
//            RecipeManager manager = minecraft.player.level().getRecipeManager();
//            List<RecipeHolder<?>> holders = manager.getRecipes().stream().filter(holder ->
//                minecraft.player.getRecipeBook().contains(holder)
//            ).toList();
//            List<String> allTypes = new ArrayList<>();
//            List<RecipeCollection> allCollections = minecraft.player.level().getRecipeManager().getRecipes();
//            allCollections.addAll(book.getCollections());

//            for (RecipeHolder<?> holder : holders) {
//                if (holder != null) {
//                    Recipe<?> recipe = holder.value();
//                    String type = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()).toString();
//                    if (!allTypes.contains(type)) {
//                        allTypes.add(type);
//                        minecraft.player.sendSystemMessage(Component.literal(type));
//                    }
//                }
//            }

            List<RecipeCollection> list = this.book.getCollection(this.selectedTab.getCategory());
            list.forEach(c -> c.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book));
            List<RecipeCollection> list1 = Lists.newArrayList(list);
            list1.removeIf(c -> !c.hasKnownRecipes());
            list1.removeIf(c -> !c.hasFitting());
            String s = this.searchBox.getValue();
            list1 = rbp$searchCollectionList(list1, s);

//            if (list1.isEmpty()) return;

            if (this.book.isFiltering(this.menu)) {
                list1.removeIf(c -> !c.hasCraftable());
            }
            this.recipeBookPage.updateCollections(list1, resetPage);
            ci.cancel();
        }
    }


    @Unique
    private List<RecipeCollection> rbp$searchCollectionList(List<RecipeCollection> list, String searchTerm) {
        if (minecraft.level == null) return list;
        searchTerm = searchTerm.toLowerCase(Locale.ROOT);
        RegistryAccess ra = minecraft.level.registryAccess();
        List<RecipeCollection> tempList = Lists.newArrayList(list);
        rbp$isGrouping = CommonClass.groupingState;
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

    @Inject(method = "initVisuals", at = @At("HEAD"),cancellable = true)
    public void initVisuals(CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && minecraft.player != null) {
            rbp$isGrouping=CommonClass.groupingState;
            this.xOffset = this.widthTooNarrow ? 0 : 86;
            int xo = (int) Math.round((this.width - 147) / 2.0) - this.xOffset;
            int yo = (int) Math.round((this.height - 166) / 2.0);
            if (Config.INSTANCE.getUseCustomUI()) {
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

                this.rbp$groupButton = new StateSwitchingButton(xo + 127, yo + 4, 7, 18, rbp$isGrouping);
                this.rbp$groupButton.setTooltip(rbp$isGrouping?Tooltip.create(Component.translatable("recipebookplus.gui.grouping")):Tooltip.create(Component.translatable("recipebookplus.gui.not_grouping")));
                this.rbp$groupButton.initTextureValues(CUSTOM_GROUP_BUTTON);

                if (Config.INSTANCE.getDisplayHelpButton()) {
                    this.rbp$helpButton = new ImageButton(xo + 119, yo + 4, 7, 18, CUSTOM_HELP_BUTTON, a -> rbp$helpButton());
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

                if (Config.INSTANCE.getDisplayHelpButton()) {
                    this.rbp$helpButton = new ImageButton(xo + 110, yo + 139, 26, 16, HELP_BUTTON, a -> rbp$helpButton());
                    this.rbp$helpButton.setTooltip(rbp$getHelpButtonTooltip());
                }
            }
            if (Config.INSTANCE.getEnableRecipeBrowser()) {
                if (Config.INSTANCE.getUseCustomUI()) {
                    this.rbp$fullscreenButton = new ImageButton(xo + 119, yo + 4, 7, 18, CUSTOM_HELP_BUTTON, a -> rbp$helpButton());
                    this.rbp$fullscreenButton.setTooltip(rbp$getHelpButtonTooltip());
                }
                else {}
            }
        }
    }


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
                ((IEditBox) this.searchBox).rbp$renderWidgetButWithoutFknShadow(guiGraphics, mouseX, mouseY, partialTick);

                for(RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
                    recipebooktabbutton.render(guiGraphics, mouseX, mouseY, partialTick);
                }
                if (Config.INSTANCE.getDisplayHelpButton()) this.rbp$helpButton.render(guiGraphics, mouseX, mouseY, partialTick);
                this.rbp$groupButton.render(guiGraphics,mouseX,mouseY,partialTick);


                this.filterButton.render(guiGraphics, mouseX, mouseY, partialTick);
                this.recipeBookPage.render(guiGraphics, i, j, mouseX, mouseY, partialTick);
                guiGraphics.pose().popPose();
            }
            ci.cancel();
        }
    }

    @Inject(method="mouseClicked",at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/components/EditBox;setFocused(Z)V"),cancellable = true)
    public void rbp$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.getModEnabled()) {
            if (this.rbp$groupButton.mouseClicked(mouseX, mouseY, button)) {
                rbp$isGrouping = !rbp$isGrouping;
                CommonClass.groupingState = rbp$isGrouping;
                rbp$groupButton.setTooltip(rbp$isGrouping ? Tooltip.create(Component.translatable("recipebookplus.gui.grouping")) : Tooltip.create(Component.translatable("recipebookplus.gui.not_grouping")));
                rbp$groupButton.initTextureValues(Config.INSTANCE.getUseCustomUI() ? CUSTOM_GROUP_BUTTON : GROUP_BUTTON);
                rbp$groupButton.setStateTriggered(rbp$isGrouping);
                sendUpdateSettings();
                rbp$updateCollections(true, new CallbackInfo("updateCollections", true));
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method="mouseClicked",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;getLastClickedRecipe()Lnet/minecraft/world/item/crafting/RecipeHolder;"))
    public void rbp$mouseClickedUnfocusSearchbar(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.getModEnabled()) {
            this.searchBox.setFocused(false);
            this.searchBox.moveCursorToStart(false);
        }
    }

    @Inject(method = "updateTabs", at = @At("HEAD"),cancellable = true)
    private void rbp$updateTabs(CallbackInfo ci) {
        if (Config.INSTANCE.getModEnabled() && Config.INSTANCE.getUseCustomUI()) {
            int xPosTab = (int) Math.round((this.width - 147) / 2.0) - this.xOffset - 28;
            int yPosTab = (int) Math.round((this.height - 166) / 2.0) + 3;
            int yOffset = 21;
            int index = 0;

            for (RecipeBookTabButton tabButton : this.tabButtons) {
                RecipeBookCategories recipebookcategories = tabButton.getCategory();
                if (recipebookcategories != RecipeBookCategories.CRAFTING_SEARCH && recipebookcategories != RecipeBookCategories.FURNACE_SEARCH) {
                    if (tabButton.updateVisibility(this.book)) {
                        tabButton.setPosition(xPosTab, yPosTab + yOffset * index++);
                        tabButton.startAnimation(this.minecraft);
                    }
                } else {
                    tabButton.visible = true;
                    tabButton.setPosition(xPosTab, yPosTab + yOffset * index++);
                }
                ((IRecipeBookButton) tabButton).rbp$setColor((int) Math.round(Math.random() * 2) + 1);
            }
            ci.cancel();
        }
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
    // endregion
}
