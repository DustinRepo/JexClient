package me.dustin.jex.gui.account;

import me.dustin.jex.helper.file.files.AltFile;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.gui.account.account.MinecraftAccountManager;
import me.dustin.jex.gui.account.impl.AccountButton;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.login.MojangLogin;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.network.login.MicrosoftLogin;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

public class AccountManagerScreen extends Screen {

    private ArrayList<AccountButton> accountButtons = new ArrayList<>();
    private MinecraftAccountManager accountManager = MinecraftAccountManager.INSTANCE;
    private ButtonWidget loginButton;
    private ButtonWidget editButton;
    private ButtonWidget removeButton;
    private ButtonWidget randomButton;
    private ButtonWidget cancelButton;
    private ButtonWidget importButton;
    private ButtonWidget exportButton;
    private TextFieldWidget searchTextField;

    public String outputString;
    private Scrollbar scrollbar;

    public AccountManagerScreen() {
        super(new LiteralText("Account Manager"));
    }

    @Override
    public void init() {
        MinecraftAccountManager.INSTANCE.getAccounts().clear();
        AltFile.read();

        float midX = width / 2;

        loadAccountButtons("");


        loginButton = new ButtonWidget((int) (midX + 3), (height / 2) - 78, 150, 20, new LiteralText("Login"), button -> {
            if (getSelected() == null)
                return;
            this.outputString = "Logging in...";
            login(getSelected());
        });
        editButton = new ButtonWidget((int) (midX + 3), (height / 2) - 56, 150, 20, new LiteralText("Edit"), button -> {
            if (getSelected().getAccount() instanceof MinecraftAccount.MojangAccount mojangAccount) {
                Wrapper.INSTANCE.getMinecraft().setScreen(new AddAccountScreen(mojangAccount, this));
            }
        });

        removeButton = new ButtonWidget((int) (midX + 3), (height / 2) - 34, 150, 20, new LiteralText("Remove"), button -> {
            if (getSelected() == null)
                return;
            MinecraftAccountManager.INSTANCE.getAccounts().remove(getSelected().getAccount());
            accountButtons.remove(getSelected());
            loadAccountButtons(searchTextField.getText());
            AltFile.write();
        });
        randomButton = new ButtonWidget((int) (midX + 3), (height / 2) - 12, 150, 20, new LiteralText("Random"), button -> {
            Random rand = new Random();
            login(accountButtons.get(rand.nextInt(accountButtons.size())));
        });
        ButtonWidget addAccountButton = new ButtonWidget((int) (midX - 151), height - 50, 150, 20, new LiteralText("Direct Login"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DirectLoginScreen(this));
        });
        ButtonWidget directLoginButton = new ButtonWidget((int) (midX + 1), height - 50, 150, 20, new LiteralText("Add Account"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new AddAccountScreen(null, this));
        });
        cancelButton = new ButtonWidget((int) (midX - 151), height - 28, 302, 20, new LiteralText("Cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new MultiplayerScreen(new TitleScreen()));
        });
        importButton = new ButtonWidget(2, 2, 50, 15, new LiteralText("Import"), button -> {
            if (new File(ModFileHelper.INSTANCE.getJexDirectory(), "Accounts-Unencrypted.json").exists()) {
                MinecraftAccountManager.INSTANCE.getAccounts().clear();
                AltFile.importFile();
                AltFile.write();
                loadAccountButtons("");
                outputString = "Imported alts from file.";
            } else
                outputString = "Could not import file. Please make sure it is named Accounts-Unencrypted.json";
        });
        exportButton = new ButtonWidget(width - 52, 2, 50, 15, new LiteralText("Export"), button -> {
            AltFile.exportFile();
            outputString = "Exported alts to Accounts-Unencrypted.json";
        });
        searchTextField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (int) midX - 150, (height / 2) - 124, 250, 20, new LiteralText(""));
        searchTextField.setVisible(true);
        searchTextField.setEditable(true);
        searchTextField.setFocusUnlocked(true);
        ButtonWidget searchButton = new ButtonWidget((int) midX + 102, (height / 2) - 124, 50, 20, new LiteralText("Search"), button -> {
            loadAccountButtons(searchTextField.getText());
        });
        this.addDrawableChild(loginButton);
        this.addDrawableChild(editButton);
        this.addDrawableChild(removeButton);
        this.addDrawableChild(randomButton);
        this.addDrawableChild(addAccountButton);
        this.addDrawableChild(directLoginButton);
        this.addDrawableChild(cancelButton);
        this.addSelectableChild(searchTextField);
        this.addDrawableChild(searchButton);

        this.addDrawableChild(exportButton);
        this.addDrawableChild(importButton);
        this.outputString = "Logged in as " + Wrapper.INSTANCE.getMinecraft().getSession().getUsername();

        if (!accountButtons.isEmpty()) {
            float contentHeight = (accountButtons.get(accountButtons.size() - 1).getY() + (accountButtons.get(accountButtons.size() - 1).getHeight())) - accountButtons.get(0).getY();
            float viewportHeight = 200;
            this.scrollbar = new Scrollbar((width / 2.f), (height / 2.f) - 102, 2, 200, viewportHeight, contentHeight, ColorHelper.INSTANCE.getClientColor());
        }
        super.init();
    }


    @Override
    public void tick() {
        searchTextField.tick();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        loginButton.active = getSelected() != null;
        editButton.active = getSelected() != null && getSelected().getAccount() instanceof MinecraftAccount.MojangAccount;
        removeButton.active = getSelected() != null;
        randomButton.active = accountButtons.size() > 1;
        renderBackground(matrixStack);

        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, (width / 2) - 154, (height / 2) - 128, (width / 2) + 156, (height / 2) + 102, 0xff000000, 0x50cccccc, 1);

        Scissor.INSTANCE.cut(0, ((height / 2) - 100), width / 2 - 2, 200);
        accountButtons.forEach(button -> {
            if (isOnScreen(button))
                button.draw(matrixStack);
        });
        Scissor.INSTANCE.seal();

        searchTextField.render(matrixStack, mouseX, mouseY, partialTicks);

        FontHelper.INSTANCE.drawWithShadow(matrixStack, "Status", (width / 2.f) + 4, (height / 2.f) - 98, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, outputString, (width / 2.f) + 4, (height / 2.f) - 88, ColorHelper.INSTANCE.getClientColor());

        Render2DHelper.INSTANCE.fill(matrixStack, 0, 0, width, 20, 0x70000000);
        Render2DHelper.INSTANCE.drawHLine(matrixStack, 0, width, 20, ColorHelper.INSTANCE.getClientColor());

        Render2DHelper.INSTANCE.fill(matrixStack, 0, height - 52, width, height, 0x70000000);
        Render2DHelper.INSTANCE.drawHLine(matrixStack, 0, width, height - 52, ColorHelper.INSTANCE.getClientColor());

        if (getSelected() != null) {
            int loginCount = getSelected().getAccount().loginCount;
            long lastLogin = getSelected().getAccount().lastUsed;
            String lastLoginString = "";
            if (lastLogin <= 0) {
                lastLoginString = "Last login: \247cNever";
            } else {
                DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm");
                String formattedDate = df.format(lastLogin);
                lastLoginString = "Last login: \247c" + formattedDate.split(" ")[0] + " \247a" + formattedDate.split(" ")[1];
            }
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, width / 2.f + 3, height / 2.f + 9, width / 2.f + 154, height / 2.f + 60, 0xff000000, 0x60000000, 1);
            FontHelper.INSTANCE.drawWithShadow(matrixStack, "Times logged in: " + loginCount, width / 2.f + 6, height / 2.f + 12, -1);
            FontHelper.INSTANCE.drawWithShadow(matrixStack, lastLoginString, width / 2.f + 6, height / 2.f + 23, -1);
        }

        if (scrollbar != null)
            scrollbar.render(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean save = super.mouseClicked(mouseX, mouseY, button);
        accountButtons.forEach(accountButton -> {
            if (isInButtons())
                accountButton.setSelected(accountButton.isHovered());
        });
        return save;
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        return super.charTyped(chr, keyCode);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double amount) {
        if (accountButtons.isEmpty())
            return false;
        if (amount > 0) {
            AccountButton topButton = accountButtons.get(0);
            if (topButton == null) return false;
            if (topButton.getY() < ((height / 2) - 100)) {
                for (int i = 0; i < 20; i++) {
                    if (topButton.getY() < ((height / 2) - 100)) {
                        for (AccountButton button : accountButtons) {
                            button.setY(button.getY() + 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveUp();
                    }
                }
            }
        } else if (amount < 0) {
            AccountButton bottomButton = accountButtons.get(accountButtons.size() - 1);
            if (bottomButton == null) return false;
            if (bottomButton.getY() + bottomButton.getHeight() > ((height / 2) + 100)) {
                for (int i = 0; i < 20; i++) {
                    if (bottomButton.getY() + bottomButton.getHeight() > ((height / 2) + 100)) {
                        for (AccountButton button : accountButtons) {
                            button.setY(button.getY() - 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveDown();
                    }
                }
            }
        }
        return false;
    }


    private boolean isOnScreen(AccountButton button) {
        return button.getX() + button.getWidth() > 0 && button.getX() < width && button.getY() + button.getHeight() > 0 && button.getY() < height;
    }

    public boolean isInButtons() {
        return MouseHelper.INSTANCE.getMouseX() > (width / 2) - 150 && MouseHelper.INSTANCE.getMouseX() >= height / 2 - 100 && MouseHelper.INSTANCE.getMouseX() <= width / 2 && MouseHelper.INSTANCE.getMouseY() <= height / 2 + 100;
    }

    public AccountButton getSelected() {
        for (AccountButton button : accountButtons) {
            if (button.isSelected())
                return button;
        }
        return null;
    }

    public AccountButton getFirst() {
        if (accountButtons.size() == 0)
            return null;
        return accountButtons.get(0);
    }

    public AccountButton getLast() {
        if (accountButtons.size() == 0)
            return null;
        return accountButtons.get(accountButtons.size() - 1);
    }

    public void login(AccountButton button) {
        new Thread("Login") {
                @Override
                public void run() {
                    if (button.getAccount() instanceof MinecraftAccount.MojangAccount mojangAccount) {
                        outputString = "Logging in...";
                        try {
                            if (MojangLogin.INSTANCE.login(mojangAccount)) {
                                button.getAccount().setUsername(Wrapper.INSTANCE.getMinecraft().getSession().getUsername());
                                outputString = "Logged in as " + Wrapper.INSTANCE.getMinecraft().getSession().getUsername();
                            } else
                                outputString = "Login failed";
                        }catch (Exception e) {
                            MCAPIHelper.APIStatus authServer = MCAPIHelper.INSTANCE.getStatus(MCAPIHelper.APIServer.AUTHSERVER);
                            if (authServer == MCAPIHelper.APIStatus.RED)
                                outputString = "Authentication servers offline.";
                            else if (authServer == MCAPIHelper.APIStatus.GREEN)
                                outputString = "Your IP may be temp banned from logging in.";
                            else
                                outputString = "Login failed";
                        }
                    } else if (button.getAccount() instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                        new MicrosoftLogin(true).login(microsoftAccount.accessToken, microsoftAccount.refreshToken);
                    }
                }
        }.start();
    }

    public void loadAccountButtons(String searchField) {
        int yCount = 0;
        accountButtons.clear();
        for (MinecraftAccount account : accountManager.getAccounts()) {
            if (!searchField.isEmpty()) {
                if (!account.getUsername().toLowerCase().contains(searchField.toLowerCase()))
                    continue;
            }
            float buttonX = (width / 2.f) - 150;
            float buttonY = ((height / 2.f) - 100) + (yCount * 41);
            accountButtons.add(new AccountButton(account, buttonX, buttonY));
            yCount++;
        }
    }
}
