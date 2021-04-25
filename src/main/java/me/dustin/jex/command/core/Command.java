package me.dustin.jex.command.core;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.jex.command.CommandManager;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Command {

    private String name, description;
    private List<String> alias, syntax;
    private ArrayList<String[]> tabCompleteList;

    public Command() {
        this.name = this.getClass().getAnnotation(Cmd.class).name();
        this.syntax = Arrays.asList(this.getClass().getAnnotation(Cmd.class).syntax());
        this.description = this.getClass().getAnnotation(Cmd.class).description();
        this.alias = Arrays.asList(this.getClass().getAnnotation(Cmd.class).alias());
        this.tabCompleteList = new ArrayList<>();
    }

    public abstract void runCommand(String command, String[] args);

    public String getName() {
        return name;
    }

    public List<String> getSyntax() {
        return syntax;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAlias() {
        return alias;
    }

    protected boolean isAddString(String s) {
        return s.equalsIgnoreCase("add") || s.equalsIgnoreCase("a");
    }

    protected boolean isDeleteString(String s) {
        return s.equalsIgnoreCase("delete") || s.equalsIgnoreCase("del") || s.equalsIgnoreCase("remove") || s.equalsIgnoreCase("r");
    }

    protected void giveSyntaxMessage() {
        ChatHelper.INSTANCE.addClientMessage("Invalid Syntax!");
        for (String s : syntax) {
            ChatHelper.INSTANCE.addClientMessage(s.replace(".", CommandManager.INSTANCE.getPrefix()));
        }
    }

    protected ArrayList<String[]> getTabCompleteList() {
        return tabCompleteList;
    }
}
