//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.dustin.jex.feature.command.core.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.class_7157;
import net.minecraft.class_7225;
import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.BlockArgumentParser.class_7211;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.util.registry.Registry;

public class BlockStateArgumentType implements ArgumentType<BlockStateArgument> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");
   private final class_7225<Block> field_37964;

   public BlockStateArgumentType(class_7157 arg) {
      this.field_37964 = arg.method_41699(Registry.BLOCK_KEY);
   }

   public static BlockStateArgumentType blockState(class_7157 arg) {
      return new BlockStateArgumentType(arg);
   }

   public BlockStateArgument parse(StringReader stringReader) throws CommandSyntaxException {
      class_7211 lv = BlockArgumentParser.method_41955(this.field_37964, stringReader, true);
      return new BlockStateArgument(lv.blockState(), lv.properties().keySet(), lv.nbt());
   }

   public static BlockStateArgument getBlockState(CommandContext<FabricClientCommandSource> context, String name) {
      return (BlockStateArgument)context.getArgument(name, BlockStateArgument.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder suggestionsBuilder) {
      return BlockArgumentParser.getSuggestions(this.field_37964, suggestionsBuilder, false, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
