package me.dustin.jex.gui.changelog.changelog;

import java.awt.*;

public class ChangelogLine {

    private String text;
    private Color color;

    public ChangelogLine(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }
}
