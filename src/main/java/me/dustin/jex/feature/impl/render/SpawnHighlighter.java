package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

@Feat(name = "SpawnHighlighter", category = FeatureCategory.VISUAL, description = "Show all blocks near you that mobs can spawn on.")
public class SpawnHighlighter extends Feature {

    @Op(name = "Radius", min = 10, max = 50, inc = 1)
    public int radius = 25;
    @OpChild(name = "Y Radius", min = 10, max = 50, inc = 1, parent = "Radius")
    public int yradius = 15;
    @Op(name = "Check Light")
    public boolean checkLight = true;
    @OpChild(name = "Light Value", min = 0, max = 15, inc = 1, parent = "Check Light")
    public int lightValue = 7;
    @Op(name = "Check Water")
    public boolean checkWater = true;
    @Op(name = "Check IsSpawnable")
    public boolean checkIsSpawnable = true;
    @Op(name = "Color", isColor = true)
    public int color = 0xffff0000;

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        for (int x = -radius; x < radius; x++) {
            for (int y = -yradius; y < 5; y++) {
                for (int z = -radius; z < radius; z++) {
                    BlockPos blockPos = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z));
                    if (isValidBlock(blockPos)) {
                        BlockPos abovePos = blockPos.add(0, 1, 0);
                        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(abovePos.getX(), abovePos.getY(), abovePos.getZ()));
                        Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 0.05f, renderPos.z + 1);
                        Render3DHelper.INSTANCE.drawBoxWithDepthTest(eventRender3D.getMatrixStack(), box, color);
                    }
                }
            }
        }
    }

    private boolean isValidBlock(BlockPos blockPos) {
        BlockPos above = blockPos.add(0, 1, 0);
        Block thisBlock = WorldHelper.INSTANCE.getBlock(blockPos);
        Block aboveBlock = WorldHelper.INSTANCE.getBlock(above);
        BlockState thisState = Wrapper.INSTANCE.getWorld().getBlockState(blockPos);
        if (thisBlock == Blocks.AIR)
            return false;
        if (checkIsSpawnable)
            if (!thisState.isSideSolidFullSquare(Wrapper.INSTANCE.getWorld(), blockPos, Direction.UP))
                return false;
        if (checkWater)
            if (aboveBlock == Blocks.WATER)
                return false;
        if (!aboveBlock.canMobSpawnInside())
            return false;
        if (checkLight) {
            int light = Wrapper.INSTANCE.getWorld().getLightLevel(LightType.BLOCK, above);
            if (light > lightValue)
                return false;
        }
        return true;
    }
}
