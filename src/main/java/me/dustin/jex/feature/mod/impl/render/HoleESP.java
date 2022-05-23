package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.awt.*;
import java.util.ArrayList;

public class HoleESP extends Feature {

    public final Property<Boolean> fadeBoxProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Fade Box")
            .value(true)
            .build();
    public final Property<Integer> rangeProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Range")
            .value(10)
            .min(5)
            .max(25)
            .inc(1)
            .build();
    public final Property<Color> obsidianColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Obsidian Color")
            .value(new Color(255, 255, 0))
            .build();
    public final Property<Color> bedrockColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Bedrock Color")
            .value(new Color(0, 255, 255))
            .build();

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

            for (int y = -Math.min(rangeProperty.value(), playerPos.getY()); y < Math.min(rangeProperty.value(), 255 - playerPos.getY()); ++y) {
                for (int x = -rangeProperty.value(); x < rangeProperty.value(); ++x) {
                    for (int z = -rangeProperty.value(); z < rangeProperty.value(); ++z) {
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
            int color = WorldHelper.INSTANCE.getBlock(blockPos.down()) == Blocks.BEDROCK ? bedrockColorProperty.value().getRGB() : obsidianColorProperty.value().getRGB();
            if (fadeBoxProperty.value()) {
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
