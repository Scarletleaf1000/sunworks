package me.scarletleaf1000.sunworks.event;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Sunworks.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ChorusFruitTransformHandler {

    @SubscribeEvent
    public static void onItemEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity entity)) {
            return;
        }

        if (entity.isRemoved()) {
            return;
        }

        ItemStack stack = entity.getItem();
        if (stack.isEmpty() || !stack.is(Items.CHORUS_FRUIT)) {
            return;
        }

        Level level = entity.level();
        if (level.isClientSide()) {
            return;
        }

        BlockPos pos = entity.blockPosition();
        if (!level.getBlockState(pos).is(Blocks.WITHER_ROSE)) {
            return;
        }

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        var motion = entity.getDeltaMovement();

        stack.shrink(1);
        entity.setItem(stack);

        boolean wasEmpty = stack.isEmpty();
        if (wasEmpty) {
            entity.discard();
        }

        if (level.getRandom().nextBoolean()) {
            ItemStack husk = new ItemStack(ModItems.CHORUS_HUSK.get(), 1);
            ItemEntity huskEntity = new ItemEntity(level, x, y, z, husk);
            huskEntity.setDeltaMovement(motion);
            level.addFreshEntity(huskEntity);
        }

        spawnEffects(level, x, y, z);
    }

    private static void spawnEffects(Level level, double x, double y, double z) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, x, y + 0.25, z, 4, 0.15, 0.1, 0.15, 0.02);
            serverLevel.sendParticles(ParticleTypes.WITCH, x, y + 0.25, z, 3, 0.15, 0.1, 0.15, 0.05);
        }
        level.playSound(null, x, y + 0.5, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F + level.getRandom().nextFloat() * 0.4F);
    }
}
