package me.dustin.jex.gui.thealtening;

import me.dustin.jex.JexClient;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.gui.thealtening.impl.TheAlteningAccountButton;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import me.dustin.jex.helper.network.login.thealtening.TheAlteningHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;

public class TheAlteningScreen extends Screen {
    public TheAlteningScreen(Screen parent) {
        super(Component.nullToEmpty("The Altening"));
        this.parent = parent;
    }

    private Screen parent;

    private ArrayList<TheAlteningAccountButton> favorites = new ArrayList<>();
    private ArrayList<TheAlteningAccountButton> privates = new ArrayList<>();
    private EditBox apiKeyWidget;
    private Button loginButton;
    private Button loginGeneratedButton;
    private Button loginTokenButton;
    private Button generateButton;
    private Button setApiKeyButton;
    private Button getTokenButton;
    private Button signUpButton;
    private Button favoriteGeneratedButton;
    private Button privateGeneratedButton;
    private EditBox tokenWidget;


    private Scrollbar scrollbar1;
    private boolean movingScrollbar1;
    private Scrollbar scrollbar2;
    private boolean movingScrollbar2;

    private String logInStatus = "";
    private String tokenStatus = "Use Generated Token:";

    private TheAlteningHelper.TheAlteningAccount generatedAccount;

    private ArrayList<TheAlteningHelper.TheAlteningAccount> favoriteAccounts = new ArrayList<>();
    private ArrayList<TheAlteningHelper.TheAlteningAccount> privateAccounts = new ArrayList<>();

