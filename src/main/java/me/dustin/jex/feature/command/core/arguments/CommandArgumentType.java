package me.dustin.jex.feature.command.core.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.command.core.Command;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CommandArgumentType implements ArgumentType<String> {

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
        Command command = null;
        for (Command cmd : CommandManagerJex.INSTANCE.getCommands()) {
            JexClient.INSTANCE.getLogger().info(cmd.getName());
            if (cmd.getName().equalsIgnoreCase(str)) {
                command = cmd;
            }
        }
        if (command != null) {
            return str;
        } else {
            throw new SimpleCommandExceptionType(Text.of("Not a command")).createWithContext(reader);
        }
    }

    public static CommandArgumentType command() {
        return new CommandArgumentType();
    }
    public static Command getCommand(CommandContext<FabricClientCommandSource> commandContext, String string) throws CommandSyntaxException {
        String s = commandContext.getArgument(string, String.class);
        Command command = null;
        for (Command cmd : CommandManagerJex.INSTANCE.getCommands()) {
            if (cmd.getName().equalsIgnoreCase(s)) {
                command = cmd;
            }
        }
        return command;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CommandManagerJex.INSTANCE.getCommands().forEach(command -> {
            builder.suggest(command.getName().toLowerCase());
        });
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }
}
