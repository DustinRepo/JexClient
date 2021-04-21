package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderHeldItem;
import me.dustin.jex.event.render.EventRenderItem;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

@Feat(name = "OldBlocking", category = FeatureCategory.VISUAL, description = "Get the pre-1.9 block animation when blocking with a shield.")
public class OldBlocking extends Feature {

    @Op(name = "Mode", all = {"Swords", "Tools", "All Items"})
    public String mode = "Swords";

    @EventListener(events = {EventRenderItem.class})
    private void runMethod(EventRenderItem eventRenderItem) {
        if (eventRenderItem.getType().isFirstPerson()) {
            MatrixStack matrixStack = eventRenderItem.getMatrixStack();
            boolean offHand = eventRenderItem.isLeftHanded() ? eventRenderItem.getType() == ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND : eventRenderItem.getType() == ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND;

            if (EntityHelper.INSTANCE.isAuraBlocking())
                switch (eventRenderItem.getRenderTime()) {
                    case PRE:
                        matrixStack.push();
                        if (!offHand) {
                            if (isGoodItem(eventRenderItem.getItemStack().getItem())) {
                                //point the tip outward
                                matrixStack.multiply(new Quaternion(new Vec3f(1.0F, 0.0F, 0.0F), -60, true));
                                //rotate infront of camera
                                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, 1.0F), 60, true));
                                //tilt
                                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 1.0F, 0.0F), 50, true));
                                matrixStack.translate(-0.05, 0.1, 0.07);
                            }
                        }
                        break;
                    case POST:
                        matrixStack.pop();
                        break;
                }

        }
    }

    @EventListener(events = {EventRenderHeldItem.class})
    private void heldItem(EventRenderHeldItem eventRenderHeldItem) {
        if (eventRenderHeldItem.getHand() == Hand.OFF_HAND && eventRenderHeldItem.getItemStack().getItem() instanceof ShieldItem && EntityHelper.INSTANCE.isAuraBlocking())
            eventRenderHeldItem.cancel();
    }

    private boolean isGoodItem(Item item) {
        switch (mode.toLowerCase()) {
            case "swords":
                return item instanceof SwordItem;
            case "tools":
                return item instanceof ToolItem;
            case "all items":
                return true;
        }
        return false;
    }

}
