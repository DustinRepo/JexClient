package me.dustin.jex.feature.mod.impl.world;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Show a 128 block radius sphere around an area to see all spots mobs could spawn in that radius.")
public class SpawnSphere extends Feature {

    @Op(name = "Spawnable Sphere Color", isColor = true)
    public int spawnableSphereColor = new Color(255, 0, 0).getRGB();
    @Op(name = "Non-Spawnable Sphere Color", isColor = true)
    public int nonSpawnableSphereColor = new Color(0, 255, 0).getRGB();
    @Op(name = "See-Through")
    public boolean seethrough = false;

    private Vec3d pos;

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        MatrixStack matrixStack = event.getMatrixStack();
        ArrayList<Render3DHelper.BoxStorage> outersphere = Render3DHelper.INSTANCE.drawSphere(matrixStack, pos, 128, 25, 25, spawnableSphereColor);
        ArrayList<Render3DHelper.BoxStorage> inner = Render3DHelper.INSTANCE.drawSphere(matrixStack, pos, 24, 25, 25, nonSpawnableSphereColor);
        outersphere.addAll(inner);
        Render3DHelper.INSTANCE.drawList(matrixStack, outersphere, seethrough);
    });

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            pos = Wrapper.INSTANCE.getLocalPlayer().getPos();
        else {
            this.setState(false);
            return;
        }
        super.onEnable();
    }
}
