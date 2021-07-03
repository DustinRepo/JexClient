package me.dustin.jex.feature.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.load.impl.IShader;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20C;

import java.util.ArrayList;

@Feature.Manifest(name = "Xray", category = Feature.Category.WORLD, description = "Have 200 iq while mining. Not cheating I promise.", key = GLFW.GLFW_KEY_X)
public class Xray extends Feature {

    public static ArrayList<Block> blockList = new ArrayList<>();
    @Op(name = "Opacity")
    public boolean opacity = true;
    @OpChild(name = "Alpha Value", parent = "Opacity", min = 0, inc = 0.01f)
    public float alphaValue = 0.5f;
    @OpChild(name = "Fade", parent = "Opacity")
    public boolean fade = true;
    @OpChild(name = "Fade Increment", parent = "Fade", min = 0.05F, max = 1F, inc = 0.05F)
    public float fadeIncrement = 0.25F;

    public static int sodiumShaderProgram;
    public static int alphaLocation = -1;

    private float currentAlpha = 1.1f;

    public static void firstLoad() {
        blockList.add(Blocks.DIAMOND_ORE);
        blockList.add(Blocks.IRON_ORE);
        blockList.add(Blocks.COAL_ORE);
        blockList.add(Blocks.EMERALD_ORE);
        blockList.add(Blocks.GOLD_ORE);
        blockList.add(Blocks.LAPIS_ORE);
        blockList.add(Blocks.NETHER_QUARTZ_ORE);
    }

    @EventListener(events = {EventShouldDrawSide.class, EventBlockBrightness.class, EventMarkChunkClosed.class, EventRenderBlockEntity.class, EventRenderBlock.class, EventRenderFluid.class, EventGetRenderLayer.class, EventIsBlockOpaque.class, EventGetTranslucentShader.class, EventTick.class, EventSodiumBeginShader.class})
    private void run(Event event) {
        if (event instanceof EventMarkChunkClosed) {
            event.cancel();
        }
        if (event instanceof EventShouldDrawSide eventShouldDrawSide) {
            if (this.opacity && (isValid(eventShouldDrawSide.getBlock()) || eventShouldDrawSide.getBlock() instanceof FluidBlock)) {
                eventShouldDrawSide.setShouldDrawSide(this.shouldDrawSide(eventShouldDrawSide.getSide(), eventShouldDrawSide.getBlockPos()));
                event.cancel();
            } else if (!this.opacity) {
                eventShouldDrawSide.setShouldDrawSide(isValid(eventShouldDrawSide.getBlock()));
                event.cancel();
            }
        }
        if (event instanceof EventBlockBrightness eventBlockBrightness) {
            if (isValid(eventBlockBrightness.getBlock()))
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
            if (this.opacity && (!this.fade)) {
                eventIsBlockOpaque.setOpaque(true);
            }
        }
        if (event instanceof EventGetTranslucentShader eventGetTranslucentShader) {
            eventGetTranslucentShader.setShader(ShaderHelper.getTranslucentShader());
            eventGetTranslucentShader.cancel();
        }
        if (event instanceof EventSodiumBeginShader) {
            updateAlpha();
        }
        if (event instanceof EventTick eventTick) {
            if (isSodiumLoaded())
                return;
            IShader translucentShader = (IShader) ShaderHelper.getTranslucentShader();
            if (translucentShader == null)
                return;
            GlUniform alphaUniform = translucentShader.getCustomUniform("Alpha");
            updateAlpha();
        }
    }

