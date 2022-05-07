package me.dustin.jex.feature.command.core.arguments.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;

import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LookingPosArgument implements PosArgument {
   public static final char field_32941 = '^';
   private final double x;
   private final double y;
   private final double z;

   public LookingPosArgument(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3 toAbsolutePos(FabricClientCommandSource source) {
      Vec2 vec2f = source.getPlayer().getRotationVector();
      Vec3 vec3d = source.getPlayer().position();
      float f = Mth.cos((vec2f.y + 90.0F) * 0.017453292F);
      float g = Mth.sin((vec2f.y + 90.0F) * 0.017453292F);
      float h = Mth.cos(-vec2f.x * 0.017453292F);
      float i = Mth.sin(-vec2f.x * 0.017453292F);
      float j = Mth.cos((-vec2f.x + 90.0F) * 0.017453292F);
      float k = Mth.sin((-vec2f.x + 90.0F) * 0.017453292F);
      Vec3 vec3d2 = new Vec3((double)(f * h), (double)i, (double)(g * h));
      Vec3 vec3d3 = new Vec3((double)(f * j), (double)k, (double)(g * j));
      Vec3 vec3d4 = vec3d2.cross(vec3d3).scale(-1.0D);
      double d = vec3d2.x * this.z + vec3d3.x * this.y + vec3d4.x * this.x;
      double e = vec3d2.y * this.z + vec3d3.y * this.y + vec3d4.y * this.x;
      double l = vec3d2.z * this.z + vec3d3.z * this.y + vec3d4.z * this.x;
      return new Vec3(vec3d.x + d, vec3d.y + e, vec3d.z + l);
   }

   public Vec2 toAbsoluteRotation(FabricClientCommandSource source) {
      return Vec2.ZERO;
   }

   public boolean isXRelative() {
      return true;
   }

   public boolean isYRelative() {
      return true;
   }

   public boolean isZRelative() {
      return true;
   }

   public static LookingPosArgument parse(StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();
      double d = readCoordinate(reader, i);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         double e = readCoordinate(reader, i);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            double f = readCoordinate(reader, i);
            return new LookingPosArgument(d, e, f);
         } else {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
         }
      } else {
         reader.setCursor(i);
         throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
      }
   }

   private static double readCoordinate(StringReader reader, int startingCursorPos) throws CommandSyntaxException {
      if (!reader.canRead()) {
         throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext(reader);
      } else if (reader.peek() != '^') {
         reader.setCursor(startingCursorPos);
         throw Vec3ArgumentType.MIXED_COORDINATE_EXCEPTION.createWithContext(reader);
      } else {
         reader.skip();
         return reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0D;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof LookingPosArgument)) {
         return false;
      } else {
         LookingPosArgument lookingPosArgument = (LookingPosArgument)o;
         return this.x == lookingPosArgument.x && this.y == lookingPosArgument.y && this.z == lookingPosArgument.z;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.x, this.y, this.z});
   }
}
