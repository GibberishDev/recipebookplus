package com.gibbdev.recipebookplus.platform;

import com.gibbdev.recipebookplus.Constants;
import com.gibbdev.recipebookplus.networking.ModStatus;
import com.gibbdev.recipebookplus.networking.ServerHandshakePayloads;
import com.gibbdev.recipebookplus.platform.services.IPlatformHelper;
import com.mojang.datafixers.kinds.Const;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void getServerModVersion() {
        if (Minecraft.getInstance().getConnection() != null) {
            if (!ClientPlayNetworking.canSend(ServerHandshakePayloads.HANDSHAKE_TYPE)) {
                ModStatus.reset();
                return;
            }
        }
        ClientPlayNetworking.send(new ServerHandshakePayloads.Handshake());
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(Constants.MOD_ID).map(c->c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }

    @Override
    public Path getInstanceDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public void sendPayloadToClient(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
