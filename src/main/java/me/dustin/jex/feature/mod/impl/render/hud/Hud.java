package me.dustin.jex.feature.mod.impl.render.hud;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender2DItem;
import me.dustin.jex.event.render.EventRenderEffects;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.feature.mod.impl.render.hud.elements.*;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.HudElementsFile;
import me.dustin.jex.gui.click.jex.JexGui;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.gui.click.window.impl.Window;
import me.dustin.jex.gui.tab.TabGui;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.*;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Renders an in-game HUD", enabled = true, visible = false)
public class Hud extends Feature {
    public static Hud INSTANCE;
    @Op(name = "Client Color", isColor = true)
    public int clientColor = 0xff00a1ff;
    @OpChild(name = "Rainbow", parent = "Client Color")
    public boolean rainbowClientColor;
    @Op(name = "Collision")
    public boolean collision = true;
    @Op(name = "Constrict Elements Key", isKeybind = true)
    public int constrictKey = GLFW.GLFW_KEY_LEFT_CONTROL;
    @Op(name = "Watermark")
    public boolean watermark = true;
    @OpChild(name = "Jex Effect", all = {"Static", "Spin Only", "Flip Only", "SpinFlip"}, parent = "Watermark")
    public String watermarkMode = "Static";
    @Op(name = "Draw Face")
    public boolean drawFace = true;
    @Op(name = "Array List")
    public boolean showArrayList = true;
    @OpChild(name = "Suffixes", parent = "Array List")
    public boolean suffixes = true;
    @OpChild(name = "Color", parent = "Array List", all = {"Client Color", "Rainbow", "Category"})
    public String colorMode = "Client Color";
    @OpChild(name = "Rainbow Speed", parent = "Color", min = 1, max = 20, dependency = "Rainbow")
    public int rainbowSpeed = 3;
    @OpChild(name = "Rainbow Saturation", parent = "Color", dependency = "Rainbow", inc = 0.1f)
    public float rainbowSaturation = 1;
    @Op(name = "Potion Effects")
    public boolean potionEffects = true;
    @OpChild(name = "Icons", parent = "Potion Effects")
    public boolean icons = true;
    @Op(name = "Lagometer")
    public boolean lagometer = true;
    @Op(name = "Coordinates")
    public boolean coords = false;
    @OpChild(name = "Nether Coords", parent = "Coordinates")
    public boolean netherCoords = true;
    @Op(name = "Armor")
    public boolean armor = true;
    @OpChild(name = "Draw Enchants", parent = "Armor")
    public boolean drawEnchants = true;
    @Op(name = "Item Durability")
    public boolean itemDurability = true;
    @OpChild(name = "Static Color", parent = "Item Durability")
    public boolean staticColor = false;
    @Op(name = "Info")
    public boolean info = true;
    @Op(name = "TabGui")
    public boolean tabGui = true;
    @OpChild(name = "Hover Bar", parent = "TabGui")
    public boolean hoverBar;
    @OpChild(name = "TabGui Width", parent = "TabGui", min = 55, max = 200)
    public float tabGuiWidth = 75;
    @OpChild(name = "Button Height", parent = "TabGui", min = 10, max = 25)
    public float buttonHeight = 12;
    @OpChild(name = "Show Username", parent = "Info")
    public boolean showUsername = true;
    @OpChild(name = "Server", parent = "Info")
    public boolean serverName = true;
    @OpChild(name = "Ping", parent = "Info")
    public boolean ping = true;
    @OpChild(name = "TPS", parent = "Info")
    public boolean tps = true;
    @OpChild(name = "Show Instant", parent = "TPS")
    public boolean instantTPS = false;
    @OpChild(name = "FPS", parent = "Info")
    public boolean fps = true;
    @OpChild(name = "Biome", parent = "Info")
    public boolean biome = true;
    @OpChild(name = "Player Count", parent = "Info")
    public boolean playerCount = true;
    @OpChild(name = "Build Info", parent = "Info")
    public boolean buildInfo = true;
    @OpChild(name = "Yaw/Pitch", parent = "Info")
    public boolean yawAndPitch = true;
    @OpChild(name = "Direction", parent = "Info")
    public boolean direction = true;
    @OpChild(name = "Saturation", parent = "Info")
    public boolean saturation = true;
    @OpChild(name = "Speed", parent = "Info")
    public boolean speed = true;
    @OpChild(name = " ", parent = "Speed", all = {"Blocks", "Feet", "Miles", "KM"})
    public String distanceMode = "Blocks";
    @OpChild(name = "Per", parent = " ", all = {"Second", "Tick", "Minute", "Hour", "Day"})
    public String timeMode = "Second";

