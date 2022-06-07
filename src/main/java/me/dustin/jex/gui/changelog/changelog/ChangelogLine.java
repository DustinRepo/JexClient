package me.dustin.jex.gui.changelog.changelog;

import java.awt.*;

public record ChangelogLine(String text, Color color) {

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }
}
