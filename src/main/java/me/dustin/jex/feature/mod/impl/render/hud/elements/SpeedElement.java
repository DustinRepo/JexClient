package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.text.WordUtils;

public class SpeedElement extends HudElement {
    public SpeedElement(float x, float y, float minWidth, float minHeight) {
        super("Speed", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
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
        return getHud().infoProperty.value() && getHud().speedProperty.value();
    }

    private String generateSpeedText() {
        Vec3d move = new Vec3d(Wrapper.INSTANCE.getLocalPlayer().getX() - Wrapper.INSTANCE.getLocalPlayer().prevX, 0, Wrapper.INSTANCE.getLocalPlayer().getZ() - Wrapper.INSTANCE.getLocalPlayer().prevZ).multiply(20);
        switch (getHud().distanceModeProperty.value()) {
            case BLOCKS, M:
                break;
            case FEET:
                move = move.multiply(3.281);
                break;
            case MILES:
                move = move.multiply(0.000621371);
                break;
            case KM:
                move = move.multiply(0.001);
                break;

        }
        float time = 1;
        switch (getHud().timeModeProperty.value()) {
            case TICK:
                time /= 20;
                break;
            case SECOND:
                break;
            case MINUTE:
                time *= 60;
                break;
            case HOUR:
                time *= 3600;
                break;
            case DAY:
                time *= 86400;
                break;
        }
        return String.format("%.2f %s/%s", (float) (Math.abs(length2D(move)) * time), WordUtils.capitalize(getHud().distanceModeProperty.value().name().toLowerCase().replace("_", " ")), WordUtils.capitalize(getHud().timeModeProperty.value().name().toLowerCase().replace("_", " ")));
    }

    public double length2D(Vec3d vec3d) {
        return MathHelper.sqrt((float)(vec3d.x * vec3d.x + vec3d.z * vec3d.z));
    }
}
