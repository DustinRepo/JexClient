package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.text.WordUtils;

public class BiomeElement extends HudElement {
    public BiomeElement(float x, float y, float minWidth, float minHeight) {
        super("Biome", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!getHud().info || !getHud().biome)
            return;
        super.render(matrixStack);
        String biome = Wrapper.INSTANCE.getWorld().getRegistryManager().get(Registry.BIOME_KEY).getId(Wrapper.INSTANCE.getWorld().getBiome(Wrapper.INSTANCE.getLocalPlayer().getBlockPos())).getPath().replace("_", " ");
        biome = WordUtils.capitalizeFully(biome);
        String str = String.format("Biome\247f: \2477%s", biome);
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if (!getHud().info || !getHud().biome)
            return;
        super.click(mouseX, mouseY, mouseButton);
    }
}
