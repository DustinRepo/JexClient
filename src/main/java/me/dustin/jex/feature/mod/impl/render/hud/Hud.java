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
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.feature.mod.impl.render.hud.elements.*;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.HudElementsFile;
import me.dustin.jex.gui.navigator.Navigator;
import me.dustin.jex.gui.tab.TabGui;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.*;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;

public class Hud extends Feature {
    public static Hud INSTANCE;

    public final Property<Color> clientColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Client Color")
            .value(new Color(0, 161, 255))
            .build();
    public final Property<Boolean> rainbowClientColorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Rainbow")
            .value(false)
            .parent(clientColorProperty)
            .build();
    public final Property<Boolean> collisionProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Collision")
            .description("Whether or not to allow the HUD elements to collide with one another.")
            .value(true)
            .build();
    public final Property<Integer> constrictKeyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Constrict Elements Key")
            .description("The key to bring all elements back on screen.")
            .value(GLFW.GLFW_KEY_LEFT_CONTROL)
            .isKey()
            .build();
    public final Property<Boolean> watermarkProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Watermark")
            .value(true)
            .build();
    public final Property<WatermarkEffect> watermarkModeProperty = new Property.PropertyBuilder<WatermarkEffect>(this.getClass())
            .name("Jex Effect")
            .value(WatermarkEffect.STATIC)
            .parent(watermarkProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> drawFaceProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Draw Face")
            .value(true)
            .build();
    public final Property<Boolean> showArrayListProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Array List")
            .value(true)
            .build();
    public final Property<Boolean> suffixesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Suffixes")
            .description("Allow for suffixes to display in the ArrayList")
            .value(true)
            .parent(showArrayListProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<ArrayListColor> colorModeProperty = new Property.PropertyBuilder<ArrayListColor>(this.getClass())
            .name("Color")
            .value(ArrayListColor.CLIENT_COLOR)
            .parent(showArrayListProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Integer> rainbowSpeedProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Rainbow Speed")
            .value(3)
            .min(1)
            .max(20)
            .parent(colorModeProperty)
            .depends(parent -> parent.value() == ArrayListColor.RAINBOW)
            .build();
    public final Property<Float> rainbowSaturationProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Rainbow Saturation")
            .value(1f)
            .inc(0.1f)
            .parent(colorModeProperty)
            .depends(parent -> parent.value() == ArrayListColor.RAINBOW)
            .build();
    public final Property<Boolean> potionEffectsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Potion Effects")
            .value(true)
            .build();
    public final Property<Boolean> iconsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Icons")
            .value(true)
            .parent(potionEffectsProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> lagometerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Lagometer")
            .value(true)
            .build();
    public final Property<Boolean> coordsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Coordinates")
            .value(false)
            .build();
    public final Property<Boolean> netherCoordsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Nether Coords")
            .value(false)
            .parent(coordsProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> armorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Armor")
            .value(true)
            .build();
    public final Property<Boolean> drawEnchantsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Draw Enchants")
            .value(false)
            .parent(armorProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> itemDurabilityProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Item Durability")
            .value(true)
            .build();
    public final Property<Boolean> staticColorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Static Color")
            .value(false)
            .parent(itemDurabilityProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> tabGuiProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("TabGui")
            .value(true)
            .build();
    public final Property<Boolean> hoverBarProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hover Bar")
            .value(false)
            .parent(tabGuiProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> tabGuiWidthProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("TabGui Width")
            .value(75f)
            .min(55)
            .max(200)
            .parent(tabGuiProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> buttonHeightProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Button Height")
            .value(12f)
            .min(10)
            .max(25)
            .parent(tabGuiProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> infoProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Info")
            .value(true)
            .build();
    public final Property<Boolean> showUsernameProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Show Username")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> serverNameProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Server")
            .description("Show the server IP on the HUD.")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> pingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Ping")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> tpsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("TPS")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> instantTPSProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Show Instant")
            .value(true)
            .parent(tpsProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> fpsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("FPS")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> biomeProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Biome")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> playerCountProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Player Count")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> yawAndPitchProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Yaw/Pitch")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> directionProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Direction")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> saturationProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Saturation")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> speedProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Speed")
            .value(true)
            .parent(infoProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<DistanceMode> distanceModeProperty = new Property.PropertyBuilder<DistanceMode>(this.getClass())
            .name(" ")
            .value(DistanceMode.BLOCKS)
            .parent(speedProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<TimeMode> timeModeProperty = new Property.PropertyBuilder<TimeMode>(this.getClass())
            .name("Per")
            .value(TimeMode.SECOND)
            .parent(distanceModeProperty)
            .build();

    private float lagOMeterY = -11;
    public ArrayList<HudElement> hudElements = new ArrayList<>();
    private boolean gaveEditorMessage;

    public Hud() {
        super("Hud", Category.VISUAL, "Mark entities/players through walls", true, false, 0);
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
        if (Wrapper.INSTANCE.getOptions().debugEnabled)
            return;
        if (!gaveEditorMessage && ModFileHelper.INSTANCE.isFirstTimeLoading()) {
            ChatHelper.INSTANCE.addClientMessage("Welcome to Jex Client");
            ChatHelper.INSTANCE.addClientMessage("If you would like to customize the HUD you see infront of you, simply open the Chat");
            ChatHelper.INSTANCE.addClientMessage("You can press \247b" + KeyboardHelper.INSTANCE.getKeyName(constrictKeyProperty.value()) + "\2477 to bring them back on-screen, and right-click them to flip them");
            ChatHelper.INSTANCE.addClientMessage("Press \247b" + KeyboardHelper.INSTANCE.getKeyName(Feature.get(Gui.class).getKey()) + "\2477 to open the ClickGui");
            gaveEditorMessage = true;
        }
        if (hudElements.isEmpty())
            loadElements();
        hudElements.forEach(hudElement -> {
            hudElement.render(event.getPoseStack());
        });
        if (lagometerProperty.value())
            drawLagometer(event);
    });

    @EventPointer
    private final EventListener<EventRenderEffects> eventRenderEffectsEventListener = new EventListener<>(event -> {
        if (this.potionEffectsProperty.value())
            event.cancel();
    });

    @EventPointer
    private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen)
            hudElements.forEach(hudElement -> hudElement.click(MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY(), event.getButton()));
    });

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen && event.getKey() == this.constrictKeyProperty.value())
            hudElements.forEach(hudElement -> {
                hudElement.setX(MathHelper.clamp(hudElement.getX(), 0, Render2DHelper.INSTANCE.getScaledWidth() - hudElement.getWidth()));
                hudElement.setY(MathHelper.clamp(hudElement.getY(), 0, Render2DHelper.INSTANCE.getScaledHeight() - hudElement.getHeight()));
            });
    });

    @EventPointer
    private final EventListener<EventRender2DItem> eventRender2DItemEventListener = new EventListener<>(event -> {
        if (this.itemDurabilityProperty.value())
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
            int damage = eventRender2DItem.getStack().getDamage();
            int durability = maxDamage - damage;
            float percent = (((float) eventRender2DItem.getStack().getMaxDamage() - (float) eventRender2DItem.getStack().getDamage()) / (float) eventRender2DItem.getStack().getMaxDamage()) * 100;
            int color = Render2DHelper.INSTANCE.getPercentColor(percent);

            MatrixStack matrixStack = new MatrixStack();
            matrixStack.translate(0.0, 0.0, eventRender2DItem.getItemRenderer().zOffset + 200.0);
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            eventRender2DItem.getFontRenderer().draw(Integer.toString(durability), eventRender2DItem.getX() * 2.0f, eventRender2DItem.getY() * 2.0f, staticColorProperty.value() ? 0xFFFFFF : color, true, matrixStack.peek().getPositionMatrix(), immediate, false, 0, 15728880);
            immediate.draw();
        }
    }

    public int getCategoryColor(Category category) {
        return category.color();
    }

    public HudElement getElement(String name) {
        for (HudElement hudElement : hudElements) {
            if (hudElement.getName().equalsIgnoreCase(name))
                return hudElement;
        }
        return null;
    }

    public enum WatermarkEffect {
        STATIC, SPIN_ONLY, FLIP_ONLY, SPINFLIP
    }
    public enum ArrayListColor {
        CLIENT_COLOR, RAINBOW, CATEGORY
    }
    public enum DistanceMode {
        BLOCKS, FEET, MILES, KM
    }
    public enum TimeMode {
        SECOND, TICK, MINUTE, HOUR, DAY
    }
}
