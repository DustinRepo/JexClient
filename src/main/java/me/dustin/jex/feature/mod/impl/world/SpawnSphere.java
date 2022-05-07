package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
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

    public Vec3 pos;
    private final ArrayList<BlockPos> innerSphere = new ArrayList<>();
    private final ArrayList<BlockPos> outerSphere = new ArrayList<>();

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        PoseStack matrixStack = event.getPoseStack();
        ArrayList<Render3DHelper.BoxStorage> boxes = new ArrayList<>();
        innerSphere.forEach(blockPos -> {
            Vec3 vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
            AABB box = WorldHelper.SINGLE_BOX.move(vec3d);
            boxes.add(new Render3DHelper.BoxStorage(box, nonSpawnableSphereColor));
        });
        outerSphere.forEach(blockPos -> {
            Vec3 vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
            AABB box = WorldHelper.SINGLE_BOX.move(vec3d);
            boxes.add(new Render3DHelper.BoxStorage(box, spawnableSphereColor));
        });

        Render3DHelper.INSTANCE.setup3DRender(seethrough);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        boxes.forEach(blockStorage -> {
            AABB box = blockStorage.box();
            int color = blockStorage.color();
            Render3DHelper.INSTANCE.drawFilledBox(matrixStack, box, color & 0x50ffffff, false);
        });
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        Render3DHelper.INSTANCE.end3DRender();
    });

    @Override
    public void onEnable() {
        innerSphere.clear();
        outerSphere.clear();
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            pos = Wrapper.INSTANCE.getLocalPlayer().position();
            innerSphere.addAll(WorldHelper.INSTANCE.cubeSphere(pos, 24, density, density));
            outerSphere.addAll(WorldHelper.INSTANCE.cubeSphere(pos, 128, density, density));
        } else {
            this.setState(false);
            return;
        }
        super.onEnable();
    }
}
