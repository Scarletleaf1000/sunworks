package me.scarletleaf1000.sunworks.multiblocks.ports;

import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

public interface IEnergyPortHost {
    @NotNull IEnergyStorage getMultiblockEnergy();
}
