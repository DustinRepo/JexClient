package me.dustin.jex.gui.plugin.button;

import me.dustin.jex.feature.plugin.JexPlugin;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringJoiner;

public class JexPluginButton extends Button {
    private final JexPlugin jexPlugin;
    private final Identifier icon;
    private boolean isSelected;
    public JexPluginButton(JexPlugin jexPlugin, float x, float y, float width, float height) {
        super(jexPlugin.getInfo().getName(), x, y, width, height, null);
        this.jexPlugin = jexPlugin;
        if (jexPlugin.getInfo().getIconFile() != null)
            this.icon = getIdentifier(jexPlugin.getInfo().getIconFile());
        else
            this.icon = null;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        String drawName = "%s v%s".formatted(jexPlugin.getInfo().getName(), jexPlugin.getInfo().getVersion());
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), isSelected ? ColorHelper.INSTANCE.getClientColor() : 0xaa000000, 0x40000000, 1);
        FontHelper.INSTANCE.draw(matrixStack, drawName, getX() + 37, getY() + 3.5f, -1);
        String preTrim = "%s%s".formatted(Formatting.GRAY, jexPlugin.getInfo().getDescription());
        String trimmed = Wrapper.INSTANCE.getTextRenderer().trimToWidth(preTrim, (int)getWidth() - 67);
        if (!trimmed.equalsIgnoreCase(preTrim))
            trimmed = trimmed + "...";
        FontHelper.INSTANCE.draw(matrixStack, trimmed, getX() + 37, getY() + 14.5f, -1);

        preTrim = "By: %s%s".formatted(Formatting.GRAY, getAuthors());
        trimmed = Wrapper.INSTANCE.getTextRenderer().trimToWidth(preTrim, (int)getWidth() - 67);
        if (!trimmed.equalsIgnoreCase(preTrim))
            trimmed = trimmed + "...";
        FontHelper.INSTANCE.draw(matrixStack, trimmed, getX() + 37, getY() + 25.5f, -1);
        if (icon != null) {
            Render2DHelper.INSTANCE.bindTexture(icon);
            Render2DHelper.INSTANCE.drawTexture(matrixStack, getX() + 2, getY() + 2, 0, 0, 32, 32, 32, 32);
        }
    }

    public String getAuthors() {
        StringJoiner sj = new StringJoiner(Formatting.RESET + ", ");
        for (String author : jexPlugin.getInfo().getAuthors()) {
            sj.add(Formatting.GRAY + author);
        }
        return sj.toString();
    }

    private Identifier getIdentifier(String iconFile) {
        InputStream inputStream = FabricLauncherBase.getLauncher().getResourceAsStream(iconFile);
        try {
            NativeImage nativeImage = NativeImage.read(inputStream);
            int imageWidth = 64;
            int imageHeight = 64;

            for (int srcWidth = nativeImage.getWidth(), srcHeight = nativeImage.getHeight(); imageWidth < srcWidth || imageHeight < srcHeight; ) {
                imageWidth *= 2;
                imageHeight *= 2;
            }

            NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
            for (int x = 0; x < nativeImage.getWidth(); x++) {
                for (int y = 0; y < nativeImage.getHeight(); y++) {
                    imgNew.setColor(x, y, nativeImage.getColor(x, y));
                }
            }
            nativeImage.close();
            Identifier id = new Identifier("jex", iconFile);
            FileHelper.INSTANCE.applyTexture(id, imgNew);
            return id;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public JexPlugin getJexPlugin() {
        return jexPlugin;
    }

    public Identifier getIcon() {
        return icon;
    }
}
