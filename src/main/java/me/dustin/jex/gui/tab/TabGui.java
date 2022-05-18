package me.dustin.jex.gui.tab;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.hud.Hud;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import me.dustin.jex.helper.render.Render2DHelper;
import org.lwjgl.glfw.GLFW;

public enum TabGui {
    INSTANCE;
    private boolean hoverBar;
    private int categorySelect;
    private int modSelect;
    private boolean categoryOpen;

    private float hoverY, spotHoverY;
    private float lastHoverY, lastModHoverY;

    private float modHoverY, modSpotHoverY;

    public void draw(MatrixStack matrixStack, float x, float y, float width, float buttonHeight) {
        int categoryCount = 0;
        spotHoverY = y + (categorySelect * buttonHeight);
        modSpotHoverY = y + (modSelect * buttonHeight);

        float hoverY = lastHoverY + ((this.hoverY - lastHoverY) * Wrapper.INSTANCE.getMinecraft().getTickDelta());
        float modHoverY = lastModHoverY + ((this.modHoverY - lastModHoverY) * Wrapper.INSTANCE.getMinecraft().getTickDelta());

        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x, y - 1, x + width, y + (Feature.Category.values().length * buttonHeight), 0x50ffffff, 0x00ffffff, 1);
        for (Feature.Category category : Feature.Category.values()) {
            int offset = 1;
            if (categoryCount != Feature.Category.values().length - 1) {
                Render2DHelper.INSTANCE.drawHLine(matrixStack, x + 1, x + width - 2, y + (categoryCount * buttonHeight) + buttonHeight - 1, 0x50ffffff);
            }
            Render2DHelper.INSTANCE.fill(matrixStack, x + 1, y + (categoryCount * buttonHeight), x + width - 1, y + (categoryCount * buttonHeight) + buttonHeight - offset, 0x35000000);
            categoryCount++;
        }
        categoryCount = 0;
        if (hoverBar)
            Render2DHelper.INSTANCE.fill(matrixStack, x + 1, hoverY, x + width - 1, hoverY + buttonHeight - 1, ColorHelper.INSTANCE.getClientColor());
        else
            FontHelper.INSTANCE.drawWithShadow(matrixStack, ">", x + 5, hoverY + (buttonHeight / 2 - 4.5f), ColorHelper.INSTANCE.getClientColor());

        for (Feature.Category category : Feature.Category.values()) {
            String catName = category.name().substring(0, 1).toUpperCase() + category.name().substring(1).toLowerCase();
            FontHelper.INSTANCE.drawWithShadow(matrixStack, catName, x + (categoryCount == categorySelect && !hoverBar ? 12 : 5), y + (categoryCount * buttonHeight) + (buttonHeight / 2 - 4.5f), categoryCount == categorySelect && !hoverBar ? ColorHelper.INSTANCE.getClientColor() : 0xffaaaaaa);
            categoryCount++;
        }

        if (categoryOpen) {
            x = x + width;
            width = getModWidth(width);
            int modCount = 0;
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x, y - 1, x + width, y + (Feature.getModules(Feature.Category.values()[categorySelect]).size() * buttonHeight), 0x50ffffff, 0x00ffffff, 1);
            for (Feature category : Feature.getModules(Feature.Category.values()[categorySelect])) {
                int offset = 1;
                if (modCount != Feature.getModules(Feature.Category.values()[categorySelect]).size() - 1) {
                    Render2DHelper.INSTANCE.drawHLine(matrixStack, x + 1, x + width - 2, y + (modCount * buttonHeight) + buttonHeight - 1, 0x50ffffff);
                }
                Render2DHelper.INSTANCE.fill(matrixStack, x + 1, y + (modCount * buttonHeight), x + width - 1, y + (modCount * buttonHeight) + buttonHeight - offset, 0x35000000);
                modCount++;
            }
            modCount = 0;
            if (hoverBar)
                Render2DHelper.INSTANCE.fill(matrixStack, x + 1, modHoverY, x + width - 1, modHoverY + buttonHeight - 1, ColorHelper.INSTANCE.getClientColor());
            else
                FontHelper.INSTANCE.drawWithShadow(matrixStack, ">", x + 5, modHoverY + (buttonHeight / 2 - 4.5f), ColorHelper.INSTANCE.getClientColor());

            for (Feature feature : Feature.getModules(Feature.Category.values()[categorySelect])) {
                FontHelper.INSTANCE.drawWithShadow(matrixStack, feature.getName(), x + (modCount == modSelect && !hoverBar ? 12 : 5), y + (modCount * buttonHeight) + (buttonHeight / 2 - 4.5f), feature.getState() ? 0xffaaaaaa : 0xff555555);
                modCount++;
            }
        }

    }

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (!Feature.get(Hud.class).tabGui || event.getType() != EventKeyPressed.PressType.IN_GAME)
            return;
        switch (event.getKey()) {
            case GLFW.GLFW_KEY_UP:
                if (!categoryOpen) {
                    categorySelect--;
                    if (categorySelect < 0)
                        categorySelect = Feature.Category.values().length - 1;
                } else {
                    modSelect--;
                    if (modSelect < 0) {
                        modSelect = modListSize(Feature.Category.values()[categorySelect]);
                    }
                }
                break;
            case GLFW.GLFW_KEY_DOWN:
                if (!categoryOpen) {
                    categorySelect++;
                    if (categorySelect > Feature.Category.values().length - 1)
                        categorySelect = 0;
                } else {
                    modSelect++;
                    if (modSelect > modListSize(Feature.Category.values()[categorySelect])) {
                        modSelect = 0;
                    }
                }
                break;
            case GLFW.GLFW_KEY_RIGHT:
            case GLFW.GLFW_KEY_ENTER:
                if (categoryOpen) {
                    if (getSelectedModule() != null) {
                        getSelectedModule().setState(!getSelectedModule().getState());
                    }
                } else {
                    categoryOpen = true;
                }
                break;

            case GLFW.GLFW_KEY_LEFT:
                categoryOpen = false;
                modSelect = 0;
                break;
        }
    });

    private float getModWidth(float origWidth) {
        for (Feature feature : Feature.getModules(Feature.Category.values()[categorySelect])) {
            float offset = hoverBar ? 8 : 15;
            if (FontHelper.INSTANCE.getStringWidth(feature.getName()) + offset > origWidth) {
                origWidth = FontHelper.INSTANCE.getStringWidth(feature.getName()) + offset;
            }
        }
        return origWidth;
    }

    private Feature getSelectedModule() {
        return Feature.getModules(Feature.Category.values()[categorySelect]).get(modSelect);
    }

    private int modListSize(Feature.Category category) {
        return Feature.getModules(category).size() - 1;
    }

    public void setHoverBar(boolean hoverBar) {
        this.hoverBar = hoverBar;
    }

    private StopWatch stopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        this.lastHoverY = hoverY;
        this.lastModHoverY = modHoverY;
        float distance = Math.abs(hoverY - spotHoverY);
        if (distance < 3) {
            hoverY = spotHoverY;
        }
        float speed = 0.45f;
        if (hoverY > spotHoverY) {
            hoverY -= distance * speed;
        } else if (hoverY < spotHoverY) {
            hoverY += distance * speed;
        }
        float modDistance = Math.abs(modHoverY - modSpotHoverY);
        if (modDistance < 3) {
            modHoverY = modSpotHoverY;
        }
        if (modHoverY > modSpotHoverY) {
            modHoverY -= modDistance * speed;
        } else if (modHoverY < modSpotHoverY) {
            modHoverY += modDistance * speed;
        }
    }, new TickFilter(EventTick.Mode.PRE));
}
