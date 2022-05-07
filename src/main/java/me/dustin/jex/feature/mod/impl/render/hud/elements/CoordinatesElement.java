package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.world.phys.Vec3;

public class CoordinatesElement extends HudElement{
    
    public CoordinatesElement(float x, float y, float minWidth, float minHeight) {
        super("Coordinates", x, y, minWidth, minHeight);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        float longest = 0;

        Vec3 pos = Wrapper.INSTANCE.getLocalPlayer().position();
        String coordString = String.format("XYZ\247f: \2477%.2f\247f/\2477%.2f\247f/\2477%.2f", pos.x(), pos.y(), pos.z());
        float strLength = FontHelper.INSTANCE.getStringWidth(coordString);
        float strX = isLeftSide() ? getX() + 3 : getX() + getWidth() - strLength;
        float strY = getY() + (isTopSide() ? 2.5f : 12.5f);
        if (strLength > longest)
            longest = strLength;
        FontHelper.INSTANCE.drawWithShadow(matrixStack, coordString, strX, strY, ColorHelper.INSTANCE.getClientColor());

        if (getHud().netherCoords) {
            double coordScale = Wrapper.INSTANCE.getLocalPlayer().clientLevel.dimensionType().coordinateScale();
            if (coordScale != 1.0D) {
                coordString = String.format("Overworld\247f: \2477%.2f\247f/\2477%.2f\247f/\2477%.2f", pos.x() * coordScale, pos.y() * coordScale, pos.z() * coordScale);
            } else {
                coordString = String.format("Nether\247f: \2477%.2f\247f/\2477%.2f\247f/\2477%.2f", pos.x() / 8, pos.y(), pos.z() / 8);
            }
            strLength = FontHelper.INSTANCE.getStringWidth(coordString);
            strX = isLeftSide() ? getX() + 3 : getX() + getWidth() - strLength;
            strY = getY() + (!isTopSide() ? 2.5f : 12.5f);
            if (strLength > longest)
                longest = strLength;
            FontHelper.INSTANCE.drawWithShadow(matrixStack, coordString, strX, strY, ColorHelper.INSTANCE.getClientColor());
        }

        this.setHeight(getHud().netherCoords ? 22.5f : 12.5f);
        this.setWidth(longest + 4);
    }

    @Override
    public boolean isVisible() {
        return getHud().coords;
    }
}
