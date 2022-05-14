package me.dustin.jex.feature.keybind;

import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public record Keybind(int key, String command, boolean isJexCommand) {
    private static final ArrayList<Keybind> keybinds = new ArrayList<>();
    public static void add(int key, String command, boolean isJexCommand) {
        Keybind existing = get(command);
        if (existing != null)
            remove(existing);
        keybinds.add(new Keybind(key, command, isJexCommand));
    }
    public static void remove(Keybind keybind) {
        keybinds.remove(keybind);
    }
    public static void clear(int key) {
        get(key).forEach(keybinds::remove);
    }
    public static ArrayList<Keybind> get(int key) {
        ArrayList<Keybind> binds = new ArrayList<>();
        for (Keybind keybind : keybinds) {
            if (keybind.key() == key) {
                binds.add(keybind);
            }
        }
        return binds;
    }
    public static Keybind get(String command) {
        for (Keybind keybind : keybinds) {
            if (keybind.command().equalsIgnoreCase(command)) {
                return keybind;
            }
        }
        return null;
    }
    public void execute() {
        if (command().startsWith("/")) {
            Wrapper.INSTANCE.getLocalPlayer().command(command().substring(1), Component.literal(command()));
            return;
        }
        Wrapper.INSTANCE.getLocalPlayer().chat((isJexCommand() ? CommandManagerJex.INSTANCE.getPrefix() : "") + command());
    }
    public static ArrayList<Keybind> getKeybinds() {
        return keybinds;
    }
}
