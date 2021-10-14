package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.TPSHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.StringUtils;

public class DirectionElement extends HudElement {
    public DirectionElement(float x, float y, float minWidth, float minHeight) {
        super("Direction", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        String str = String.format("Direction\247f: \2477%s %s", StringUtils.capitalize(Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing().getName()), getDirection());
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public boolean isVisible() {
        return getHud().info && getHud().direction;
    }

    private String getDirection() {
        Direction direction = Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing();
        String string7;
        switch (direction) {
            case NORTH:
                string7 = "(-Z)";
                break;
            case SOUTH:
                string7 = "(+Z)";
                break;
            case WEST:
                string7 = "(-X)";
                break;
            case EAST:
                string7 = "(+X)";
                break;
            default:
                string7 = "";
        }
        return string7;
    }
}
