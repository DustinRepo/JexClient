package me.dustin.jex.helper.misc;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Locale;

public enum KeyboardHelper {
    INSTANCE;

    public final int MIDDLE_CLICK = 10002;
    public final int MB4 = 10002;
    public final int MB5 = 10002;

    public boolean isPressed(int key) {
        if (key > 10000)
            return MouseHelper.INSTANCE.isMouseButtonDown(key - 10000);
        return GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getWindow(), key) == 1;
    }

    public String getKeyName(int key) {
        if (key > 10000) {
            int mousebutton = key - 10000;
            if (key == MIDDLE_CLICK) {
                return "Middle-Click";
            }
            return "MB" + (mousebutton + 1);
        }
        String s = (GLFW.glfwGetKeyName(key, 0) == null ? InputConstants.getKey(key, 0).getName().replace("key.keyboard.", "").replace(".", "_") : GLFW.glfwGetKeyName(key, 0).toUpperCase()).toUpperCase().replace("key.keyboard.", "").replace(".", "_");
        return s.equalsIgnoreCase("_") ? "." : s;
    }

    public int getKeyFromName(String keyName) {
        if (keyName.equalsIgnoreCase("Middle-Click")) {
            return MIDDLE_CLICK;
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
            return InputConstants.getKey(keyName).getValue();
        } catch (Exception e) {}
        return -1;
    }

}
