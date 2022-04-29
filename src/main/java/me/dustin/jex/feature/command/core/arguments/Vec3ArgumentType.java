package me.dustin.jex.feature.command.core.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import me.dustin.jex.feature.command.core.arguments.impl.DefaultPosArgument;
import me.dustin.jex.feature.command.core.arguments.impl.LookingPosArgument;
import me.dustin.jex.feature.command.core.arguments.impl.PosArgument;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class Vec3ArgumentType implements ArgumentType<PosArgument> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "0.1 -0.5 .9", "~0.5 ~1 ~-5");
   public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos3d.incomplete"));
   public static final SimpleCommandExceptionType MIXED_COORDINATE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.mixed"));
   private final boolean centerIntegers;

   public Vec3ArgumentType(boolean centerIntegers) {
      this.centerIntegers = centerIntegers;
   }

   public static Vec3ArgumentType vec3() {
      return new Vec3ArgumentType(true);
   }

   public static Vec3ArgumentType vec3(boolean centerIntegers) {
      return new Vec3ArgumentType(centerIntegers);
   }

   public static Vec3d getVec3(CommandContext<FabricClientCommandSource> context, String name) {
      return ((PosArgument)context.getArgument(name, PosArgument.class)).toAbsolutePos((FabricClientCommandSource)context.getSource());
   }

   public static PosArgument getPosArgument(CommandContext<FabricClientCommandSource> commandContext, String string) {
      return (PosArgument)commandContext.getArgument(string, PosArgument.class);
   }

   public PosArgument parse(StringReader stringReader) throws CommandSyntaxException {
      return (PosArgument)(stringReader.canRead() && stringReader.peek() == '^' ? LookingPosArgument.parse(stringReader) : DefaultPosArgument.parse(stringReader, this.centerIntegers));
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      if (!(context.getSource() instanceof CommandSource)) {
         return Suggestions.empty();
      } else {
         String string = builder.getRemaining();
         Object collection2;
         if (!string.isEmpty() && string.charAt(0) == '^') {
            collection2 = Collections.singleton(CommandSource.RelativePosition.ZERO_LOCAL);
         } else {
            collection2 = ((CommandSource)context.getSource()).getPositionSuggestions();
         }

         return CommandSource.suggestPositions(string, (Collection)collection2, builder, CommandManager.getCommandValidator(this::parse));
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
