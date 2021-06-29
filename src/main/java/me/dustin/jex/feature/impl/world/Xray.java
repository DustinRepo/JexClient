package me.dustin.jex.feature.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.render.RenderLayer;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@Feature.Manifest(name = "Xray", category = Feature.Category.WORLD, description = "Have 200 iq while mining. Not cheating I promise.", key = GLFW.GLFW_KEY_X)
public class Xray extends Feature {

    public static ArrayList<Block> blockList = new ArrayList<>();
    @Op(name = "Opacity")
    public boolean opacity = true;
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

    @EventListener(events = {EventShouldDrawSide.class, EventBlockBrightness.class, EventMarkChunkClosed.class, EventRenderBlockEntity.class, EventRenderBlock.class, EventRenderFluid.class, EventGetRenderLayer.class, EventIsBlockOpaque.class, EventBufferQuadAlpha.class})
    private void run(Event event) {
        if (event instanceof EventMarkChunkClosed) {
            event.cancel();
        }
        if (event instanceof EventShouldDrawSide eventShouldDrawSide) {
            if (this.opacity && (isValid(eventShouldDrawSide.getBlock()) || eventShouldDrawSide.getBlock() instanceof FluidBlock)) {
                eventShouldDrawSide.setShouldDrawSide(true);
                event.cancel();
            } else if (!this.opacity) {
                eventShouldDrawSide.setShouldDrawSide(isValid(eventShouldDrawSide.getBlock()));
                event.cancel();
            }
        }
        if (event instanceof EventBlockBrightness eventBlockBrightness) {
            eventBlockBrightness.setBrightness(15);
        }
        if (event instanceof EventRenderBlockEntity eventRenderBlockEntity) {
            if (!blockList.contains(WorldHelper.INSTANCE.getBlock(eventRenderBlockEntity.blockEntity.getPos())) && !this.opacity)
                event.cancel();
        }
        if (event instanceof EventRenderBlock eventRenderBlock) {
            if (!blockList.contains(eventRenderBlock.block) && !this.opacity)
                event.cancel();
        }
        if (event instanceof EventRenderFluid eventRenderFluid) {
            if (!blockList.contains(eventRenderFluid.getBlock()) && !opacity)
                event.cancel();
        }
        if (event instanceof EventGetRenderLayer eventGetRenderLayer) {
            if (!blockList.contains(eventGetRenderLayer.getState().getBlock()) && this.opacity) {
                eventGetRenderLayer.setRenderLayer(RenderLayer.getTranslucent());
                event.cancel();
            }
        }
        if (event instanceof EventIsBlockOpaque eventIsBlockOpaque) {
            //This is intended to stop non-opaque blocks from rendering if they're fully covered by other blocks i.e clumps of leaves on trees, glass etc..
            if (this.opacity) {
                eventIsBlockOpaque.setOpaque(true);
            }
        }
        if (event instanceof EventBufferQuadAlpha eventBufferQuadAlpha) {
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
