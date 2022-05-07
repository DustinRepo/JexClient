package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SpeedElement extends HudElement {
    public SpeedElement(float x, float y, float minWidth, float minHeight) {
        super("Speed", x, y, minWidth, minHeight);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        String str = String.format("Speed\247f:\2477 %s", generateSpeedText());
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public boolean isVisible() {
        return getHud().info && getHud().speed;
    }

    private String generateSpeedText() {
        Vec3 move = new Vec3(Wrapper.INSTANCE.getLocalPlayer().getX() - Wrapper.INSTANCE.getLocalPlayer().xo, 0, Wrapper.INSTANCE.getLocalPlayer().getZ() - Wrapper.INSTANCE.getLocalPlayer().zo).scale(20);
        switch (getHud().distanceMode) {
            case "Blocks":
                break;
            case "Feet":
                move = move.scale(3.281);
                break;
            case "Miles":
                move = move.scale(0.000621371);
                break;
            case "KM":
                move = move.scale(0.001);
                break;

        }
        float time = 1;
        switch (getHud().timeMode) {
            case "Tick":
                time /= 20;
                break;
            case "Second":
                break;
            case "Minute":
                time *= 60;
                break;
            case "Hour":
                time *= 3600;
                break;
            case "Day":
                time *= 86400;
                break;
        }
        return String.format("%.2f %s/%s", (float) (Math.abs(length2D(move)) * time), getHud().distanceMode, getHud().timeMode);
    }

    public double length2D(Vec3 vec3d) {
        return Mth.sqrt((float)(vec3d.x * vec3d.x + vec3d.z * vec3d.z));
    }
}
