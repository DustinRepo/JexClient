package me.dustin.jex.feature.mod.impl.world.xray;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.world.xray.impl.NormalXray;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Have 200 iq while mining. Not cheating I promise.", key = GLFW.GLFW_KEY_X)
public class Xray extends Feature {
    public static Xray INSTANCE;

    public static ArrayList<Block> blockList = new ArrayList<>();

    //@Op(name = "Mode", all = {"Opacity", "Normal"})
    public String mode = "Normal";
    @OpChild(name = "Alpha Value", parent = "Mode", min = 0, inc = 0.01f, dependency = "Opacity")
    public float alphaValue = 0.5f;

    public String lastMode;

    public Xray() {
        //new OpacityXray();
        new NormalXray();
        INSTANCE = this;
    }

    public static void firstLoad() {
        blockList.add(Blocks.DIAMOND_ORE);
        blockList.add(Blocks.IRON_ORE);
        blockList.add(Blocks.COAL_ORE);
        blockList.add(Blocks.EMERALD_ORE);
        blockList.add(Blocks.GOLD_ORE);
        blockList.add(Blocks.LAPIS_ORE);
        blockList.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        blockList.add(Blocks.DEEPSLATE_IRON_ORE);
        blockList.add(Blocks.DEEPSLATE_COAL_ORE);
        blockList.add(Blocks.DEEPSLATE_EMERALD_ORE);
        blockList.add(Blocks.DEEPSLATE_GOLD_ORE);
        blockList.add(Blocks.DEEPSLATE_LAPIS_ORE);
        blockList.add(Blocks.NETHER_QUARTZ_ORE);
    }

    @EventPointer
    private final EventListener<EventShouldRenderFace> eventShouldDrawSideEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventBlockBrightness> eventBlockBrightnessEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventMarkChunkClosed> eventMarkChunkClosedEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRenderBlockEntity> eventRenderBlockEntityEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRenderBlock> eventRenderBlockEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRenderFluid> eventRenderFluidEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventGetRenderType> eventGetRenderTypeEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventGetTranslucentShader> eventGetTranslucentShaderEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> sendEvent(event), new TickFilter(EventTick.Mode.PRE));
    @EventPointer
    private final EventListener<EventSodiumBeginShader> eventSodiumBeginShaderEventListener = new EventListener<>(event -> sendEvent(event));

    public void sendEvent(Event event) {
        if (!mode.equalsIgnoreCase(lastMode) && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(mode, this).enable();
        }
        FeatureExtension.get(mode, this).pass(event);
        this.setSuffix(mode);
        lastMode = mode;
    }

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getWorld() == null) {
            this.setState(false);
            return;
        }
        FeatureExtension.get(mode, this).enable();
        lastMode = mode;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getWorld() == null) {
            return;
        }
        FeatureExtension.get(mode, this).disable();
        lastMode = mode;
        super.onDisable();
    }

    public boolean shouldDrawSide(Direction side, BlockPos blockPos) {
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
            case UP -> {return currentBlock instanceof LiquidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.above())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.above())) || WorldHelper.INSTANCE.getBlock(blockPos.above()) instanceof LiquidBlock);}
            case DOWN -> {return currentBlock instanceof LiquidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.below())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.below())) || WorldHelper.INSTANCE.getBlock(blockPos.below()) instanceof LiquidBlock);}
            case NORTH -> {return currentBlock instanceof LiquidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.north())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.north())) || WorldHelper.INSTANCE.getBlock(blockPos.north()) instanceof LiquidBlock);}
            case SOUTH -> {return currentBlock instanceof LiquidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.south())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.south())) || WorldHelper.INSTANCE.getBlock(blockPos.south()) instanceof LiquidBlock);}
            case EAST -> {return currentBlock instanceof LiquidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.east())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.east())) || WorldHelper.INSTANCE.getBlock(blockPos.east()) instanceof LiquidBlock);}
            case WEST -> {return currentBlock instanceof LiquidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.west())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.west())) || WorldHelper.INSTANCE.getBlock(blockPos.west()) instanceof LiquidBlock);}
        }
        return true;
    }

    public void renderChunksSmooth() {
        if (Wrapper.INSTANCE.getMinecraft().levelRenderer != null && Wrapper.INSTANCE.getLocalPlayer() != null) {
            final int x = (int) Wrapper.INSTANCE.getLocalPlayer().getX() >> 4;
            final int y = (int) Wrapper.INSTANCE.getLocalPlayer().getY() >> 4;
            final int z = (int) Wrapper.INSTANCE.getLocalPlayer().getZ() >> 4;

            final int distance = Wrapper.INSTANCE.getOptions().renderDistance().get();
            for (int i = x - distance; i < x + distance; i++) {
                for (int k = z - distance; k < z + distance; k++) {
                    for (int j = y - distance; j < 16; j++) {
                        Wrapper.INSTANCE.getWorldRenderer().setSectionDirty(i, j, k);
                    }
                }
            }
        }
    }

    public boolean isSodiumLoaded() {
        return FabricLoader.getInstance().isModLoaded("sodium");
    }

    public boolean isValid(Block block) {
        return blockList.contains(block) || ("Opacity".equalsIgnoreCase(this.mode) && block instanceof LiquidBlock);
    }
}
