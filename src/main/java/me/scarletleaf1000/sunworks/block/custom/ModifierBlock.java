package me.scarletleaf1000.sunworks.block.custom;

import net.minecraft.world.level.block.Block;

public class ModifierBlock extends Block {

    public final int delayAddition;
    public final int generationAddition;
    public final float delayMult;
    public final float generationMult;

    public ModifierBlock(Properties properties, int delayAddition, int generationAddition, float delayMult, float generationMult) {
        super(properties);
        this.delayAddition = delayAddition;
        this.generationAddition = generationAddition;
        this.delayMult = delayMult;
        this.generationMult = generationMult;
    }

}
