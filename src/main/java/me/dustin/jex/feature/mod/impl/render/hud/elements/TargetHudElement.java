package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.TargetHud;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.EntityPositionHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

public class TargetHudElement extends HudElement{
    private LivingEntity target;
    private TargetHud targetHud;
    public TargetHudElement(float x, float y, float minWidth, float minHeight) {
        super("Target Info", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (targetHud == null)
            targetHud = Feature.get(TargetHud.class);
        if (target != null) {
            if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(target) > targetHud.stopTargetDistanceProperty.value() || !target.isAlive()) {
                target = null;
            }
        }
        if (!isVisible())
            return;
        super.render(matrixStack);
        if (target != null) {
            if (targetHud.markTargetProperty.value()) {
                Vec3d headPos = EntityPositionHelper.INSTANCE.getHeadPos(target);
                if (Render2DHelper.INSTANCE.isOnScreen(headPos)) {
                    Color color = targetHud.markColorProperty.value();
                    BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                    int size = 5;
                    float[] bottom = {(float) headPos.x, (float) (headPos.y + size)};
                    float[] left = {(float) headPos.x - size / 2.f, (float) headPos.y};
                    float[] right = {(float) headPos.x + size / 2.f, (float)headPos.y};
                    float bob = (target.age % 20.f) / (20.f / 3.f);
                    matrixStack.push();
                    matrixStack.translate(0, bob, 0);
                    Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
                    bufferBuilder.vertex(matrix4f, bottom[0], bottom[1], 0).color(color.getRGB()).next();
                    bufferBuilder.vertex(matrix4f, left[0], left[1], 0).color(color.getRGB()).next();
                    bufferBuilder.vertex(matrix4f, left[0], left[1], 0).color(color.getRGB()).next();
                    bufferBuilder.vertex(matrix4f, right[0], right[1], 0).color(color.getRGB()).next();
                    bufferBuilder.vertex(matrix4f, right[0], right[1], 0).color(color.getRGB()).next();
                    bufferBuilder.vertex(matrix4f, bottom[0], bottom[1], 0).color(color.getRGB()).next();
                    bufferBuilder.clear();
                    BufferRenderer.drawWithShader(bufferBuilder.end());
                    matrixStack.translate(0, -bob, 0);
                    matrixStack.pop();
                }
            }
            float xOff = 20;
            if (target instanceof WardenEntity || target instanceof IronGolemEntity || target instanceof WitherEntity)
                xOff += 15;
            if (target instanceof SpiderEntity)
                xOff += 10;
            InventoryScreen.drawEntity((int) (getX() + xOff), (int) (getY() + (30 * target.getHeight())) + 10, 30, 0, 0, target);
            ArrayList<String> lines = new ArrayList<>();
            lines.add(target.getName().getString());
            lines.add("Health: %s%.1f%s/%s%.1f".formatted(Render2DHelper.INSTANCE.getPercentFormatting((target.getHealth() / target.getMaxHealth()) * 100), target.getHealth(), Formatting.RESET, Formatting.GREEN, target.getMaxHealth()));
            lines.add("Armor Level: %d".formatted(target.getArmor()));
            lines.add("Item Damage: %.1f".formatted(InventoryHelper.INSTANCE.getAdjustedDamage(target.getMainHandStack())));

            float textX = getX() + xOff + 20;
            if (target instanceof WardenEntity || target instanceof IronGolemEntity || target instanceof WitherEntity)
                textX += 15;
            int i = 0;
            for (String line : lines) {
                FontHelper.INSTANCE.drawWithShadow(matrixStack, line, textX, getY() + 4 + (i * 11), -1);
                i++;
            }
            setWidth(getEstWidth(xOff, lines));
            setHeight(30 * target.getHeight() + 12 > 75 ? (30 * target.getHeight()) + 12 : 75);
        }
    }

    private float getEstWidth(float targetOffset, ArrayList<String> lines) {
        String longest = "";
        for (String line : lines) {
            if (FontHelper.INSTANCE.getStringWidth(longest) < FontHelper.INSTANCE.getStringWidth(line))
                longest = line;
        }
        float textX = targetOffset + 20;
        if (target instanceof WardenEntity || target instanceof IronGolemEntity)
            textX += 15;
        return textX + FontHelper.INSTANCE.getStringWidth(longest) + 10;
    }

    @Override
    public boolean isVisible() {
        boolean vis = Feature.getState(TargetHud.class);
        if (EventManager.isRegistered(this)) {
            if (!vis)
                EventManager.unregister(this);
        } else {
            if (vis)
                EventManager.register(this);
        }
        return vis;
    }

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            target = livingEntity;
        }
    });
}
