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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

public class BlockStateArgumentType implements ArgumentType<BlockInput> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");
   private final HolderLookup<Block> registryWrapper;

   public BlockStateArgumentType(CommandBuildContext commandRegistryAccess) {
      this.registryWrapper = commandRegistryAccess.holderLookup(Registry.BLOCK_REGISTRY);
   }

   public static BlockStateArgumentType blockState(CommandBuildContext commandRegistryAccess) {
      return new BlockStateArgumentType(commandRegistryAccess);
   }

   public BlockInput parse(StringReader stringReader) throws CommandSyntaxException {
      BlockResult blockResult = BlockStateParser.parseForBlock(this.registryWrapper, stringReader, true);
      return new BlockInput(blockResult.blockState(), blockResult.properties().keySet(), blockResult.nbt());
   }

   public static BlockInput getBlockState(CommandContext<FabricClientCommandSource> context, String name) {
      return (BlockInput)context.getArgument(name, BlockInput.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return BlockStateParser.fillSuggestions(this.registryWrapper, builder, false, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
