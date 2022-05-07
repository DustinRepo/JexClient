package me.dustin.jex.helper.misc;

import me.dustin.jex.helper.render.Render2DHelper;
import org.lwjgl.glfw.GLFW;

public enum MouseHelper {
	INSTANCE;

	public int getMouseX() {
		return  (int)(Wrapper.INSTANCE.getMinecraft().mouseHandler.xpos() * Render2DHelper.INSTANCE.getScaledWidth() / Wrapper.INSTANCE.getWindow().getScreenWidth());
	}

	public double getMouseX_D() {
		return Wrapper.INSTANCE.getMinecraft().mouseHandler.xpos() * Render2DHelper.INSTANCE.getScaledWidth() / Wrapper.INSTANCE.getWindow().getScreenWidth();
	}

	public int getMouseY() {
		return Render2DHelper.INSTANCE.getScaledHeight() - (Render2DHelper.INSTANCE.getScaledHeight() - (int) Wrapper.INSTANCE.getMinecraft().mouseHandler.ypos() * Render2DHelper.INSTANCE.getScaledHeight() / Wrapper.INSTANCE.getWindow().getScreenHeight() - 1);
	}

	public double getMouseY_D() {
		return Render2DHelper.INSTANCE.getScaledHeight() - (Render2DHelper.INSTANCE.getScaledHeight() - Wrapper.INSTANCE.getMinecraft().mouseHandler.ypos() * Render2DHelper.INSTANCE.getScaledHeight() / Wrapper.INSTANCE.getWindow().getScreenHeight() - 1);
	}

	public boolean isMouseButtonDown(int button) {
		return GLFW.glfwGetMouseButton(Wrapper.INSTANCE.getWindow().getWindow(), button) != 0;
	}
}
