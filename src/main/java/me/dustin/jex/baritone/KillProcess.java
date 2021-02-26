package me.dustin.jex.baritone;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.utils.BaritoneProcessHelper;

public class KillProcess extends BaritoneProcessHelper implements IBaritoneProcess {


    public KillProcess(Baritone baritone) {
        super(baritone);
    }
    private boolean kill;
    @Override
    public boolean isActive() {
        return kill;
    }

    public void kill() {
        this.kill = true;
    }

    @Override
    public PathingCommand onTick(boolean b, boolean b1) {
        kill = false;
        return new PathingCommand(BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal(), PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    public boolean isTemporary() {
        return false;
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
        return "Kill Process";
    }

    @Override
    public String displayName0() {
        return "killProcess";
    }
}
