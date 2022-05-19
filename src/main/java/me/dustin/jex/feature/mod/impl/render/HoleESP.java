package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.option.annotate.Op;
import java.awt.*;
import java.util.ArrayList;

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

    public HoleESP() {
        super(Category.VISUAL, "Automatically show holes for safe crystal-ing");
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (stopWatch.hasPassed(250)) {
            holes.clear();
            BlockPos playerPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();

            for (int y = -Math.min(range, playerPos.getY()); y < Math.min(range, 255 - playerPos.getY()); ++y) {
                for (int x = -range; x < range; ++x) {
                    for (int z = -range; z < range; ++z) {
                        BlockPos pos = playerPos.add(x, y, z);
                        if ((Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.OBSIDIAN) && Wrapper.INSTANCE.getWorld().getBlockState(pos.up()).getBlock() == Blocks.AIR && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up().offset(Direction.EAST)).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.up().offset(Direction.EAST)).getBlock() == Blocks.OBSIDIAN) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up().offset(Direction.WEST)).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.up().offset(Direction.WEST)).getBlock() == Blocks.OBSIDIAN) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up().offset(Direction.NORTH)).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.up().offset(Direction.NORTH)).getBlock() == Blocks.OBSIDIAN) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up().offset(Direction.SOUTH)).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.up().offset(Direction.SOUTH)).getBlock() == Blocks.OBSIDIAN) && Wrapper.INSTANCE.getWorld().getBlockState(pos.up(2)).getBlock() == Blocks.AIR && Wrapper.INSTANCE.getWorld().getBlockState(pos.up(3)).getBlock() == Blocks.AIR) {
                            holes.add(pos.up());
                        }
                    }
                }
            }
            stopWatch.reset();
        }
        for (BlockPos blockPos : holes) {
            Vec3d vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            int color = WorldHelper.INSTANCE.getBlock(blockPos.down()) == Blocks.BEDROCK ? bedrockColor : obsidianColor;
            if (fadeBox) {
                Box box = new Box(vec3d.x, vec3d.y, vec3d.z, vec3d.x + 1, vec3d.y + 1.5f, vec3d.z + 1);
                Render3DHelper.INSTANCE.setup3DRender(true);
                Render3DHelper.INSTANCE.drawFadeBox(event.getPoseStack(), box, color & 0xa9ffffff);
                Render3DHelper.INSTANCE.end3DRender();
            } else {
                Box box = new Box(vec3d.x, vec3d.y, vec3d.z, vec3d.x + 1, vec3d.y + 1, vec3d.z + 1);
                Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, color);
            }
        }
    });
}
