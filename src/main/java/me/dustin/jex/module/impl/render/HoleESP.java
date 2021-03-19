package me.dustin.jex.module.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

@ModClass(name = "HoleESP", category = ModCategory.VISUAL, description = "Automatically show holes for safe crystal-ing")
public class HoleESP extends Module {

    @Op(name = "Range", min = 5, max = 25, inc = 1)
    public int range = 10;
    @Op(name = "Obsidian Color", isColor = true)
    public int obsidianColor = new Color(255, 255, 0).getRGB();
    @Op(name = "Bedrock Color", isColor = true)
    public int bedrockColor = new Color(0, 255, 255).getRGB();

    private Timer timer = new Timer();
    private ArrayList<BlockPos> holes = new ArrayList<>();

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        if (timer.hasPassed(250)) {
            holes.clear();
            BlockPos playerPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();

            for (int y = -Math.min(range, playerPos.getY()); y < Math.min(range, 255 - playerPos.getY()); ++y) {
                for (int x = -range; x < range; ++x) {
                    for (int z = -range; z < range; ++z) {
                        BlockPos pos = playerPos.add(x, y, z);
                        if ((Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock() == Blocks.OBSIDIAN) && Wrapper.INSTANCE.getWorld().getBlockState(pos.up()).getBlock() == Blocks.AIR && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up().east()).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.up().east()).getBlock() == Blocks.OBSIDIAN) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up().west()).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.up().west()).getBlock() == Blocks.OBSIDIAN) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up().north()).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.up().north()).getBlock() == Blocks.OBSIDIAN) && (Wrapper.INSTANCE.getWorld().getBlockState(pos.up().south()).getBlock() == Blocks.BEDROCK || Wrapper.INSTANCE.getWorld().getBlockState(pos.up().south()).getBlock() == Blocks.OBSIDIAN) && Wrapper.INSTANCE.getWorld().getBlockState(pos.up(2)).getBlock() == Blocks.AIR && Wrapper.INSTANCE.getWorld().getBlockState(pos.up(3)).getBlock() == Blocks.AIR) {
                            holes.add(pos.up());
                        }
                    }
                }
            }
            timer.reset();
        }
        for (BlockPos blockPos : holes) {
            Vec3d vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            Box box = new Box(vec3d.x, vec3d.y, vec3d.z, vec3d.x + 1, vec3d.y + 1, vec3d.z + 1);
            Render3DHelper.INSTANCE.drawBox(box, WorldHelper.INSTANCE.getBlock(blockPos.down()) == Blocks.BEDROCK ? bedrockColor : obsidianColor);
        }
    }

}
