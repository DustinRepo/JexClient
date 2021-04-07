package me.dustin.jex.gui.click.impl;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.file.ModuleFile;
import me.dustin.jex.gui.click.ClickGui;
import me.dustin.jex.gui.click.listener.ButtonListener;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.render.Gui;
import me.dustin.jex.option.types.ColorOption;
import me.dustin.jex.option.types.StringOption;
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
    private Module module;
    private int childCount;
    private float buttonsHeight;

    public ModuleButton(Window window, Module module, float x, float y, float width, float height) {
        super(window, module.getName(), x, y, width, height, null);
        this.module = module;
    }

    @Override
    public void draw(MatrixStack matrixStack) {
        updateOnOff();
        Gui.clickgui.setZOffset(-200);
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), module.getState() ? ColorHelper.INSTANCE.getColor(getWindow().getColor()).darker().darker().getRGB() : 0x80000000);
        Gui.clickgui.setZOffset(0);

        FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getModule().getName(), this.getX() + 3, (this.getY() + (this.getHeight() / 2)) - (Wrapper.INSTANCE.getTextRenderer().fontHeight / 2), -1);
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
                    this.getModule().setVisible(!this.getModule().isVisible());
                    if (ClickGui.doesPlayClickSound())
                        Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    if (JexClient.INSTANCE.isAutoSaveEnabled())
                        ModuleFile.write();
                    return;
                }
                this.getModule().toggleState();
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ModuleFile.write();
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
            button.click(double_1, double_2, int_1);
        });
    }

    public void open() {
        buttonsHeight = 0;
        childCount = 0;
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
        module.getOptions().forEach(option ->
        {
            if (!option.hasParent()) {
                OptionButton optionButton = new OptionButton(this.getWindow(), option, this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, this.getHeight());

                if (option instanceof ColorOption)
                    optionButton.setHeight(100);

                if (option instanceof StringOption)
                    optionButton.setHeight(this.getHeight() + 10);

                this.getChildren().add(optionButton);
                childCount++;
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
                    moduleButton.getModule().setKey(0);
                    thisButton.setName("Key: <>");
                } else {
                    moduleButton.getModule().setKey(event.getKey());
                    thisButton.setName("Key: " + (GLFW.glfwGetKeyName(event.getKey(), event.getScancode()) == null ? InputUtil.fromKeyCode(event.getKey(), event.getScancode()).getTranslationKey().replace("key.keyboard.", "").replace(".", "_") : GLFW.glfwGetKeyName(event.getKey(), event.getScancode()).toUpperCase()).toUpperCase().replace("key.keyboard.", "").replace(".", "_"));
                }
                while (EventAPI.getInstance().alreadyRegistered(this))
                    EventAPI.getInstance().unregister(this);
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ModuleFile.write();
            }

            @Override
            public void invoke() {
                Button thisButton = this.button.getChildren().get(this.button.getChildren().size() - 2);
                thisButton.setName("\2479Key: ...");
                EventAPI.getInstance().register(this);
            }


        };
        String keyString = module.getKey() == 0 ? "<>" : (GLFW.glfwGetKeyName(module.getKey(), 0) == null ? InputUtil.fromKeyCode(module.getKey(), 0).getTranslationKey().replace("key.keyboard.", "").replace(".", "_") : GLFW.glfwGetKeyName(module.getKey(), 0).toUpperCase()).replace("key.keyboard.", "").replace(".", "_");
        this.getChildren().add(new Button(this.getWindow(), "Key: " + (keyString.equalsIgnoreCase("0") ? "<>" : keyString.toUpperCase()), this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, this.getHeight(), keybind));
        childCount++;
        buttonsHeight += this.getHeight();
        ButtonListener visible = new ButtonListener(this) {
            @Override
            public void invoke() {
                ((ModuleButton) this.button).getModule().setVisible(!((ModuleButton) this.button).getModule().isVisible());
                this.button.getChildren().get(this.button.getChildren().size() - 1).setName("Visible: " + ((ModuleButton) this.button).getModule().isVisible());
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ModuleFile.write();
            }
        };
        this.getChildren().add(new Button(this.getWindow(), "Visible: " + this.getModule().isVisible(), this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, this.getHeight(), visible));
        childCount++;
        buttonsHeight += this.getHeight();
    }

    private void updateOnOff() {
        if (!timer.hasPassed(10))
            return;
        timer.reset();
        for (int i = 0; i < 2; i++) {
            if (this.getModule().getState()) {
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
        return !module.getOptions().isEmpty();
    }

    public Module getModule() {
        return module;
    }
}
