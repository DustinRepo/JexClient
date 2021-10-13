package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.math.MatrixStack;

public class BuildInfoElement extends HudElement {
    public BuildInfoElement(float x, float y, float minWidth, float minHeight) {
        super("Build Info", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!getHud().info || !getHud().buildInfo)
            return;
        super.render(matrixStack);
        String str = String.format("Build Info: \2477%s %s", JexClient.INSTANCE.getBuildMetaData().equals("${buildVersion}") ? "Built Improperly" : JexClient.INSTANCE.getBuildMetaData(), FabricLoader.getInstance().isDevelopmentEnvironment() ? "(\247rDev\2477)" : "(\247rRelease\2477)");
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if (!getHud().info || !getHud().buildInfo)
            return;
        super.click(mouseX, mouseY, mouseButton);
    }
}
