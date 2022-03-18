package me.dustin.jex.feature.command.core.arguments;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.dustin.jex.helper.misc.Wrapper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlayerNameArgumentType implements ArgumentType<String> {

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int argBeginning = reader.getCursor(); // The starting position of the cursor is at the beginning of the argument.
        if (!reader.canRead()) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') { // peek provides the character at the current cursor position.
            reader.skip(); // Tells the StringReader to move it's cursor to the next position.
        }
        String nameString = reader.getString().substring(argBeginning, reader.getCursor());

        Pattern p = Pattern.compile("[^a-z0-9_]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(nameString);
        boolean b = m.find();

        if (nameString.length() <= 16 && !b) {
            return nameString;
        } else {
            throw new SimpleCommandExceptionType(new LiteralText("Not a name")).createWithContext(reader);
        }
    }

    public static PlayerNameArgumentType playerName() {
        return new PlayerNameArgumentType();
    }
    public static String getPlayerName(CommandContext<FabricClientCommandSource> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, String.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        getPlayerNames().stream().filter(s -> {
            Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(s);
            boolean b = m.find();

            if (s.length() <= 16 && !b) {
                return true;
            }
            return false;
        }).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private Collection<String> getPlayerNames() {
        return Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerList().stream().map(e -> e.getProfile().getName()).collect(Collectors.toList());
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }
}
