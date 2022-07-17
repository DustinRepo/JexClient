package me.dustin.jex.gui.waypoints;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.feature.mod.impl.world.Waypoints;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.WaypointFile;
import me.dustin.jex.helper.render.BufferHelper;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import me.dustin.jex.helper.render.Render2DHelper;
import java.awt.*;
import java.util.Random;

public class WaypointEditScreen extends Screen {

    private Waypoints.Waypoint waypoint;
    private final String server;

    private TextFieldWidget serverName;
    private TextFieldWidget name;
    private TextFieldWidget xPos;
    private TextFieldWidget yPos;
    private TextFieldWidget zPos;
    private Button hiddenButton;
    private Button nametagButton;
    private Button beaconButton;
    private Button tracerButton;
    private Button saveButton;
    private Button cancelButton;

    private boolean isSliding;
    private Property<Color> v = new Property.PropertyBuilder<Color>(this.getClass())
            .name("###waypoints")
            .value(Color.WHITE)
            .build();
    private final Identifier colorSlider = new Identifier("jex", "gui/click/colorslider.png");
    private float colorX;
    private float colorY;
    private float colorWidth;
    private int currentColor;

    private boolean tempIsHidden = false;
    private boolean tempDrawBeacon = true;
    private boolean tempDrawNametag = true;
    private boolean tempDrawTracer = false;

