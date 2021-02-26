package me.dustin.jex.module.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

import java.awt.*;

@ModClass(name = "SpawnSphere", category = ModCategory.WORLD, description = "Show a 128 block radius sphere around an area to see all spots mobs could spawn in that radius.")
public class SpawnSphere extends Module {

    @Op(name = "Mode", all = {"Dots", "Lines"})
    public String mode = "Dots";
    @Op(name = "Sphere Color", isColor = true)
    public int sphereColor = new Color(255, 250, 0).getRGB();
    @Op(name = "See-Through")
    public boolean seethrough = true;

    private Vec3d pos;

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        if (seethrough)
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(1);
        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(pos);
        GL11.glTranslated(renderPos.x, renderPos.y, renderPos.z);
        Render2DHelper.INSTANCE.glColor(sphereColor);
        GL11.glPointSize(3f);
        GL11.glRotated(90, 1, 0, 0);
        Sphere sphere = new Sphere();
        sphere.setDrawStyle(mode.equalsIgnoreCase("Dots") ? GLU.GLU_POINT : GLU.GLU_SILHOUETTE);
        sphere.setNormals(GLU.GLU_SMOOTH);
        sphere.draw(128, (int)30, 20);
        GL11.glTranslated(-renderPos.x, -renderPos.y, -renderPos.z);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        if (seethrough)
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
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
