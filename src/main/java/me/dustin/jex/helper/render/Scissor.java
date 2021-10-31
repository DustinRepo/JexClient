package me.dustin.jex.helper.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.misc.Wrapper;
import org.lwjgl.opengl.GL11;

public enum Scissor {

    INSTANCE;

    public void cut(int x, int y, int width, int height) {
        double factor = Render2DHelper.INSTANCE.getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((Wrapper.INSTANCE.getWindow().getHeight() - (y * factor) - height * factor)), (int) (width * factor), (int) (height * factor));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public void seal() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

}
