package com.neoloxal.neospuppets.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.neoloxal.neospuppets.NeosPuppets;

public record SowingTextPacket(String text) implements CustomPacketPayload {

    public static final Type<SowingTextPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NeosPuppets.MODID, "sowing_text"));

    public static final StreamCodec<ByteBuf, SowingTextPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SowingTextPacket::text,
                    SowingTextPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}