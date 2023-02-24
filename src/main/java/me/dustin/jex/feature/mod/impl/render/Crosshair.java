package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRenderCrosshair;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import me.dustin.jex.feature.mod.core.Feature;

import java.awt.*;

public class Crosshair extends Feature {

	public final Property<Color> colorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("Color")
			.value(Color.RED)
			.build();
	public final Property<Float> thicknessProperty = new Property.PropertyBuilder<Float>(this.getClass())
			.name("Thickness")
			.value(2f)
			.min(0)
			.max(20)
			.inc(0.1f)
			.build();
	public final Property<Float> sizeProperty = new Property.PropertyBuilder<Float>(this.getClass())
			.name("Size")
			.value(7f)
			.min(0)
			.max(20)
			.inc(0.1f)
			.build();
	public final Property<Float> gapProperty = new Property.PropertyBuilder<Float>(this.getClass())
			.name("Gap")
			.value(1f)
			.min(-1)
			.max(20)
			.inc(0.1f)
			.build();
	public final Property<Float> outlineProperty = new Property.PropertyBuilder<Float>(this.getClass())
			.name("Outline")
			.value(2f)
			.min(0)
			.max(20)
			.inc(0.5f)
			.build();
	public final Property<Boolean> attackIndicatorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Attack Indicator")
			.value(true)
			.build();
	public final Property<Boolean> spinProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Spin")
			.value(false)
			.build();
	public final Property<Integer> spinSpeedProperty = new Property.PropertyBuilder<Integer>(this.getClass())
			.name("Spin Speed")
			.value(1)
			.min(1)
			.max(20)
			.parent(spinProperty)
			.depends(parent -> (boolean) parent.value())
			.build();

	private int spinAmount;
	private final StopWatch stopWatch = new StopWatch();

	public Crosshair() {
		super(Category.VISUAL, "Draw a custom crosshair on-screen.");
	}

	@EventPointer
	private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
		float x = Render2DHelper.INSTANCE.getScaledWidth() / 2.f;
		float y = Render2DHelper.INSTANCE.getScaledHeight() / 2.f;
		MatrixStack matrixStack = ((EventRender2D) event).getPoseStack();
		if (spinProperty.value()) {
			matrixStack.push();
			matrixStack.translate(Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f, 0);
			matrixStack.multiply(new Quaternion(new Vec3f(0F, 0F, 1F), spinAmount, true));
			matrixStack.translate(-(Render2DHelper.INSTANCE.getScaledWidth() / 2.f), -(Render2DHelper.INSTANCE.getScaledHeight() / 2.f), 0);
		}
		Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - gapProperty.value() - sizeProperty.value() - thicknessProperty.value(), y - thicknessProperty.value(), x - gapProperty.value() - thicknessProperty.value(), y + thicknessProperty.value(), 0xff000000, colorProperty.value().getRGB(), outlineProperty.value());
		Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x + gapProperty.value() + thicknessProperty.value(), y - thicknessProperty.value(), x + gapProperty.value() + sizeProperty.value() + thicknessProperty.value(), y + thicknessProperty.value(), 0xff000000, colorProperty.value().getRGB(), outlineProperty.value());
		Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - thicknessProperty.value(), y - gapProperty.value() - sizeProperty.value() - thicknessProperty.value(), x + thicknessProperty.value(), y - gapProperty.value() - thicknessProperty.value(), 0xff000000, colorProperty.value().getRGB(), outlineProperty.value());
		Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - thicknessProperty.value(), y + gapProperty.value() + thicknessProperty.value(), x + thicknessProperty.value(), y + gapProperty.value() + sizeProperty.value() + thicknessProperty.value(), 0xff000000, colorProperty.value().getRGB(), outlineProperty.value());
		if (spinProperty.value()) {
			matrixStack.pop();
		}
		if (attackIndicatorProperty.value() && Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) < 1) {
			float width = 30;
			if (Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) > 0)
				Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - 15, y + gapProperty.value() + sizeProperty.value() + thicknessProperty.value() + 10, x - 15 + (width * Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0)), y + gapProperty.value() + sizeProperty.value() + thicknessProperty.value() + 14, 0x00000000, colorProperty.value().getRGB(), 1);
			Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - 15, y + gapProperty.value() + sizeProperty.value() + thicknessProperty.value() + 10, x + 15, y + gapProperty.value() + sizeProperty.value() + thicknessProperty.value() + 14, 0xff000000, 0x00ffffff, 1);
		}
		if (!stopWatch.hasPassed((long) (20 / spinSpeedProperty.value())))
			return;
		stopWatch.reset();
		spinAmount += spinSpeedProperty.value();
		if (spinAmount > 360)
			spinAmount -= 360;
	});

	@EventPointer
	private final EventListener<EventRenderCrosshair> eventRenderCrosshairEventListener = new EventListener<>(event -> event.cancel());
}