    @Override
    protected void init() {
        apiKeyWidget = new EditBox(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 150, 12, 200, 20, Component.nullToEmpty(""));
        tokenWidget = new EditBox(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 150, 365, 200, 20, Component.nullToEmpty(""));
        if (!TheAlteningHelper.INSTANCE.getApiKey().isEmpty()) {
            apiKeyWidget.setValue(TheAlteningHelper.INSTANCE.getApiKey().substring(0, 4) + "****-****-****");
            updateAPIKey();
        }
        setApiKeyButton = new Button(width / 2 + 52, 12, 98, 20, Component.nullToEmpty("Set API Key"), button -> {
            TheAlteningHelper.INSTANCE.setApiKey(this.apiKeyWidget.getValue());
            apiKeyWidget.setValue(TheAlteningHelper.INSTANCE.getApiKey().substring(0, 4) + "****-****-****");
            updateAPIKey();
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        });
        loginButton = new Button(width / 2 - 152, 330, 150, 20, Component.nullToEmpty("Login to Selected"), button -> {
            if (getSelected() != null) {
                TheAlteningHelper.INSTANCE.login(getSelected().getAccount(), session -> {
                    if (session != null) {
                        JexClient.INSTANCE.getLogger().info("Logged in to TheAltening account named " + session.getName());
                        Wrapper.INSTANCE.getIMinecraft().setSession(session);
                        logInStatus = "Logged in to TheAltening account named \247b" + session.getName();
                    } else {
                        logInStatus = "Unable to login";
                    }
                });
            }
        });
        loginGeneratedButton = new Button(width / 2 + 2, 330, 150, 20, Component.nullToEmpty("Login to Generated"), button -> {
            if (generatedAccount != null) {
                TheAlteningHelper.INSTANCE.login(generatedAccount, session -> {
                    if (session != null) {
                        JexClient.INSTANCE.getLogger().info("Logged in to TheAltening account named " + session.getName());
                        Wrapper.INSTANCE.getIMinecraft().setSession(session);
                        logInStatus = "Logged in to TheAltening account named \247b" + session.getName();
                    } else {
                        logInStatus = "Unable to login";
                    }
                });
            }
        });
        loginTokenButton = new Button(width / 2 + 52, 365, 100, 20, Component.nullToEmpty("Login With Token"), button -> {
            TheAlteningHelper.INSTANCE.login(this.tokenWidget.getValue(), session -> {
                if (session != null) {
                    JexClient.INSTANCE.getLogger().info("Logged in to TheAltening account named " + session.getName());
                    Wrapper.INSTANCE.getIMinecraft().setSession(session);
                    tokenStatus = "Logged in to TheAltening account named \247b" + session.getName();
                } else {
                    tokenStatus = ChatFormatting.RED + "Invalid Token";
                }
                this.tokenWidget.setValue("");
            });
        });
        generateButton = new Button(width / 2 - 152, 305, 150, 20, Component.nullToEmpty("Generate"), button -> generatedAccount = TheAlteningHelper.INSTANCE.generateAccount());
        favoriteGeneratedButton = new Button(width / 2 + 2, 305, 75, 20, Component.nullToEmpty("Favorite"), button -> {
            if (generatedAccount != null) {
                if (TheAlteningHelper.INSTANCE.favoriteAcc(generatedAccount)) {
                    this.favoriteAccounts.add(generatedAccount);
                    String s = "\247b" + generatedAccount.username + " \247rfavorited.";
                    this.updateAPIKey();
                    this.logInStatus = s;
                    generatedAccount = null;
                }
            }
        });

        privateGeneratedButton = new Button(width / 2 + 77, 305, 75, 20, Component.nullToEmpty("Private"), button -> {
            if (generatedAccount != null) {
                if (TheAlteningHelper.INSTANCE.privateAcc(generatedAccount)) {
                    this.privateAccounts.add(generatedAccount);
                    String s = "\247b" + generatedAccount.username + " \247rprivated.";
                    this.updateAPIKey();
                    this.logInStatus = s;
                    generatedAccount = null;
                }
            }
        });
        getTokenButton = new Button(width - 127, 2, 125, 20, Component.nullToEmpty("Get Free Token"), button -> {
            WebHelper.INSTANCE.openLink("https://thealtening.com/free/free-minecraft-alts");
        });
        signUpButton = new Button(width - 127, 25, 125, 20, Component.nullToEmpty("Sign Up For TheAltening"), button -> {
            WebHelper.INSTANCE.openLink("https://thealtening.com/?i=wohc9");
        });
        Button cancelButton = new Button(width / 2 - 100, height - 22, 200, 20, Component.nullToEmpty("Cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(parent);
        });
        if (TheAlteningHelper.INSTANCE.isConnectedToAltening())
            logInStatus = "Logged in to TheAltening account named \247b" + Wrapper.INSTANCE.getMinecraft().getUser().getName();

        this.addWidget(apiKeyWidget);
        this.addWidget(tokenWidget);
        this.addRenderableWidget(setApiKeyButton);
        this.addRenderableWidget(generateButton);
        this.addRenderableWidget(loginButton);
        this.addRenderableWidget(loginGeneratedButton);
        this.addRenderableWidget(loginTokenButton);
        this.addRenderableWidget(getTokenButton);
        this.addRenderableWidget(signUpButton);
        this.addRenderableWidget(favoriteGeneratedButton);
        this.addRenderableWidget(privateGeneratedButton);
        this.addRenderableWidget(cancelButton);
        super.init();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        Render2DHelper.INSTANCE.fillAndBorder(matrices, (width / 2.f) - 154, 45, (width / 2.f) + 156, 263, 0xff000000, 0x50cccccc, 1);

        Scissor.INSTANCE.cut(0, 60, width, 200);
        favorites.forEach(theAlteningAccountButton -> theAlteningAccountButton.draw(matrices));
        privates.forEach(theAlteningAccountButton -> theAlteningAccountButton.draw(matrices));
        if (scrollbar1 != null)
            scrollbar1.render(matrices);
        if (scrollbar2 != null)
            scrollbar2.render(matrices);
        Scissor.INSTANCE.seal();

        this.loginGeneratedButton.active = generatedAccount != null;
        this.favoriteGeneratedButton.active = generatedAccount != null;
        this.privateGeneratedButton.active = generatedAccount != null;
        this.loginButton.active = getSelected() != null;
        this.loginTokenButton.active = !this.tokenWidget.getValue().isEmpty();
        this.setApiKeyButton.active = !this.apiKeyWidget.getValue().isEmpty();
        this.generateButton.active = TheAlteningHelper.INSTANCE.getLicense() != null && TheAlteningHelper.INSTANCE.hasValidLicense() && !"starter".equalsIgnoreCase(TheAlteningHelper.INSTANCE.getLicense().licenseType);

        if (generatedAccount != null) {
            Render2DHelper.INSTANCE.fillAndBorder(matrices, width / 2.f - 100, 265, width / 2.f + 100, 300, 0xff000000, 0x50404040, 1);
            FontHelper.INSTANCE.drawCenteredString(matrices, "Generated Account:", width / 2.f, 267, 0xff606060);
            FontHelper.INSTANCE.drawWithShadow(matrices, "Username: \247a" +  generatedAccount.username, width / 2.f - 97, 277, 0xff606060);
            FontHelper.INSTANCE.drawWithShadow(matrices, "Token: \247b" + generatedAccount.token, width / 2.f - 97, 287, 0xff606060);
            FontHelper.INSTANCE.drawWithShadow(matrices, "Limit: " + (generatedAccount.limit ? ChatFormatting.GREEN + "true" : ChatFormatting.RED + "false"), width / 2.f + 99 - (FontHelper.INSTANCE.getStringWidth("Limit: " + (generatedAccount.limit ? "\247atrue" : "\247cfalse"))), 277, 0xff606060);

            Render2DHelper.INSTANCE.bindTexture(TheAlteningHelper.INSTANCE.getSkin(generatedAccount));
            Render2DHelper.INSTANCE.drawTexture(matrices, width / 2.f + 102, 265, 0, 0, 18, 32, 18, 32);
        }
        FontHelper.INSTANCE.drawWithShadow(matrices, "API Key:", width / 2.f - 150, 1, -1);
        FontHelper.INSTANCE.drawWithShadow(matrices, "Favorites:", width / 2.f - 148, 50, -1);
        FontHelper.INSTANCE.drawWithShadow(matrices, "Privates:", width / 2.f + 4, 50, -1);
        FontHelper.INSTANCE.drawWithShadow(matrices, tokenStatus, width / 2.f - 150, 355, 0xff606060);
        FontHelper.INSTANCE.drawCenteredString(matrices, logInStatus, width / 2.f, 35, 0xff606060);

        if (TheAlteningHelper.INSTANCE.getLicense() != null) {
            TheAlteningHelper.TheAlteningLicense license = TheAlteningHelper.INSTANCE.getLicense();
            FontHelper.INSTANCE.drawWithShadow(matrices, "Has License: " + (license.hasLicense ? ChatFormatting.GREEN + "true" : ChatFormatting.RED + "false"), 2, 2, -1);
            if (TheAlteningHelper.INSTANCE.hasValidLicense()) {
                FontHelper.INSTANCE.drawWithShadow(matrices, "License Type:" + ChatFormatting.AQUA + StringUtils.capitalize(license.licenseType), 2, 12, -1);
                if (license.expires != null)
                    FontHelper.INSTANCE.drawWithShadow(matrices, "Expires:" + ChatFormatting.AQUA + license.expires.split("T")[0], 2, 22, -1);
            }
        }
        apiKeyWidget.render(matrices, mouseX, mouseY, delta);
        tokenWidget.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Wrapper.INSTANCE.getMinecraft().setScreen(parent);
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean save = super.mouseClicked(mouseX, mouseY, button);
        if (scrollbar1 != null)
            if (scrollbar1.isHovered()) {
                movingScrollbar1 = true;
            }
        if (scrollbar2 != null)
            if (scrollbar2.isHovered()) {
                movingScrollbar2 = true;
            }
        favorites.forEach(accountButton -> {
            if (isInButtons())
                accountButton.setSelected(accountButton.isHovered());
        });
        privates.forEach(accountButton -> {
            if (isInButtons())
                accountButton.setSelected(accountButton.isHovered());
        });
        return save;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double amount) {
        if (isInButtons()) {
            if (MouseHelper.INSTANCE.getMouseX() <= width/2.f)
                return scroll(amount, favorites, scrollbar1);
            else
                return scroll(amount, privates, scrollbar2);
        }
        return false;
    }

