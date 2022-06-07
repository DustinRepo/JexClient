package me.dustin.jex.helper.misc;

import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;
import net.minecraft.client.util.InputUtil;

public enum KeyboardHelper {
    INSTANCE;

    public final int MIDDLE_CLICK = 10002;
    public final int MB4 = 10004;
    public final int MB5 = 10003;

    public boolean isPressed(int key) {
        if (key > 10000)
            return MouseHelper.INSTANCE.isMouseButtonDown(key - 10000);
        return GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), key) == 1;
    }

    public String getKeyName(int key) {
        if (key > 10000) {
            int mousebutton = key - 10000;
            if (key == MIDDLE_CLICK) {
                return "Middle-Click";
            }
            return "MB" + (mousebutton + 1);
        }
        String s = InputUtil.fromTranslationKey(InputUtil.fromKeyCode(key, 0).getTranslationKey()).getLocalizedText().getString();
        return s.length() == 1 ? s.toUpperCase() : s;
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
            return InputUtil.fromTranslationKey(keyName).getCode();
        } catch (Exception e) {}
        return -1;
    }

}
