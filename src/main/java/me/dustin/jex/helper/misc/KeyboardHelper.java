package me.dustin.jex.helper.misc;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public enum KeyboardHelper {
    INSTANCE;

    public boolean isPressed(int key) {
        if (key > 10000)
            return MouseHelper.INSTANCE.isMouseButtonDown(key - 10000);
        return GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), key) == 1;
    }

    public String getKeyName(int key) {
        if (key > 10000) {
            int mousebutton = key - 10000;
            if (mousebutton == 2) {
                return "Middle-Click";
            }
            return "MB" + (mousebutton + 1);
        }
        String s = (GLFW.glfwGetKeyName(key, 0) == null ? InputUtil.fromKeyCode(key, 0).getTranslationKey().replace("key.keyboard.", "").replace(".", "_") : GLFW.glfwGetKeyName(key, 0).toUpperCase()).toUpperCase().replace("key.keyboard.", "").replace(".", "_");
        return s.equalsIgnoreCase("_") ? "." : s;
    }

    public int getKeyFromName(String keyName) {
        if (keyName.equalsIgnoreCase("Middle-Click")) {
            return 10002;
        }
        else if (keyName.toLowerCase().startsWith("mb")) {
            try {
                String n = keyName.toLowerCase().replace("mb", "");
                int i = Integer.parseInt(n) - 1;
                if (i > 2)
                return 10000 + i;
            } catch (Exception e) {}
        }
        keyName = keyName.replace("_", ".");
        if (!keyName.startsWith("key.keyboard."))
            keyName = "key.keyboard." + keyName.toLowerCase();

        try {
            return InputUtil.fromTranslationKey(keyName).getCode();
        } catch (Exception e) {}
        return -1;
    }

}
