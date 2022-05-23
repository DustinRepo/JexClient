package me.dustin.jex.feature.command.core.arguments.impl;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface PosArgument {
   Vec3d toAbsolutePos(FabricClientCommandSource source);

   Vec2f toAbsoluteRotation(FabricClientCommandSource source);

   default BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
      return new BlockPos(this.toAbsolutePos(source));
   }

   boolean isXRelative();

   boolean isYRelative();

   boolean isZRelative();
}
