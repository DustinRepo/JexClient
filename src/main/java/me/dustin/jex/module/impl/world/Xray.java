package me.dustin.jex.module.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.*;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@ModClass(name = "Xray", category = ModCategory.WORLD, description = "Have 200 iq while mining. Not cheating I promise.")
public class Xray extends Module {

    public static ArrayList<Block> blockList = new ArrayList<>();

    public static void firstLoad() {
        blockList.add(Blocks.DIAMOND_ORE);
        blockList.add(Blocks.IRON_ORE);
        blockList.add(Blocks.COAL_ORE);
        blockList.add(Blocks.EMERALD_ORE);
        blockList.add(Blocks.GOLD_ORE);
        blockList.add(Blocks.LAPIS_ORE);
        blockList.add(Blocks.NETHER_QUARTZ_ORE);
    }

    public Xray() {
        this.setKey(GLFW.GLFW_KEY_X);
    }

    @EventListener(events = {EventShouldDrawSide.class, EventBlockBrightness.class, EventMarkChunkClosed.class, EventRenderBlockEntity.class, EventRenderBlock.class, EventRenderFluid.class})
    private void run(Event event) {
        if (event instanceof EventMarkChunkClosed) {
            event.cancel();
        }
        if (event instanceof EventShouldDrawSide) {
            try {
                EventShouldDrawSide eventShouldDrawSide = (EventShouldDrawSide) event;
                eventShouldDrawSide.setShouldDrawSide(isValid(eventShouldDrawSide.getBlock()));
                event.cancel();
            } catch (Exception e) {

            }
        }
        if (event instanceof EventBlockBrightness) {
            EventBlockBrightness eventBlockBrightness = (EventBlockBrightness) event;
            eventBlockBrightness.setBrightness(15);
        }
        if (event instanceof EventRenderBlockEntity) {
            EventRenderBlockEntity eventRenderBlockEntity = (EventRenderBlockEntity) event;
            if (!blockList.contains(WorldHelper.INSTANCE.getBlock(eventRenderBlockEntity.blockEntity.getPos())))
                event.cancel();
        }
        if (event instanceof EventRenderBlock) {
            EventRenderBlock eventRenderBlock = (EventRenderBlock) event;
            if (!blockList.contains(eventRenderBlock.block))
                event.cancel();
        }
        if (event instanceof EventRenderFluid) {
            EventRenderFluid eventRenderFluid = (EventRenderFluid) event;
            if (!blockList.contains(eventRenderFluid.getBlock()))
                event.cancel();
        }
    }

    @Override
    public void setState(boolean state) {
        if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null)
            Wrapper.INSTANCE.getMinecraft().worldRenderer.reload();
        super.setState(state);
    }

    private boolean isValid(Block block) {
        return blockList.contains(block);
    }
}
