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

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandRegistryWrapper;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.BlockArgumentParser.BlockResult;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.util.registry.Registry;

public class BlockStateArgumentType implements ArgumentType<BlockStateArgument> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");
   private final CommandRegistryWrapper<Block> registryWrapper;

   public BlockStateArgumentType(CommandRegistryAccess commandRegistryAccess) {
      this.registryWrapper = commandRegistryAccess.createWrapper(Registry.BLOCK_KEY);
   }

   public static BlockStateArgumentType blockState(CommandRegistryAccess commandRegistryAccess) {
      return new BlockStateArgumentType(commandRegistryAccess);
   }

   public BlockStateArgument parse(StringReader stringReader) throws CommandSyntaxException {
      BlockResult blockResult = BlockArgumentParser.block(this.registryWrapper, stringReader, true);
      return new BlockStateArgument(blockResult.blockState(), blockResult.properties().keySet(), blockResult.nbt());
   }

   public static BlockStateArgument getBlockState(CommandContext<FabricClientCommandSource> context, String name) {
      return (BlockStateArgument)context.getArgument(name, BlockStateArgument.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return BlockArgumentParser.getSuggestions(this.registryWrapper, builder, false, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
