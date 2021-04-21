package me.dustin.jex.baritone;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalRunAway;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.impl.combat.killaura.Killaura;
import net.minecraft.entity.LivingEntity;

public class KillauraTargetProcess implements IBaritoneProcess {

    private LivingEntity target;
    private Killaura killaura;

    public void followUntilDead(LivingEntity target, Killaura killaura) {
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
        //had to comment out the original ones because the official baritone jar either builds with already yarn mapped code or running the "jar" gradle command builds with forge mappings even using the baritone.fabric_build property
        //since I'm not doing any fancy stuff with multiple baritones I don't really have to worry too much though
        if (!/*BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().player()*/Wrapper.INSTANCE.getLocalPlayer().canSee(target))
            dist = 1;

        if (/*BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().player()*/Wrapper.INSTANCE.getLocalPlayer().distanceTo(target) > dist)
            goal = new GoalNear(target.getBlockPos(), (int) killaura.reach);
        else
            goal = new GoalRunAway(dist, target.getBlockPos());
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