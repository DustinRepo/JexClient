package me.dustin.jex.gui.changelog;

import me.dustin.jex.JexClient;
import me.dustin.jex.gui.account.impl.AccountButton;
import me.dustin.jex.gui.changelog.changelog.JexChangelog;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import java.util.ArrayList;

public class ChangelogScreen extends Screen {

    public static final ArrayList<JexChangelog> changelogs = new ArrayList<>();
    private Scrollbar scrollbar;
    private boolean movingScrollbar;

    public ChangelogScreen() {
        super(Text.of("Changelog"));
    }

    @Override
    protected void init() {
        int height = 10;
        for (JexChangelog changelog : changelogs) {
            changelog.setY(height);
            height += changelog.getContentHeight() + 5;
        }
        scrollbar = new Scrollbar(Render2DHelper.INSTANCE.getScaledWidth() - 15, 10, 9, 0, Render2DHelper.INSTANCE.getScaledHeight() - 45, height - 15, 0xff696969);
        this.addDrawableChild(new ButtonWidget(width / 2 - 40, this.height - 22, 80, 20, Text.of("Back"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new TitleScreen());
        }));
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        FontHelper.INSTANCE.drawCenteredString(matrices, "Changelog", width / 2.f, 1, ColorHelper.INSTANCE.getClientColor());
        float changelogX = 20;
        float changelogWidth = Render2DHelper.INSTANCE.getScaledWidth() - (changelogX * 2);
        Scissor.INSTANCE.cut(0, 10, width, height - 45);
        for (JexChangelog changelog : changelogs) {
            if (changelog.getY() + changelog.getContentHeight() > 10 && changelog.getY() < height - 35)
                changelog.render(matrices, changelogX, changelogWidth);
        }
        Scissor.INSTANCE.seal();
        scrollbar.render(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrollbar.isHovered()) {
            movingScrollbar = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double amount) {
        if (changelogs.isEmpty())
            return false;
        if (amount > 0) {
            JexChangelog topChangelog = changelogs.get(0);
            if (topChangelog == null) return false;
            for (int i = 0; i < 50; i++) {
                if (topChangelog.getY() < 10) {
                    for (JexChangelog button : changelogs) {
                        button.setY(button.getY() + 1);
                    }
                    if (scrollbar != null)
                        scrollbar.moveUp();
                }
            }
        } else if (amount < 0) {
            JexChangelog bottomChangelog = changelogs.get(changelogs.size() - 1);
            if (bottomChangelog == null) return false;
            for (int i = 0; i < 50; i++) {
                if (bottomChangelog.getY() + bottomChangelog.getContentHeight() > height - 35) {
                    for (JexChangelog changelog : changelogs) {
                        changelog.setY(changelog.getY() - 1);
                    }
                    if (scrollbar != null)
                        scrollbar.moveDown();
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        if (movingScrollbar) {
            if (MouseHelper.INSTANCE.isMouseButtonDown(0))
                moveScrollbar();
            else
                movingScrollbar = false;
        }
        super.tick();
    }

    private void moveScrollbar() {
        float mouseY = MouseHelper.INSTANCE.getMouseY();
        float scrollBarHoldingArea = scrollbar.getY() + (scrollbar.getHeight() / 2.f);
        float dif = mouseY - scrollBarHoldingArea;
        if (dif > 1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() + scrollbar.getHeight() < scrollbar.getViewportY() + scrollbar.getViewportHeight()) {
                    scrollbar.moveDown();
                    for (JexChangelog changelog : changelogs) {
                        changelog.setY(changelog.getY() - 1);
                    }
                }
            }
        } else if (dif < -1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() > scrollbar.getViewportY()) {
                    scrollbar.moveUp();
                    for (JexChangelog changelog : changelogs) {
                        changelog.setY(changelog.getY() + 1);
                    }
                }
            }
        }
    }
}
