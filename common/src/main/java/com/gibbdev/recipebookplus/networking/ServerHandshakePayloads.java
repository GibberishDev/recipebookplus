package com.gibbdev.recipebookplus.networking;

import com.gibbdev.recipebookplus.RecipeBookPlus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ServerHandshakePayloads {

    public static final ResourceLocation HANDSHAKE_ID =
            RecipeBookPlus.rl("server_handshake");
    public static final ResourceLocation HANDSHAKE_RESPONSE =
            RecipeBookPlus.rl("server_response");

    public static final CustomPacketPayload.Type<Handshake> HANDSHAKE_TYPE =
            new CustomPacketPayload.Type<>(HANDSHAKE_ID);
    public static final CustomPacketPayload.Type<HandshakeResponse> RESPONSE_TYPE =
            new CustomPacketPayload.Type<>(HANDSHAKE_RESPONSE);

    public record Handshake() implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, Handshake> CODEC =
                StreamCodec.unit(new Handshake());
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return HANDSHAKE_TYPE;
        }
    }

    public record HandshakeResponse(String version) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, HandshakeResponse> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, HandshakeResponse::version,
                        HandshakeResponse::new
                    );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return RESPONSE_TYPE;
        }
    }

    private ServerHandshakePayloads() {}
}
