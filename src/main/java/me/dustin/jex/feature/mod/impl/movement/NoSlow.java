package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventSlowdown;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Prevent actions from slowing you down")
public class NoSlow extends Feature {

	@Op(name = "Use Item")
	public boolean useItem = true;
	@Op(name = "Soul Sand")
	public boolean soulSand = true;
	@Op(name = "Cobweb")
	public boolean cobweb = true;
	@Op(name = "Berry Bush")
	public boolean berryBush = true;
	@Op(name = "Powder Snow")
	public boolean powderSnow = true;

	@EventPointer
	private final EventListener<EventSlowdown> eventSlowdownEventListener = new EventListener<>(event -> {
		if (event.getState() == EventSlowdown.State.USE_ITEM && useItem) {
			event.cancel();
		}
		if (event.getState() == EventSlowdown.State.COBWEB && cobweb) {
			event.cancel();
		}
		if (event.getState() == EventSlowdown.State.BERRY_BUSH && berryBush) {
			event.cancel();
		}
		if (event.getState() == EventSlowdown.State.POWDERED_SNOW && powderSnow) {
			event.cancel();
		}
	}, Priority.FIRST);

	@EventPointer
	private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
		Block block = WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getLocalPlayer(), 0.7f);
		if (block == Blocks.SOUL_SAND && soulSand) {
			event.setX(event.getX() * 1.72111554);
			event.setZ(event.getZ() * 1.72111554);
		}
	}, Priority.FIRST);
}
