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
import net.minecraft.command.CommandSource;
import net.minecraft.command.CommandSource.SuggestedIdType;
import net.minecraft.tag.TagKey;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

public class RegistryPredicateArgumentType<T> implements ArgumentType<RegistryPredicateArgumentType.RegistryPredicate<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    private static final DynamicCommandExceptionType INVALID_BIOME_EXCEPTION = new DynamicCommandExceptionType((id) -> new TranslatableText("commands.locatebiome.invalid", new Object[]{id}));
    private static final DynamicCommandExceptionType INVALID_CONFIGURED_STRUCTURE_FEATURE_EXCEPTION = new DynamicCommandExceptionType((id) -> new TranslatableText("commands.locate.invalid", new Object[]{id}));
    final RegistryKey<? extends Registry<T>> registryRef;

    public RegistryPredicateArgumentType(RegistryKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
    }

    public static <T> RegistryPredicateArgumentType<T> registryPredicate(RegistryKey<? extends Registry<T>> registryRef) {
        return new RegistryPredicateArgumentType(registryRef);
    }

    private static <T> RegistryPredicateArgumentType.RegistryPredicate<T> getPredicate(CommandContext<FabricClientCommandSource> context, String name, RegistryKey<Registry<T>> registryRef, DynamicCommandExceptionType invalidException) throws CommandSyntaxException {
        RegistryPredicateArgumentType.RegistryPredicate<?> registryPredicate = (RegistryPredicateArgumentType.RegistryPredicate)context.getArgument(name, RegistryPredicateArgumentType.RegistryPredicate.class);
        Optional<RegistryPredicateArgumentType.RegistryPredicate<T>> optional = registryPredicate.tryCast(registryRef);
        return (RegistryPredicateArgumentType.RegistryPredicate)optional.orElseThrow(() -> {
            return invalidException.create(registryPredicate);
        });
    }

    public static RegistryPredicateArgumentType.RegistryPredicate<Biome> getBiomePredicate(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getPredicate(context, name, Registry.BIOME_KEY, INVALID_BIOME_EXCEPTION);
    }

    public RegistryPredicateArgumentType.RegistryPredicate<T> parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            int i = stringReader.getCursor();

            try {
                stringReader.skip();
                Identifier identifier = Identifier.fromCommandInput(stringReader);
                return new RegistryPredicateArgumentType.TagBased(TagKey.of(this.registryRef, identifier));
            } catch (CommandSyntaxException var4) {
                stringReader.setCursor(i);
                throw var4;
            }
        } else {
            Identifier i = Identifier.fromCommandInput(stringReader);
            return new RegistryPredicateArgumentType.RegistryKeyBased(RegistryKey.of(this.registryRef, i));
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Object var4 = context.getSource();
        if (var4 instanceof CommandSource) {
            CommandSource commandSource = (CommandSource)var4;
            return commandSource.listIdSuggestions(this.registryRef, SuggestedIdType.ALL, builder, context);
        } else {
            return builder.buildFuture();
        }
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public interface RegistryPredicate<T> extends Predicate<RegistryEntry<T>> {
        Either<RegistryKey<T>, TagKey<T>> getKey();

        <E> Optional<RegistryPredicateArgumentType.RegistryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef);

        String asString();
    }

    record TagBased<T>(TagKey<T> key) implements RegistryPredicateArgumentType.RegistryPredicate<T> {

        public Either<RegistryKey<T>, TagKey<T>> getKey() {
            return Either.right(this.key);
        }

        public <E> Optional<RegistryPredicateArgumentType.RegistryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef) {
            return this.key.tryCast(registryRef).map(RegistryPredicateArgumentType.TagBased::new);
        }

        public boolean test(RegistryEntry<T> registryEntry) {
            return registryEntry.isIn(this.key);
        }

        public String asString() {
            return "#" + this.key.id();
        }
    }

    record RegistryKeyBased<T>(RegistryKey<T> key) implements RegistryPredicateArgumentType.RegistryPredicate<T> {

        public Either<RegistryKey<T>, TagKey<T>> getKey() {
            return Either.left(this.key);
        }

        public <E> Optional<RegistryPredicateArgumentType.RegistryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef) {
            return this.key.tryCast(registryRef).map(RegistryPredicateArgumentType.RegistryKeyBased::new);
        }

        public boolean test(RegistryEntry<T> registryEntry) {
            return registryEntry.matchesKey(this.key);
        }

        public String asString() {
            return this.key.getValue().toString();
        }
    }
}
