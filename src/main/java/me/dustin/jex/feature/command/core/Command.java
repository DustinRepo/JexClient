package me.dustin.jex.feature.command.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;
import java.util.Arrays;
import java.util.List;

public abstract class Command implements com.mojang.brigadier.Command<FabricClientCommandSource> {
    protected CommandDispatcher<FabricClientCommandSource> dispatcher = CommandManagerJex.DISPATCHER;
    protected CommandRegistryAccess commandRegistryAccess;
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

    public abstract void registerCommand();

    public void setCommandRegistryAccess(ClientPlayNetworkHandler networkHandler) {
        commandRegistryAccess = new CommandRegistryAccess(networkHandler.getRegistryManager());
    }

    public LiteralArgumentBuilder<FabricClientCommandSource> literal(String s) {
        return ClientCommandManager.literal(s);
    }

    public <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(String s, ArgumentType<T> type) {
        return ClientCommandManager.argument(s, type);
    }
}
