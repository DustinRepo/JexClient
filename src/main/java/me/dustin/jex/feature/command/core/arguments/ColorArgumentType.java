package me.dustin.jex.feature.command.core.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class ColorArgumentType implements ArgumentType<ChatFormatting> {
   private static final Collection<String> EXAMPLES = Arrays.asList("red", "green");
   public static final DynamicCommandExceptionType INVALID_COLOR_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return Component.translatable("argument.color.invalid", new Object[]{object});
   });

   private ColorArgumentType() {
   }

   public static ColorArgumentType color() {
      return new ColorArgumentType();
   }

   public static ChatFormatting getColor(CommandContext<FabricClientCommandSource> context, String name) {
      return (ChatFormatting)context.getArgument(name, ChatFormatting.class);
   }

   public ChatFormatting parse(StringReader stringReader) throws CommandSyntaxException {
      String string = stringReader.readUnquotedString();
      ChatFormatting formatting = ChatFormatting.getByName(string);
      if (formatting != null && !formatting.isFormat()) {
         return formatting;
      } else {
         throw INVALID_COLOR_EXCEPTION.create(string);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return SharedSuggestionProvider.suggest((Iterable)ChatFormatting.getNames(true, false), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
