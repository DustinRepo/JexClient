package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.option.annotate.Op;
import java.awt.*;
import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Automatically show holes for safe crystal-ing")
public class HoleESP extends Feature {

    @Op(name = "Fade Box")
    public boolean fadeBox = true;
    @Op(name = "Range", min = 5, max = 25, inc = 1)
    public int range = 10;
    @Op(name = "Obsidian Color", isColor = true)
    public int obsidianColor = new Color(255, 255, 0).getRGB();
    @Op(name = "Bedrock Color", isColor = true)
    public int bedrockColor = new Color(0, 255, 255).getRGB();

    private final StopWatch stopWatch = new StopWatch();
    private final ArrayList<BlockPos> holes = new ArrayList<>();

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (stopWatch.hasPassed(250)) {
            holes.clear();
            BlockPos playerPos = Wrapper.INSTANCE.getLocalPlayer().blockPosition();

            for (int y = -Math.min(range, playerPos.getY()); y < Math.min(range, 255 - playerPos.getY()); ++y) {
                for (int x = -range; x < range; ++x) {
                    for (int z = -range; z < range; ++z) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        if ((Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.OBSIDIAN) && Wrapper.INSTANCE.getWorld().getBlockState(pos.above()).getBlock() == Blocks.AIR && (Wrapper.INSTANCE.getWorld().getBlockState(pos.above().relative(Direction.EAST)).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.above().relative(Direction.EAST)).getBlock() == Blocks.OBSIDIAN) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.above().relative(Direction.WEST)).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.above().relative(Direction.WEST)).getBlock() == Blocks.OBSIDIAN) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.above().relative(Direction.NORTH)).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.above().relative(Direction.NORTH)).getBlock() == Blocks.OBSIDIAN) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.above().relative(Direction.SOUTH)).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.above().relative(Direction.SOUTH)).getBlock() == Blocks.OBSIDIAN) && Wrapper.INSTANCE.getWorld().getBlockState(pos.above(2)).getBlock() == Blocks.AIR && Wrapper.INSTANCE.getWorld().getBlockState(pos.above(3)).getBlock() == Blocks.AIR) {
                            holes.add(pos.above());
                        }
                    }
                }
            }
            stopWatch.reset();
        }
        for (BlockPos blockPos : holes) {
            Vec3 vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            int color = WorldHelper.INSTANCE.getBlock(blockPos.below()) == Blocks.BEDROCK ? bedrockColor : obsidianColor;
            if (fadeBox) {
                AABB box = new AABB(vec3d.x, vec3d.y, vec3d.z, vec3d.x + 1, vec3d.y + 1.5f, vec3d.z + 1);
                Render3DHelper.INSTANCE.setup3DRender(true);
                Render3DHelper.INSTANCE.drawFadeBox(event.getPoseStack(), box, color & 0xa9ffffff);
                Render3DHelper.INSTANCE.end3DRender();
            } else {
                AABB box = new AABB(vec3d.x, vec3d.y, vec3d.z, vec3d.x + 1, vec3d.y + 1, vec3d.z + 1);
                Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, color);
            }
        }
    });
}
