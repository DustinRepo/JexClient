package me.dustin.jex.gui.click.impl;


import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.JexClient;
import me.dustin.jex.file.ModuleFile;
import me.dustin.jex.gui.click.ClickGui;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.option.Option;
import me.dustin.jex.option.enums.OpType;
import me.dustin.jex.option.types.*;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class OptionButton extends Button {

    Timer timer = new Timer();
    int togglePos = 0;
    int cogSpin = 0;
    private Option option;
    private boolean isSliding;
    private OptionButton masterButton;
    private OptionButton parentButton;
    private TextFieldWidget textField;
    private Identifier colorSlider = new Identifier("jex", "gui/click/colorslider.png");
    private int buttonsHeight;

    public OptionButton(Window window, Option option, float x, float y, float width, float height) {
        super(window, option.getName(), x, y, width, height, null);
        this.option = option;
    }

    @Override
    public void draw(MatrixStack matrixStack) {
        updateOnOff();
        if (textField == null) {
            if (this.getOption().getType() == OpType.STRING)
                textField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight(), new LiteralText(((StringOption) this.getOption()).getValue()));
        }

        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x60000000);


        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);

        switch (this.getOption().getType()) {
            case BOOL:
                FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getOption().getName(), this.getX() + (this.getWidth() / 2), this.getY() + 4, ((BoolOption) option).getValue() ? ColorHelper.INSTANCE.getClientColor() : 0xffaaaaaa);
                break;
            case STRINGARRAY:
                FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getOption().getName() + ": \247f" + ((StringArrayOption) this.getOption()).getValue(), this.getX() + (this.getWidth() / 2), this.getY() + 4, 0xffaaaaaa);
                break;
            case STRING:
                FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getOption().getName(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
                if (textField.isFocused())
                    Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY() + 12, this.getX() + this.getWidth(), this.getY() + this.getHeight(), ColorHelper.INSTANCE.getClientColor(), 0x00ffffff, 1);
                textField.x = (int) this.getX();
                textField.y = (int) this.getY() + 7;
                textField.setHasBorder(false);
                textField.setVisible(true);
                textField.setEditable(true);
                textField.render(matrixStack, MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY(), Wrapper.INSTANCE.getMinecraft().getTickDelta());
                break;
            case COLOR:
            case INT:
            case FLOAT:
                drawSliders(this.getOption(), matrixStack);
                break;
        }
        if (hasChild()) {
            GL11.glPushMatrix();
            GL11.glTranslated(this.getX() + this.getWidth() - 7, this.getY() + 7.5f, 0);
            GL11.glRotated(cogSpin, 0, 0, 1);
            Render2DHelper.INSTANCE.drawArrow(matrixStack, 0, 0, this.isOpen(), !this.isOpen() ? 0xff999999 : ColorHelper.INSTANCE.getClientColor());
            GL11.glPopMatrix();
        }
        if (isOpen())
            this.getChildren().forEach(button -> {
                button.draw(matrixStack);
                Render2DHelper.INSTANCE.drawVLine(matrixStack, button.getX() - 1, button.getY() - 1, button.getY() + button.getHeight(), ColorHelper.INSTANCE.getClientColor());
                Render2DHelper.INSTANCE.drawVLine(matrixStack, button.getX() + button.getWidth(), button.getY() - 1, button.getY() + button.getHeight(), ColorHelper.INSTANCE.getClientColor());
            });
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered()) {
            if (int_1 == 0) {
                if (this.getOption() instanceof BoolOption) {
                    ((BoolOption) this.getOption()).setValue(!((BoolOption) this.getOption()).getValue());
                    if (ClickGui.doesPlayClickSound())
                        Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                if (this.getOption() instanceof StringArrayOption) {
                    ((StringArrayOption) this.getOption()).inc();
                    if (this.isOpen())
                        this.close();
                    if (ClickGui.doesPlayClickSound())
                        Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                if (this.getOption() instanceof StringOption) {
                    textField.changeFocus(true);
                    textField.mouseClicked((int) double_1, (int) double_2, int_1);
                    textField.changeFocus(true);
                    if (ClickGui.doesPlayClickSound())
                        Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                if (this.getOption() instanceof FloatOption || this.getOption() instanceof IntOption || this.getOption() instanceof ColorOption) {
                    isSliding = true;
                }
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ModuleFile.write();
                return;
            }
            if (int_1 == 1) {
                this.setOpen(!this.isOpen());
                if (this.isOpen())
                    this.open();
                else
                    this.close();
            }
        } else {
            if (this.getOption() instanceof StringOption) {
                if (textField != null) {
                    textField.changeFocus(false);
                    textField.mouseClicked((int) double_1, (int) double_2, int_1);
                    textField.changeFocus(false);
                }
            }
        }
        getChildren().forEach(button -> {
            button.click(double_1, double_2, int_1);
        });
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (textField != null)
            textField.charTyped(typedChar, keyCode);
        if (this.getOption().getType() == OpType.STRING && textField != null) {
            ((StringOption) this.getOption()).setValue(textField.getText());
        }

        for (Button button : this.getChildren()) {
            if (button != this)
                button.keyTyped(typedChar, keyCode);
        }
    }

    private boolean hasChild() {
        if (this.getOption() instanceof StringArrayOption) {
            StringArrayOption sArrayOption = (StringArrayOption) this.getOption();
            for (Option option : this.getOption().getChildren()) {
                if (option.hasDependency()) {
                    if (option.getDependency().equalsIgnoreCase(sArrayOption.getValue()))
                        return true;
                } else {
                    return true;
                }
            }
        } else {
            return this.getOption().hasChild();
        }

        return false;
    }

    public void open() {
        buttonsHeight = 0;
        option.getChildren().forEach(option ->
        {
            OptionButton optionButton = new OptionButton(this.getWindow(), option, this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, option instanceof ColorOption ? 100 : 15);
            optionButton.masterButton = this.masterButton == null ? this : this.masterButton;
            optionButton.parentButton = this;

            if (option instanceof StringOption)
                optionButton.setHeight(this.getHeight() + 10);

            if (optionButton.getOption().hasDependency() && this.getOption() instanceof StringArrayOption) {
                if (optionButton.getOption().getDependency().equalsIgnoreCase(((StringArrayOption) this.getOption()).getValue())) {
                    this.getChildren().add(optionButton);
                    buttonsHeight += optionButton.getHeight();
                }
            } else {
                this.getChildren().add(optionButton);
                buttonsHeight += optionButton.getHeight();
            }
        });
        allButtonsAfter().forEach(button -> {
            button.move(0, buttonsHeight);
        });
    }

    @Override
    public ArrayList<Button> allButtonsAfter() {
        ArrayList<Button> buttons = new ArrayList<>();

        if (parentButton != null)
            parentButton.getChildren().forEach(button -> {
                if (parentButton.getChildren().indexOf(button) > parentButton.getChildren().indexOf(this)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            });

        getWindow().get(this.getOption().getModule()).getChildren().forEach(button -> {
            if (this.masterButton == null) {
                if (getWindow().get(this.getOption().getModule()).getChildren().indexOf(button) > getWindow().get(this.getOption().getModule()).getChildren().indexOf(this)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            } else {
                if (getWindow().get(this.getOption().getModule()).getChildren().indexOf(button) > getWindow().get(this.getOption().getModule()).getChildren().indexOf(masterButton)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            }
        });
        buttons.addAll(super.allButtonsAfter(getWindow().get(this.getOption().getModule())));
        return buttons;
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
        this.setOpen(false);
    }

    private void updateOnOff() {
        if (!timer.hasPassed(10))
            return;
        timer.reset();
        if (this.getOption().getType() == OpType.BOOL) {
            boolean enabled = ((BoolOption) this.getOption()).getValue();
            for (int i = 0; i < 2; i++) {
                if (enabled) {
                    if (togglePos < 20) {
                        togglePos++;
                    }
                } else {
                    if (togglePos > 0) {
                        togglePos--;
                    }
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

    public void drawSliders(Option property, MatrixStack matrixStack) {
        if (property instanceof FloatOption) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ModuleFile.write();
            }
            FloatOption v = (FloatOption) property;

            float startV = v.getValue() - v.getMin();

            float pos = ((float) (startV) / (v.getMax() - v.getMin())) * (this.getWidth());


            handleSliders(v);

            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + pos, this.getY() + this.getHeight(), Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(ColorHelper.INSTANCE.getClientColor())).darker().getRGB());
            FontHelper.INSTANCE.drawCenteredString(matrixStack, property.getName() + ": " + ((FloatOption) property).getValue(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        }
        if (property instanceof IntOption) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
            }
            IntOption v = (IntOption) property;


            float startV = v.getValue() - v.getMin();

            float pos = ((float) (startV) / (v.getMax() - v.getMin())) * (this.getWidth());

            handleSliders(v);

            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + pos, this.getY() + this.getHeight(), Color.decode("0x" + Integer.toHexString(ColorHelper.INSTANCE.getClientColor()).substring(2)).darker().getRGB());
            FontHelper.INSTANCE.drawCenteredString(matrixStack, property.getName() + ": " + ((IntOption) property).getValue(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        }

        if (property instanceof ColorOption) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
            }
            ColorOption v = (ColorOption) property;


            float huepos = (((float) v.getH() / 270)) * (80);

            float satpos = ((float) (v.getS())) * (80);
            float brightpos = ((float) ((1 - v.getB())) * 79);


            handleSliders(v);
            Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, -1, 0xff000000);
            drawGradientRect(matrixStack, this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, ColorHelper.INSTANCE.getColorViaHue(v.getH()).getRGB(), 0xff000000);
            Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, 0x20000000, 0xff000000);
            //color cursor
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 5 + satpos - 1, this.getY() + 15 + brightpos - 1, this.getX() + 5 + satpos + 1, this.getY() + 15 + brightpos + 1, -1);

            GL11.glColor4f(1, 1, 1, 1);
            //hue slider
            Wrapper.INSTANCE.getMinecraft().getTextureManager().bindTexture(colorSlider);
            DrawableHelper.drawTexture(matrixStack, (int) this.getX() + (int) this.getWidth() - 10, (int) this.getY() + 15, 0, 0, 5, 80, 10, 80);
            //hue cursor
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + this.getWidth() - 10, this.getY() + 15 + huepos - 1, (this.getX() + this.getWidth() - 10) + 5, this.getY() + 15 + huepos + 1, -1);

            FontHelper.INSTANCE.drawWithShadow(matrixStack, property.getName(), this.getX() + 1, this.getY() + 3, v.getValue());
        }
    }


    protected void drawGradientRect(MatrixStack matrixStack, float left, float top, float right, float bottom, int startColor, int endColor) {
        Matrix4f matrix = matrixStack.peek().getModel();
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float g = (float) (startColor >> 16 & 255) / 255.0F;
        float h = (float) (startColor >> 8 & 255) / 255.0F;
        float i = (float) (startColor & 255) / 255.0F;
        float j = (float) (endColor >> 24 & 255) / 255.0F;
        float k = (float) (endColor >> 16 & 255) / 255.0F;
        float l = (float) (endColor >> 8 & 255) / 255.0F;
        float m = (float) (endColor & 255) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, (float) right, (float) top, (float) 0).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) top, (float) 0).color(1, 1, 1, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, (float) 0).color(0, 0, 0, j).next();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, (float) 0).color(k, l, m, j).next();

        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    void handleSliders(IntOption v) {
        if (MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
            float position = MouseHelper.INSTANCE.INSTANCE.INSTANCE.getMouseX() - this.getX();
            float percent = position / this.getWidth() * 100;
            float increment = v.getInc();
            if (percent > 100) {
                percent = 100;
            }
            if (percent < 0) {
                percent = 0;
            }
            float value = (percent / 100) * ((v.getMax() - v.getMin()) + increment);
            value += v.getMin();
            if (value > v.getMax()) {
                value = v.getMax();
            }
            if (value < v.getMin()) {
                value = v.getMin();
            }
            v.setValue((int) ((int) Math.round(value * (1.0D / increment)) / (1.0D / increment)));
            v.setValue((int) ClientMathHelper.INSTANCE.round(v.getValue(), 2));
        }
    }

    void handleSliders(FloatOption v) {
        if (MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
            float position = MouseHelper.INSTANCE.INSTANCE.INSTANCE.getMouseX() - this.getX();
            float percent = position / this.getWidth() * 100;
            float increment = v.getInc();
            if (percent > 100) {
                percent = 100;
            }
            if (percent < 0) {
                percent = 0;
            }
            float value = (percent / 100) * ((v.getMax() - v.getMin()) + increment);
            value += v.getMin();
            if (value > v.getMax()) {
                value = v.getMax();
            }
            if (value < v.getMin()) {
                value = v.getMin();
            }
            v.setValue((float) ((float) Math.round(value * (1.0D / increment)) / (1.0D / increment)));
            v.setValue((float) ClientMathHelper.INSTANCE.INSTANCE.INSTANCE.INSTANCE.round(v.getValue(), 2));
        }
    }

    void handleSliders(ColorOption v) {
        if (MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
            if (MouseHelper.INSTANCE.INSTANCE.INSTANCE.getMouseX() > this.getX() + 100) {
                float position = MouseHelper.INSTANCE.INSTANCE.INSTANCE.getMouseY() - (this.getY() + 15);
                float percent = position / 79 * 100;
                float increment = 1;
                if (percent > 100) {
                    percent = 100;
                }
                if (percent < 0) {
                    percent = 0;
                }
                float value = (percent / 100) * ((270) + increment);
                if (value > 270) {
                    value = 270;
                }
                if (value < 0) {
                    value = 0;
                }
                v.setH((int) value);
            } else {
                float position = MouseHelper.INSTANCE.INSTANCE.INSTANCE.getMouseX() - (this.getX() + 5);
                float percent = position / 80 * 100;
                float increment = 1;
                if (percent > 100) {
                    percent = 100;
                }
                if (percent < 0) {
                    percent = 0;
                }
                v.setS(percent / 100);

                position = MouseHelper.INSTANCE.INSTANCE.INSTANCE.getMouseY() - (this.getY() + 15);
                percent = position / 79 * 100;
                percent = 100 - percent;
                increment = 1;
                if (percent > 100) {
                    percent = 100;
                }
                if (percent < 0) {
                    percent = 0;
                }

                v.setB(percent / 100);
            }
            v.setValue(ColorHelper.INSTANCE.getColorViaHue(v.getH(), v.getS(), v.getB()).getRGB());
        }
    }

    public Option getOption() {
        return option;
    }
}
