package me.dustin.jex.gui.navigator.impl;


import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.navigator.NavigatorOptionScreen;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

public class NavigatorOptionButton extends Button {

    StopWatch stopWatch = new StopWatch();
    int togglePos = 0;
    int cogSpin = 0;
    private final Property property;
    private boolean isSliding;
    private NavigatorOptionButton masterButton;
    private NavigatorOptionButton parentButton;
    private final Identifier colorSlider = new Identifier("jex", "gui/click/colorslider.png");
    private int buttonsHeight;

    public NavigatorOptionButton(Property property, float x, float y, float width, float height) {
        super(property.getName(), x, y, width, height, null);
        this.property = property;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        updateOnOff();
        if (this.masterButton != null) {
            Render2DHelper.INSTANCE.fill(matrixStack, masterButton.getX(), this.getY(), this.getX(), this.getY() + this.getHeight(), ColorHelper.INSTANCE.getClientColor());
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + this.getWidth(), this.getY(), masterButton.getX() + masterButton.getWidth(), this.getY() + this.getHeight(), ColorHelper.INSTANCE.getClientColor());
        }

        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);

        Object value = this.getProperty().value();

        if (value instanceof Boolean booleanValue) {
            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX() + 2, this.getY() + 2, this.getX() + this.getHeight() - 4, this.getY() + this.getHeight() - 4, 0xff656565, 0x00ffffff);
            if (booleanValue)
                Render2DHelper.INSTANCE.drawCheckmark(matrixStack, this.getX() + 2, this.getY() + 2, ColorHelper.INSTANCE.getClientColor());
            FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getProperty().getName(), this.getX() + 14, this.getY() + 4, -1);
        } else if (value instanceof Enum<?> enumValue) {
            String name = enumValue.name().replace("_", " ");
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getProperty().getName() + ": \247f" + name, this.getX() + 3, this.getY() + 4, 0xffaaaaaa);
        } else if (value instanceof String str) {
            FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getProperty().getName(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
            FontHelper.INSTANCE.drawCenteredString(matrixStack, str, this.getX() + (this.getWidth() / 2), this.getY() + 14, 0xffaaaaaa);
            if (EventManager.isRegistered(this)) {
                Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), ColorHelper.INSTANCE.getClientColor(), 0x00ffffff, 1);
            }
        } else if (this.getProperty().isKeybind()) {
                int key = (int)value;
                String s = EventManager.isRegistered(this) ? "Press a key..." : this.getProperty().getName() + ": " + (key == 0 ? "None" : KeyboardHelper.INSTANCE.getKeyName(key));
                FontHelper.INSTANCE.drawWithShadow(matrixStack, s, this.getX() + 2, this.getY() + 3, 0xffaaaaaa);
        } else if (value instanceof Color || value instanceof Integer || value instanceof Float || value instanceof Double || value instanceof Long) {
            drawSliders(getProperty(), matrixStack);
        }

        if (hasChild()) {
            matrixStack.push();
            matrixStack.translate(this.getX() + this.getWidth() - 7, this.getY() + 7.5f, 0);
            matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, 1.0F), cogSpin, true));
            Render2DHelper.INSTANCE.drawArrow(matrixStack, 0, 0, this.isOpen(), !this.isOpen() ? 0xff999999 : ColorHelper.INSTANCE.getClientColor());
            matrixStack.pop();
        }
        if (isOpen())
            this.getChildren().forEach(button -> {
                button.render(matrixStack);
            });
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered()) {
            if (int_1 == 0) {
                Object value = getProperty().value();
                if (value instanceof Boolean) {
                    Property<Boolean> booleanProperty = (Property<Boolean>)getProperty();
                    booleanProperty.setValue(!booleanProperty.value());
                    if (isOpen()) {
                        setOpen(false);
                        close();
                    }
                } else if (value instanceof Enum<?>) {
                    getProperty().incrementEnumValue();
                    if (this.isOpen())
                        this.close();
                } else if (value instanceof String) {
                    if (!EventManager.isRegistered(this))
                        EventManager.register(this);
                } else if (getProperty().isKeybind()) {
                    EventManager.register(this);
                } else if (value instanceof Float || value instanceof Integer || value instanceof Double || value instanceof Long || value instanceof Color) {
                    isSliding = true;
                }
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ConfigManager.INSTANCE.get(FeatureFile.class).write();
                return;
            }
            if (int_1 == 1) {
                if (hasChild()) {
                    this.setOpen(!this.isOpen());
                    if (this.isOpen())
                        this.open();
                    else
                        this.close();
                }
            }
        } else {
            if (this.getProperty().value() instanceof String) {
                while (EventManager.isRegistered(this))
                    EventManager.unregister(this);
            }
        }
        getChildren().forEach(button -> {
            button.click(double_1, double_2, int_1);
        });
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (Button button : this.getChildren()) {
            if (button != this)
                button.keyTyped(typedChar, keyCode);
        }
    }

    private boolean hasChild() {
        for (Object option : this.getProperty().getChildren()) {
            Property property = (Property) option;
            if (property.getDepends() != null) {
                if (((Property<?>) option).getDepends().test(getProperty()))
                    return true;
            } else {
                return true;
            }
        }
        return false;
    }

    public void open() {
        buttonsHeight = 0;
        getProperty().getChildren().forEach(option ->
        {
            Property<?> property = (Property<?>)option;
            NavigatorOptionButton optionButton = new NavigatorOptionButton(property, this.getX() + 1, (this.getY() + this.getHeight()) + buttonsHeight, this.getWidth() - 2, property.value() instanceof Color ? 100 : 15);
            optionButton.masterButton = this.masterButton == null ? this : this.masterButton;
            optionButton.parentButton = this;

            if (property.value() instanceof String)
                optionButton.setHeight(this.getHeight() + 10);

            if (optionButton.getProperty().getDepends() != null) {
                if (optionButton.getProperty().getDepends().test(optionButton.getProperty().getParent())) {
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

    public ArrayList<Button> allButtonsAfter() {
        ArrayList<Button> buttons = new ArrayList<>();

        if (parentButton != null)
            parentButton.getChildren().forEach(button -> {
                if (parentButton.getChildren().indexOf(button) > parentButton.getChildren().indexOf(this)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            });
        buttons.addAll(allButtonsAfter(masterButton != null ? masterButton : parentButton != null ? parentButton : this));
        return buttons;
    }

    public ArrayList<Button> allButtonsAfter(Button button1) {
        ArrayList<Button> buttons = new ArrayList<>();
        for (Button button : NavigatorOptionScreen.options) {
            if (NavigatorOptionScreen.options.indexOf(button) > NavigatorOptionScreen.options.indexOf(button1) && button.isVisible()) {
                buttons.add(button);
                buttons = addAllChildren(buttons, button);
            }
        }
        return buttons;
    }

    public void close() {
        this.getChildren().forEach(button -> {
            if (button instanceof NavigatorOptionButton) {
                if (button.isOpen())
                    ((NavigatorOptionButton) button).close();
            }
        });
        allButtonsAfter().forEach(button -> {
            button.move(0, -buttonsHeight);
        });
        this.getChildren().clear();
        this.setOpen(false);
    }

    private void updateOnOff() {
        if (!stopWatch.hasPassed(10))
            return;
        stopWatch.reset();
        if (this.getProperty().value() instanceof Boolean enabled) {
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

    @EventPointer
    private final EventListener<EventKeyPressed> eventListener = new EventListener<>(event -> {
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof NavigatorOptionScreen)) {
            while (EventManager.isRegistered(this))
                EventManager.unregister(this);
            return;
        }
        int keyCode = event.getKey();
        if (this.getProperty().isKeybind()) {
            if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_ESCAPE) {
                this.getProperty().setValue(keyCode);
            } else {
                this.getProperty().setValue(0);
            }
            while (EventManager.isRegistered(this))
                EventManager.unregister(this);
            ConfigManager.INSTANCE.get(FeatureFile.class).write();
        } else if (this.getProperty().value() instanceof String stringValue) {
            if (Screen.isPaste(keyCode)) {
                this.getProperty().setValue(stringValue + MinecraftClient.getInstance().keyboard.getClipboard());
                return;
            }
            switch (keyCode) {
                case GLFW.GLFW_KEY_ENTER:
                case GLFW.GLFW_KEY_ESCAPE:
                    while (EventManager.isRegistered(this))
                        EventManager.unregister(this);
                    break;
                case GLFW.GLFW_KEY_SPACE:
                    this.getProperty().setValue(stringValue + " ");
                    break;
                case GLFW.GLFW_KEY_BACKSPACE:
                    if (stringValue.isEmpty())
                        break;
                    String str = stringValue.substring(0, stringValue.length() - 1);
                    this.getProperty().setValue(str);
                    break;
                default:
                    String keyName = InputUtil.fromKeyCode(keyCode, event.getScancode()).getTranslationKey().replace("key.keyboard.", "");
                    if (keyName.length() == 1) {
                        if (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                            keyName = keyName.toUpperCase();
                            if (isInt(keyName))
                                keyName = getFromNumKey(Integer.parseInt(keyName));
                        }
                        this.getProperty().setValue(stringValue + keyName);
                    }
                    break;
            }
            int len = String.valueOf(this.getProperty().value()).length();
            if (len > this.getProperty().getMax())
                this.getProperty().setValue(String.valueOf(this.getProperty().value()).substring(0, (int)getProperty().getMax()));
        }
    });

    private boolean isInt(String intStr) {
        try {
            Integer.parseInt(intStr);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    private String getFromNumKey(int i) {
        return switch (i) {
            case 1 -> "!";
            case 2 -> "@";
            case 3 -> "#";
            case 4 -> "$";
            case 5 -> "%";
            case 6 -> "^";
            case 7 -> "&";
            case 8 -> "*";
            case 9 -> "(";
            case 0 -> ")";
            default -> String.valueOf(i);
        };
    }

    public void drawSliders(Property<?> property, MatrixStack matrixStack) {
        if (property.value() instanceof Float floatValue) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ConfigManager.INSTANCE.get(FeatureFile.class).write();
            }

            float startV = floatValue - property.getMin();

            float pos = (startV / (property.getMax() - property.getMin())) * (this.getWidth());


            handleSliders(property);

            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX(), this.getY() + (this.getHeight() / 2.f) - 2, this.getX() + this.getWidth(), this.getY() + (this.getHeight() / 2.f) + 2, 0xff696969, 0x00ffffff);
            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX() + pos - 4, this.getY() + (this.getHeight() / 2.f) - 4, this.getX() + pos + 4, this.getY() + (this.getHeight() / 2.f) + 4, 0x70696969, Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(ColorHelper.INSTANCE.getClientColor())).darker().getRGB() & 0xc0ffffff);
            FontHelper.INSTANCE.drawCenteredString(matrixStack, property.getName() + ": " + property.value(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        } else if (property.value() instanceof Double doubleValue) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ConfigManager.INSTANCE.get(FeatureFile.class).write();
            }

            float startV = (float)(double)doubleValue - property.getMin();

            float pos = (startV / (property.getMax() - property.getMin())) * (this.getWidth());


            handleSliders(property);

            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX(), this.getY() + (this.getHeight() / 2.f) - 2, this.getX() + this.getWidth(), this.getY() + (this.getHeight() / 2.f) + 2, 0xff696969, 0x00ffffff);
            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX() + pos - 4, this.getY() + (this.getHeight() / 2.f) - 4, this.getX() + pos + 4, this.getY() + (this.getHeight() / 2.f) + 4, 0x70696969, Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(ColorHelper.INSTANCE.getClientColor())).darker().getRGB() & 0xc0ffffff);
            FontHelper.INSTANCE.drawCenteredString(matrixStack, property.getName() + ": " + property.value(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        } else if (property.value() instanceof Integer intValue) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ConfigManager.INSTANCE.get(FeatureFile.class).write();
            }


            float startV = intValue - property.getMin();

            float pos = ((float) (startV) / (property.getMax() - property.getMin())) * (this.getWidth());

            handleSliders(property);

            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX(), this.getY() + (this.getHeight() / 2.f) - 2, this.getX() + this.getWidth(), this.getY() + (this.getHeight() / 2.f) + 2, 0xff696969, 0x00ffffff);
            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX() + pos - 4, this.getY() + (this.getHeight() / 2.f) - 4, this.getX() + pos + 4, this.getY() + (this.getHeight() / 2.f) + 4, 0x70696969, Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(ColorHelper.INSTANCE.getClientColor())).darker().getRGB() & 0xc0ffffff);
            FontHelper.INSTANCE.drawCenteredString(matrixStack, property.getName() + ": " + property.value(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        } else if (property.value() instanceof Long longValue) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ConfigManager.INSTANCE.get(FeatureFile.class).write();
            }


            float startV = longValue - property.getMin();

            float pos = ((float) (startV) / (property.getMax() - property.getMin())) * (this.getWidth());

            handleSliders(property);

            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX(), this.getY() + (this.getHeight() / 2.f) - 2, this.getX() + this.getWidth(), this.getY() + (this.getHeight() / 2.f) + 2, 0xff696969, 0x00ffffff);
            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX() + pos - 4, this.getY() + (this.getHeight() / 2.f) - 4, this.getX() + pos + 4, this.getY() + (this.getHeight() / 2.f) + 4, 0x70696969, Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(ColorHelper.INSTANCE.getClientColor())).darker().getRGB() & 0xc0ffffff);
            FontHelper.INSTANCE.drawCenteredString(matrixStack, property.getName() + ": " + property.value(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        } else if (property.value() instanceof Color color) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
                isSliding = false;
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ConfigManager.INSTANCE.get(FeatureFile.class).write();
            }

            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            float huepos = hsb[0] * 80;

            float satpos = hsb[1] * 80;
            float brightpos = ((1 - hsb[2])) * 79;


            handleSliders(property);
            Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, -1, 0xff000000);
            drawGradientRect(matrixStack, this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, ColorHelper.INSTANCE.getColorViaHue(hsb[0] * 270).getRGB(), 0xff000000);
            Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, 0x20000000, 0xff000000);
            //color cursor
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 5 + satpos - 1, this.getY() + 15 + brightpos - 1, this.getX() + 5 + satpos + 1, this.getY() + 15 + brightpos + 1, -1);

            //hue slider
            Render2DHelper.INSTANCE.bindTexture(colorSlider);
            DrawableHelper.drawTexture(matrixStack, (int) this.getX() + (int) this.getWidth() - 10, (int) this.getY() + 15, 0, 0, 5, 80, 10, 80);
            //hue cursor
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + this.getWidth() - 10, this.getY() + 15 + huepos - 1, (this.getX() + this.getWidth() - 10) + 5, this.getY() + 15 + huepos + 1, 0xff000000);

            FontHelper.INSTANCE.drawWithShadow(matrixStack, property.getName(), this.getX() + 3, this.getY() + 3, color.getRGB());
        }
    }


    protected void drawGradientRect(MatrixStack matrixStack, float left, float top, float right, float bottom, int startColor, int endColor) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
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
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, (float) right, (float) top, (float) 0).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) top, (float) 0).color(1, 1, 1, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, (float) 0).color(0, 0, 0, j).next();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, (float) 0).color(k, l, m, j).next();

        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }


    void handleSliders(Property property) {
        if (MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
            if (property.value() instanceof Integer) {
                Property<Integer> integerProperty = (Property<Integer>) property;
                float position = MouseHelper.INSTANCE.getMouseX() - this.getX();
                float percent = MathHelper.clamp(position / this.getWidth(), 0, 1);
                int increment = (int)property.getInc();
                int value = (int) (property.getMin() + (int) (percent * (property.getMax() - property.getMin())));
                integerProperty.setValue((int) ((int) Math.round(value * (1.0D / increment)) / (1.0D / increment)));
                integerProperty.setValue((int) ClientMathHelper.INSTANCE.round(integerProperty.value(), 2));
            } else if (property.value() instanceof Long) {
                Property<Long> longProperty = (Property<Long>) property;
                float position = MouseHelper.INSTANCE.getMouseX() - this.getX();
                float percent = MathHelper.clamp(position / this.getWidth(), 0, 1);
                long increment = (long)property.getInc();
                long value = (long) (property.getMin() + (long) (percent * (property.getMax() - property.getMin())));
                longProperty.setValue((long) (Math.round(value * (1.0D / increment)) / (1.0D / increment)));
                longProperty.setValue((long) ClientMathHelper.INSTANCE.round(longProperty.value(), 2));
            } else if (property.value() instanceof Float) {
                Property<Float> floatProperty = (Property<Float>) property;
                float position = MouseHelper.INSTANCE.getMouseX() - this.getX();
                float percent = MathHelper.clamp(position / this.getWidth(), 0, 1);
                float increment = property.getInc();
                float value = property.getMin() + percent * (property.getMax() - property.getMin());
                floatProperty.setValue((float) ((float) Math.round(value * (1.0D / increment)) / (1.0D / increment)));
                floatProperty.setValue((float) ClientMathHelper.INSTANCE.round(floatProperty.value(), 2));
            } else if (property.value() instanceof Double) {
                Property<Double> doubleProperty = (Property<Double>) property;
                float position = MouseHelper.INSTANCE.getMouseX() - this.getX();
                float percent = MathHelper.clamp(position / this.getWidth(), 0, 1);
                float increment = property.getInc();
                double value = property.getMin() + percent * (property.getMax() - property.getMin());
                doubleProperty.setValue(Math.round(value * (1.0D / increment)) / (1.0D / increment));
                doubleProperty.setValue(ClientMathHelper.INSTANCE.round(doubleProperty.value(), 2));
            } else if (property.value() instanceof Color) {
                Property<Color> colorProperty = (Property<Color>) property;
                float[] hsb = Color.RGBtoHSB(colorProperty.value().getRed(), colorProperty.value().getGreen(), colorProperty.value().getBlue(), null);
                if (MouseHelper.INSTANCE.getMouseX() > this.getX() + (this.getWidth() / 2.f)) {
                    float position = MouseHelper.INSTANCE.getMouseY() - (this.getY() + 15);
                    float percent = MathHelper.clamp(position / 79, 0, 1);
                    float value = percent * 270;
                    hsb[0] = value;
                } else {
                    hsb[0] *= 270;
                    float position = MouseHelper.INSTANCE.getMouseX() - (this.getX() + 5);
                    float percent = MathHelper.clamp(position / 80, 0, 1);
                    hsb[1] = percent;

                    position = MouseHelper.INSTANCE.getMouseY() - (this.getY() + 15);
                    percent = MathHelper.clamp(position / 79, 0, 1);
                    hsb[2] = 1 - percent;
                }
                colorProperty.setValue(ColorHelper.INSTANCE.getColorViaHue(hsb[0], hsb[1], hsb[2]));
            }
        }
    }

    public Property getProperty() {
        return property;
    }
}
