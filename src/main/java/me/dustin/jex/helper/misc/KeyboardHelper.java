package me.dustin.jex.helper.misc;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public enum KeyboardHelper {
    INSTANCE;

    public boolean isPressed(int key) {
        return GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), key) == 1;
    }

    public String getKeyName(int key) {
        return (GLFW.glfwGetKeyName(key, 0) == null ? InputUtil.fromKeyCode(key, 0).getTranslationKey().replace("key.keyboard.", "").replace(".", "_") : GLFW.glfwGetKeyName(key, 0).toUpperCase()).toUpperCase().replace("key.keyboard.", "").replace(".", "_");
    }

    public int getKeyFromName(String keyName) {
        keyName = keyName.replace("_", ".");
        if (!keyName.startsWith("key.keyboard."))
            keyName = "key.keyboard." + keyName.toLowerCase();

        try {
            return InputUtil.fromTranslationKey(keyName).getCode();
        } catch (Exception e) {}
        return -1;
    }

}
