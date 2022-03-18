package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PotionEffectsElement extends HudElement {

    public PotionEffectsElement(float x, float y, float minWidth, float minHeight) {
        super("Potion Effects", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        float longestString = 0;
        int strCount = 0;
        AtomicInteger spriteCount = new AtomicInteger();
        List<Runnable> list_1 = Lists.newArrayListWithExpectedSize(Wrapper.INSTANCE.getLocalPlayer().getActiveStatusEffects().size());
        for (StatusEffectInstance effect : Wrapper.INSTANCE.getLocalPlayer().getActiveStatusEffects().values()) {
            String effectString = String.format("%s %s\247f: \2477%s", effect.getEffectType().getName().getString(), getAmpString(effect), StatusEffectUtil.durationToString(effect, 1.0F));
            if (FontHelper.INSTANCE.getStringWidth(effectString) > longestString)
                longestString = FontHelper.INSTANCE.getStringWidth(effectString);
            float strY = isTopSide() ? this.getY() + 2 + (strCount * 10) : this.getY() + this.getHeight() - 10 - (strCount * 10);
            float strX = isLeftSide() ? this.getX() + 3 : this.getX() + this.getWidth() - FontHelper.INSTANCE.getStringWidth(effectString) - (getHud().icons ? 11 : 2);
            FontHelper.INSTANCE.drawWithShadow(matrixStack, effectString, strX, strY, effect.getEffectType().getColor());
            if (getHud().icons) {
                Sprite sprite_1 = Wrapper.INSTANCE.getMinecraft().getStatusEffectSpriteManager().getSprite(effect.getEffectType());
                list_1.add(() -> {
                    float spriteY = isTopSide() ? this.getY() + 2 + (spriteCount.get() * 10) : this.getY() + this.getHeight() - 11 - (spriteCount.get() * 10);
                    float spriteX = isLeftSide() ? this.getX() + 3 + FontHelper.INSTANCE.getStringWidth(effectString) : this.getX() + this.getWidth() - 10;

                    Render2DHelper.INSTANCE.bindTexture(sprite_1.getAtlas().getId());
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1);
                    DrawableHelper.drawSprite(matrixStack, (int)spriteX, (int)spriteY, 50, 9, 9, sprite_1);
                    spriteCount.getAndIncrement();
                });
            }
            strCount++;
        }
        this.setHeight(3 + (strCount * 10));
        this.setWidth(longestString + 14);

        list_1.forEach(Runnable::run);
    }

    @Override
    public boolean isVisible() {
        return getHud().potionEffects;
    }

    private String getAmpString(StatusEffectInstance effectInstance) {
        switch (effectInstance.getAmplifier()) {
            case -1:
                return ">120";
            case 0:
                return "I";
            case 1:
                return "II";
            case 2:
                return "III";
            case 3:
                return "IV";
            case 4:
                return "V";
            case 5:
                return "VI";
            case 6:
                return "VII";
            case 7:
                return "VIII";
            case 8:
                return "IX";
            case 9:
                return "X";
        }
        return effectInstance.getAmplifier() + "";
    }
}
