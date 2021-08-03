package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EventClickBlock extends Event {

	private BlockPos blockPos;
	private Direction face;

	public EventClickBlock(BlockPos loc, Direction face) {
		this.blockPos = loc;
		this.face = face;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public Direction getFace() {
		return face;
	}

}
