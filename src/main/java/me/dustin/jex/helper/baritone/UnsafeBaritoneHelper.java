package me.dustin.jex.helper.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.ChatEvent;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.utils.input.Input;
import me.dustin.jex.helper.baritone.process.KillauraTargetProcess;
import me.dustin.jex.helper.baritone.process.PauseProcess;
import net.minecraft.util.math.BlockPos;

public class UnsafeBaritoneHelper {

    protected static PauseProcess pauseProcess;
    protected static KillauraTargetProcess killauraTargetProcess;

    protected static void initBaritoneProcesses() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().registerProcess(killauraTargetProcess = new KillauraTargetProcess());
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().registerProcess(pauseProcess = new PauseProcess());
    }

    protected static boolean isBaritoneRunning() {
        return BaritoneHelper.INSTANCE.baritoneExists() && (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing() || BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath());
    }

    protected static boolean isTakingControl() {
        return BaritoneHelper.INSTANCE.baritoneExists() && isBaritoneRunning() && (BaritoneAPI.getProvider().getPrimaryBaritone().getInputOverrideHandler().isInputForcedDown(Input.CLICK_LEFT) || BaritoneAPI.getProvider().getPrimaryBaritone().getInputOverrideHandler().isInputForcedDown(Input.CLICK_RIGHT));
    }

    public static void gotoLocation(BlockPos pos) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(pos.getX(), pos.getZ()));
    }

    protected static void sendCommand(String command) {
        BaritoneAPI.getSettings().prefix.value = "*&";
        ChatEvent event = new ChatEvent(BaritoneAPI.getSettings().prefix.value + command);
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (baritone != null) {
            baritone.getGameEventHandler().onSendChatMessage(event);
        }
    }

    protected static void setAssumeJesus(boolean jesus) {
        BaritoneAPI.getSettings().assumeWalkOnWater.value = jesus;
    }

    protected static void setAssumeStep(boolean step) {
        BaritoneAPI.getSettings().assumeStep.value = step;
    }

    protected static boolean getAllowedBreak() {
        return BaritoneAPI.getSettings().allowBreak.value;
    }
    protected static boolean getAllowedPlace() {
        return BaritoneAPI.getSettings().allowPlace.value;
    }

    protected static void setAllowBreak(boolean allowBreak) {
        BaritoneAPI.getSettings().allowBreak.value = allowBreak;
    }

    protected static void setAllowPlace(boolean allowPlace) {
        BaritoneAPI.getSettings().allowPlace.value = allowPlace;
    }

    protected static void pathTo(BlockPos blockPos) {
        if (blockPos == null) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            return;
        }
        Goal goal = new GoalBlock(blockPos);
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
    }

    protected static void pathNear(BlockPos blockPos, int range) {
        if (blockPos == null) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            return;
        }

        GoalNear goal = new GoalNear(blockPos, range);
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
    }

    protected static void pathTo(int x, int z) {
        Goal goal = new GoalXZ(x, z);
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
    }
}
