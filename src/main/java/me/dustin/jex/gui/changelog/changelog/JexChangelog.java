package me.dustin.jex.gui.changelog.changelog;

import me.dustin.jex.JexClient;
import me.dustin.jex.gui.changelog.ChangelogScreen;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.network.WebHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.update.JexVersion;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;

public class JexChangelog {
    private JexVersion version;
    private ArrayList<ChangelogLine> lines = new ArrayList<>();
    private float y;

    public JexChangelog(JexVersion version) {
        this.version = version;
    }

    public void render(MatrixStack matrixStack, float x, float width) {
        int count = 0;
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x, y, x + width, y + getContentHeight(), 0x60999999, 0x60000000, 1);
        FontHelper.INSTANCE.draw(matrixStack, this.version.version(), x + 4, y + 3, JexClient.INSTANCE.getVersion().equals(this.version) ? ColorHelper.INSTANCE.getClientColor() : 0xff999999);
        Render2DHelper.INSTANCE.drawHLine(matrixStack, x + 1, x + width - 2, y + 14, JexClient.INSTANCE.getVersion().equals(this.version) ? ColorHelper.INSTANCE.getClientColor() : 0xff999999);
        for (ChangelogLine line : lines) {
            FontHelper.INSTANCE.drawWithShadow(matrixStack, line.getText(), x + 4, y + 17.5f + (count * 11), line.getColor().getRGB());
            count++;
        }
    }

    public int getContentHeight() {
        return (lines.size() + 1) * 11 + 10;
    }

    public static void loadChangelogList() {
        new Thread(() -> {
            try {
                String[] sArray = WebHelper.INSTANCE.readURL(new URL("https://jexclient.com/JexChangelog.txt")).split("\n");
                JexChangelog jexChangelog = null;
                for (String s : sArray) {
                    if (isVersionLine(s)) {
                        JexVersion jexVersion = new JexVersion(s.replace("\n", ""));
                        jexChangelog = new JexChangelog(jexVersion);
                    } else if (isBreakLine(s)) {
                        ChangelogScreen.changelogs.add(jexChangelog);
                        jexChangelog = null;
                    } else {
                        jexChangelog.lines.add(new ChangelogLine(s, getColorForLine(s)));
                    }
                }
                if (jexChangelog != null)
                    ChangelogScreen.changelogs.add(jexChangelog);
            } catch(Exception e){}
        }).start();
    }

    private static Color getColorForLine(String str) {
        String s = str.toLowerCase();
        if (s.startsWith("added") || s.startsWith("improved") || s.startsWith("greatly improved"))
            return Color.GREEN;
        if (s.startsWith("fixed") || s.startsWith("updated") || s.startsWith("redesigned"))
            return Color.YELLOW;
        return Color.ORANGE;
    }

    private static boolean isBreakLine(String line) {
        return line.equals("\n") || line.strip().isEmpty();
    }

    private static boolean isVersionLine(String line) {
        if (line.contains(".")) {
            try {
                Integer.parseInt(line.split("\\.")[0]);
                return true;
            } catch (Exception e) {}
        }
        return false;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
