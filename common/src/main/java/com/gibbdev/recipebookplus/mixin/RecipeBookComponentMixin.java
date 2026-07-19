package com.gibbdev.recipebookplus.mixin;

import com.gibbdev.recipebookplus.Config;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
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
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
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
public abstract class RecipeBookComponentMixin {

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
    protected void sendUpdateSettings(){};

    @Shadow
    protected abstract void updateCollections(boolean resetPage, boolean isFiltering);
    @Shadow
    abstract int getXOrigin();
    @Shadow
    abstract int getYOrigin();

    /**
     * @Mixin {@link RecipeBookComponent#updateCollections(boolean, boolean)}
     */
    @Inject(at=@At("HEAD"),method = "updateCollections",cancellable = true)
    private void rbp$updateCollections(boolean resetPage, boolean isFiltering, CallbackInfo ci) {
        ci.cancel();
        List<RecipeCollection> tabCollection = this.book.getCollection(this.selectedTab.getCategory());
        List<RecipeCollection> collection = Lists.newArrayList(tabCollection);
        collection.removeIf((c) -> !c.hasAnySelected());
        String searchTerm = this.searchBox.getValue();

        List<RecipeCollection> tempCollectionList = List.copyOf(collection);

        if (!rbp$isGrouping) {
            for (RecipeCollection c : tempCollectionList) {
                if (c.getSelectedRecipes(RecipeCollection.CraftableStatus.ANY).size() >= 2) {
                    for (RecipeDisplayEntry recipe : c.getSelectedRecipes(RecipeCollection.CraftableStatus.ANY)) {
                        RecipeCollection newCollection = new RecipeCollection(List.of(recipe));
                        newCollection.selectRecipes(stackedContents,recipeDisplay -> true);
                        collection.add(collection.indexOf(c),newCollection);
                    }
                    collection.remove(c);
                }
            }
        }

        if (searchTerm.startsWith(Config.INSTANCE.getIngredientPrefix()) && !searchTerm.equals(Config.INSTANCE.getIngredientPrefix())) {
            searchTerm = searchTerm.replaceFirst(Matcher.quoteReplacement(Config.INSTANCE.getIngredientPrefix()),"");
            List<ItemStack> searchItems = rbp$searchItems(searchTerm);
            tempCollectionList = List.copyOf(collection);
            for (RecipeCollection c : tempCollectionList) {
                List<RecipeDisplayEntry> validRecipes = new ArrayList<>();
                for (RecipeDisplayEntry recipe : c.getSelectedRecipes(RecipeCollection.CraftableStatus.ANY)) {
                    Optional<List<Ingredient>> ingredients = recipe.craftingRequirements();
                    boolean ingredientFound = false;
                    if (!ingredients.isEmpty()) {
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
                    newCollection.selectRecipes(stackedContents,recipeDisplay -> true);
                    collection.add(collection.indexOf(c),newCollection);
                }
                collection.remove(c);
            }
        } else if (!searchTerm.isEmpty()) {
            ClientPacketListener connection = this.minecraft.getConnection();
            if (connection != null) {
                ObjectSet<RecipeCollection> set = new ObjectLinkedOpenHashSet(connection.searchTrees().recipes().search(searchTerm.toLowerCase(Locale.ROOT)));
                collection.removeIf((e) -> !set.contains(e));
            }
        }
        if (isFiltering) {
            collection.removeIf((c) -> !c.hasCraftable());
        }

        this.recipeBookPage.updateCollections(collection, resetPage, isFiltering);
    }

    /**
     * Initialises aditional two ui elements - Help button (usage tooltip) and Group button
     * @Mixin {@link RecipeBookComponent#initVisuals()}
     */
    @Inject(method = "initVisuals",at = @At("TAIL"))
    public void recipebookplus_initVisuals(CallbackInfo ci) {
//        if (Config.MOD_ENABLED.get()) {
            int i = getXOrigin();
            int j = getYOrigin();
            //I DONT CARE THAT I USE BUTTON FOR THE STATIC TEXTURE. SUE ME
            this.rbp$helpButton = new ImageButton(i + 110,j + 139,26,16,HELP_BUTTON, fuck -> rbp$helpButton());
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
                (button, value) -> {
                    rbp$isGrouping = !rbp$isGrouping;
                    sendUpdateSettings();
                    updateCollections(true, book.isFiltering(menu.getRecipeBookType()));
            });
//        }

    }

    /**
     * Injects right before rendering craftable filter toggle button to render 2 new windgets
     * @Mixin {@link RecipeBookComponent#extractRenderState(GuiGraphicsExtractor, int, int, float)}
     */
    @Inject(method="extractRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/CycleButton;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    public void recipebookplus_render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
//        if (Config.MOD_ENABLED.get()) {
            this.rbp$helpButton.extractRenderState(guiGraphics,mouseX,mouseY,partialTick);
            this.rbp$groupButton.extractRenderState(guiGraphics,mouseX,mouseY,partialTick);
//        }
    }

    /**
     * Injects right before procesing the input of recipe book elements and hooks in processiong for the grouping button
     * @Mixin {@link RecipeBookComponent#mouseClicked(net.minecraft.client.input.MouseButtonEvent, boolean)}
     */
    @Inject(method="mouseClicked",at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/components/EditBox;setFocused(Z)V"),cancellable = true)
    public void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
//        if (Config.MOD_ENABLED.get()) {
            if (this.rbp$groupButton.mouseClicked(event, doubleClick)) {
                cir.setReturnValue(true);
            }
//        }
    }


    @Unique
    private  List<ItemStack> rbp$searchItems(String term) {
        String namespace = rbp$getNamespace(term);
        if (!namespace.isEmpty()) {
            return BuiltInRegistries.ITEM.stream().filter(item -> {
                return BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT).contains(term);
            }).map(ItemStack::new).toList();
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

}
