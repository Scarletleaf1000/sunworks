package me.scarletleaf1000.sunworks.entity.projectile;

import me.scarletleaf1000.sunworks.entity.ModEntityTypes;
import me.scarletleaf1000.sunworks.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ThrownCorruptedPearl extends ThrowableItemProjectile {

    public ThrownCorruptedPearl(EntityType<ThrownCorruptedPearl> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownCorruptedPearl(Level level, LivingEntity owner) {
        super(ModEntityTypes.CORRUPTED_PEARL.get(), owner, level);
    }

    public ThrownCorruptedPearl(Level level, double x, double y, double z) {
        super(ModEntityTypes.CORRUPTED_PEARL.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CORRUPTED_PEARL.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (this.level().isClientSide()) {
            return;
        }

        Entity hit = result.getEntity();
        Entity owner = this.getOwner();
        if (owner == null || hit == owner) {
            return;
        }

        double sourceX = hit.getX();
        double sourceY = hit.getY();
        double sourceZ = hit.getZ();

        double destX = owner.getX();
        double destY = owner.getY();
        double destZ = owner.getZ();

        teleportEntity(hit, destX, destY, destZ);

        Level level = owner.level();
        spawnTeleportEffects(level, destX, destY + owner.getBbHeight() / 2.0, destZ);
        spawnTeleportEffects(level, sourceX, sourceY + hit.getBbHeight() / 2.0, sourceZ);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level().isClientSide) return;

        this.discard();
    }

    private static void teleportEntity(Entity entity, double x, double y, double z) {
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(x, y, z);
        } else {
            entity.teleportTo(x, y, z);
        }
    }

    private static void spawnTeleportEffects(Level level, double x, double y, double z) {
        level.playSound(null, x, y, z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 32, 0.25, 0.5, 0.25, 0.15);
        }
    }
}
