package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.render.EventRenderHeldItem;
import me.dustin.jex.event.render.EventRenderItem;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.entity.EntityHelper;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

public class OldBlocking extends Feature {

    @Op(name = "Mode", all = {"Swords", "Tools", "All Items"})
    public String mode = "Swords";

    public OldBlocking() {
        super(Category.VISUAL, "Get the pre-1.9 block animation when blocking with a shield.");
    }

    @EventPointer
    private final EventListener<EventRenderItem> eventRenderItemEventListener = new EventListener<>(event -> {
        if (event.getType().isFirstPerson()) {
            MatrixStack matrixStack = event.getPoseStack();
            boolean offHand = event.isLeftHanded() ? event.getType() == ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND : event.getType() == ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND;

            if (EntityHelper.INSTANCE.isAuraBlocking())
                switch (event.getRenderTime()) {
                    case PRE -> {
                        matrixStack.push();
                        if (!offHand) {
                            if (isGoodItem(event.getItemStack().getItem())) {
                                //point the tip outward
                                matrixStack.multiply(new Quaternion(new Vec3f(1.0F, 0.0F, 0.0F), -60, true));
                                //rotate infront of camera
                                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, 1.0F), 60, true));
                                //tilt
                                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 1.0F, 0.0F), 50, true));
                                matrixStack.translate(-0.05, 0.1, 0.07);
                            }
                        }
                    }
                    case POST -> matrixStack.pop();
                }

        }
    }, Priority.LAST);

    @EventPointer
    private final EventListener<EventRenderHeldItem> eventRenderHeldItemEventListener = new EventListener<>(event -> {
        if (event.getHand() == Hand.OFF_HAND && event.getItemStack().getItem() instanceof ShieldItem && EntityHelper.INSTANCE.isAuraBlocking())
            event.cancel();
    });

    private boolean isGoodItem(Item item) {
        return switch (mode.toLowerCase()) {
            case "swords" -> item instanceof SwordItem;
            case "tools" -> item instanceof ToolItem;
            case "all items" -> true;
            default -> false;
        };
    }

}