    private float lagOMeterY = -11;
    public ArrayList<HudElement> hudElements = new ArrayList<>();
    private boolean gaveEditorMessage;

    public Hud() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (!EventAPI.getInstance().alreadyRegistered(TabGui.INSTANCE))
            EventAPI.getInstance().register(TabGui.INSTANCE);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        while (EventAPI.getInstance().alreadyRegistered(TabGui.INSTANCE))
            EventAPI.getInstance().unregister(TabGui.INSTANCE);
        super.onDisable();
    }

    @EventListener(events = {EventRender2D.class, EventRenderEffects.class, EventMouseButton.class, EventKeyPressed.class, EventRender2DItem.class, EventTick.class})
    private void runRenderMethod(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            if (Wrapper.INSTANCE.getOptions().debugEnabled)
                return;
            if (!gaveEditorMessage && ModFileHelper.INSTANCE.isFirstTimeLoading()) {
                ChatHelper.INSTANCE.addClientMessage("Welcome to Jex Client");
                ChatHelper.INSTANCE.addClientMessage("If you would like to customize the HUD you see infront of you, simply open the Chat");
                ChatHelper.INSTANCE.addClientMessage("You can press \247b" + KeyboardHelper.INSTANCE.getKeyName(constrictKey) + "\2477 to bring them back on-screen, and right-click them to flip them");
                ChatHelper.INSTANCE.addClientMessage("Press \247b" + KeyboardHelper.INSTANCE.getKeyName(Feature.get(Gui.class).getKey()) + "\2477 to open the ClickGui");
                gaveEditorMessage = true;
            }
            if (hudElements.isEmpty())
                loadElements();
            hudElements.forEach(hudElement -> {
                hudElement.render(eventRender2D.getMatrixStack());
            });
            if (lagometer)
                drawLagometer(eventRender2D);
            for (Window window : ClickGui.windows) {
                if (window.isPinned() && !(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ClickGui || Wrapper.INSTANCE.getMinecraft().currentScreen instanceof JexGui)) {
                    window.draw(eventRender2D.getMatrixStack());
                }
            }
        } else if (event instanceof EventRenderEffects eventRenderEffects) {
            if (this.potionEffects)
                eventRenderEffects.cancel();
        } else if (event instanceof EventTick) {
            hudElements.forEach(HudElement::tick);

            float shouldBeY = Lagometer.INSTANCE.isServerLagging() ? 2 : -11;
            float distance = Math.abs(lagOMeterY - shouldBeY);

            if (distance > 30)
                lagOMeterY = shouldBeY;
            if (lagOMeterY < shouldBeY)
                lagOMeterY += distance * 0.5f;
            if (lagOMeterY > shouldBeY)
                lagOMeterY -= distance * 0.5f;
        } else if (event instanceof EventMouseButton eventMouseButton) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen)
                hudElements.forEach(hudElement -> {
                    hudElement.click(MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY(), eventMouseButton.getButton());
                });
        }else if (event instanceof EventKeyPressed eventKeyPressed) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen && eventKeyPressed.getKey() == this.constrictKey)
                hudElements.forEach(hudElement -> {
                    hudElement.setX(MathHelper.clamp(hudElement.getX(), 0, Render2DHelper.INSTANCE.getScaledWidth() - hudElement.getWidth()));
                    hudElement.setY(MathHelper.clamp(hudElement.getY(), 0, Render2DHelper.INSTANCE.getScaledHeight() - hudElement.getHeight()));
                });
        } else if (event instanceof EventRender2DItem eventRender2DItem) {
            if (this.itemDurability)
                drawItemDurability(eventRender2DItem);
        }
    }

    public void loadElements() {
        hudElements.add(new PotionEffectsElement(0, 12 * 6 + 150, 60, 20));
        hudElements.add(new CoordinatesElement(Render2DHelper.INSTANCE.getScaledWidth() - 60, Render2DHelper.INSTANCE.getScaledHeight() - 36, 60, 10));
        hudElements.add(new ArrayListElement(Render2DHelper.INSTANCE.getScaledWidth() - 20, 0, 20, 10));
        hudElements.add(new WatermarkElement(0, 0, 34, 34));
        hudElements.add(new PlayerFaceElement(34, 0, 34, 34));
        hudElements.add(new UsernameElement(69, 0, 30, 11));
        hudElements.add(new TPSElement(69, 22, 30, 11));
        hudElements.add(new FPSElement(69, 11, 30, 11));
        hudElements.add(new ServerElement(0, 34, 20, 11));
        hudElements.add(new PingElement(0, 45, 20, 11));
        hudElements.add(new LookElement(0, 56, 20, 11));
        hudElements.add(new BiomeElement(0, 67, 20, 11));
        hudElements.add(new SpeedElement(0, 78, 20, 11));
        hudElements.add(new DirectionElement(0, 89, 20, 11));
        hudElements.add(new SaturationElement(0, 100, 20, 11));
        hudElements.add(new PlayerCountElement(0, 111, 20, 11));
        hudElements.add(new BuildInfoElement(0, 122, 20, 11));
        hudElements.add(new TabGuiElement(0, 133, 55, 55));
        hudElements.add(new ArmorElement((Render2DHelper.INSTANCE.getScaledWidth() / 2.f) + 5, Render2DHelper.INSTANCE.getScaledHeight() - 50, 16, 16));
        hudElements.add(new RadarElement(Render2DHelper.INSTANCE.getScaledWidth() - 120 - 100, 0, 120, 120));
        ConfigManager.INSTANCE.get(HudElementsFile.class).read();
    }

    public void drawLagometer(EventRender2D eventRender2D) {
        FontHelper.INSTANCE.drawCenteredString(eventRender2D.getMatrixStack(), String.format("Server is lagging: %.1f", (float) Lagometer.INSTANCE.getLagTime() / 1000), Render2DHelper.INSTANCE.getScaledWidth() / 2, lagOMeterY, ColorHelper.INSTANCE.getClientColor());
    }

    private void drawItemDurability(EventRender2DItem eventRender2DItem) {
        if (eventRender2DItem.getStack().getMaxDamage() > 0) {
            int maxDamage = eventRender2DItem.getStack().getMaxDamage();
            int damage = eventRender2DItem.getStack().getDamage();
            int durability = maxDamage - damage;
            float percent = (((float) eventRender2DItem.getStack().getMaxDamage() - (float) eventRender2DItem.getStack().getDamage()) / (float) eventRender2DItem.getStack().getMaxDamage()) * 100;
            int color = Render2DHelper.INSTANCE.getPercentColor(percent);

            MatrixStack matrixStack = new MatrixStack();
            matrixStack.translate(0.0, 0.0, eventRender2DItem.getItemRenderer().zOffset + 200.0);
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            eventRender2DItem.getFontRenderer().draw(Integer.toString(durability), eventRender2DItem.getX() * 2.0f, eventRender2DItem.getY() * 2.0f, staticColor ? 0xFFFFFF : color, true, matrixStack.peek().getPositionMatrix(), immediate, false, 0, 15728880);
            immediate.draw();
        }
    }
    private static Gui gui;
    public static int getCategoryColor(Feature.Category category) {
        if (gui == null)
            gui = (Gui)Feature.get(Gui.class);
        return switch (category) {
            case MOVEMENT -> gui.movementColor;
            case VISUAL -> gui.visualColor;
            case PLAYER -> gui.playerColor;
            case MISC -> gui.miscColor;
            case WORLD -> gui.worldColor;
            case COMBAT -> gui.combatColor;
        };
    }

    public HudElement getElement(String name) {
        for (HudElement hudElement : hudElements) {
            if (hudElement.getName().equalsIgnoreCase(name))
                return hudElement;
        }
        return null;
    }
}
