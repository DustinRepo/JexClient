package me.dustin.jex.feature.mod.impl.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.option.annotate.Op;
import java.util.HashMap;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Display the text of a hovered sign on screen.")
public class SignReader extends Feature {

    @Op(name = "Scale", min = 0.1f, max = 2, inc = 0.05f)
    public float scale = 1;
    @Op(name = "Hover Only")
    public boolean hoverOnly = true;
    @Op(name = "Backgrounds")
    public boolean backgrounds = true;

    private HashMap<SignBlockEntity, Vec3> positions = Maps.newHashMap();

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        positions.clear();
        if (hoverOnly) {
            HitResult result = Wrapper.INSTANCE.getLocalPlayer().pick(1024, 1, false);// Wrapper.clientWorld().rayTraceBlock(getVec(entity), getVec(entity).add(0, -256, 0), false, true, false);
            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult)result;
                if (Wrapper.INSTANCE.getWorld().getBlockEntity(blockHitResult.getBlockPos()) instanceof SignBlockEntity signBlockEntity) {
                    if (signBlockEntity != null) {
                        Vec3 pos = new Vec3(signBlockEntity.getBlockPos().getX(), signBlockEntity.getBlockPos().getY(), signBlockEntity.getBlockPos().getZ());
                        positions.put(signBlockEntity, Render2DHelper.INSTANCE.to2D(pos.add(0.5f, 1.5, 0.5f), event.getPoseStack()));
                    }
                }
            }
        } else {
            for (BlockEntity blockEntity : WorldHelper.INSTANCE.getBlockEntities()) {
                if (blockEntity instanceof SignBlockEntity signBlockEntity) {
                    Vec3 pos = new Vec3(signBlockEntity.getBlockPos().getX(), signBlockEntity.getBlockPos().getY(), signBlockEntity.getBlockPos().getZ());
                    positions.put(signBlockEntity, Render2DHelper.INSTANCE.to2D(pos.add(0.5f, 1.5, 0.5f), event.getPoseStack()));
                }
            }
        }
    });

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        PoseStack matrixStack = ((EventRender2D) event).getPoseStack();
        matrixStack.pushPose();
        matrixStack.scale(scale, scale,1);
        positions.forEach((signBlockEntity, vec3d) -> {
            if (Render2DHelper.INSTANCE.isOnScreen(vec3d)) {
                float x = (float)vec3d.x / scale;
                float y = (float)vec3d.y / scale;
                int count = 0;
                for (int i = 0; i < 4; i++) {
                    String text = signBlockEntity.getMessage(3 - i, false).getString().trim();
                    float strWidth = FontHelper.INSTANCE.getStringWidth(FontHelper.INSTANCE.fix(text));
                    if (!text.isEmpty()) {
                        if (backgrounds)
                            Render2DHelper.INSTANCE.fill(((EventRender2D) event).getPoseStack(), x - (strWidth / 2) - 2, y - (10 * count) - 0.5f, x + (strWidth / 2) + 2, y - (10 * count) + 9.5f, 0x35000000);

                        FontHelper.INSTANCE.drawCenteredString(((EventRender2D) event).getPoseStack(), text, x, y - (10 * count) + 0.5f, -1);
                        count++;
                    }
                }
            }
        });
        matrixStack.scale(1 / scale, 1 / scale,1);
        matrixStack.popPose();
    });
}
