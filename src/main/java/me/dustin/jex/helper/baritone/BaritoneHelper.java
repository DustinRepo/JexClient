package me.dustin.jex.helper.baritone;

import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

public enum BaritoneHelper {
    INSTANCE;

    public void pause() {
        if (baritoneExists()) {
            UnsafeBaritoneHelper.pauseProcess.pause(true);
        }
    }

    public void resume() {
        if (baritoneExists()) {
            UnsafeBaritoneHelper.pauseProcess.pause(false);
        }
    }

    public void gotoLocation(BlockPos pos) {
        if (baritoneExists()) {
            UnsafeBaritoneHelper.gotoLocation(pos);
        }
    }

    public void sendCommand(String command) {
        if (baritoneExists())
            UnsafeBaritoneHelper.sendCommand(command);
    }

    public boolean baritoneExists() {
        //baritoe because fabritone misspells it
        return FabricLoader.getInstance().getModContainer("baritoe").isPresent() || FabricLoader.getInstance().getModContainer("baritone").isPresent();
    }

    public void initBaritoneProcesses() {
        if (baritoneExists())
            UnsafeBaritoneHelper.initBaritoneProcesses();
    }

    public boolean isBaritoneRunning() {
        if (!baritoneExists())
            return false;
        try {
            return UnsafeBaritoneHelper.isBaritoneRunning();
        }catch (Exception e) {
            return false;
        }
    }

    public boolean isTakingControl() {
        if (!baritoneExists())
            return false;
        try {
            return UnsafeBaritoneHelper.isTakingControl();
        }catch (Exception e) {
            return false;
        }
    }

    public void followUntilDead(LivingEntity entity, KillAura killaura) {
        if (baritoneExists())
            UnsafeBaritoneHelper.killauraTargetProcess.followUntilDead(entity, killaura);
    }

    public void disableKillauraTargetProcess() {
        if (baritoneExists())
            UnsafeBaritoneHelper.killauraTargetProcess.disable();
    }

    public void setAssumeJesus(boolean jesus) {
        if (baritoneExists())
            UnsafeBaritoneHelper.setAssumeJesus(jesus);
    }

    public void setAssumeStep(boolean step) {
        if (baritoneExists())
            UnsafeBaritoneHelper.setAssumeStep(step);
    }

    public boolean getAllowBreak() {
        if (baritoneExists())
            return UnsafeBaritoneHelper.getAllowedBreak();
        else return false;
    }

    public boolean getAllowPlace() {
        if (baritoneExists())
            return UnsafeBaritoneHelper.getAllowedPlace();
        else return false;
    }

    public void setAllowBreak(boolean allowBreak) {
        if (baritoneExists())
            UnsafeBaritoneHelper.setAllowBreak(allowBreak);
    }

    public void setAllowPlace(boolean allowPlace) {
        if (baritoneExists())
            UnsafeBaritoneHelper.setAllowPlace(allowPlace);
    }

    public void pathTo(BlockPos blockPos) {
        if (baritoneExists())
            UnsafeBaritoneHelper.pathTo(blockPos);
    }
    public void pathNear(BlockPos blockPos, int range) {
        if (baritoneExists())
            UnsafeBaritoneHelper.pathNear(blockPos, range);
    }

    public void pathTo(int x, int z) {
        if (baritoneExists())
            UnsafeBaritoneHelper.pathTo(x, z);
    }
}
