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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EnchantmentArgumentType implements ArgumentType<Enchantment> {
   private static final Collection<String> EXAMPLES = Arrays.asList("unbreaking", "silk_touch");
   public static final DynamicCommandExceptionType UNKNOWN_ENCHANTMENT_EXCEPTION = new DynamicCommandExceptionType((object) -> Text.translatable("enchantment.unknown", new Object[]{object}));

   public static EnchantmentArgumentType enchantment() {
      return new EnchantmentArgumentType();
   }

   public static Enchantment getEnchantment(CommandContext<FabricClientCommandSource> context, String name) {
      return (Enchantment)context.getArgument(name, Enchantment.class);
   }

   public Enchantment parse(StringReader stringReader) throws CommandSyntaxException {
      Identifier identifier = Identifier.fromCommandInput(stringReader);
      return (Enchantment)Registry.ENCHANTMENT.getOrEmpty(identifier).orElseThrow(() -> {
         return UNKNOWN_ENCHANTMENT_EXCEPTION.create(identifier);
      });
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      Registry.ENCHANTMENT.getIds().forEach(identifier -> {
         builder.suggest(identifier.toString());
      });
      return builder.buildFuture();
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
