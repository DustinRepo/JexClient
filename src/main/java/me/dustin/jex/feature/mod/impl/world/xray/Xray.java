package me.dustin.jex.feature.mod.impl.world.xray;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventGetSkinTexture;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.world.xray.impl.NormalXray;
import me.dustin.jex.feature.mod.impl.world.xray.impl.OpacityXray;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class Xray extends Feature {
    public static Xray INSTANCE;
    public static ArrayList<Block> blockList = new ArrayList<>();

    public Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.NORMAL)
            .build();
    public Property<Integer> alphaProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Opacity")
            .description("How opaque the blocks should be (less = less visible)")
            .value(50)
            .min(1)
            .max(100)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.OPACITY)
            .build();

    public Property<Boolean> playerXray = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Player Xray")
            .description("See through the clothes of other players!")
            .value(false)
            .build();
    private Mode lastMode;

    public Xray() {
        super(Category.WORLD, "Have 200 iq while mining. Not cheating I promise.", GLFW.GLFW_KEY_X);
        INSTANCE = this;
        new NormalXray();
        new OpacityXray();
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
    private final EventListener<EventShouldDrawSide> eventShouldDrawSideEventListener = new EventListener<>(event -> sendEvent(event));

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
    private final EventListener<EventBindShader> eventBindShaderEventListener = new EventListener<>(event -> sendEvent(event));

    @EventPointer
    private final EventListener<EventWorldRender> eventWorldRenderEventListener = new EventListener<>(event -> sendEvent(event));

    @EventPointer
    private final EventListener<EventSodiumQuadAlpha> eventSodiumQuadAlphaEventListener = new EventListener<>(event -> sendEvent(event));

    @EventPointer
    private final EventListener<EventGetSkinTexture> eventGetSkinTextureEventListener = new EventListener<>(event -> {
        if (!playerXray.value())
            return;
        if (event.getPlayerEntity() == Wrapper.INSTANCE.getLocalPlayer())
            return;
        event.setSkin(new Identifier("jex", "skin/speedo.png"));
        event.cancel();
    });

    private void sendEvent(Event event) {
        if (lastMode != null && modeProperty.value() != lastMode) {
            if (Wrapper.INSTANCE.getWorldRenderer() != null)
                Wrapper.INSTANCE.getWorldRenderer().reload();
        }
        FeatureExtension.get(modeProperty.value(), this).pass(event);
        this.setSuffix(modeProperty.value());
        lastMode = modeProperty.value();
    }

    @Override
    public void setState(boolean state) {
        if (Wrapper.INSTANCE.getWorldRenderer() != null)
            Wrapper.INSTANCE.getWorldRenderer().reload();
        super.setState(state);
        lastMode = modeProperty.value();
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
            case UP -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.up())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.up())) || WorldHelper.INSTANCE.getBlock(blockPos.up()) instanceof FluidBlock);}
            case DOWN -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.down())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.down())) || WorldHelper.INSTANCE.getBlock(blockPos.down()) instanceof FluidBlock);}
            case NORTH -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.north())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.north())) || WorldHelper.INSTANCE.getBlock(blockPos.north()) instanceof FluidBlock);}
            case SOUTH -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.south())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.south())) || WorldHelper.INSTANCE.getBlock(blockPos.south()) instanceof FluidBlock);}
            case EAST -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.east())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.east())) || WorldHelper.INSTANCE.getBlock(blockPos.east()) instanceof FluidBlock);}
            case WEST -> {return currentBlock instanceof FluidBlock ? !isValid(WorldHelper.INSTANCE.getBlock(blockPos.west())) : (!isValid(WorldHelper.INSTANCE.getBlock(blockPos.west())) || WorldHelper.INSTANCE.getBlock(blockPos.west()) instanceof FluidBlock);}
        }
        return true;
    }

    public boolean isValid(Block block) {
        return blockList.contains(block);
    }

    public enum Mode {
        NORMAL, OPACITY
    }
}
