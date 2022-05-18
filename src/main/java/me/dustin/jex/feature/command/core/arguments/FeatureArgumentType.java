package me.dustin.jex.feature.command.core.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.Text;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class FeatureArgumentType implements ArgumentType<String> {

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int argBeginning = reader.getCursor(); // The starting position of the cursor is at the beginning of the argument.
        if (!reader.canRead()) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') { // peek provides the character at the current cursor position.
            reader.skip(); // Tells the StringReader to move it's cursor to the next position.
        }
        String str = reader.getString().substring(argBeginning, reader.getCursor());

        if (Feature.get(str) != null) {
            return str;
        } else {
            throw new SimpleCommandExceptionType(Text.of("Not a feature")).createWithContext(reader);
        }
    }

    public static FeatureArgumentType feature() {
        return new FeatureArgumentType();
    }
    public static Feature getFeature(CommandContext<FabricClientCommandSource> commandContext, String string) throws CommandSyntaxException {
        return Feature.get(commandContext.getArgument(string, String.class));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        FeatureManager.INSTANCE.getFeatures().forEach(feature -> {
            builder.suggest(feature.getName().toLowerCase());
        });
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }
}
