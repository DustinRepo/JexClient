package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.render.BufferHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.awt.*;
import java.util.ArrayList;

public class SpawnSphere extends Feature {

    public final Property<Integer> densityProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Density")
            .value(50)
            .min(25)
            .max(100)
            .inc(5)
            .build();
    public final Property<Color> spawnableSphereColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Spawnable Sphere Color")
            .value(Color.RED)
            .build();
    public final Property<Color> nonSpawnableSphereColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Non-Spawnable Sphere Color")
            .value(Color.GREEN)
            .build();
    public final Property<Boolean> seethroughProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("See-Through")
            .value(false)
            .build();

    public Vec3d pos;
    private final ArrayList<BlockPos> innerSphere = new ArrayList<>();
    private final ArrayList<BlockPos> outerSphere = new ArrayList<>();

    public SpawnSphere() {
        super(Category.WORLD);
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        MatrixStack matrixStack = event.getPoseStack();
        ArrayList<Render3DHelper.BoxStorage> boxes = new ArrayList<>();
        innerSphere.forEach(blockPos -> {
            Vec3d vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
            Box box = WorldHelper.SINGLE_BOX.offset(vec3d);
            boxes.add(new Render3DHelper.BoxStorage(box, nonSpawnableSphereColorProperty.value().getRGB()));
        });
        outerSphere.forEach(blockPos -> {
            Vec3d vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
            Box box = WorldHelper.SINGLE_BOX.offset(vec3d);
            boxes.add(new Render3DHelper.BoxStorage(box, spawnableSphereColorProperty.value().getRGB()));
        });

        Render3DHelper.INSTANCE.setup3DRender(seethroughProperty.value());
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        boxes.forEach(blockStorage -> {
            Box box = blockStorage.box();
            int color = blockStorage.color();
            Render3DHelper.INSTANCE.drawFilledBox(matrixStack, box, color & 0x50ffffff, false);
        });
        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        Render3DHelper.INSTANCE.end3DRender();
    });

    @Override
    public void onEnable() {
        innerSphere.clear();
        outerSphere.clear();
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            pos = Wrapper.INSTANCE.getLocalPlayer().getPos();
            innerSphere.addAll(WorldHelper.INSTANCE.cubeSphere(pos, 24, densityProperty.value(), densityProperty.value()));
            outerSphere.addAll(WorldHelper.INSTANCE.cubeSphere(pos, 128, densityProperty.value(), densityProperty.value()));
        } else {
            this.setState(false);
            return;
        }
        super.onEnable();
    }
}
