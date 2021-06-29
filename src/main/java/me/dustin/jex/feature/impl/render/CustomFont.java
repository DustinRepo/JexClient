package me.dustin.jex.feature.impl.render;

import com.google.common.collect.Maps;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.file.FeatureFile;
import me.dustin.jex.font.NahrFont;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.option.OptionManager;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.types.StringArrayOption;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

@Feature.Manifest(name = "CustomFont", category = Feature.Category.VISUAL, description = "Change the font in aspects of the game. Disable then re-enable to reload fonts from folder (.minecraft/JexClient/fonts)")
public class CustomFont extends Feature {
    public static CustomFont INSTANCE;

    @Op(name = "Text Shadows")
    public boolean textShadows = true;
    @Op(name = "X Offset", min = -5, max = 5, inc = 0.5f)
    public float xOffset = -1;
    @Op(name = "Y Offset", min = -5, max = 5, inc = 0.5f)
    public float yOffset = -4.5f;

    @Op(name = "Font", all = {"Verdana", "Lucida Console"})
    public String font = "Verdana";

    public CustomFont() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        loadFontFiles();
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
            FeatureFile.write();
        } else {
            FontHelper.INSTANCE.setClientFont(new NahrFont(font, 18, 1.2f));
        }
    }

    private void loadFontFiles() {
        File fontsDir = new File(ModFileHelper.INSTANCE.getJexDirectory(), "fonts");
        if (!fontsDir.exists())
            fontsDir.mkdir();
        ArrayList<String> all = new ArrayList<>();
        StringArrayOption fontOption = (StringArrayOption) OptionManager.INSTANCE.getOption("Font", this);
        all.add("Verdana");
        all.add("Lucida Console");
        for (File file : fontsDir.listFiles()) {
            if (file.getName().toLowerCase().endsWith("ttf") || file.getName().toLowerCase().endsWith("otf")) {
                all.add(file.getName());
            }
        }
        String[] finalArray = new String[all.size()];
        Object[] obj = all.toArray();
        for (int i = 0; i < obj.length; i++) {
            finalArray[i] = obj[i].toString();
        }
        assert fontOption != null;
        fontOption.setAll(finalArray);
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
