package me.dustin.jex.helper.baritone.process;

import baritone.api.BaritoneAPI;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;

public class PauseProcess implements IBaritoneProcess {

    boolean pause = false;

    @Override
    public boolean isActive() {
        return pause;
    }

    public void pause(boolean pause) {
        this.pause = pause;
    }

    @Override
    public PathingCommand onTick(boolean b, boolean b1) {
        return new PathingCommand(BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal(), PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public void onLostControl() {
    }

    @Override
    public double priority() {
        return 5;
    }

    @Override
    public String displayName() {
        return "Pause Process";
    }

    @Override
    public String displayName0() {
        return "pauseProcess";
    }
}
