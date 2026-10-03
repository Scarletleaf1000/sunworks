package me.scarletleaf1000.sunworks.beams;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BeamType<T extends AbstractBeam>(
        StreamCodec<RegistryFriendlyByteBuf, T> codec
) {}
