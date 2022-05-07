package me.dustin.jex.feature.command.core.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider.ElementSuggestionType;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class RegistryPredicateArgumentType<T> implements ArgumentType<ResourceOrTagLocationArgument.Result<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    private static final DynamicCommandExceptionType ERROR_INVALID_BIOME = new DynamicCommandExceptionType((object) -> {
        return Component.translatable("commands.locatebiome.invalid", new Object[]{object});
    });
    private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType((object) -> {
        return Component.translatable("commands.locate.invalid", new Object[]{object});
    });
    final ResourceKey<? extends Registry<T>> registryKey;

    public RegistryPredicateArgumentType(ResourceKey<? extends Registry<T>> resourceKey) {
        this.registryKey = resourceKey;
    }

    public static <T> ResourceOrTagLocationArgument<T> resourceOrTag(ResourceKey<? extends Registry<T>> resourceKey) {
        return new ResourceOrTagLocationArgument(resourceKey);
    }

    private static <T> ResourceOrTagLocationArgument.Result<T> getRegistryType(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        ResourceOrTagLocationArgument.Result<?> result = (ResourceOrTagLocationArgument.Result)commandContext.getArgument(string, ResourceOrTagLocationArgument.Result.class);
        Optional<ResourceOrTagLocationArgument.Result<T>> optional = result.cast(resourceKey);
        return (ResourceOrTagLocationArgument.Result)optional.orElseThrow(() -> {
            return dynamicCommandExceptionType.create(result);
        });
    }

    public static ResourceOrTagLocationArgument.Result<Biome> getBiome(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return getRegistryType(commandContext, string, Registry.BIOME_REGISTRY, ERROR_INVALID_BIOME);
    }

    public static ResourceOrTagLocationArgument.Result<Structure> getStructure(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return getRegistryType(commandContext, string, Registry.STRUCTURE_REGISTRY, ERROR_INVALID_STRUCTURE);
    }

    public ResourceOrTagLocationArgument.Result<T> parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            int i = stringReader.getCursor();

            try {
                stringReader.skip();
                ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
                return new TagResult(TagKey.create(this.registryKey, resourceLocation));
            } catch (CommandSyntaxException var4) {
                stringReader.setCursor(i);
                throw var4;
            }
        } else {
            ResourceLocation resourceLocation2 = ResourceLocation.read(stringReader);
            return new ResourceResult(ResourceKey.create(this.registryKey, resourceLocation2));
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        Object var4 = commandContext.getSource();
        if (var4 instanceof SharedSuggestionProvider) {
            SharedSuggestionProvider sharedSuggestionProvider = (SharedSuggestionProvider)var4;
            return sharedSuggestionProvider.suggestRegistryElements(this.registryKey, ElementSuggestionType.ALL, suggestionsBuilder, commandContext);
        } else {
            return suggestionsBuilder.buildFuture();
        }
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public interface Result<T> extends Predicate<Holder<T>> {
        Either<ResourceKey<T>, TagKey<T>> unwrap();

        <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey);

        String asPrintable();
    }

    static record TagResult<T>(TagKey<T> key) implements ResourceOrTagLocationArgument.Result<T> {
        TagResult(TagKey<T> key) {
            this.key = key;
        }

        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.right(this.key);
        }

        public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
            return this.key.cast(resourceKey).map(TagResult::new);
        }

        public boolean test(Holder<T> holder) {
            return holder.is(this.key);
        }

        public String asPrintable() {
            return "#" + this.key.location();
        }
    }

    static record ResourceResult<T>(ResourceKey<T> key) implements ResourceOrTagLocationArgument.Result<T> {
        ResourceResult(ResourceKey<T> key) {
            this.key = key;
        }

        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.left(this.key);
        }

        public <E> Optional<ResourceOrTagLocationArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
            return this.key.cast(resourceKey).map(ResourceResult::new);
        }

        public boolean test(Holder<T> holder) {
            return holder.is(this.key);
        }

        public String asPrintable() {
            return this.key.location().toString();
        }
    }

}
