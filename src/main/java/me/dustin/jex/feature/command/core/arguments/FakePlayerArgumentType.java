package me.dustin.jex.feature.command.core.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import java.util.concurrent.CompletableFuture;

public class FakePlayerArgumentType extends PlayerNameArgumentType {

    public static FakePlayerArgumentType fakePlayer() {
        return new FakePlayerArgumentType();
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

        FakePlayerEntity player = null;
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof FakePlayerEntity) {
                if (entity.getName().getString().equalsIgnoreCase(nameString)) {
                    player = (FakePlayerEntity)entity;
                }
            }
        }

        if (player != null) {
            return nameString;
        } else {
            throw new SimpleCommandExceptionType(Text.of("Not a fake player")).createWithContext(reader);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof FakePlayerEntity) {
                builder.suggest(entity.getName().getString());
            }
        });
        return builder.buildFuture();
    }
}
