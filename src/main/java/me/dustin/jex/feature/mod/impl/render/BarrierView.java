package me.dustin.jex.feature.mod.impl.render;

import java.util.ArrayList;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.Feature.Category;
import me.dustin.jex.feature.mod.core.Feature.Manifest;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.Render3DHelper.BoxStorage;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.BarrierBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Manifest(name = "BarrierView", category = Category.VISUAL, description = "See barriers as if you are in creative")
public class BarrierView extends Feature {
	
	@Op(name = "Color", isColor = true)
	public int color = 0xffff0000;
	
	private ArrayList<BlockPos> renderPositions = new ArrayList<>();
	private Timer timer = new Timer();
	
	@EventListener(events = {EventRender3D.class})
	private void runMethod(EventRender3D eventRender3D) {
		if (timer.hasPassed(250)) {
			renderPositions.clear();
			for (int x = -4; x < 4; x++) {
				for (int y = -4; y < 4; y++) {
					for (int z = -4; z < 4; z++) {
						BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
						if (WorldHelper.INSTANCE.getBlock(pos) instanceof BarrierBlock) {
							renderPositions.add(pos);
						}
					}
				}
			}
			timer.reset();
		}
		ArrayList<BoxStorage> list = new ArrayList<>();
		renderPositions.forEach(blockPos -> {
			Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
			Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
			list.add(new BoxStorage(box, color));
		});
		Render3DHelper.INSTANCE.drawList(eventRender3D.getMatrixStack(), list);
	}
}
