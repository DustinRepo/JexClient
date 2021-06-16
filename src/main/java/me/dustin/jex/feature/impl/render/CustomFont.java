package me.dustin.jex.feature.impl.render;

import com.google.common.collect.Maps;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.font.NahrFont;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.option.annotate.Op;

import java.util.Map;

@Feat(name = "CustomFont", category = FeatureCategory.VISUAL, description = "Change the font in aspects of the game")
public class CustomFont extends Feature {
    public static CustomFont INSTANCE;

    @Op(name = "X Offset", min = -5, max = 5, inc = 0.5f)
    public float xOffset = -1;
    @Op(name = "Y Offset", min = -5, max = 5, inc = 0.5f)
    public float yOffset = -4.5f;
    @Op(name = "Font")
    public String font = "Verdana";

    public CustomFont() {
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
            if (!origFont.getFont().getFontName().equalsIgnoreCase(font))
                if (!FontHelper.INSTANCE.setClientFont(new NahrFont(font, 18, 1.2f))) {
                    ChatHelper.INSTANCE.addClientMessage("Font not found. Reverting to last found");
                    font = origFont.getFont().getFontName();
                }
        } else {
            FontHelper.INSTANCE.setClientFont(new NahrFont(font, 18, 1.2f));
        }
    }

    @Override
    public Map<String, ButtonListener> addButtons() {
        Map<String, ButtonListener> map = Maps.newHashMap();
        ButtonListener listener = new ButtonListener() {
            @Override
            public void invoke() {
                loadFont();
            }
        };
        map.put("Set Font", listener);
        return map;
    }
}
