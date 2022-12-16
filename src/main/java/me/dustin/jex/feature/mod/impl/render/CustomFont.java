package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.helper.render.font.NahrFont;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.render.font.FontHelper;


public class CustomFont extends Feature {
    public static CustomFont INSTANCE;

    public final Property<Boolean> textShadowsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Text Shadows")
            .value(true)
            .build();
    public final Property<Float> xOffsetProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("X Offset")
            .value(-1f)
            .min(-5)
            .max(5)
            .inc(0.5f)
            .build();
    public final Property<Float> yOffsetProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Y Offset")
            .value(-1f)
            .min(-5)
            .max(5)
            .inc(0.5f)
            .build();
    public final Property<String> fontProperty = new Property.PropertyBuilder<String>(this.getClass())
            .name("Font")
            .value("Verdana")
            .max(30)
            .build();

    public CustomFont() {
        super(Category.VISUAL, "Change the font in aspects of the game. Disable then re-enable to reload fonts from folder (.minecraft/JexClient/fonts)");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        loadFont();
        super.onEnable();
    }

    public void loadFont() {
        if (FontHelper.INSTANCE.getClientFont() != null) {
            NahrFont origFont = FontHelper.INSTANCE.getClientFont();
            if (!origFont.getFont().getFontName().equalsIgnoreCase(fontProperty.value()))
                if (!FontHelper.INSTANCE.setClientFont(new NahrFont(fontProperty.value(), 18, 1.2f))) {
                    ChatHelper.INSTANCE.addClientMessage("Font not found. Reverting to last found");
                    fontProperty.setValue(origFont.getFont().getFontName());
                }
            ConfigManager.INSTANCE.get(FeatureFile.class).write();
        } else {
            FontHelper.INSTANCE.setClientFont(new NahrFont(fontProperty.value(), 18, 1.2f));
        }
    }
}
