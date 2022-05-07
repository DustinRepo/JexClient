package me.dustin.jex.feature.command.core.arguments.impl;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface PosArgument {
   Vec3 toAbsolutePos(FabricClientCommandSource source);

   Vec2 toAbsoluteRotation(FabricClientCommandSource source);

   default BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
      return new BlockPos(this.toAbsolutePos(source));
   }

   boolean isXRelative();

   boolean isYRelative();

   boolean isZRelative();
}
