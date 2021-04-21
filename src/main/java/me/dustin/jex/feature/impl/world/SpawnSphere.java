package me.dustin.jex.feature.impl.world;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@Feat(name = "SpawnSphere", category = FeatureCategory.WORLD, description = "Show a 128 block radius sphere around an area to see all spots mobs could spawn in that radius.")
public class SpawnSphere extends Feature {

    @Op(name = "Sphere Color", isColor = true)
    public int sphereColor = new Color(255, 250, 0).getRGB();
    @Op(name = "See-Through")
    public boolean seethrough = true;

    private Vec3d pos;

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        MatrixStack matrixStack = eventRender3D.getMatrixStack();
        matrixStack.push();
        Render3DHelper.INSTANCE.setup3DRender(true);
        RenderSystem.lineWidth(1);
        Vec3d subtractable = Render3DHelper.INSTANCE.getEntityRenderPosition(Wrapper.INSTANCE.getLocalPlayer(), eventRender3D.getPartialTicks()).subtract(pos);
        Render3DHelper.INSTANCE.drawSphere(matrixStack, 128, 25, sphereColor, !seethrough, Vec3d.ZERO.subtract(subtractable));
        Render3DHelper.INSTANCE.end3DRender();
        matrixStack.pop();
    }

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
