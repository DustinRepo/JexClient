package me.dustin.jex.feature.mod.impl.render.hud;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
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
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.gui.click.navigator.Navigator;
import me.dustin.jex.gui.tab.TabGui;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.*;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
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
    @OpChild(name = "Combat", parent = "Color", dependency = "Category", isColor = true)
    public int combatColor = new Color(255, 61, 56).getRGB();
    @OpChild(name = "Player", parent = "Color", dependency = "Category", isColor = true)
    public int playerColor = new Color(64, 255, 83).getRGB();
    @OpChild(name = "Movement", parent = "Color", dependency = "Category", isColor = true)
    public int movementColor = new Color(141, 95, 255).getRGB();
    @OpChild(name = "Visual", parent = "Color", dependency = "Category", isColor = true)
    public int visualColor = new Color(255, 92, 252).getRGB();
    @OpChild(name = "World", parent = "Color", dependency = "Category", isColor = true)
    public int worldColor = new Color(74, 84, 255).getRGB();
    @OpChild(name = "Misc", parent = "Color", dependency = "Category", isColor = true)
    public int miscColor = new Color(247, 255, 65).getRGB();
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
        EventManager.register(TabGui.INSTANCE);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        EventManager.unregister(TabGui.INSTANCE);
        super.onDisable();
    }

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getOptions().renderDebug)
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
            hudElement.render(event.getPoseStack());
        });
        if (lagometer)
            drawLagometer(event);
        for (DropdownWindow window : DropDownGui.getCurrentTheme().windows) {
            if (window.isPinned() && !(Wrapper.INSTANCE.getMinecraft().screen instanceof DropDownGui || Wrapper.INSTANCE.getMinecraft().screen instanceof Navigator)) {
                window.render(event.getPoseStack());
            }
        }
    });

    @EventPointer
    private final EventListener<EventRenderEffects> eventRenderEffectsEventListener = new EventListener<>(event -> {
        if (this.potionEffects)
            event.cancel();
    });

    @EventPointer
    private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().screen instanceof ChatScreen)
            hudElements.forEach(hudElement -> hudElement.click(MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY(), event.getButton()));
    });

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().screen instanceof ChatScreen && event.getKey() == this.constrictKey)
            hudElements.forEach(hudElement -> {
                hudElement.setX(Mth.clamp(hudElement.getX(), 0, Render2DHelper.INSTANCE.getScaledWidth() - hudElement.getWidth()));
                hudElement.setY(Mth.clamp(hudElement.getY(), 0, Render2DHelper.INSTANCE.getScaledHeight() - hudElement.getHeight()));
            });
    });

    @EventPointer
    private final EventListener<EventRender2DItem> eventRender2DItemEventListener = new EventListener<>(event -> {
        if (this.itemDurability)
            drawItemDurability(event);
    });
    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        hudElements.forEach(HudElement::tick);

        float shouldBeY = Lagometer.INSTANCE.isServerLagging() ? 2 : -11;
        float distance = Math.abs(lagOMeterY - shouldBeY);

        if (distance > 30)
            lagOMeterY = shouldBeY;
        if (lagOMeterY < shouldBeY)
            lagOMeterY += distance * 0.5f;
        if (lagOMeterY > shouldBeY)
            lagOMeterY -= distance * 0.5f;
    }, new TickFilter(EventTick.Mode.PRE));

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
        hudElements.add(new TabGuiElement(0, 133, 55, 55));
        hudElements.add(new ArmorElement((Render2DHelper.INSTANCE.getScaledWidth() / 2.f) + 5, Render2DHelper.INSTANCE.getScaledHeight() - 50, 16, 16));
        hudElements.add(new RadarElement(Render2DHelper.INSTANCE.getScaledWidth() - 120 - 100, 0, 120, 120));
        ConfigManager.INSTANCE.get(HudElementsFile.class).read();
    }

    public void drawLagometer(EventRender2D eventRender2D) {
        FontHelper.INSTANCE.drawCenteredString(eventRender2D.getPoseStack(), String.format("Server is lagging: %.1f", (float) Lagometer.INSTANCE.getLagTime() / 1000), Render2DHelper.INSTANCE.getScaledWidth() / 2, lagOMeterY, ColorHelper.INSTANCE.getClientColor());
    }

    private void drawItemDurability(EventRender2DItem eventRender2DItem) {
        if (eventRender2DItem.getStack().getMaxDamage() > 0) {
            int maxDamage = eventRender2DItem.getStack().getMaxDamage();
            int damage = eventRender2DItem.getStack().getDamageValue();
            int durability = maxDamage - damage;
            float percent = (((float) eventRender2DItem.getStack().getMaxDamage() - (float) eventRender2DItem.getStack().getDamageValue()) / (float) eventRender2DItem.getStack().getMaxDamage()) * 100;
            int color = Render2DHelper.INSTANCE.getPercentColor(percent);

            PoseStack matrixStack = new PoseStack();
            matrixStack.translate(0.0, 0.0, eventRender2DItem.getItemRenderer().blitOffset + 200.0);
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            eventRender2DItem.getFontRenderer().drawInBatch(Integer.toString(durability), eventRender2DItem.getX() * 2.0f, eventRender2DItem.getY() * 2.0f, staticColor ? 0xFFFFFF : color, true, matrixStack.last().pose(), immediate, false, 0, 15728880);
            immediate.endBatch();
        }
    }

    public int getCategoryColor(Feature.Category category) {
        switch (category) {
            case MOVEMENT -> { return movementColor; }
            case VISUAL -> { return visualColor; }
            case PLAYER -> { return playerColor; }
            case MISC -> { return miscColor; }
            case WORLD -> { return worldColor; }
            case COMBAT -> { return combatColor; }
        }
        return 0xffffffff;
    }

    public HudElement getElement(String name) {
        for (HudElement hudElement : hudElements) {
            if (hudElement.getName().equalsIgnoreCase(name))
                return hudElement;
        }
        return null;
    }
}
