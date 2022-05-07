package me.dustin.jex.helper.world.testpathfinder;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;

public class TestPathFinder {

    private final BlockPos goal;
    private long startTime;
    private ArrayList<PathNode> path = new ArrayList<>();

    private boolean isDone, isFailed;
    private int thinkSpeed = 4;

    private final int[][] posOffsets = {new int[]{1, 0, 0}, new int[]{0, 0, 1}, new int[]{1, 0, 1}, new int[]{-1, 0, 1}, new int[]{1, 0, -1}, new int[]{-1, 0, -1}, new int[]{-1, 0, 0}, new int[]{0, 0, -1}, new int[]{1, 1, 0}, new int[]{0, 1, 1}, new int[]{1, -1, 0}, new int[]{0, -1, 1}, new int[]{-1, -1, 0}, new int[]{0, -1, -1}};

    public TestPathFinder(BlockPos goal) {
        this.goal = goal;
        this.startTime = System.currentTimeMillis();
    }

    public void think() {
        if (isDone() || isFailed())
            return;
        if (System.currentTimeMillis() - startTime >= 5000) {
            isFailed = true;
            JexClient.INSTANCE.getLogger().info("Path timeout");
            JexClient.INSTANCE.getLogger().info("Path size: " + path.size());
        }
        for (int i = 0; i < thinkSpeed; i++) {
            if (path.isEmpty())
                path.add(new PathNode(Wrapper.INSTANCE.getLocalPlayer().blockPosition()));
            PathNode latestNode = path.get(path.size() - 1);
            PathNode nextNode = null;
            double bestDistance = Double.POSITIVE_INFINITY;
            for (int[] posOffset : posOffsets) {
                PathNode testNode = latestNode.offset(posOffset[0], posOffset[1], posOffset[2]);
                if (canMoveTo(testNode, latestNode)) {
                    if (testNode.isAt(goal)) {
                        isDone = true;
                        return;
                    } else {
                        double distance = ClientMathHelper.INSTANCE.getDistance(Vec3.atLowerCornerOf(testNode), Vec3.atLowerCornerOf(goal));
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            nextNode = testNode;
                            if (nextNode.getY() > latestNode.getY())
                                nextNode.setJump(true);
                        }
                    }
                }
            }
            if (nextNode == null) {
                isDone = true;
                return;
            }
            path.add(nextNode);
        }
    }

    public void render(PoseStack matrixStack) {
        ArrayList<Render3DHelper.BoxStorage> boxes = new ArrayList<>();
        path.forEach(pathNode -> {
            Vec3 renderVec = Render3DHelper.INSTANCE.getRenderPosition(pathNode.getX(), pathNode.getY(), pathNode.getZ());
            AABB box = new AABB(0, 0, 0, 1, 1, 1).move(renderVec);
            boxes.add(new Render3DHelper.BoxStorage(box, path.indexOf(pathNode) == path.size() - 1 && isDone() ? 0xff00ff00 : 0xffff0000));
        });
        Render3DHelper.INSTANCE.drawList(matrixStack, boxes, true);
    }

    private boolean canMoveTo(PathNode pathNode, PathNode latestNode) {
        BlockState lastBelowState = WorldHelper.INSTANCE.getBlockState(new BlockPos(latestNode).below());
        BlockState blockState = WorldHelper.INSTANCE.getBlockState(new BlockPos(pathNode));
        BlockState belowState = WorldHelper.INSTANCE.getBlockState(new BlockPos(pathNode).below());
        if (pathNode.getY() == latestNode.getY()) {
            return !blockState.getMaterial().blocksMotion() && lastBelowState.getMaterial().blocksMotion();
        } else if (pathNode.getY() < latestNode.getY()) {//moving down
            return !blockState.getMaterial().blocksMotion();
        } else if (pathNode.getY() > latestNode.getY()) {
            return !belowState.getMaterial().blocksMotion();
        }
        return false;
    }

    public BlockPos getGoal() {
        return goal;
    }

    public ArrayList<PathNode> getPath() {
        return path;
    }

    public boolean isDone() {
        return isDone;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public int getThinkSpeed() {
        return thinkSpeed;
    }

    public void setThinkSpeed(int thinkSpeed) {
        this.thinkSpeed = thinkSpeed;
    }

    public static class PathNode extends Vec3i {
        private boolean isJump;
        public PathNode(int x, int y, int z) {
            super(x, y, z);
        }
        public PathNode(BlockPos blockPos) {
            super(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        public PathNode offset(int x, int y, int z) {
            int x1 = this.getX() + x;
            int y1 = this.getY() + y;
            int z1 = this.getZ() + z;
            return new PathNode(x1, y1, z1);
        }

        public boolean isAt(BlockPos blockPos) {
            return this.getX() == blockPos.getX() && this.getY() == blockPos.getY() && this.getZ() == blockPos.getZ();
        }

        public boolean isJump() {
            return isJump;
        }

        public void setJump(boolean jump) {
            isJump = jump;
        }
    }
}
