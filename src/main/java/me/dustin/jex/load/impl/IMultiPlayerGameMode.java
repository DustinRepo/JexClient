package me.dustin.jex.load.impl;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;

public interface IMultiPlayerGameMode {

    void setBlockBreakProgress(float progress);
    void setBlockBreakingCooldown(int cooldown);
    float getBlockBreakProgress();
    int getBlockBreakingCooldown();
    BlockPos currentBreakingPos();
    void setClientPacketListener(ClientPacketListener clientGamePacketListener);
}
