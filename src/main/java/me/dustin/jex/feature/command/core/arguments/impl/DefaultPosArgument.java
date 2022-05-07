package me.dustin.jex.feature.command.core.arguments.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class DefaultPosArgument implements PosArgument {
   private final WorldCoordinate x;
   private final WorldCoordinate y;
   private final WorldCoordinate z;

   public DefaultPosArgument(WorldCoordinate x, WorldCoordinate y, WorldCoordinate z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3 toAbsolutePos(FabricClientCommandSource source) {
      Vec3 vec3d = source.getPlayer().position();
      return new Vec3(this.x.get(vec3d.x), this.y.get(vec3d.y), this.z.get(vec3d.z));
   }

   public Vec2 toAbsoluteRotation(FabricClientCommandSource source) {
      Vec2 vec2f = source.getPlayer().getRotationVector();
      return new Vec2((float)this.x.get((double)vec2f.x), (float)this.y.get((double)vec2f.y));
   }

   public boolean isXRelative() {
      return this.x.isRelative();
   }

   public boolean isYRelative() {
      return this.y.isRelative();
   }

   public boolean isZRelative() {
      return this.z.isRelative();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof DefaultPosArgument)) {
         return false;
      } else {
         DefaultPosArgument defaultPosArgument = (DefaultPosArgument)o;
         if (!this.x.equals(defaultPosArgument.x)) {
            return false;
         } else {
            return !this.y.equals(defaultPosArgument.y) ? false : this.z.equals(defaultPosArgument.z);
         }
      }
   }

   public static DefaultPosArgument parse(StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();
      WorldCoordinate coordinateArgument = WorldCoordinate.parseInt(reader);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         WorldCoordinate coordinateArgument2 = WorldCoordinate.parseInt(reader);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            WorldCoordinate coordinateArgument3 = WorldCoordinate.parseInt(reader);
            return new DefaultPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
         } else {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
         }
      } else {
         reader.setCursor(i);
         throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
      }
   }

   public static DefaultPosArgument parse(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
      int i = reader.getCursor();
      WorldCoordinate coordinateArgument = WorldCoordinate.parseDouble(reader, centerIntegers);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         WorldCoordinate coordinateArgument2 = WorldCoordinate.parseDouble(reader, false);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            WorldCoordinate coordinateArgument3 = WorldCoordinate.parseDouble(reader, centerIntegers);
            return new DefaultPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
         } else {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
         }
      } else {
         reader.setCursor(i);
         throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
      }
   }

   public static DefaultPosArgument absolute(double x, double y, double z) {
      return new DefaultPosArgument(new WorldCoordinate(false, x), new WorldCoordinate(false, y), new WorldCoordinate(false, z));
   }

   public static DefaultPosArgument absolute(Vec2 vec) {
      return new DefaultPosArgument(new WorldCoordinate(false, (double)vec.x), new WorldCoordinate(false, (double)vec.y), new WorldCoordinate(true, 0.0D));
   }

   public static DefaultPosArgument zero() {
      return new DefaultPosArgument(new WorldCoordinate(true, 0.0D), new WorldCoordinate(true, 0.0D), new WorldCoordinate(true, 0.0D));
   }

   public int hashCode() {
      int i = this.x.hashCode();
      i = 31 * i + this.y.hashCode();
      i = 31 * i + this.z.hashCode();
      return i;
   }
}
