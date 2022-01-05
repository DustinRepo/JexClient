package me.dustin.jex.feature.mod.impl.world;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Boxes;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Show a 128 block radius sphere around an area to see all spots mobs could spawn in that radius.")
public class SpawnSphere extends Feature {

    @Op(name = "Density", min = 25, max = 100, inc = 5)
    public int density = 50;
    @Op(name = "Spawnable Sphere Color", isColor = true)
    public int spawnableSphereColor = new Color(255, 0, 0).getRGB();
    @Op(name = "Non-Spawnable Sphere Color", isColor = true)
    public int nonSpawnableSphereColor = new Color(0, 255, 0).getRGB();
    @Op(name = "See-Through")
    public boolean seethrough = false;

    private Vec3d pos;
    private final ArrayList<BlockPos> innerSphere = new ArrayList<>();
    private final ArrayList<BlockPos> outerSphere = new ArrayList<>();

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        MatrixStack matrixStack = event.getMatrixStack();
        ArrayList<Render3DHelper.BoxStorage> boxes = new ArrayList<>();
        innerSphere.forEach(blockPos -> {
            Vec3d vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
            Box box = WorldHelper.SINGLE_BOX.offset(vec3d);
            boxes.add(new Render3DHelper.BoxStorage(box, nonSpawnableSphereColor));
        });
        outerSphere.forEach(blockPos -> {
            Vec3d vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
            Box box = WorldHelper.SINGLE_BOX.offset(vec3d);
            boxes.add(new Render3DHelper.BoxStorage(box, spawnableSphereColor));
        });

        Render3DHelper.INSTANCE.setup3DRender(seethrough);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        boxes.forEach(blockStorage -> {
            Box box = blockStorage.box();
            int color = blockStorage.color();
            Render3DHelper.INSTANCE.drawFilledBox(matrixStack, box, color & 0x50ffffff, false);
        });
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        Render3DHelper.INSTANCE.end3DRender();
    });

    @Override
    public void onEnable() {
        innerSphere.clear();
        outerSphere.clear();
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            pos = Wrapper.INSTANCE.getLocalPlayer().getPos();
            innerSphere.addAll(WorldHelper.INSTANCE.cubeSphere(pos, 24, density, density));
            outerSphere.addAll(WorldHelper.INSTANCE.cubeSphere(pos, 128, density, density));
        } else {
            this.setState(false);
            return;
        }
        super.onEnable();
    }
}
