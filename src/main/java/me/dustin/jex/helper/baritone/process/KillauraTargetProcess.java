package me.dustin.jex.helper.baritone.process;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalRunAway;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.world.entity.LivingEntity;

public class KillauraTargetProcess implements IBaritoneProcess {

    private LivingEntity target;
    private KillAura killaura;

    public void followUntilDead(LivingEntity target, KillAura killaura) {
        this.target = target;
        this.killaura = killaura;
    }

    public void disable() {
        this.target = null;
    }

    @Override
    public boolean isActive() {
        return killaura != null && killaura.getState() && target != null && target.getHealth() > 0 && target.isAlive();
    }

    @Override
    public PathingCommand onTick(boolean b, boolean b1) {
        Goal goal;

        float dist = killaura.bMinDist;
        if (!BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().player().hasLineOfSight(target))
            dist = 1;

        if (BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().player().distanceTo(target) > dist)
            goal = new GoalNear(target.blockPosition(), (int) killaura.reach);
        else
            goal = new GoalRunAway(dist, target.blockPosition());
        return new PathingCommand(goal, PathingCommandType.REVALIDATE_GOAL_AND_PATH);
    }

    @Override
    public void onLostControl() {
        this.target = null;
    }

    @Override
    public double priority() {
        return 4;
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public String displayName0() {
        return "Killing " + target;
    }
}