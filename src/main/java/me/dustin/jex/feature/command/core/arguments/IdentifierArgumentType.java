//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.dustin.jex.feature.command.core.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.resources.ResourceLocation;

public class IdentifierArgumentType implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    public static IdentifierArgumentType identifier() {
        return new IdentifierArgumentType();
    }

    public static ResourceLocation getIdentifier(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, ResourceLocation.class);
    }

    public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException {
        return ResourceLocation.read(stringReader);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
