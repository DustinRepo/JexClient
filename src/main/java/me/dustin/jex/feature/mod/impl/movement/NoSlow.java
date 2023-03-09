package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventSlowdown;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import me.dustin.jex.feature.mod.core.Feature;

public class NoSlow extends Feature {

	public final Property<Boolean> useItemProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Use Item")
			.value(true)
			.build();
	public final Property<Boolean> soulSandProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Soul Sand")
			.value(true)
			.build();
	public final Property<Boolean> cobwebProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Cobweb")
			.value(true)
			.build();
	public final Property<Boolean> berryBushProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Berry Bush")
			.value(true)
			.build();
	public final Property<Boolean> powderSnowProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Powder Snow")
			.value(true)
			.build();

	public NoSlow() {
		super(Category.MOVEMENT);
	}

	@EventPointer
	private final EventListener<EventSlowdown> eventSlowdownEventListener = new EventListener<>(event -> {
		if (event.getState() == EventSlowdown.State.USE_ITEM && useItemProperty.value()) {
			event.cancel();
		}
		if (event.getState() == EventSlowdown.State.COBWEB && cobwebProperty.value()) {
			event.cancel();
		}
		if (event.getState() == EventSlowdown.State.BERRY_BUSH && berryBushProperty.value()) {
			event.cancel();
		}
		if (event.getState() == EventSlowdown.State.POWDERED_SNOW && powderSnowProperty.value()) {
			event.cancel();
		}
	}, Priority.FIRST);

	@EventPointer
	private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
		Block block = WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getLocalPlayer(), 0.7f);
		if (block == Blocks.SOUL_SAND && soulSandProperty.value()) {
			event.setX(event.getX() * 1.72111554);
			event.setZ(event.getZ() * 1.72111554);
		}
	}, Priority.FIRST);
}
