package me.dustin.jex.feature.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.dustin.jex.feature.command.CommandManager;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import java.util.Arrays;
import java.util.List;

public abstract class Command implements com.mojang.brigadier.Command<FabricClientCommandSource> {
    protected String name, description;
    private List<String> alias, syntax;

    public Command() {
        this.name = this.getClass().getAnnotation(Cmd.class).name();
        this.description = this.getClass().getAnnotation(Cmd.class).description();
        this.syntax = Arrays.asList(this.getClass().getAnnotation(Cmd.class).syntax());
        this.alias = Arrays.asList(this.getClass().getAnnotation(Cmd.class).alias());
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSyntax() {
        return syntax;
    }

    public List<String> getAlias() {
        return alias;
    }

    public abstract void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess);

    public LiteralArgumentBuilder<FabricClientCommandSource> literal(String s) {
        return ClientCommandManager.literal(s);
    }

    public <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(String s, ArgumentType<T> type) {
        return ClientCommandManager.argument(s, type);
    }
}