    public boolean scroll(double amount, ArrayList<TheAlteningAccountButton> list, Scrollbar scrollbar) {
        if (list.isEmpty())
            return false;
        if (amount > 0) {
            TheAlteningAccountButton topButton = list.get(0);
            if (topButton == null) return false;
            if (topButton.getY() < 60) {
                for (int i = 0; i < 20; i++) {
                    if (topButton.getY() < 60) {
                        for (TheAlteningAccountButton button : list) {
                            button.setY(button.getY() + 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveUp();
                    }
                }
            }
        } else if (amount < 0) {
            TheAlteningAccountButton bottomButton = list.get(list.size() - 1);
            if (bottomButton == null) return false;
            if (bottomButton.getY() + bottomButton.getHeight() > 260) {
                for (int i = 0; i < 20; i++) {
                    if (bottomButton.getY() + bottomButton.getHeight() > 260) {
                        for (TheAlteningAccountButton button : list) {
                            button.setY(button.getY() - 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveDown();
                    }
                }
            }
        }
        return true;
    }

    public boolean isInButtons() {
        return MouseHelper.INSTANCE.getMouseX() > (width / 2) - 150 && MouseHelper.INSTANCE.getMouseY() >= 60 && MouseHelper.INSTANCE.getMouseX() <= width / 2 + 150 && MouseHelper.INSTANCE.getMouseY() <= 260;
    }

    public void updateAPIKey() {
        logInStatus = "Loading Altening profile";
        TheAlteningHelper.INSTANCE.fetchLicense();
        if (TheAlteningHelper.INSTANCE.hasValidLicense()) {
            favoriteAccounts = TheAlteningHelper.INSTANCE.getFavorites();
            privateAccounts = TheAlteningHelper.INSTANCE.getPrivates();
            this.favorites.clear();
            this.privates.clear();
            this.generatedAccount = null;
            int favoriteCount = 0;
            for (TheAlteningHelper.TheAlteningAccount favorite : favoriteAccounts) {
                TheAlteningAccountButton accountButton = new TheAlteningAccountButton(favorite, width / 2.f - 150, 60 + (31 * favoriteCount));
                this.favorites.add(accountButton);
                favoriteCount++;
            }

            int privateCount = 0;
            for (TheAlteningHelper.TheAlteningAccount private_ : privateAccounts) {
                TheAlteningAccountButton accountButton = new TheAlteningAccountButton(private_, width / 2.f + 2, 60 + (31 * privateCount));
                this.privates.add(accountButton);
                privateCount++;
            }

            if (!favorites.isEmpty()) {
                float contentHeight = (favorites.get(favorites.size() - 1).getY() + (favorites.get(favorites.size() - 1).getHeight())) - favorites.get(0).getY();
                float viewportHeight = 200;
                this.scrollbar1 = new Scrollbar((width / 2.f) - 2, 60, 3, 200, viewportHeight, contentHeight, -1);
            }

            if (!privates.isEmpty()) {
                float contentHeight = (privates.get(privates.size() - 1).getY() + (privates.get(privates.size() - 1).getHeight())) - privates.get(0).getY();
                float viewportHeight = 200;
                this.scrollbar2 = new Scrollbar((width / 2.f) + 150, 60, 3, 200, viewportHeight, contentHeight, -1);
            }
        }
        logInStatus = "TheAltening profile \247b" + TheAlteningHelper.INSTANCE.getLicense().username + " \247rloaded.";
    }

    @Override
    public void tick() {
        this.apiKeyWidget.tick();
        this.tokenWidget.tick();
        if (movingScrollbar1) {
            if (MouseHelper.INSTANCE.isMouseButtonDown(0))
                moveScrollbar(scrollbar1, favorites);
            else
                movingScrollbar1 = false;
        }
        if (movingScrollbar2) {
            if (MouseHelper.INSTANCE.isMouseButtonDown(0))
                moveScrollbar(scrollbar2, privates);
            else
                movingScrollbar2 = false;
        }
        super.tick();
    }

    private void moveScrollbar(Scrollbar scrollbar, ArrayList<TheAlteningAccountButton> list) {
        float mouseY = MouseHelper.INSTANCE.getMouseY();
        float scrollBarHoldingArea = scrollbar.getY() + (scrollbar.getHeight() / 2.f);
        float dif = mouseY - scrollBarHoldingArea;
        if (dif > 1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() + scrollbar.getHeight() < scrollbar.getViewportY() + scrollbar.getViewportHeight()) {
                    scrollbar.moveDown();
                    for (TheAlteningAccountButton button : list) {
                        button.setY(button.getY() - 1);
                    }
                }
            }
        } else if (dif < -1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() > scrollbar.getViewportY()) {
                    scrollbar.moveUp();
                    for (TheAlteningAccountButton button : list) {
                        button.setY(button.getY() + 1);
                    }
                }
            }
        }
    }

    private TheAlteningAccountButton getSelected() {
        for (TheAlteningAccountButton button : favorites) {
            if (button.isSelected())
                return button;
        }

        for (TheAlteningAccountButton button : privates) {
            if (button.isSelected())
                return button;
        }
        return null;
    }
}
