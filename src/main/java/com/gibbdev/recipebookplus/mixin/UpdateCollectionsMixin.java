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
import net.neoforged.fml.loading.FMLConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
        String s = this.searchBox.getValue();
        if (!s.isEmpty()) {
            ClientPacketListener clientpacketlistener = minecraft.getConnection();
            if (clientpacketlistener != null) {
                List<RecipeCollection> tempList = Lists.newArrayList(list1);
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
        }

        if (this.book.isFiltering(this.menu)) {
            list1.removeIf((p_100331_) -> !p_100331_.hasCraftable());
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

}
