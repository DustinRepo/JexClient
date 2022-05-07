package me.dustin.jex.feature.command.core.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

import me.dustin.jex.helper.player.FriendHelper;
import net.minecraft.network.chat.Component;

public class FriendArgumentType extends PlayerNameArgumentType {

    public static FriendArgumentType friend() {
        return new FriendArgumentType();
    }

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

        if (FriendHelper.INSTANCE.isFriend(nameString)) {
            return nameString;
        } else {
            throw new SimpleCommandExceptionType(Component.nullToEmpty("Not a friend")).createWithContext(reader);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        FriendHelper.INSTANCE.getFriendsList().forEach(friend -> {
            builder.suggest(friend.name());
        });
        return builder.buildFuture();
    }
}
