package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.core.enums.EventPriority;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventSlowdown;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

@ModClass(name = "NoSlow", category = ModCategory.MOVEMENT, description = "Prevent actions from slowing you down")
public class NoSlow extends Module {

    @Op(name = "Use Item")
    public boolean useItem = true;
    @Op(name = "Soul Sand")
    public boolean soulSand = true;
    @Op(name = "Cobweb")
    public boolean cobweb = true;
    @Op(name = "Berry Bush")
    public boolean berryBush = true;

    @EventListener(events = {EventSlowdown.class, EventMove.class}, priority = EventPriority.HIGHEST)
    public void run(Event event) {
        if (event instanceof EventSlowdown) {
            EventSlowdown eventSlowdown = (EventSlowdown) event;
            if (eventSlowdown.getState() == EventSlowdown.State.USE_ITEM && useItem) {
                event.cancel();
            }
            if (eventSlowdown.getState() == EventSlowdown.State.COBWEB && cobweb) {
                event.cancel();
            }
            if (eventSlowdown.getState() == EventSlowdown.State.BERRY_BUSH && berryBush) {
                event.cancel();
            }
        }
        if (event instanceof EventMove) {
            EventMove eventMove = (EventMove) event;
            Block block = WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getLocalPlayer(), 0.7f);
            if (block == Blocks.SOUL_SAND && soulSand) {
                eventMove.setX(eventMove.getX() * 1.72111554);
                eventMove.setZ(eventMove.getZ() * 1.72111554);
            }
        }
    }

}
