package me.dustin.jex.gui.account;

import me.dustin.jex.file.AltFile;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.gui.account.account.MinecraftAccountManager;
import me.dustin.jex.gui.account.impl.AccountButton;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.Login;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.render.FontHelper;
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
import net.minecraft.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class AccountManager extends Screen {

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

    private String outputString;
    private Scrollbar scrollbar;

    public AccountManager(Text title) {
        super(title);
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
            Wrapper.INSTANCE.getMinecraft().openScreen(new GuiAddAccount(getSelected().getAccount(), this));
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
        ButtonWidget directButton = new ButtonWidget((int) (midX - 75), height - 50, 150, 20, new LiteralText("Add Account"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(new GuiAddAccount(null, this));
        });
        cancelButton = new ButtonWidget((int) (midX - 75), height - 28, 150, 20, new LiteralText("Cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(new MultiplayerScreen(new TitleScreen()));
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
        this.addDrawableChild(directButton);
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
        editButton.active = getSelected() != null;
        removeButton.active = getSelected() != null;
        randomButton.active = accountButtons.size() > 1;
        renderBackground(matrixStack);

        Render2DHelper.INSTANCE.fill(matrixStack, (width / 2) - 152, (height / 2) - 102, (width / 2), (height / 2) + 102, 0x50000000);

        Scissor.INSTANCE.cut(0, ((height / 2) - 100), width / 2 - 2, 200);
        accountButtons.forEach(button -> {
            if (isOnScreen(button))
                button.draw(matrixStack);
        });
        Scissor.INSTANCE.seal();

        searchTextField.render(matrixStack, mouseX, mouseY, partialTicks);

        FontHelper.INSTANCE.drawWithShadow(matrixStack, "Status", (width / 2) + 3, (height / 2) - 98, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, outputString, (width / 2) + 3, (height / 2) - 88, 0xff696969);

        Render2DHelper.INSTANCE.fill(matrixStack, 0, 0, width, 20, 0x70000000);
        Render2DHelper.INSTANCE.drawHLine(matrixStack, 0, width, 20, ColorHelper.INSTANCE.getClientColor());

        Render2DHelper.INSTANCE.fill(matrixStack, 0, height - 52, width, height, 0x70000000);
        Render2DHelper.INSTANCE.drawHLine(matrixStack, 0, width, height - 52, ColorHelper.INSTANCE.getClientColor());

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
                outputString = "Logging in...";
                outputString = Login.INSTANCE.loginToAccount(button.getAccount());
                if (outputString.contains("Logged in")) {
                    button.getAccount().setUsername(Wrapper.INSTANCE.getMinecraft().getSession().getUsername());
                }
                if (outputString.equalsIgnoreCase("Cannot contact authentication server")) {
                    MCAPIHelper.APIStatus authServer = MCAPIHelper.INSTANCE.getStatus(MCAPIHelper.APIServer.AUTHSERVER);
                    if (authServer == MCAPIHelper.APIStatus.RED)
                        outputString = "Authentication servers offline.";
                    if (authServer == MCAPIHelper.APIStatus.GREEN)
                        outputString = "Your IP may be temp banned from logging in.";
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
