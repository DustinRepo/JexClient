package me.dustin.jex.helper.render;

public abstract class ButtonListener {
    public Button button;

    public ButtonListener() {
    }

    public ButtonListener(Button button) {
        this.button = button;
    }

    public abstract void invoke();
}
