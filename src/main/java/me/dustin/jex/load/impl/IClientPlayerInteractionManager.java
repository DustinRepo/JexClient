package me.dustin.jex.load.impl;

public interface IClientPlayerInteractionManager {

    void setBlockBreakProgress(float progress);
    void setBlockBreakingCooldown(int cooldown);
    float getBlockBreakProgress();
    int getBlockBreakingCooldown();
}
