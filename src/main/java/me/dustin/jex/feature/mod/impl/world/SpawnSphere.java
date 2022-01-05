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

@Feature.Manifest(category = Feature.Category.WORLD, description = "Show a 128 block radius sphere around an area to see all spots mobs could spawn in that radius.")
public class SpawnSphere extends Feature {

    @Op(name = "Sphere Color", isColor = true)
    public int sphereColor = new Color(255, 250, 0).getRGB();
    @Op(name = "See-Through")
    public boolean seethrough = true;

    private Vec3d pos;

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        MatrixStack matrixStack = event.getMatrixStack();
        matrixStack.push();
        Render3DHelper.INSTANCE.setup3DRender(true);
        RenderSystem.lineWidth(1);
        Vec3d subtractable = Render3DHelper.INSTANCE.getEntityRenderPosition(Wrapper.INSTANCE.getLocalPlayer(), event.getPartialTicks()).subtract(pos);
        Render3DHelper.INSTANCE.drawSphere(matrixStack, pos, 128, 25, 25, sphereColor);
        Render3DHelper.INSTANCE.end3DRender();
        matrixStack.pop();
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
