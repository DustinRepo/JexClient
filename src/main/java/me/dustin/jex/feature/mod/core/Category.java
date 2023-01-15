package me.dustin.jex.feature.mod.core;

import java.awt.*;
import java.util.ArrayList;

public record Category(String name, int color) {
    private static final ArrayList<Category> categories = new ArrayList<>();
    public static final Category COMBAT = new Category("Combat", new Color(113, 12, 20).getRGB());
    public static final Category PLAYER = new Category("Player", new Color(22, 116, 38).getRGB());
    public static final Category MOVEMENT = new Category("Movement", new Color(106, 16, 116).getRGB());
    public static final Category WORLD = new Category("World", new Color(18, 81, 125).getRGB());
    public static final Category VISUAL = new Category("Visual", new Color(251, 80, 8).getRGB());
    public static final Category MISC = new Category("Misc", new Color(247, 255, 65).getRGB());

    public Category(String name, int color) {
        this.name = name;
        this.color = color;
        categories.add(this);
    }

    public static ArrayList<Category> values() {
        return categories;
    }
}