    public WaypointEditScreen(String server, Waypoints.Waypoint waypoint) {
        super(Text.of("Waypoint Edit"));
        this.waypoint = waypoint;
        this.server = server;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        if (waypoint != null) {
            tempIsHidden = waypoint.isHidden();
            tempDrawBeacon = waypoint.isDrawBeacon();
            tempDrawNametag = waypoint.isDrawNametag();
            tempDrawTracer = waypoint.isDrawTracer();
        }
        String waypointName = waypoint != null ? waypoint.getName() : "";
        String waypointX = waypoint != null ? String.valueOf(ClientMathHelper.INSTANCE.round(waypoint.getX(), 2)) : "";
        String waypointY = waypoint != null ? String.valueOf(ClientMathHelper.INSTANCE.round(waypoint.getY(), 2)) : "";
        String waypointZ = waypoint != null ? String.valueOf(ClientMathHelper.INSTANCE.round(waypoint.getZ(), 2)) : "";
        String hiddenButtonName = waypoint != null ? Text.translatable("jex.waypoint.hidden", waypoint.isHidden()).getString() : "";
        String nametagButtonName = waypoint != null ? Text.translatable("jex.waypoint.nametag", waypoint.isDrawNametag()).getString() : "";
        String beaconButtonName = waypoint != null ? Text.translatable("jex.waypoint.beacon", waypoint.isDrawBeacon()).getString() : "";
        String tracerButtonName = waypoint != null ? Text.translatable("jex.waypoint.tracer", waypoint.isDrawTracer()).getString() : "";
        serverName = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 95, 200, 20, Text.of(server));
        name = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 60, 200, 20, Text.of(waypointName));
        xPos = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 100, height / 2 - 25, 65, 20, Text.of(waypointX));
        yPos = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 33, height / 2 - 25, 66, 20, Text.of(waypointY));
        zPos = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 + 35, height / 2 - 25, 65, 20, Text.of(waypointZ));
        nametagButton = new Button(nametagButtonName, width / 2.f - 100, height / 2.f + 5, 65, 20, nametagListener);
        beaconButton = new Button(beaconButtonName, width / 2.f - 32.5f, height / 2.f + 5, 65, 20, beaconListener);
        tracerButton = new Button(tracerButtonName, width / 2.f + 35, height / 2.f + 5, 65, 20, tracerListener);
        hiddenButton = new Button(hiddenButtonName, width / 2.f - 100, height / 2.f + 30, 200, 20, hiddenListener);
        serverName.setText(server);
        name.setText(waypointName);
        xPos.setText(waypointX);
        yPos.setText(waypointY);
        zPos.setText(waypointZ);
        this.colorX = width / 2.f - 5;
        this.colorY = height / 2.f + 50;
        this.colorWidth = 110;
        saveButton = new Button(Text.translatable("jex.button.save").getString(), width / 2.f - 100, height / 2.f + 55, 80, 20, saveListener);
        cancelButton = new Button(Text.translatable("jex.button.cancel").getString(), width / 2.f - 100, height / 2.f + 80, 80, 20, cancelListener);
        this.addSelectableChild(name);
        this.addSelectableChild(xPos);
        this.addSelectableChild(yPos);
        this.addSelectableChild(zPos);
        if (waypoint != null) {
            Color color = Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(waypoint.getColor()));
            v.setValue(color);
            currentColor = color.getRGB();
        } else {
            float h = new Random().nextFloat() * 270.f;
            v.setValue(ColorHelper.INSTANCE.getColorViaHue(h));

            this.currentColor = ColorHelper.INSTANCE.getColorViaHue(h).getRGB();
        }
        super.init();
    }

    @Override
    public void tick() {
        name.tick();
        xPos.tick();
        yPos.tick();
        zPos.tick();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        Render2DHelper.INSTANCE.fillAndBorder(matrices, width / 2.f - 105, height / 2.f - 112, width / 2.f + 105, height / 2.f + 150, ColorHelper.INSTANCE.getClientColor(), 0x60000000, 1);
        serverName.render(matrices, mouseX, mouseY, delta);
        name.render(matrices, mouseX, mouseY, delta);
        xPos.render(matrices, mouseX, mouseY, delta);
        yPos.render(matrices, mouseX, mouseY, delta);
        zPos.render(matrices, mouseX, mouseY, delta);
        hiddenButton.render(matrices);
        nametagButton.render(matrices);
        beaconButton.render(matrices);
        tracerButton.render(matrices);
        saveButton.render(matrices);
        cancelButton.render(matrices);
        drawColorPicker(matrices);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.waypoint.server"), width / 2.f, height / 2.f - 107, -1);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.waypoint.name"), width / 2.f, height / 2.f - 72, -1);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.waypoint.x"), width / 2.f - 100 + (65 / 2.f), height / 2.f - 37, -1);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.waypoint.y"), width / 2.f - 33 + (65 / 2.f), height / 2.f - 37, -1);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.waypoint.z"), width / 2.f + 35 + (65 / 2.f), height / 2.f - 37, -1);
        saveButton.setEnabled(areCoordsGood());
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Render2DHelper.INSTANCE.isHovered(this.colorX, this.colorY, this.colorWidth - 5, 95)) {
            isSliding = true;
        }
        nametagButton.click(mouseX, mouseY, button);
        beaconButton.click(mouseX, mouseY, button);
        tracerButton.click(mouseX, mouseY, button);
        hiddenButton.click(mouseX, mouseY, button);
        saveButton.click(mouseX, mouseY, button);
        cancelButton.click(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean areCoordsGood() {
        try {
            float x = Float.parseFloat(xPos.getText());
            float y = Float.parseFloat(yPos.getText());
            float z = Float.parseFloat(zPos.getText());
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    private void drawColorPicker(MatrixStack matrixStack) {
        if (!MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
            isSliding = false;
        }


        float[] hsb = Color.RGBtoHSB(v.value().getRed(), v.value().getGreen(), v.value().getBlue(), null);
        float huepos = hsb[0] * 80;

        float satpos = hsb[1] * 80;
        float brightpos = ((1 - hsb[2])) * 79;


        handleSliders(v);
        Render2DHelper.INSTANCE.drawGradientRect(this.colorX + 5, this.colorY + 15, this.colorX + 85, this.colorY + 95, -1, 0xff000000);
        drawGradientRect(matrixStack, this.colorX + 5, this.colorY + 15, this.colorX + 85, this.colorY + 95, ColorHelper.INSTANCE.getColorViaHue(hsb[0] * 270).getRGB(), 0xff000000);
        Render2DHelper.INSTANCE.drawGradientRect(this.colorX + 5, this.colorY + 15, this.colorX + 85, this.colorY + 95, 0x20000000, 0xff000000);
        //color cursor
        Render2DHelper.INSTANCE.fill(matrixStack, this.colorX + 5 + satpos - 1, this.colorY + 15 + brightpos - 1, this.colorX + 5 + satpos + 1, this.colorY + 15 + brightpos + 1, -1);

        //hue slider
        Render2DHelper.INSTANCE.bindTexture(colorSlider);
        DrawableHelper.drawTexture(matrixStack, (int) this.colorX + (int) this.colorWidth - 10, (int) this.colorY + 15, 0, 0, 5, 80, 10, 80);
        //hue cursor
        Render2DHelper.INSTANCE.fill(matrixStack, this.colorX + this.colorWidth - 10, this.colorY + 15 + huepos - 1, (this.colorX + this.colorWidth - 5), this.colorY + 15 + huepos + 1, -1);

        FontHelper.INSTANCE.drawWithShadow(matrixStack, "Color", this.colorX + 3, this.colorY + 3, currentColor);
    }

    void handleSliders(Property<Color> colorProperty) {
        if (MouseHelper.INSTANCE.isMouseButtonDown(0) && isSliding) {
            float[] hsb = Color.RGBtoHSB(colorProperty.value().getRed(), colorProperty.value().getGreen(), colorProperty.value().getBlue(), null);
            if (MouseHelper.INSTANCE.getMouseX() > this.colorX + 100) {
                float position = MouseHelper.INSTANCE.getMouseY() - (this.colorY + 15);
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
                hsb[0] = value;
            } else {
                hsb[0] *= 270;
                float position = MouseHelper.INSTANCE.getMouseX() - (this.colorX + 5);
                float percent = position / 80.f;
                if (percent > 100) {
                    percent = 100;
                }
                if (percent < 0) {
                    percent = 0;
                }
                hsb[1] = percent;

                position = MouseHelper.INSTANCE.getMouseY() - (this.colorY + 15);
                percent = position / 79.f;
                percent = 100 - percent;
                if (percent > 100) {
                    percent = 100;
                }
                if (percent < 0) {
                    percent = 0;
                }
                hsb[2] = percent;
            }
            currentColor = ColorHelper.INSTANCE.getColorViaHue(hsb[0], hsb[1], hsb[2]).getRGB();
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
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, (float) right, (float) top, (float) 0).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) top, (float) 0).color(1, 1, 1, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, (float) 0).color(0, 0, 0, j).next();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, (float) 0).color(k, l, m, j).next();

        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private ButtonListener saveListener = new ButtonListener() {
        @Override
        public void invoke() {
            if (waypoint == null)
                waypoint = new Waypoints.Waypoint("", "", 0, 0, 0, "", -1);
            waypoint.setDrawBeacon(tempDrawBeacon);
            waypoint.setDrawTracer(tempDrawTracer);
            waypoint.setDrawNametag(tempDrawNametag);
            waypoint.setHidden(tempIsHidden);
            waypoint.setName(name.getText());
            waypoint.setServer(serverName.getText());
            waypoint.setColor(currentColor);
            waypoint.setX(Float.parseFloat(xPos.getText()));
            waypoint.setY(Float.parseFloat(yPos.getText()));
            waypoint.setZ(Float.parseFloat(zPos.getText()));
            ConfigManager.INSTANCE.get(WaypointFile.class).write();
            Wrapper.INSTANCE.getMinecraft().setScreen(new WaypointScreen(serverName.getText()));
        }
    };
    private ButtonListener cancelListener = new ButtonListener() {
        @Override
        public void invoke() {
            Wrapper.INSTANCE.getMinecraft().setScreen(new WaypointScreen(serverName.getText()));
        }
    };

    private ButtonListener nametagListener = new ButtonListener() {
        @Override
        public void invoke() {
            tempDrawNametag = !tempDrawNametag;
            nametagButton.setName(Text.translatable("jex.waypoint.nametag", tempDrawNametag).getString());
        }
    };
    private ButtonListener beaconListener = new ButtonListener() {
        @Override
        public void invoke() {
            tempDrawBeacon = !tempDrawBeacon;
            beaconButton.setName(Text.translatable("jex.waypoint.beacon", tempDrawBeacon).getString());
        }
    };
    private ButtonListener tracerListener = new ButtonListener() {
        @Override
        public void invoke() {
            tempDrawTracer = !tempDrawTracer;
            tracerButton.setName(Text.translatable("jex.waypoint.tracer", tempDrawTracer).getString());
        }
    };
    private ButtonListener hiddenListener = new ButtonListener() {
        @Override
        public void invoke() {
            tempIsHidden = !tempIsHidden;
            hiddenButton.setName(Text.translatable("jex.waypoint.hidden", tempIsHidden).getString());
        }
    };
}
