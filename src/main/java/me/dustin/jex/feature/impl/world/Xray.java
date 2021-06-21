package me.dustin.jex.feature.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.*;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@Feat(name = "Xray", category = FeatureCategory.WORLD, description = "Have 200 iq while mining. Not cheating I promise.")
public class Xray extends Feature {

    public static ArrayList<Block> blockList = new ArrayList<>();
    @Op(name = "Opacity")
    public boolean opacity = false;
    @OpChild(name = "Alpha Value", parent = "Opacity", max = 255)
    public int alphaValue = 64;

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

    @EventListener(events = {EventShouldDrawSide.class, EventBlockBrightness.class, EventMarkChunkClosed.class, EventRenderBlockEntity.class, EventRenderBlock.class, EventRenderFluid.class,
            EventGetRenderLayer.class, EventIsBlockOpaque.class, EventBufferQuadAlpha.class})
    private void run(Event event) {
        if (event instanceof EventMarkChunkClosed) {
            event.cancel();
        }
        if (event instanceof EventShouldDrawSide) {
            EventShouldDrawSide eventShouldDrawSide = (EventShouldDrawSide) event;
            if (this.opacity && isValid(eventShouldDrawSide.getBlock())) {
                eventShouldDrawSide.setShouldDrawSide(true);
                event.cancel();
            } else if (!this.opacity) {
                eventShouldDrawSide.setShouldDrawSide(isValid(eventShouldDrawSide.getBlock()));
                event.cancel();
            }
        }
        if (event instanceof EventBlockBrightness) {
            EventBlockBrightness eventBlockBrightness = (EventBlockBrightness) event;
            eventBlockBrightness.setBrightness(15);
        }
        if (event instanceof EventRenderBlockEntity) {
            EventRenderBlockEntity eventRenderBlockEntity = (EventRenderBlockEntity) event;
            if (!blockList.contains(WorldHelper.INSTANCE.getBlock(eventRenderBlockEntity.blockEntity.getPos())) && !this.opacity)
                event.cancel();
        }
        if (event instanceof EventRenderBlock) {
            EventRenderBlock eventRenderBlock = (EventRenderBlock) event;
            if (!blockList.contains(eventRenderBlock.block) && !this.opacity)
                event.cancel();
        }
        if (event instanceof EventRenderFluid) {
            EventRenderFluid eventRenderFluid = (EventRenderFluid) event;
            if (!blockList.contains(eventRenderFluid.getBlock()))
                event.cancel();
        }
        if (event instanceof EventGetRenderLayer) {
            EventGetRenderLayer eventGetRenderLayer = (EventGetRenderLayer) event;
            if (!blockList.contains(eventGetRenderLayer.getState().getBlock()) && this.opacity) {
                eventGetRenderLayer.setRenderLayer(RenderLayer.getTranslucent());
                event.cancel();
            }
        }
        if (event instanceof EventIsBlockOpaque) {
            //This is intended to stop non-opaque blocks from rendering if they're fully covered by other blocks i.e clumps of leaves on trees, glass etc..
            EventIsBlockOpaque eventIsBlockOpaque = (EventIsBlockOpaque) event;
            if (this.opacity) {
                eventIsBlockOpaque.setOpaque(true);
            }
        }
        if (event instanceof EventBufferQuadAlpha) {
            EventBufferQuadAlpha eventBufferQuadAlpha = (EventBufferQuadAlpha) event;
            if (this.opacity) {
                eventBufferQuadAlpha.setAlpha(alphaValue);
            }
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
