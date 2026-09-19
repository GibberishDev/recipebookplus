package com.gibbdev.recipebookplus.networking;

import com.gibbdev.recipebookplus.platform.Services;
import com.gibbdev.recipebookplus.recipediscovery.RecipeDiscovery;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ModStatus {
    private static final int HANDSHAKE_TIMEOUT_MS = 1000;
    private static boolean installed = false;
    private static String version = null;
    private static CompletableFuture<Boolean> handshakeFuture;

    public static void reset() {
        installed = false;
        version = null;
    }
    public static void setVersion(String serverVersion) {
        installed = true;
        version = serverVersion;
        if (handshakeFuture != null) {
            handshakeFuture.complete(true);
            handshakeFuture = null;
        }
    }

    public static boolean getInstalled() {
        return installed;
    }
    public static String getVersion() {
        return version;
    }

    public static void pingServer(Runnable next) {
        reset();
        handshakeFuture = new CompletableFuture<>();
        Services.PLATFORM.getServerModVersion();
        handshakeFuture
                .orTimeout(1, TimeUnit.SECONDS)
                .exceptionally(err->false)
                .thenAccept(installed -> Minecraft.getInstance().execute(next));
    }
}
