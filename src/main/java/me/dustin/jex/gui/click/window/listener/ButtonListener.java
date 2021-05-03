package me.dustin.jex.gui.click.window.listener;

import me.dustin.jex.gui.click.window.impl.Button;

public abstract class ButtonListener {
    public Button button;

    public ButtonListener() {
    }

    public ButtonListener(Button button) {
        this.button = button;
    }

    public abstract void invoke();
}
