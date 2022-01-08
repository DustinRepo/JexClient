package me.dustin.jex.load.impl;

import net.minecraft.util.math.BlockPos;

public interface IClientPlayerInteractionManager {

    void setBlockBreakProgress(float progress);
    void setBlockBreakingCooldown(int cooldown);
    float getBlockBreakProgress();
    int getBlockBreakingCooldown();
    BlockPos currentBreakingPos();
}
