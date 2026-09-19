package com.gibbdev.recipebookplus.platform;

import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.networking.ModStatus;
import com.gibbdev.recipebookplus.networking.ServerHandshakePayloads;
import com.gibbdev.recipebookplus.platform.services.IPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.nio.file.Path;
import java.util.List;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public void getServerModVersion() {
        if (Minecraft.getInstance().getConnection() != null) {
            if (!Minecraft.getInstance().getConnection().hasChannel(ServerHandshakePayloads.HANDSHAKE_TYPE.id())) {
                ModStatus.reset();
                return;
            }
        }
        PacketDistributor.sendToServer(new ServerHandshakePayloads.Handshake());
    };

    @Override
    public String getModVersion() {
        return ModList.get().getModContainerById(Constants.MOD_ID).orElseThrow().getModInfo().getVersion().toString();
    }

    @Override
    public Path getInstanceDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public void sendPayloadToClient(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

}