    private boolean shouldDrawSide(Direction side, BlockPos blockPos) {
        Block currentBlock = WorldHelper.INSTANCE.getBlock(blockPos);
        //fix this at some point to make it not render water faces not facing the player
        /*if (currentBlock instanceof FluidBlock) {
            float yaw = PlayerHelper.INSTANCE.getRotations(ClientMathHelper.INSTANCE.getVec(blockPos), Wrapper.INSTANCE.getLocalPlayer()).getYaw();
            Direction dir = Direction.fromRotation(yaw);
            if (dir == side || (Wrapper.INSTANCE.getLocalPlayer().getY() >= blockPos.getY() - 1 && side == Direction.UP) || (Wrapper.INSTANCE.getLocalPlayer().getY() < blockPos.getY() - 1 && side == Direction.DOWN))
                return true;
            return false;
        }*/
        switch (side) {//Don't draw side if it can't be seen (e.g don't render inside faces for ore vein)
            case UP -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.up())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.up())) || WorldHelper.INSTANCE.getBlock(blockPos.up()) instanceof FluidBlock);}
            case DOWN -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.down())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.down())) || WorldHelper.INSTANCE.getBlock(blockPos.down()) instanceof FluidBlock);}
            case NORTH -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.north())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.north())) || WorldHelper.INSTANCE.getBlock(blockPos.north()) instanceof FluidBlock);}
            case SOUTH -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.south())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.south())) || WorldHelper.INSTANCE.getBlock(blockPos.south()) instanceof FluidBlock);}
            case EAST -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.east())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.east())) || WorldHelper.INSTANCE.getBlock(blockPos.east()) instanceof FluidBlock);}
            case WEST -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.west())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.west())) || WorldHelper.INSTANCE.getBlock(blockPos.west()) instanceof FluidBlock);}
        }
        return true;
    }

    void updateAlpha() {
        IShader translucentShader = (IShader) ShaderHelper.getTranslucentShader();
        GlUniform alphaUniform = null;
        if (translucentShader != null)
            alphaUniform = translucentShader.getCustomUniform("Alpha");
        if (isSodiumLoaded()) {
            currentAlpha = GL20C.glGetUniformf(sodiumShaderProgram, alphaLocation);
        } else {
            assert alphaUniform != null;
            currentAlpha = alphaUniform.getFloatData().get(0);
        }
        //TODO Clean this up, made add some fade modes? Linear curve, adjustable fade increments etc..
        if (this.fade) {
            if (!getState()) {
                if (currentAlpha < 1.1F) {
                    if (alphaUniform != null)
                        alphaUniform.set(currentAlpha + (fadeIncrement / 10.f));
                    if (isSodiumLoaded())
                        GL20C.glUniform1f(alphaLocation, currentAlpha + (fadeIncrement / 10.f));
                } else {
                    this.renderChunksSmooth();
                    super.onDisable();
                }
            } else {
                if (Math.abs(currentAlpha - alphaValue) <= (fadeIncrement / 10.f)) {
                    if (alphaUniform != null)
                        alphaUniform.set(alphaValue);
                    if (isSodiumLoaded())
                        GL20C.glUniform1f(alphaLocation, alphaValue);

                } else if (currentAlpha > alphaValue) {
                    if (alphaUniform != null)
                        alphaUniform.set(currentAlpha - (fadeIncrement / 10.f));
                    if (isSodiumLoaded())
                        GL20C.glUniform1f(alphaLocation, currentAlpha - (fadeIncrement / 10.f));
                } else if (currentAlpha < alphaValue) {
                    if (alphaUniform != null)
                        alphaUniform.set(currentAlpha + (fadeIncrement / 10.f));
                    if (isSodiumLoaded())
                        GL20C.glUniform1f(alphaLocation, currentAlpha + (fadeIncrement / 10.f));
                }
            }
        } else {
            if (alphaUniform != null)
                alphaUniform.set(alphaValue);
            if (isSodiumLoaded())
                GL20C.glUniform1f(alphaLocation, alphaValue);
        }
    }

    @Override
    public void onEnable() {
        this.renderChunksSmooth();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if(!this.fade) {
            this.renderChunksSmooth();
            super.onDisable();
        }
    }

    private void renderChunksSmooth() {
        if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
            final int x = (int) Wrapper.INSTANCE.getLocalPlayer().getX() >> 4;
            final int y = (int) Wrapper.INSTANCE.getLocalPlayer().getY() >> 4;
            final int z = (int) Wrapper.INSTANCE.getLocalPlayer().getZ() >> 4;

            final int distance = Wrapper.INSTANCE.getOptions().viewDistance;
            for (int i = x - distance; i < x + distance; i++) {
                for (int k = z - distance; k < z + distance; k++) {
                    for (int j = y - distance; j < 16; j++) {
                        Wrapper.INSTANCE.getWorldRenderer().scheduleBlockRender(i, j, k);
                    }
                }
            }
        }
    }

    private boolean isSodiumLoaded() {
        return FabricLoader.getInstance().isModLoaded("sodium");
    }

    private boolean isValid(Block block) {
        return blockList.contains(block) || (this.opacity && block instanceof FluidBlock);
    }
}
