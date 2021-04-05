package me.dustin.jex.helper.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.misc.Wrapper;

public enum Scissor {

    INSTANCE;

    public void cut(int x, int y, int width, int height) {
        double factor = Render2DHelper.INSTANCE.getScaleFactor();
        int factor2 = Render2DHelper.INSTANCE.getScaledWidth();
        int factor3 = Render2DHelper.INSTANCE.getScaledHeight();
        RenderSystem.enableScissor((int) (x * factor), (int) ((Wrapper.INSTANCE.getWindow().getHeight() - (y * factor) - height * factor)), (int) (width * factor), (int) (height * factor));
    }

    public void seal() {
        RenderSystem.disableScissor();
    }

}
