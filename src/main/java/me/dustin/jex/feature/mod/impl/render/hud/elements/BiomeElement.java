package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.core.Registry;
import org.apache.commons.lang3.text.WordUtils;

public class BiomeElement extends HudElement {
    public BiomeElement(float x, float y, float minWidth, float minHeight) {
        super("Biome", x, y, minWidth, minHeight);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        String biome = Wrapper.INSTANCE.getWorld().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(Wrapper.INSTANCE.getWorld().getBiome(Wrapper.INSTANCE.getLocalPlayer().blockPosition()).value()).getPath().replace("_", " ");
        biome = WordUtils.capitalizeFully(biome);
        String str = String.format("Biome\247f: \2477%s", biome);
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public boolean isVisible() {
        return getHud().info && getHud().biome;
    }
}
