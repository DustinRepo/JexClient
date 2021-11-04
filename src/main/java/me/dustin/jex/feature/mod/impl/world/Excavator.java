package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.wurstpathfinder.PathFinder;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;

import java.util.Random;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Mine out a selected area")
public class Excavator extends Feature {

    private PathFinder pathFinder;
    private PathProcessor pathProcessor;

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets) {
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (pathFinder == null)
                    pathFinder = new PathFinder(Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(-100 + new Random().nextInt(200), 0, -100 + new Random().nextInt(200)));
                if (!pathFinder.isDone() && !pathFinder.isFailed()) {
                    PathProcessor.lockControls();
                    pathFinder.think();

                    if (!pathFinder.isDone() && !pathFinder.isFailed())
                        return;

                    pathFinder.formatPath();
                    pathProcessor = pathFinder.getProcessor();
                }

                if (pathProcessor != null && !pathFinder.isPathStillValid(pathProcessor.getIndex())) {
                    pathFinder = new PathFinder(pathFinder);
                    return;
                }

                pathProcessor.process();

                if (pathProcessor.isDone()) {
                    pathFinder = null;
                    pathProcessor = null;
                    PathProcessor.releaseControls();
                }
            }
        } else if (event instanceof EventRender3D eventRender3D) {
            if (pathFinder != null)
                pathFinder.renderPath(eventRender3D.getMatrixStack(), false, false);
        }
    }

    @Override
    public void onDisable() {
        pathProcessor = null;
        pathFinder = null;
        PathProcessor.releaseControls();
        super.onDisable();
    }
}
