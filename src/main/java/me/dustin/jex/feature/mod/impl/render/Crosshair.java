package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRenderCrosshair;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Draw a custom crosshair on-screen.")
public class Crosshair extends Feature {

	@Op(name = "Color", isColor = true)
	public int color = 0xffff0000;

	@Op(name = "Thickness", min = 0, max = 20, inc = 0.1f)
	public float thickness = 2;
	@Op(name = "Size", min = 0, max = 20, inc = 0.1f)
	public float size = 7;
	@Op(name = "Gap", min = 0, max = 20, inc = 0.1f)
	public float gap = 1;
	@Op(name = "Outline", min = 0, max = 10, inc = 0.5f)
	public float outline = 2;
	@Op(name = "Attack Indicator")
	public boolean attackIndicator = false;
	@Op(name = "Spin")
	public boolean spin = false;
	@OpChild(name = "Spin Speed", min = 1, max = 5, parent = "Spin")
	public int spinSpeed = 1;

	private int spinAmount;
	private final StopWatch stopWatch = new StopWatch();

	@EventPointer
	private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
		float x = Render2DHelper.INSTANCE.getScaledWidth() / 2.f;
		float y = Render2DHelper.INSTANCE.getScaledHeight() / 2.f;
		MatrixStack matrixStack = ((EventRender2D) event).getMatrixStack();
		if (spin) {
			matrixStack.push();
			matrixStack.translate(Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f, 0);
			matrixStack.multiply(new Quaternion(new Vec3f(0F, 0F, 1F), spinAmount, true));
			matrixStack.translate(-(Render2DHelper.INSTANCE.getScaledWidth() / 2.f), -(Render2DHelper.INSTANCE.getScaledHeight() / 2.f), 0);
		}
		Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - gap - size - thickness, y - thickness, x - gap - thickness, y + thickness, 0xff000000, color, outline);
		Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x + gap + thickness, y - thickness, x + gap + size + thickness, y + thickness, 0xff000000, color, outline);
		Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - thickness, y - gap - size - thickness, x + thickness, y - gap - thickness, 0xff000000, color, outline);
		Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - thickness, y + gap + thickness, x + thickness, y + gap + size + thickness, 0xff000000, color, outline);
		if (spin) {
			matrixStack.pop();
		}
		if (attackIndicator && Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) < 1) {
			float width = 30;
			if (Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) > 0)
				Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - 15, y + gap + size + thickness + 10, x - 15 + (width * Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0)), y + gap + size + thickness + 14, 0x00000000, color, 1);
			Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - 15, y + gap + size + thickness + 10, x + 15, y + gap + size + thickness + 14, 0xff000000, 0x00ffffff, 1);
		}
		if (!stopWatch.hasPassed(20 / spinSpeed))
			return;
		stopWatch.reset();
		spinAmount += spinSpeed;
		if (spinAmount > 360)
			spinAmount -= 360;
	});

	@EventPointer
	private final EventListener<EventRenderCrosshair> eventRenderCrosshairEventListener = new EventListener<>(event -> event.cancel());
}
