package me.dustin.jex.feature.mod.impl.world;

import bedrockminer.utils.BreakingFlowController;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.KeyPressFilter;
import me.dustin.jex.event.filters.MousePressFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.PathingHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Excavator extends Feature {

    public final Property<Boolean> useBaritoneProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Use Baritone If Available")
            .value(true)
            .build();
    public final Property<Boolean> allowMiningProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Mine Path")
            .value(false)
            .build();
    public final Property<Boolean> renderAreaBoxProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Render Area Box")
            .value(false)
            .build();
    public final Property<Integer> layerDepthProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Layer Depth")
            .value(2)
            .min(1)
            .max(5)
            .build();
    public final Property<Long> sortDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Sort Delay")
            .value(350L)
            .max(1000)
            .inc(10)
            .build();
    //@Op(name = "LogOut when Done")
    public boolean logoutWhenDone = false;

    public static MiningArea miningArea;
    private Stage stage = Stage.SET_POS1;

    private BlockPos tempPos1;
    private BlockPos tempPos2;
    private final StopWatch sortStopWatch = new StopWatch();

    private boolean baritoneAllowPlace;

    public Excavator() {
        super(Category.WORLD, "Mine out a selected area");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating || (Feature.get(AntiLiquid.class).getPos() != null))
            return;
        if (miningArea == null)
            return;
        if (stage != Stage.EXCAVATING)
            return;
        BlockPos closestBlock = miningArea.getClosest();
        if (closestBlock != null) {
            double distanceTo = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().getPos(), Vec3d.ofCenter(closestBlock));
            if (distanceTo <= (WorldHelper.INSTANCE.getBlock(closestBlock) == Blocks.BEDROCK ? 3 : Wrapper.INSTANCE.getClientPlayerInteractionManager().getReachDistance() - 1)) {
                if (!KillAura.INSTANCE.hasTarget() && !BreakingFlowController.isWorking()) {
                    BlockHitResult blockHitResult = rayCast(Wrapper.INSTANCE.getPlayer(), closestBlock);
                    RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getPlayer(), Vec3d.ofCenter(closestBlock));
                    event.setRotation(rotationVector);
                    Wrapper.INSTANCE.getPlayer().setHeadYaw(rotationVector.getYaw());
                    Wrapper.INSTANCE.getPlayer().setBodyYaw(rotationVector.getYaw());

                    Wrapper.INSTANCE.getClientPlayerInteractionManager().updateBlockBreakingProgress(closestBlock, blockHitResult == null ? Direction.UP : blockHitResult.getSide());
                    PlayerHelper.INSTANCE.swing(Hand.MAIN_HAND);
                }
                if (distanceTo <= 1.5f && WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getPlayer()) != Blocks.AIR) {
                    PathingHelper.INSTANCE.cancelPathing();
                    if (BaritoneHelper.INSTANCE.baritoneExists() && useBaritoneProperty.value())
                        BaritoneHelper.INSTANCE.pathTo(null);
                }
            } else
            if (miningArea != null && distanceTo > 2.5f) {
                if (BaritoneHelper.INSTANCE.baritoneExists() && useBaritoneProperty.value()) {
                    if (!BaritoneHelper.INSTANCE.isBaritoneRunning()) {
                        BaritoneHelper.INSTANCE.pathNear(closestBlock, layerDepthProperty.value());
                    }
                } else {
                    if (!PathingHelper.INSTANCE.isPathing()) {
                        PathingHelper.INSTANCE.setAllowMining(allowMiningProperty.value());
                        PathingHelper.INSTANCE.pathNear(closestBlock, layerDepthProperty.value());
                    }
                }
            }
        } else if (miningArea.empty()) {
            ChatHelper.INSTANCE.addClientMessage("Excavator finished.");
            setState(false);
            if (logoutWhenDone) {
                NetworkHelper.INSTANCE.disconnect(Formatting.AQUA + "Excavator", Formatting.GREEN + "Excavator has finished.");
            }
            return;
        }
        if (miningArea != null)
            setSuffix(String.format("%.2f%%", (1 - ((float)miningArea.blocksLeft() / (float)miningArea.totalBlocks())) * 100));
        else
            setSuffix("0%");
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (renderAreaBoxProperty.value() && (tempPos1 != null && tempPos2 != null) || miningArea != null) {
            MiningArea copy = miningArea != null ? miningArea : new MiningArea(tempPos1, tempPos2);
            Vec3d miningAreaVec1 = Render3DHelper.INSTANCE.getRenderPosition(new BlockPos(copy.getAreaBB().minX, copy.getAreaBB().minY, copy.getAreaBB().minZ));
            Vec3d miningAreaVec2 = Render3DHelper.INSTANCE.getRenderPosition(new BlockPos(copy.getAreaBB().maxX, copy.getAreaBB().maxY, copy.getAreaBB().maxZ));
            Box miningAreaBox = new Box(miningAreaVec1.x, miningAreaVec1.y, miningAreaVec1.z, miningAreaVec2.x + 1, miningAreaVec2.y + 1, miningAreaVec2.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), miningAreaBox, 0xffffff00);
        }
        if (tempPos1 != null) {//draws yellow box on first set pos
            Vec3d tempVec = Render3DHelper.INSTANCE.getRenderPosition(tempPos1);
            Box closestBox = new Box(tempVec.x, tempVec.y, tempVec.z, tempVec.x + 1, tempVec.y + 1, tempVec.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), closestBox, 0xffffff00);
        }
        if (tempPos2 != null) {//draws yellow box on first set pos
            Vec3d tempVec = Render3DHelper.INSTANCE.getRenderPosition(tempPos2);
            Box closestBox = new Box(tempVec.x, tempVec.y, tempVec.z, tempVec.x + 1, tempVec.y + 1, tempVec.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), closestBox, 0xffffff00);
        }
        //draws yellow box on crosshair block
        if (miningArea == null && Wrapper.INSTANCE.getMinecraft().crosshairTarget instanceof BlockHitResult blockHitResult) {
            Vec3d hitVec = Render3DHelper.INSTANCE.getRenderPosition(blockHitResult.getBlockPos());
            Box hoverBox = new Box(hitVec.x, hitVec.y, hitVec.z, hitVec.x + 1, hitVec.y + 1, hitVec.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), hoverBox, 0xff00ff00);
        }
    });

    @EventPointer
    private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().crosshairTarget instanceof BlockHitResult blockHitResult) {
            switch (stage) {
                case SET_POS1 -> tempPos1 = blockHitResult.getBlockPos();
                case SET_POS2 -> tempPos2 = blockHitResult.getBlockPos();
            }
        }
    }, new MousePressFilter(EventMouseButton.ClickType.IN_GAME, 1));

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        String message = "";
        float percent = 0;
        switch (stage) {
            case SET_POS1 -> {
                if (tempPos1 == null) {
                    message = "Select First Pos with Right-Click";
                } else {
                    message = "Press Enter to Confirm Pos 1";
                }
            }
            case SET_POS2 -> {
                if (tempPos2 == null) {
                    message = "Select Second Pos with Right-Click";
                } else {
                    message = "Press Enter to Confirm Pos 2";
                }
            }
            case EXCAVATING -> {
                percent = 1 - ((float)miningArea.blocksLeft() / (float)miningArea.totalBlocks());
                message = Formatting.WHITE + "Excavating... " + Formatting.RESET + String.format("%.2f", percent * 100) + Formatting.WHITE + "%";
            }
            case PAUSED -> message = "Excavator Paused... Press Enter to Resume";
        }
        float width = FontHelper.INSTANCE.getStringWidth(message);
        Render2DHelper.INSTANCE.outlineAndFill(event.getPoseStack(), Render2DHelper.INSTANCE.getScaledWidth() / 2.f - width / 2.f - 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 10, Render2DHelper.INSTANCE.getScaledWidth() / 2.f + width / 2.f + 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 24, 0x70696969, 0x40000000);
        FontHelper.INSTANCE.drawCenteredString(event.getPoseStack(), message, Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 13, miningArea != null ? getColor(percent).getRGB() : -1);

        if (PathingHelper.INSTANCE.isThinking()) {
            message = Formatting.GREEN + "Wurst AI" + Formatting.GRAY + ": " + Formatting.WHITE + "Thinking";
            width = FontHelper.INSTANCE.getStringWidth(message);
            Render2DHelper.INSTANCE.outlineAndFill(event.getPoseStack(), Render2DHelper.INSTANCE.getScaledWidth() / 2.f - width / 2.f - 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 25, Render2DHelper.INSTANCE.getScaledWidth() / 2.f + width / 2.f + 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 39, 0x70696969, 0x40000000);
            FontHelper.INSTANCE.drawCenteredString(event.getPoseStack(), message, Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 28,-1);
        }
    });

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getKey() == GLFW.GLFW_KEY_ENTER) {
            switch (stage) {
                case SET_POS1 -> {
                    if (tempPos1 != null)
                        stage = Stage.SET_POS2;
                }
                case SET_POS2 -> {
                    if (tempPos2 != null)
                        if (miningArea == null) {
                            miningArea = new MiningArea(tempPos1, tempPos2);
                            stage = Stage.EXCAVATING;
                            tempPos1 = null;
                            tempPos2 = null;
                        }
                }
                case EXCAVATING -> {
                    stage = Stage.PAUSED;
                    PathProcessor.releaseControls();
                    if (BaritoneHelper.INSTANCE.baritoneExists() && useBaritoneProperty.value())
                        BaritoneHelper.INSTANCE.pathTo(null);
                }
                case PAUSED -> stage = Stage.EXCAVATING;
            }
        } else if (event.getKey() == GLFW.GLFW_KEY_BACKSPACE) {
            switch (stage) {
                case SET_POS1 -> {
                    if (tempPos1 != null)
                        tempPos1 = null;
                }
                case SET_POS2 -> {
                    if (tempPos2 != null)
                        tempPos2 = null;
                    else
                        stage = Stage.SET_POS1;
                }
                case PAUSED -> stage = Stage.EXCAVATING;
            }
        }
    }, new KeyPressFilter(EventKeyPressed.PressType.IN_GAME, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_BACKSPACE));

    public boolean isPaused() {
        return this.stage != Stage.EXCAVATING;
    }

    @Override
    public void onEnable() {
        stage = Stage.SET_POS1;
        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            baritoneAllowPlace = BaritoneHelper.INSTANCE.getAllowPlace();
            BaritoneHelper.INSTANCE.setAllowPlace(false);
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (BaritoneHelper.INSTANCE.baritoneExists() && Wrapper.INSTANCE.getPlayer() != null) {
            BaritoneHelper.INSTANCE.setAllowPlace(baritoneAllowPlace);
            BaritoneHelper.INSTANCE.pathTo(null);
        }
        PathingHelper.INSTANCE.cancelPathing();
        miningArea = null;
        tempPos1 = null;
        tempPos2 = null;
        PathProcessor.releaseControls();
        super.onDisable();
    }

    public Color getColor(double power) {
        if (power > 1)
            power = 1;
        double H = power * 0.35;
        double S = 0.9;
        double B = 0.9;

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }

    public BlockHitResult rayCast(PlayerEntity player, BlockPos blockPos) {
        RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(player, Vec3d.of(blockPos).add(0.5, 0, 0.5));
        RotationVector saved = new RotationVector(player);
        Wrapper.INSTANCE.getPlayer().setYaw(rotationVector.getYaw());
        Wrapper.INSTANCE.getPlayer().setPitch(rotationVector.getPitch());
        HitResult result = player.raycast(Wrapper.INSTANCE.getClientPlayerInteractionManager().getReachDistance(), 1, false);// Wrapper.clientWorld().rayTraceBlock(getVec(entity), getVec(entity).add(0, -256, 0), false, true, false);
        Wrapper.INSTANCE.getPlayer().setYaw(saved.getYaw());
        Wrapper.INSTANCE.getPlayer().setPitch(saved.getPitch());
        if (result instanceof BlockHitResult blockHitResult)
            return blockHitResult;
        return null;
    }

    public static class MiningArea {
        private final Excavator excavator;
        private final Box areaBB;

        private final ArrayList<BlockPos> blockPosList = new ArrayList<>();

        private int highestY = -64;
        private int startBlocksAmount;

        public MiningArea(BlockPos pos1, BlockPos pos2) {
            BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
            BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
            this.areaBB = new Box(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            this.excavator = Feature.get(Excavator.class);

            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        blockPosList.add(new BlockPos(x, y, z));
                    }
                }
            }
            startBlocksAmount = blocksLeft();
            sortList();
        }

        public int blocksLeft() {
            int blocks = 0;
            for (BlockPos blockPos : blockPosList) {
                if (WorldHelper.INSTANCE.getBlockState(blockPos).getOutlineShape(Wrapper.INSTANCE.getWorld(), blockPos) != VoxelShapes.empty())
                   blocks++;
            }
            return blocks;
        }

        public int totalBlocks() {
            return startBlocksAmount;
        }

        public boolean empty() {
            return blocksLeft() == 0;
        }

        public BlockPos getClosest() {
            if (excavator.sortStopWatch.hasPassed(excavator.sortDelayProperty.value())) {
                sortList();
                int y = -64;
                for (BlockPos blockPos : blockPosList) {
                    if (WorldHelper.INSTANCE.getBlockState(blockPos).getOutlineShape(Wrapper.INSTANCE.getWorld(), blockPos) != VoxelShapes.empty()) {
                        if (blockPos.getY() > y) {
                            y = blockPos.getY();
                        }
                    }
                }
                highestY = y;
                excavator.sortStopWatch.reset();
            }
            for (BlockPos blockPos : blockPosList) {
                //able to be clicked
                if (WorldHelper.INSTANCE.getBlockState(blockPos).getOutlineShape(Wrapper.INSTANCE.getWorld(), blockPos) != VoxelShapes.empty()) {
                    //cheeky little workaround for being able to dig layers
                    if (Math.abs(blockPos.getY() - getHighestBlockY()) <= excavator.layerDepthProperty.value() - 1) {
                        return blockPos;
                    }
                }
            }
            return null;
        }

        public int getHighestBlockY() {
            return highestY;
        }

        public Box getAreaBB() {
            return areaBB;
        }

        public void sortList() {
            blockPosList.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().getPos().add(0, 2, 0), Vec3d.ofCenter(value))));
        }
    }

    private enum Stage {
        SET_POS1, SET_POS2, EXCAVATING, PAUSED
    }
}
