package me.dustin.jex.gui.click.listener;

import me.dustin.jex.gui.click.impl.Button;

public abstract class ButtonListener {
    public Button button;

    public ButtonListener() {
    }

    public ButtonListener(Button button) {
        this.button = button;
    }

    public abstract void invoke();
}
