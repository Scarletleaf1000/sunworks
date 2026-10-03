package me.scarletleaf1000.sunworks.block.custom.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.multiblocks.ports.CapabilityPortBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyPortBlock extends CapabilityPortBlock {

    public final int ioRate;

    public EnergyPortBlock(Properties properties, int ioRate) {
        super(properties);
        this.ioRate = ioRate;
    }

    @Override
    public BlockEntityType<?> getBlockEntityType() {
        return ModBlockEntities.ENERGY_PORT_BE.get();
    }

    @Override
    protected MapCodec<? extends EnergyPortBlock> codec() {
        return RecordCodecBuilder.mapCodec(e -> e.group(
                propertiesCodec(),
                Codec.INT.fieldOf("ioRate").forGetter(port -> port.ioRate)
        ).apply(e, EnergyPortBlock::new));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
