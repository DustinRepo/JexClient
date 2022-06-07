package me.dustin.jex.feature.command.core.arguments.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class DefaultPosArgument implements PosArgument {
   private final CoordinateArgument x;
   private final CoordinateArgument y;
   private final CoordinateArgument z;

   public DefaultPosArgument(CoordinateArgument x, CoordinateArgument y, CoordinateArgument z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3d toAbsolutePos(FabricClientCommandSource source) {
      Vec3d vec3d = source.getPlayer().getPos();
      return new Vec3d(this.x.toAbsoluteCoordinate(vec3d.x), this.y.toAbsoluteCoordinate(vec3d.y), this.z.toAbsoluteCoordinate(vec3d.z));
   }

   public Vec2f toAbsoluteRotation(FabricClientCommandSource source) {
      Vec2f vec2f = source.getPlayer().getRotationClient();
      return new Vec2f((float)this.x.toAbsoluteCoordinate((double)vec2f.x), (float)this.y.toAbsoluteCoordinate((double)vec2f.y));
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
      CoordinateArgument coordinateArgument = CoordinateArgument.parse(reader);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(reader);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            CoordinateArgument coordinateArgument3 = CoordinateArgument.parse(reader);
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
      CoordinateArgument coordinateArgument = CoordinateArgument.parse(reader, centerIntegers);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(reader, false);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            CoordinateArgument coordinateArgument3 = CoordinateArgument.parse(reader, centerIntegers);
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
      return new DefaultPosArgument(new CoordinateArgument(false, x), new CoordinateArgument(false, y), new CoordinateArgument(false, z));
   }

   public static DefaultPosArgument absolute(Vec2f vec) {
      return new DefaultPosArgument(new CoordinateArgument(false, (double)vec.x), new CoordinateArgument(false, (double)vec.y), new CoordinateArgument(true, 0.0D));
   }

   public static DefaultPosArgument zero() {
      return new DefaultPosArgument(new CoordinateArgument(true, 0.0D), new CoordinateArgument(true, 0.0D), new CoordinateArgument(true, 0.0D));
   }

   public int hashCode() {
      int i = this.x.hashCode();
      i = 31 * i + this.y.hashCode();
      i = 31 * i + this.z.hashCode();
      return i;
   }
}
