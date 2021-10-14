package me.dustin.jex.gui.click.window.impl;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.helper.file.files.FeatureFile;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.feature.option.types.ColorOption;
import me.dustin.jex.feature.option.types.StringOption;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.glfw.GLFW;

public class ModuleButton extends Button {

    Timer timer = new Timer();
    int togglePos = 0;
    int cogSpin = 0;
    private Feature feature;
    private float buttonsHeight;

    public ModuleButton(Window window, Feature feature, float x, float y, float width, float height) {
        super(window, feature.getName(), x, y, width, height, null);
        this.feature = feature;
    }

    @Override
    public void draw(MatrixStack matrixStack) {
        updateOnOff();
        Gui.clickgui.setZOffset(-200);
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), feature.getState() ? ColorHelper.INSTANCE.setAlpha(ColorHelper.INSTANCE.getColor(getWindow().getColor()).darker().darker().getRGB(), 180) : 0x80000000);
        Gui.clickgui.setZOffset(0);

        FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getFeature().getName(), this.getX() + 3, (this.getY() + (this.getHeight() / 2)) - (Wrapper.INSTANCE.getTextRenderer().fontHeight / 2), -1);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);

        matrixStack.push();
        matrixStack.translate(this.getX() + this.getWidth() - 7, this.getY() + 7.5f, 0);
        matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, 1.0F), cogSpin, true));
        Render2DHelper.INSTANCE.drawArrow(matrixStack, 0, 0, this.isOpen(), !this.isOpen() ? 0xff999999 : getWindow().getColor());
        matrixStack.pop();
        this.getChildren().forEach(button -> {
            button.draw(matrixStack);
            Render2DHelper.INSTANCE.drawVLine(matrixStack, button.getX() - 1, button.getY() - 1, button.getY() + button.getHeight(), getWindow().getColor());
            Render2DHelper.INSTANCE.drawVLine(matrixStack, button.getX() + button.getWidth(), button.getY() - 1, button.getY() + button.getHeight(), getWindow().getColor());
        });
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered()) {
            if (int_1 == 0) {
                if (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    this.getFeature().setVisible(!this.getFeature().isVisible());
                    if (ClickGui.doesPlayClickSound())
                        Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    if (JexClient.INSTANCE.isAutoSaveEnabled())
                        FeatureFile.write();
                    return;
                }
                this.getFeature().toggleState();
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    FeatureFile.write();
                if (ClickGui.doesPlayClickSound())
                    Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return;
            }
            if (int_1 == 1) {
                this.setOpen(!this.isOpen());
                if (this.isOpen())
                    this.open();
                else
                    this.close();
            }
        }
        getChildren().forEach(button -> {
            float maxHeight = Math.min(Render2DHelper.INSTANCE.getScaledHeight() - getWindow().getY() - 35, Gui.INSTANCE.maxWindowHeight);
            if (button.isVisible() && button.getY() < this.getWindow().getY() + this.getWindow().getHeight() + maxHeight) {
                button.click(double_1, double_2, int_1);
            }
        });
    }

    public void open() {
        buttonsHeight = 0;
        /*if(module instanceof Breadcrumbs)
        {
        	Button button = new Button(this.getWindow(), "Clear", this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, this.getHeight(), new ButtonListener() {

				@Override
				public void invoke() {
					((Breadcrumbs)Module.get(Breadcrumbs.class)).clearPositions();
				}
			});
        	this.getChildren().add(button);
        	childCount++;
            buttonsHeight += button.getHeight();
        }*/
        feature.getOptions().forEach(option ->
        {
            if (!option.hasParent()) {
                OptionButton optionButton = new OptionButton(this.getWindow(), option, this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, this.getHeight());

                if (option instanceof ColorOption)
                    optionButton.setHeight(100);

                if (option instanceof StringOption)
                    optionButton.setHeight(this.getHeight() + 10);

                this.getChildren().add(optionButton);
                buttonsHeight += optionButton.getHeight();
            }
        });

        addExtraButtons();

        allButtonsAfter().forEach(button -> {
            button.move(0, buttonsHeight);
        });
    }

    public void close() {
        this.getChildren().forEach(button -> {
            if (button instanceof OptionButton) {
                if (button.isOpen())
                    ((OptionButton) button).close();
            }
        });
        allButtonsAfter().forEach(button -> {
            button.move(0, -buttonsHeight);
        });
        this.getChildren().clear();
    }

    public void addExtraButtons() {
        for (String s : feature.addButtons().keySet()) {
            ButtonListener listener = feature.addButtons().get(s);
            this.getChildren().add(new Button(this.getWindow(), s, this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, this.getHeight(), listener));
            buttonsHeight += this.getHeight();
        }
        ButtonListener keybind = new ButtonListener(this) {

            @EventListener(events = {EventKeyPressed.class})
            public void runEvent(EventKeyPressed event) {
                if (event.getType() == EventKeyPressed.PressType.IN_GAME) {
                    while (EventAPI.getInstance().alreadyRegistered(this))
                        EventAPI.getInstance().unregister(this);
                    return;
                }
                ModuleButton moduleButton = (ModuleButton) this.button;
                Button thisButton = this.button.getChildren().get(this.button.getChildren().size() - 2);

                if (event.getKey() == GLFW.GLFW_KEY_ESCAPE || event.getKey() == GLFW.GLFW_KEY_ENTER) {
                    moduleButton.getFeature().setKey(0);
                    thisButton.setName("Key: None");
                } else {
                    moduleButton.getFeature().setKey(event.getKey());
                    thisButton.setName("Key: " + KeyboardHelper.INSTANCE.getKeyName(event.getKey()));
                }
                while (EventAPI.getInstance().alreadyRegistered(this))
                    EventAPI.getInstance().unregister(this);
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    FeatureFile.write();
            }

            @Override
            public void invoke() {
                Button thisButton = this.button.getChildren().get(this.button.getChildren().size() - 2);
                thisButton.setName("Press a key...");
                EventAPI.getInstance().register(this);
            }


        };
        String keyString = feature.getKey() == 0 ? "None" : KeyboardHelper.INSTANCE.getKeyName(feature.getKey());
        this.getChildren().add(new Button(this.getWindow(), "Key: " + keyString.toUpperCase(), this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, this.getHeight(), keybind));
        buttonsHeight += this.getHeight();
        ButtonListener visible = new ButtonListener(this) {
            @Override
            public void invoke() {
                ((ModuleButton) this.button).getFeature().setVisible(!((ModuleButton) this.button).getFeature().isVisible());
                this.button.getChildren().get(this.button.getChildren().size() - 1).setName("Visible: " + ((ModuleButton) this.button).getFeature().isVisible());
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    FeatureFile.write();
            }
        };
        this.getChildren().add(new Button(this.getWindow(), "Visible: " + this.getFeature().isVisible(), this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, this.getHeight(), visible));
        buttonsHeight += this.getHeight();
    }

    private void updateOnOff() {
        if (!timer.hasPassed(10))
            return;
        timer.reset();
        for (int i = 0; i < 2; i++) {
            if (this.getFeature().getState()) {
                if (togglePos < 20) {
                    togglePos++;
                }
            } else {
                if (togglePos > 0) {
                    togglePos--;
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            if (this.isOpen()) {
                if (cogSpin < 150) {
                    cogSpin++;
                }
            } else {
                if (cogSpin > 0) {
                    cogSpin--;
                }
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        this.getChildren().forEach(button -> {
            if (button instanceof OptionButton) {
                button.keyTyped(typedChar, keyCode);
            }
        });
    }

    public boolean hasOptions() {
        return !feature.getOptions().isEmpty();
    }

    public Feature getFeature() {
        return feature;
    }
}
