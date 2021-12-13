package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.core.enums.EventPriority;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.helper.world.wurstpathfinder.PathFinder;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Mine out a selected area")
public class Excavator extends Feature {

    @Op(name = "Render Path")
    public boolean renderPath = true;
    @Op(name = "Render Area Box")
    public boolean renderAreaBox = true;
    //@Op(name = "LogOut when Done")
    public boolean logoutWhenDone = false;
    @Op(name = "Sort Delay", max = 1000, inc = 10)
    public int sortDelay = 350;

    private PathFinder pathFinder;
    private PathProcessor pathProcessor;

    public static MiningArea miningArea;

    private BlockPos tempPos;
    private final Timer sortTimer = new Timer();

    private boolean baritoneAllowPlace;

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class, EventMouseButton.class, EventRender2D.class}, priority = EventPriority.LOW)
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets) {
            if (AutoEat.isEating)
                return;
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (miningArea == null)
                    return;
                BlockPos closestBlock = miningArea.getClosest();
                if (closestBlock != null) {
                    double distanceTo = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), Vec3d.ofCenter(closestBlock));
                    if (distanceTo <= Wrapper.INSTANCE.getInteractionManager().getReachDistance() - 0.1f) {
                        BlockHitResult blockHitResult = rayCast(closestBlock);
                        if (blockHitResult != null) {
                            Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(blockHitResult.getBlockPos(), blockHitResult.getSide());
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        if (distanceTo <= 3) {
                            pathFinder = null;
                            if (BaritoneHelper.INSTANCE.baritoneExists())
                                BaritoneHelper.INSTANCE.pathTo(null);
                        }
                    } else
                    if (pathFinder == null && miningArea != null && distanceTo > 3) {
                        if (BaritoneHelper.INSTANCE.baritoneExists()) {
                            if (!BaritoneHelper.INSTANCE.isBaritoneRunning()) {
                                BaritoneHelper.INSTANCE.pathNear(closestBlock, 2);
                            }
                        } else {
                            pathFinder = new ExcavatorPathFinder(closestBlock);
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

                if (!BaritoneHelper.INSTANCE.baritoneExists()) {
                    if (pathFinder != null) {
                        if (!pathFinder.isDone() && !pathFinder.isFailed()) {
                            PathProcessor.lockControls();
                            pathFinder.think();
                            if (!pathFinder.isDone() && !pathFinder.isFailed()) {
                                return;

                            }
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
                }
                if (miningArea != null)
                    setSuffix(String.format("%.2f%%", (1 - ((float)miningArea.blocksLeft() / (float)miningArea.totalBlocks())) * 100));
                else
                    setSuffix("0%");
            }
        } else if (event instanceof EventRender3D eventRender3D) {
            if (renderPath && pathFinder != null)
                pathFinder.renderPath(eventRender3D.getMatrixStack(), false, false);

            if (miningArea != null && renderAreaBox) {
                Vec3d miningAreaVec1 = Render3DHelper.INSTANCE.getRenderPosition(new BlockPos(miningArea.getAreaBB().minX, miningArea.getAreaBB().minY, miningArea.getAreaBB().minZ));
                Vec3d miningAreaVec2 = Render3DHelper.INSTANCE.getRenderPosition(new BlockPos(miningArea.getAreaBB().maxX, miningArea.getAreaBB().maxY, miningArea.getAreaBB().maxZ));
                Box miningAreaBox = new Box(miningAreaVec1.x, miningAreaVec1.y, miningAreaVec1.z, miningAreaVec2.x + 1, miningAreaVec2.y + 1, miningAreaVec2.z + 1);
                Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), miningAreaBox, 0xffffff00);
            }else
            if (tempPos != null) {//draws yellow box on first set pos
                Vec3d tempVec = Render3DHelper.INSTANCE.getRenderPosition(tempPos);
                Box closestBox = new Box(tempVec.x, tempVec.y, tempVec.z, tempVec.x + 1, tempVec.y + 1, tempVec.z + 1);
                Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), closestBox, 0xffffff00);
            }
            //draws yellow box on crosshair block
            if (miningArea == null && Wrapper.INSTANCE.getMinecraft().crosshairTarget instanceof BlockHitResult blockHitResult) {
                Vec3d hitVec = Render3DHelper.INSTANCE.getRenderPosition(blockHitResult.getBlockPos());
                Box hoverBox = new Box(hitVec.x, hitVec.y, hitVec.z, hitVec.x + 1, hitVec.y + 1, hitVec.z + 1);
                Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), hoverBox, 0xffffff00);
            }
        } else if (event instanceof EventMouseButton eventMouseButton) {
            if (eventMouseButton.getButton() == 1 && eventMouseButton.getClickType() == EventMouseButton.ClickType.IN_GAME) {
                if (Wrapper.INSTANCE.getMinecraft().crosshairTarget instanceof BlockHitResult blockHitResult) {
                    if (tempPos == null)
                        tempPos = blockHitResult.getBlockPos();
                    else if (miningArea == null)
                        miningArea = new MiningArea(tempPos, blockHitResult.getBlockPos());
                }
            }
        } else if (event instanceof EventRender2D eventRender2D) {
            String message;
            float percent = 0;
            if (tempPos == null) {
                message = "Select First Pos with Right-Click";
            } else if (miningArea == null) {
                message = "Select Second Pos with Right-Click";
            } else {
                percent = 1 - ((float)miningArea.blocksLeft() / (float)miningArea.totalBlocks());
                message = Formatting.WHITE + "Excavating... " + Formatting.RESET + String.format("%.2f", percent * 100) + Formatting.WHITE + "%";
            }
            float width = FontHelper.INSTANCE.getStringWidth(message);
            Render2DHelper.INSTANCE.outlineAndFill(eventRender2D.getMatrixStack(), Render2DHelper.INSTANCE.getScaledWidth() / 2.f - width / 2.f - 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 10, Render2DHelper.INSTANCE.getScaledWidth() / 2.f + width / 2.f + 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 24, 0x70696969, 0x40000000);
            FontHelper.INSTANCE.drawCenteredString(eventRender2D.getMatrixStack(), message, Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 13, miningArea != null ? getColor(percent).getRGB() : -1);

            if (pathFinder != null && !pathFinder.isDone() && !pathFinder.isFailed()) {
                message = Formatting.GREEN + "Wurst AI" + Formatting.GRAY + ": " + Formatting.WHITE + "Thinking";
                width = FontHelper.INSTANCE.getStringWidth(message);
                Render2DHelper.INSTANCE.outlineAndFill(eventRender2D.getMatrixStack(), Render2DHelper.INSTANCE.getScaledWidth() / 2.f - width / 2.f - 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 25, Render2DHelper.INSTANCE.getScaledWidth() / 2.f + width / 2.f + 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 39, 0x70696969, 0x40000000);
                FontHelper.INSTANCE.drawCenteredString(eventRender2D.getMatrixStack(), message, Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 28,-1);
            }
        }
    }

    @Override
    public void onEnable() {
        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            baritoneAllowPlace = BaritoneHelper.INSTANCE.getAllowPlace();
            BaritoneHelper.INSTANCE.setAllowPlace(false);
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (BaritoneHelper.INSTANCE.baritoneExists() && Wrapper.INSTANCE.getLocalPlayer() != null) {
            BaritoneHelper.INSTANCE.setAllowPlace(baritoneAllowPlace);
            BaritoneHelper.INSTANCE.pathTo(null);
        }
        pathProcessor = null;
        pathFinder = null;
        miningArea = null;
        tempPos = null;
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

    public BlockHitResult rayCast(BlockPos blockPos) {
        RotationVector rotationVector = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), Vec3d.of(blockPos).add(0.5, 0, 0.5));
        RotationVector saved = new RotationVector(Wrapper.INSTANCE.getLocalPlayer());
        PlayerHelper.INSTANCE.setRotation(rotationVector);
        HitResult result = Wrapper.INSTANCE.getLocalPlayer().raycast(Wrapper.INSTANCE.getInteractionManager().getReachDistance(), 1, false);// Wrapper.clientWorld().rayTraceBlock(getVec(entity), getVec(entity).add(0, -256, 0), false, true, false);
        PlayerHelper.INSTANCE.setRotation(saved);
        if (result instanceof BlockHitResult blockHitResult)
            return blockHitResult;
        return null;
    }

    public static class MiningArea {
        private final Excavator excavator;
        private final Box areaBB;

        private final ArrayList<BlockPos> blockPosList = new ArrayList<>();

        public MiningArea(BlockPos pos1, BlockPos pos2) {
            BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
            BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
            this.areaBB = new Box(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            this.excavator = (Excavator) Feature.get(Excavator.class);

            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        blockPosList.add(new BlockPos(x, y, z));
                    }
                }
            }
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
            return blockPosList.size();
        }

        public boolean empty() {
            return blocksLeft() == 0;
        }

        public BlockPos getClosest() {
            if (excavator.sortTimer.hasPassed(excavator.sortDelay)) {
                sortList();
                excavator.sortTimer.reset();
            }
            for (BlockPos blockPos : blockPosList) {
                //able to be clicked
                if (WorldHelper.INSTANCE.getBlockState(blockPos).getOutlineShape(Wrapper.INSTANCE.getWorld(), blockPos) != VoxelShapes.empty()) {
                    return blockPos;
                }
            }
            return null;
        }

        public Box getAreaBB() {
            return areaBB;
        }

        public void sortList() {
            blockPosList.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), Vec3d.ofCenter(value))));
            blockPosList.sort(Comparator.comparingInt(value -> -value.getY()));
        }
    }

    private static class ExcavatorPathFinder extends PathFinder
    {
        public ExcavatorPathFinder(BlockPos goal)
        {
            super(goal);
            setThinkTime(10);
        }

        public ExcavatorPathFinder(ExcavatorPathFinder pathFinder)
        {
            super(pathFinder);
        }

        @Override
        protected boolean checkDone()
        {
            BlockPos goal = getGoal();

            return done = goal.down(2).equals(current)
                    || goal.up().equals(current) || goal.north().equals(current)
                    || goal.south().equals(current) || goal.west().equals(current)
                    || goal.east().equals(current)
                    || goal.down().north().equals(current)
                    || goal.down().south().equals(current)
                    || goal.down().west().equals(current)
                    || goal.down().east().equals(current);
        }
    }
}
