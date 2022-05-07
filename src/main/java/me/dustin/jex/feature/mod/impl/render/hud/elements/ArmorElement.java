package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.mod.impl.render.CustomFont;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ArmorElement extends HudElement{
    public ArmorElement(float x, float y, float minWidth, float minHeight) {
        super("Armor", x, y, minWidth, minHeight);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        int count = 0;
        for (ItemStack itemStack : InventoryHelper.INSTANCE.getInventory().armor) {
            if (itemStack.getItem() == Items.AIR)
                continue;
            float x = (getX() + getWidth() - 16 - (16 * count));
            Render2DHelper.INSTANCE.drawItem(itemStack, x, getY());
            if (itemStack.isEnchanted() && getHud().drawEnchants) {
                float scale = 0.5f;
                matrixStack.pushPose();
                matrixStack.scale(scale, scale, 1);
                int enchCount = 1;
                for (Tag tag : itemStack.getEnchantmentTags()) {
                    try {
                        CompoundTag compoundTag = (CompoundTag) tag;
                        float newY = !isTopSide() ? ((getY() + getHeight() + ((10 * scale) * (enchCount - 1)) + 0.5f) / scale) : ((getY() - ((10 * scale) * enchCount) + 0.5f) / scale);
                        float newerX = (x / scale) + 1;
                        String name = getEnchantName(compoundTag);
                        if (compoundTag.getString("id").equalsIgnoreCase("minecraft:binding_curse") || compoundTag.getString("id").equalsIgnoreCase("minecraft:vanishing_curse"))
                            name = "\247c" + name;
                        float nameWidth = FontHelper.INSTANCE.getStringWidth(name);
                        Render2DHelper.INSTANCE.fill(matrixStack, newerX, newY - 1, newerX + nameWidth, newY + 9, 0x35000000);
                        FontHelper.INSTANCE.draw(matrixStack, name, newerX + 1.5f, newY, ColorHelper.INSTANCE.getClientColor());
                        enchCount++;
                    } catch (Exception ignored) {}
                }
                matrixStack.popPose();
            }
            count++;
        }
        this.setWidth(16 * count);
    }

    @Override
    public boolean isVisible() {
        return getHud().armor;
    }

    private String getEnchantName(CompoundTag compoundTag) {
        int level = compoundTag.getShort("lvl");
        String name = compoundTag.getString("id").split(":")[1];
        if (name.contains("_")) {
            String[] s = name.split("_");
            name = s[0].substring(0, 1).toUpperCase() + s[0].substring(1, 3) + s[1].substring(0, 1).toUpperCase();
        } else {
            name = name.substring(0, 1).toUpperCase() + name.substring(1, 3);
        }
        name += level;
        return name;
    }
}
