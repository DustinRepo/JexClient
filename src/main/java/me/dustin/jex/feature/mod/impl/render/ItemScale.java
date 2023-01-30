package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderItem;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import net.minecraft.client.util.math.MatrixStack;

public class ItemScale extends Feature {

    public final Property<Boolean> rightHandProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Right Hand")
            .value(true)
            .build();
    public final Property<Float> rightHandScaleProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("RH Scale")
            .value(1f)
            .min(0f)
            .max(1f)
            .inc(0.01f)
            .parent(rightHandProperty)
            .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Float> rightHandXProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("RH X")
            .value(0f)
            .min(-2)
            .max(2)
            .inc(0.02f)
            .parent(rightHandProperty)
            .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Float> rightHandYProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("RH Y")
            .value(0f)
            .min(-2)
            .max(2)
            .inc(0.02f)
            .parent(rightHandProperty)
            .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Float> rightHandZProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("RH Z")
            .value(0f)
            .min(-2)
            .max(2)
            .inc(0.02f)
            .parent(rightHandProperty)
            .depends(parent -> (boolean)parent.value())
            .build();

    public final Property<Boolean> leftHandProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Left Hand")
            .value(true)
            .build();
    public final Property<Float> leftHandScaleProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("LH Scale")
            .value(1f)
            .min(0f)
            .max(1)
            .inc(0.01f)
            .parent(leftHandProperty)
            .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Float> leftHandXProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("LH X")
            .value(0f)
            .min(-2)
            .max(2)
            .inc(0.02f)
            .parent(leftHandProperty)
            .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Float> leftHandYProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("LH Y")
            .value(0f)
            .min(-2)
            .max(2)
            .inc(0.02f)
            .parent(leftHandProperty)
            .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Float> leftHandZProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("LH Z")
            .value(0f)
            .min(-2)
            .max(2)
            .inc(0.02f)
            .parent(leftHandProperty)
            .depends(parent -> (boolean)parent.value())
            .build();

    public ItemScale() {
        super(Category.VISUAL, "Change the scale and positioning of items in your hands");
    }

    @EventPointer
    private final EventListener<EventRenderItem> eventRenderItemEventListener = new EventListener<>(event -> {
        if (event.getType().isFirstPerson()) {
            MatrixStack matrixStack = event.getPoseStack();
            switch (event.getRenderTime()) {
                case PRE -> {
                    matrixStack.push();
                    switch (event.getType()) {
                        case FIRST_PERSON_RIGHT_HAND -> {
                            matrixStack.translate(rightHandXProperty.value(), rightHandYProperty.value(), rightHandZProperty.value());
                            matrixStack.scale(rightHandScaleProperty.value(), rightHandScaleProperty.value(), rightHandScaleProperty.value());
                        }
                        case FIRST_PERSON_LEFT_HAND -> {
                            matrixStack.translate(leftHandXProperty.value(), leftHandYProperty.value(), leftHandZProperty.value());
                            matrixStack.scale(leftHandScaleProperty.value(), leftHandScaleProperty.value(), leftHandScaleProperty.value());
                        }
                    }
                }
                case POST -> matrixStack.pop();
            }
        }
    });
}
