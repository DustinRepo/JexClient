package me.dustin.jex.gui.profileedit;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class ProfileEditingScreen extends Screen {
    private Screen parent;
    public ProfileEditingScreen(Screen parent) {
        super(new LiteralText("Profile Editor"));
        this.parent = parent;
    }

    private static String latestUUID;

    private boolean canChangeName, canMigrateToMSA, isMSAProfile;
    private TextFieldWidget setNameField, nameAvailableField;
    private ButtonWidget setNameButton, checkNameButton;

    private String nameSetString, checkNameString;

    @Override
    protected void init() {
        if (!Wrapper.INSTANCE.getMinecraft().getSession().getUuid().equalsIgnoreCase(latestUUID)) {
            if (!MCAPIHelper.INSTANCE.verifySecurityLocation()) {
                Wrapper.INSTANCE.getMinecraft().openScreen(new NoticeScreen(() -> Wrapper.INSTANCE.getMinecraft().openScreen(parent), new LiteralText(Formatting.RED + "Invalid Session"), new LiteralText(Formatting.GRAY + "Your session is invalid. Please re-login to your account first.")));
                return;
            }
            if (MCAPIHelper.INSTANCE.needsSecurityQuestions()) {
                Wrapper.INSTANCE.getMinecraft().openScreen(new SecurityQuestionsScreen(parent));
                return;
            }

            new Thread(() -> {
                //set all values from profile that would be checked
                canChangeName = MCAPIHelper.INSTANCE.canChangeName();
                canMigrateToMSA = MCAPIHelper.INSTANCE.canMigrateAccount();
                isMSAProfile = MCAPIHelper.INSTANCE.isMSAAccount();
            }).start();
            latestUUID = Wrapper.INSTANCE.getMinecraft().getSession().getUuid();
        }

        setNameField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), 2, 22, 150, 20, new LiteralText(""));
        setNameField.setMaxLength(16);
        setNameButton = new ButtonWidget(2, 55, 100, 20, new LiteralText("Set Name"), button -> {
            nameSetString = Formatting.GRAY + "Setting name...";
            new Thread(() -> {
                String name = setNameField.getText();
                if (!MCAPIHelper.INSTANCE.isNameAvailable(name)) {
                    nameSetString = Formatting.RED + "Name taken";
                    return;
                }
                Wrapper.INSTANCE.getMinecraft().openScreen(new ConfirmScreen(t -> {
                    if (MCAPIHelper.INSTANCE.setName(name)) {
                        nameSetString = Formatting.GRAY + "Name set to " + Formatting.GREEN + name;
                        canChangeName = false;
                    } else {
                        nameSetString = Formatting.RED + "Could not set name";
                    }
                    //Wrapper.INSTANCE.getMinecraft().openScreen(this);
                }, new LiteralText("Set Name"), new LiteralText("Are you sure you want to set your name?\nYou can only do this once every 30 days.")));
            }).start();
        });

        checkNameButton = new ButtonWidget(2, 78, 100, 20, new LiteralText("Check Name"), button -> {
            nameSetString = Formatting.GRAY + "Checking name availability...";
            new Thread(() -> {
                String name = setNameField.getText();
                if (!MCAPIHelper.INSTANCE.isNameAvailable(name)) {
                    nameSetString = Formatting.RED + "Name not available";
                } else {
                    nameSetString = Formatting.GREEN + "Name is available!";
                }
            }).start();
        });
        this.addSelectableChild(setNameField);
        this.addDrawableChild(setNameButton);
        this.addDrawableChild(checkNameButton);
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        FontHelper.INSTANCE.drawWithShadow(matrices, "Username: " + Formatting.AQUA + Wrapper.INSTANCE.getMinecraft().getSession().getUsername(), 2, 2, 0xff696969);
        FontHelper.INSTANCE.drawWithShadow(matrices, "Can Change name: " + (canChangeName ? Formatting.GREEN + "true" : Formatting.RED + "false"), 2, 12, 0xff696969);
        setNameField.render(matrices, mouseX, mouseY, delta);
        FontHelper.INSTANCE.drawWithShadow(matrices, nameSetString, 2, 45, 0xff696969);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        setNameField.tick();
        setNameButton.active = canChangeName && setNameField.getText().length() >= 3;
        checkNameButton.active = setNameField.getText().length() >= 3;
        super.tick();
    }
}
