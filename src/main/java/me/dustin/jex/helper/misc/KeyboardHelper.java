package me.dustin.jex.helper.misc;

import org.lwjgl.glfw.GLFW;

public enum KeyboardHelper {
    INSTANCE;

    public boolean isPressed(int key) {
        return GLFW.glfwGetKey(Wrapper.INSTANCE.getWindow().getHandle(), key) == 1;
    }
}
