package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.render.EventRenderHeldItem;
import me.dustin.jex.event.render.EventRenderItem;
import me.dustin.jex.helper.entity.EntityHelper;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Get the pre-1.9 block animation when blocking with a shield.")
public class OldBlocking extends Feature {

    @Op(name = "Mode", all = {"Swords", "Tools", "All Items"})
    public String mode = "Swords";

    @EventPointer
    private final EventListener<EventRenderItem> eventRenderItemEventListener = new EventListener<>(event -> {
        if (event.getType().firstPerson()) {
            PoseStack matrixStack = event.getPoseStack();
            boolean offHand = event.isLeftHanded() ? event.getType() == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : event.getType() == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;

            if (EntityHelper.INSTANCE.isAuraBlocking())
                switch (event.getRenderTime()) {
                    case PRE -> {
                        matrixStack.pushPose();
                        if (!offHand) {
                            if (isGoodItem(event.getItemStack().getItem())) {
                                //point the tip outward
                                matrixStack.mulPose(new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), -60, true));
                                //rotate infront of camera
                                matrixStack.mulPose(new Quaternion(new Vector3f(0.0F, 0.0F, 1.0F), 60, true));
                                //tilt
                                matrixStack.mulPose(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 50, true));
                                matrixStack.translate(-0.05, 0.1, 0.07);
                            }
                        }
                    }
                    case POST -> matrixStack.popPose();
                }

        }
    }, Priority.LAST);

    @EventPointer
    private final EventListener<EventRenderHeldItem> eventRenderHeldItemEventListener = new EventListener<>(event -> {
        if (event.getHand() == InteractionHand.OFF_HAND && event.getItemStack().getItem() instanceof ShieldItem && EntityHelper.INSTANCE.isAuraBlocking())
            event.cancel();
    });

    private boolean isGoodItem(Item item) {
        return switch (mode.toLowerCase()) {
            case "swords" -> item instanceof SwordItem;
            case "tools" -> item instanceof TieredItem;
            case "all items" -> true;
            default -> false;
        };
    }

}
