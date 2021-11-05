package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.helper.world.wurstpathfinder.PathFinder;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Mine out a selected area")
public class Excavator extends Feature {

    @Op(name = "Render Path")
    public boolean renderPath = true;
    @Op(name = "Render Area Box")
    public boolean renderAreaBox = true;
    //@Op(name = "LogOut when Done") crashes atm, no clue why
    public boolean logoutWhenDone = false;
    @Op(name = "Sort Delay")
    public int sortDelay = 350;

    private ExcavatorPathFinder pathFinder;
    private PathProcessor pathProcessor;

    public static MiningArea miningArea;

    private BlockPos tempPos;
    private Timer sortTimer = new Timer();

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class, EventMouseButton.class, EventRender2D.class})
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
                        //TODO: actually check if visible and send correct face
                        Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(closestBlock, Direction.UP);
                        Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        if (distanceTo <= 3)
                            pathFinder = null;
                    } else
                    if (pathFinder == null && miningArea != null && distanceTo > 3) {
                        pathFinder = new ExcavatorPathFinder(closestBlock);
                    }
                } else if (miningArea.empty()) {
                    ChatHelper.INSTANCE.addClientMessage("Excavator finished.");
                    setState(false);
                    if (logoutWhenDone) {
                        NetworkHelper.INSTANCE.disconnect(Formatting.AQUA + "Excavator", Formatting.GREEN + "Excavator has finished.");
                    }
                    return;
                }

                if (pathFinder != null) {
                    if (!pathFinder.isDone() && !pathFinder.isFailed()) {
                        PathProcessor.lockControls();
                        pathFinder.think();

                        if (!pathFinder.isDone() && !pathFinder.isFailed())
                            return;

                        pathFinder.formatPath();
                        pathProcessor = pathFinder.getProcessor();
                    }

                    if (pathProcessor != null && !pathFinder.isPathStillValid(pathProcessor.getIndex())) {
                        pathFinder = new ExcavatorPathFinder(pathFinder);
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
        } else if (event instanceof EventRender3D eventRender3D) {
            if (renderPath && pathFinder != null)
                pathFinder.renderPath(eventRender3D.getMatrixStack(), false, false);

            if (miningArea != null) {
                if (renderAreaBox) {
                    Vec3d miningAreaVec1 = Render3DHelper.INSTANCE.getRenderPosition(new BlockPos(miningArea.getAreaBB().minX, miningArea.getAreaBB().minY, miningArea.getAreaBB().minZ));
                    Vec3d miningAreaVec2 = Render3DHelper.INSTANCE.getRenderPosition(new BlockPos(miningArea.getAreaBB().maxX, miningArea.getAreaBB().maxY, miningArea.getAreaBB().maxZ));
                    Box miningAreaBox = new Box(miningAreaVec1.x, miningAreaVec1.y, miningAreaVec1.z, miningAreaVec2.x + 1, miningAreaVec2.y + 1, miningAreaVec2.z + 1);
                    Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), miningAreaBox, 0xffffff00);
                }
                BlockPos closestBlock = miningArea.getClosest();
                if (closestBlock == null)
                    return;
                Vec3d closestVec = Render3DHelper.INSTANCE.getRenderPosition(closestBlock);
                Box closestBox = new Box(closestVec.x, closestVec.y, closestVec.z, closestVec.x + 1, closestVec.y + 1, closestVec.z + 1);
                Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), closestBox, ColorHelper.INSTANCE.getClientColor());
            }else
            if (tempPos != null) {//draws yellow box on first set pos
                Vec3d tempVec = Render3DHelper.INSTANCE.getRenderPosition(tempPos);
                Box closestBox = new Box(tempVec.x, tempVec.y, tempVec.z, tempVec.x + 1, tempVec.y + 1, tempVec.z + 1);
                Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), closestBox, 0xffffff00);
            }
            //draws yellow box on crosshair block
            if (miningArea == null && Wrapper.INSTANCE.getMinecraft().crosshairTarget instanceof BlockHitResult blockHitResult && WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()) != Blocks.AIR) {
                Vec3d hitVec = Render3DHelper.INSTANCE.getRenderPosition(blockHitResult.getBlockPos());
                Box hoverBox = new Box(hitVec.x, hitVec.y, hitVec.z, hitVec.x + 1, hitVec.y + 1, hitVec.z + 1);
                Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), hoverBox, 0xffffff00);
            }
        } else if (event instanceof EventMouseButton eventMouseButton) {
            if (eventMouseButton.getButton() == 1 && eventMouseButton.getClickType() == EventMouseButton.ClickType.IN_GAME) {
                if (Wrapper.INSTANCE.getMinecraft().crosshairTarget instanceof BlockHitResult blockHitResult && WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()) != Blocks.AIR) {
                    if (tempPos == null)
                        tempPos = blockHitResult.getBlockPos();
                    else if (miningArea == null)
                        miningArea = new MiningArea(tempPos, blockHitResult.getBlockPos());
                }
            }
        } else if (event instanceof EventRender2D eventRender2D) {
            String message = null;
            if (tempPos == null) {
                message = "Select First Pos with Right-Click";
            } else if (miningArea == null) {
                message = "Select Second Pos with Right-Click";
            }
            if (message != null) {
                float width = FontHelper.INSTANCE.getStringWidth(message);
                Render2DHelper.INSTANCE.outlineAndFill(eventRender2D.getMatrixStack(), Render2DHelper.INSTANCE.getScaledWidth() / 2.f - width / 2.f - 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 10, Render2DHelper.INSTANCE.getScaledWidth() / 2.f + width / 2.f + 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 24, 0x70696969, 0x40000000);
                FontHelper.INSTANCE.drawCenteredString(eventRender2D.getMatrixStack(), message, Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 12, -1);
            }
        }
    }

    @Override
    public void onDisable() {
        pathProcessor = null;
        pathFinder = null;
        miningArea = null;
        tempPos = null;
        PathProcessor.releaseControls();
        super.onDisable();
    }

    public static class MiningArea {
        private final Excavator excavator;
        private final BlockPos pos1;
        private final BlockPos pos2;
        private final Box areaBB;

        private final ArrayList<BlockPos> blockPosList = new ArrayList<>();

        public MiningArea(BlockPos pos1, BlockPos pos2) {
            BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
            BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
            this.pos1 = pos1;
            this.pos2 = pos2;
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

        public boolean empty() {
            for (BlockPos blockPos : blockPosList) {
                if (WorldHelper.INSTANCE.getBlockState(blockPos).getOutlineShape(Wrapper.INSTANCE.getWorld(), blockPos) != VoxelShapes.empty())
                    return false;
            }
            return true;
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

        public BlockPos getPos1() {
            return pos1;
        }

        public BlockPos getPos2() {
            return pos2;
        }

        public Box getAreaBB() {
            return areaBB;
        }

        public void sortList() {
            blockPosList.sort((o1, o2) -> {
                double distanceTo1 = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), Vec3d.ofCenter(o1));
                double distanceTo2 = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), Vec3d.ofCenter(o2));
                if (distanceTo1 == distanceTo2)
                    return 0;
                return distanceTo1 > distanceTo2 ? 1 : -1;
            });
            blockPosList.sort((o1, o2) -> {
                if (o1.getY() == o2.getY())
                    return 0;
                return o1.getY() > o2.getY() ? -1 : 1;
            });
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
