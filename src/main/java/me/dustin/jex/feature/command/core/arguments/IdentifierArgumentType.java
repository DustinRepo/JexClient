//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.dustin.jex.feature.command.core.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class IdentifierArgumentType implements ArgumentType<Identifier> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    public static IdentifierArgumentType identifier() {
        return new IdentifierArgumentType();
    }

    public static Identifier getIdentifier(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Identifier.class);
    }

    public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
        return Identifier.fromCommandInput(stringReader);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
