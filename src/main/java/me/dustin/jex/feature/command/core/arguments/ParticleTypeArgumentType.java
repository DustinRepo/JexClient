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
import net.minecraft.particle.ParticleType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ParticleTypeArgumentType implements ArgumentType<String> {

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

        if (Registry.PARTICLE_TYPE.get(new Identifier(str)) != null) {
            return str;
        } else {
            throw new SimpleCommandExceptionType(Text.of("Not a feature")).createWithContext(reader);
        }
    }

    public static ParticleTypeArgumentType particleType() {
        return new ParticleTypeArgumentType();
    }
    public static ParticleType<?> getParticleType(CommandContext<FabricClientCommandSource> commandContext, String string) throws CommandSyntaxException {
        return Registry.PARTICLE_TYPE.get(new Identifier(commandContext.getArgument(string, String.class)));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Registry.PARTICLE_TYPE.getIds().forEach(identifier -> {
            builder.suggest(identifier.toString());
        });
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }
}
