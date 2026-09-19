package com.gibbdev.recipebookplus.networking;

import com.gibbdev.recipebookplus.RecipeBookPlus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeDiscoveryPayloads {
    public static final ResourceLocation RL_RD_PLAYER_RECIPE_DATA = RecipeBookPlus.rl("player_recipe_data_payload");
    public static final ResourceLocation RL_RD_GRANT_ONE_RECIPE = RecipeBookPlus.rl("grant_one_recipe_payload");
    public static final ResourceLocation RL_RD_GRANT_RECIPE_LIST = RecipeBookPlus.rl("grant_recipe_list_payload");
    public static final ResourceLocation RL_RD_TAKE_ONE_RECIPE = RecipeBookPlus.rl("take_one_recipe_payload");
    public static final ResourceLocation RL_RD_TAKE_RECIPE_LIST = RecipeBookPlus.rl("take_recipe_list_payload");

    public static final CustomPacketPayload.Type<RDPlayerData> TYPE_RD_PLAYER_RECIPE_DATA =
            new CustomPacketPayload.Type<>(RL_RD_PLAYER_RECIPE_DATA);
    public static final CustomPacketPayload.Type<RDGrantOneRecipe> TYPE_RD_GRANT_ONE_RECIPE =
            new CustomPacketPayload.Type<>(RL_RD_GRANT_ONE_RECIPE);
    public static final CustomPacketPayload.Type<RDGrantRecipeList> TYPE_RD_GRANT_RECIPE_LIST =
            new CustomPacketPayload.Type<>(RL_RD_GRANT_RECIPE_LIST);
    public static final CustomPacketPayload.Type<RDTakeOneRecipe> TYPE_RD_TAKE_ONE_RECIPE =
            new CustomPacketPayload.Type<>(RL_RD_TAKE_ONE_RECIPE);
    public static final CustomPacketPayload.Type<RDTakeRecipeList> TYPE_RD_TAKE_RECIPE_LIST =
            new CustomPacketPayload.Type<>(RL_RD_TAKE_RECIPE_LIST);

    public record RDPlayerData(CompoundTag tag) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, RDPlayerData> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.COMPOUND_TAG, RDPlayerData::tag,
                        RDPlayerData::new
                );
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE_RD_PLAYER_RECIPE_DATA;}
    }
    public record RDGrantOneRecipe(String id) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, RDGrantOneRecipe> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, RDGrantOneRecipe::id,
                        RDGrantOneRecipe::new
                );
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE_RD_GRANT_ONE_RECIPE;}
    }
    public record RDGrantRecipeList(CompoundTag tag) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, RDGrantRecipeList> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.COMPOUND_TAG, RDGrantRecipeList::tag,
                        RDGrantRecipeList::new
                );
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE_RD_GRANT_RECIPE_LIST;}
    }
    public record RDTakeOneRecipe(String id) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, RDTakeOneRecipe> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, RDTakeOneRecipe::id,
                        RDTakeOneRecipe::new
                );
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE_RD_TAKE_ONE_RECIPE;}
    }
    public record RDTakeRecipeList(CompoundTag tag) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, RDTakeRecipeList> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.COMPOUND_TAG, RDTakeRecipeList::tag,
                        RDTakeRecipeList::new
                );
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE_RD_TAKE_RECIPE_LIST;}
    }

    public static List<String> getListFromTag(CompoundTag tag) {
        List<String> list = new ArrayList<>();
        ((ListTag) tag.get("recipes")).forEach((str) -> list.add(str.getAsString()));
        return list;
    }

    public static CompoundTag getTagFromList(List<String> list) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        list.stream().map(StringTag::valueOf).forEach(listTag::add);
        tag.put("recipes", listTag);
        return tag;
    }
}
