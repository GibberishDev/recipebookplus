package com.gibbdev.recipebookplus.recipediscovery;

import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.interfaces.IRecipeDiscovery;
import com.gibbdev.recipebookplus.networking.ModStatus;
import com.gibbdev.recipebookplus.networking.RecipeDiscoveryPayloads;
import com.gibbdev.recipebookplus.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class RecipeDiscovery {
    /**Reference to client saved data. May be {@link RecipeDiscoveryLocalData} if dedicated server appears to not have mod installed, otherwise its {@link RecipeDiscoverySavedData}*/
    public static IRecipeDiscovery CLIENT_INSTANCE = null;
    /**Reference to server saved data. Instance of {@link RecipeDiscoverySavedData}*/
    public static IRecipeDiscovery SERVER_INSTANCE = null;
    /**Marks current state of RecipeDiscovery Instance to respect server authority*/
    private static boolean serverAuthored = false;
    /**Marks current state of RecipeDiscovery Instance as client side*/
    private static boolean isClient = false;
    private static MinecraftServer mcServer;

    /**
     * Called from client LoggingIn event
     */
    public static void initClient() {
        isClient = true;
        resetClient();
        if (Minecraft.getInstance().isSingleplayer() && Minecraft.getInstance().getSingleplayerServer() != null) {
            serverAuthored = false;
            if (Minecraft.getInstance().getSingleplayerServer().getLevel(Level.OVERWORLD) != null) {
                CLIENT_INSTANCE = RecipeDiscoverySavedData.get(Objects.requireNonNull(Minecraft.getInstance().getSingleplayerServer().getLevel(Level.OVERWORLD)));
            } else throw new RuntimeException("NO LEVEL DUMBASS");
        } else {
            serverAuthored = ModStatus.getInstalled();
            CLIENT_INSTANCE = new RecipeDiscoveryLocalData(getServerID());
        }
        if (!serverAuthored && Minecraft.getInstance().level != null) {
            assert Minecraft.getInstance().getSingleplayerServer() != null;
            RecipeLookup.rebuild(Minecraft.getInstance().getSingleplayerServer().getRecipeManager(), Minecraft.getInstance().getSingleplayerServer().registryAccess());
        }
    }

    /**
     * Initializes {@link #SERVER_INSTANCE} that contains KnownRecipe data for all players when Dedicated Server starts.
     * @param server {@link MinecraftServer} reference to current server
     */
    public static void initServer(MinecraftServer server) {
        isClient = false;
        mcServer = server;
        if (server.getLevel(Level.OVERWORLD) == null) throw new RuntimeException("NO LEVEL DUMBASS");
        SERVER_INSTANCE = RecipeDiscoverySavedData.get(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)));
    }

    public static void resetClient() {
        if (CLIENT_INSTANCE != null) {
            if (CLIENT_INSTANCE instanceof RecipeDiscoveryLocalData && !serverAuthored && !Minecraft.getInstance().isSingleplayer()) ((RecipeDiscoveryLocalData) CLIENT_INSTANCE).save();
        }
        CLIENT_INSTANCE = null;
    }

    /**
     * @return Current server IP converted to SHA256 string. Used only for saving local data in case Server does not have mod installed.
     */
    @Nullable
    private static String getServerID() {
        if (Minecraft.getInstance().isSingleplayer()) return null;
        if (Minecraft.getInstance().getCurrentServer() == null) return null;
        String stringIP = Minecraft.getInstance().getCurrentServer().ip;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stringIP.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void processPacket(CompoundTag tag) {
        if (Minecraft.getInstance().player != null) {
            serverAuthored = true;
            ListTag recipesListTag = (ListTag) tag.get("recipes");
            RecipeDiscoveryLocalData.knownRecipes.clear();
            assert recipesListTag != null;
            recipesListTag.forEach((s)->RecipeDiscoveryLocalData.knownRecipes.add(s.getAsString()));
        }
    }


    public static List<RecipeHolder<?>> getKnownHolders(UUID uuid) {
        if (isClient && CLIENT_INSTANCE!=null) {
            Constants.LOG.info("CLIENT: "+CLIENT_INSTANCE.getKnownHolders(uuid).toString());
            return CLIENT_INSTANCE.getKnownHolders(uuid);
        }
        else if (SERVER_INSTANCE!=null) {
            return SERVER_INSTANCE.getKnownHolders(uuid);
        }
        return new ArrayList<>();
    }

    public static boolean hasAll(List<String> ids, UUID uuid) {
        if (isClient && CLIENT_INSTANCE!=null) return CLIENT_INSTANCE.hasAll(ids, uuid);
        else if (SERVER_INSTANCE!=null) return SERVER_INSTANCE.hasAll(ids, uuid);
        return false;
    }

    public static boolean isKnown(String id, UUID uuid) {
        if (isClient && CLIENT_INSTANCE!=null) return CLIENT_INSTANCE.isKnown(id, uuid);
        else if (SERVER_INSTANCE!=null) return SERVER_INSTANCE.isKnown(id, uuid);
        return false;
    }

    public static boolean giveRecipe(String id, UUID uuid) {
        if (isClient) {
            return CLIENT_INSTANCE.giveRecipe(id, uuid);
        } else if (SERVER_INSTANCE!=null) {
            if (SERVER_INSTANCE.giveRecipe(id, uuid)) {
                Services.PLATFORM.sendPayloadToClient(mcServer.getPlayerList().getPlayer(uuid), new RecipeDiscoveryPayloads.RDGrantOneRecipe(id));
                return true;
            }
        }
        return false;
    }

    public static List<String> giveRecipe(List<String> ids, UUID uuid) {
        if (isClient) {
            return ids.stream().filter(id -> giveRecipe(id, uuid)).toList();
        } else if (SERVER_INSTANCE!=null) {
            CompoundTag idList = new CompoundTag();
            ListTag recipes = new ListTag();
            ids.stream().map(StringTag::valueOf).forEach(recipes::add);
            idList.put("recipes", recipes);
            Services.PLATFORM.sendPayloadToClient(mcServer.getPlayerList().getPlayer(uuid), new RecipeDiscoveryPayloads.RDGrantRecipeList(idList));
            return ids.stream().filter(id->SERVER_INSTANCE.giveRecipe(id, uuid)).toList();
        }
        return new ArrayList<>();
    }

    public static boolean takeRecipe(String id, UUID uuid) {
        if (isClient && CLIENT_INSTANCE!=null) {
            return CLIENT_INSTANCE.takeRecipe(id, uuid);
        } else if (SERVER_INSTANCE!=null) {
            if (SERVER_INSTANCE.takeRecipe(id, uuid)) {
                Services.PLATFORM.sendPayloadToClient(mcServer.getPlayerList().getPlayer(uuid), new RecipeDiscoveryPayloads.RDTakeOneRecipe(id));
                return true;
            }
        }
        return false;
    }

    public static List<String> takeRecipe(List<String> ids, UUID uuid) {
        if (isClient && CLIENT_INSTANCE!=null) {
            return ids.stream().filter(id -> takeRecipe(id, uuid)).toList();
        } else if (SERVER_INSTANCE!=null) {
            CompoundTag idList = new CompoundTag();
            ListTag recipes = new ListTag();
            ids.stream().map(StringTag::valueOf).forEach(recipes::add);
            idList.put("recipes", recipes);
            Services.PLATFORM.sendPayloadToClient(mcServer.getPlayerList().getPlayer(uuid), new RecipeDiscoveryPayloads.RDTakeRecipeList(idList));
            return ids.stream().filter(id->SERVER_INSTANCE.takeRecipe(id, uuid)).toList();
        }
        return new ArrayList<>();
    }

    public static void save() {
        if (!serverAuthored && CLIENT_INSTANCE != null && !Minecraft.getInstance().isSingleplayer() && CLIENT_INSTANCE instanceof RecipeDiscoveryLocalData) {
            ((RecipeDiscoveryLocalData) CLIENT_INSTANCE).save();
        }
    }
}
