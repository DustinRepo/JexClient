package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderItem;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.client.util.math.MatrixStack;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Change the scale and positioning of items in your hands")
public class ItemScale extends Feature {

    @Op(name = "Right Hand")
    public boolean rightHand = true;
    @OpChild(name = "RH Scale", min = 0.1f, max = 3, inc = 0.05f, parent = "Right Hand")
    public float rightHandScale = 1;
    @OpChild(name = "RH X", min = -2, max = 2, inc = 0.05f, parent = "Right Hand")
    public float rightHandX;
    @OpChild(name = "RH Y", min = -2, max = 2, inc = 0.05f, parent = "Right Hand")
    public float rightHandY;
    @OpChild(name = "RH Z", min = -2, max = 2, inc = 0.05f, parent = "Right Hand")
    public float rightHandZ;

    @Op(name = "Left Hand")
    public boolean leftHand = true;
    @OpChild(name = "LH Scale", min = 0.1f, max = 3, inc = 0.05f, parent = "Left Hand")
    public float leftHandScale = 1;
    @OpChild(name = "LH X", min = -2, max = 2, inc = 0.05f, parent = "Left Hand")
    public float leftHandX;
    @OpChild(name = "LH Y", min = -2, max = 2, inc = 0.05f, parent = "Left Hand")
    public float leftHandY;
    @OpChild(name = "LH Z", min = -2, max = 2, inc = 0.05f, parent = "Left Hand")
    public float leftHandZ;

    @EventPointer
    private final EventListener<EventRenderItem> eventRenderItemEventListener = new EventListener<>(event -> {
        if (event.getType().isFirstPerson()) {
            MatrixStack matrixStack = event.getMatrixStack();
            switch (event.getRenderTime()) {
                case PRE -> {
                    matrixStack.push();
                    switch (event.getType()) {
                        case FIRST_PERSON_RIGHT_HAND -> {
                            matrixStack.translate(rightHandX, rightHandY, rightHandZ);
                            matrixStack.scale(rightHandScale, rightHandScale, rightHandScale);
                        }
                        case FIRST_PERSON_LEFT_HAND -> {
                            matrixStack.translate(leftHandX, leftHandY, leftHandZ);
                            matrixStack.scale(leftHandScale, leftHandScale, leftHandScale);
                        }
                    }
                }
                case POST -> matrixStack.pop();
            }
        }
    });
}
