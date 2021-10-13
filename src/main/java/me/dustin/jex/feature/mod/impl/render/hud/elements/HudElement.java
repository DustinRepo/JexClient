package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.hud.Hud;
import me.dustin.jex.helper.file.files.HudElementsFile;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class HudElement {
    private static Hud hud;

    private boolean topSide = true;
    private boolean leftSide = true;

    private String name;
    private float x, y, minWidth, minHeight, width, height, xDif, yDif, lastWidth, lastHeight, lastX, lastY;

    private boolean isDragging;

    public HudElement(String name, float x, float y, float minWidth, float minHeight) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.width = minWidth;
        this.height = minHeight;
        this.lastWidth = minWidth;
        this.lastHeight = minHeight;
    }

    public void render(MatrixStack matrixStack) {
        handleElement();
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen) {
            if (isHovered())
                FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getName() + " | \247bRight-Click to Flip ", getX() + (getWidth() / 2.f), getY() - 10, -1);
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), isHovered() ? ColorHelper.INSTANCE.getClientColor() : 0xff696969, 0x40000000, 1);
        }
    }

    public void tick() {
    }

    public void click(int mouseX, int mouseY, int mouseButton) {
        if (isHovered()) {
            if (mouseButton == 0) {
                isDragging = true;
                xDif = getX() - MouseHelper.INSTANCE.getMouseX();
                yDif = getY() - MouseHelper.INSTANCE.getMouseY();
            } else if (mouseButton == 1) {
                setTopSide(!isTopSide());
                HudElementsFile.write();
            }
        }
    }

    public void handleElement() {
        setLeftSide(this.getX() + (this.getWidth() / 2.f) < Render2DHelper.INSTANCE.getScaledWidth() / 2.f);
        this.setWidth(Math.max(width, minWidth));
        this.setHeight(Math.max(height, minHeight));
        if (this.isDragging) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0)) {
                this.isDragging = false;
                HudElementsFile.write();
            } else {
                x = xDif + MouseHelper.INSTANCE.getMouseX();
                y = yDif + MouseHelper.INSTANCE.getMouseY();
                checkCollisions();
                this.setX(MathHelper.clamp(this.getX(), 0, Render2DHelper.INSTANCE.getScaledWidth() - this.getWidth()));
                this.setY(MathHelper.clamp(this.getY(), 0, Render2DHelper.INSTANCE.getScaledHeight() - this.getHeight()));
            }
        }

        if (!this.isLeftSide()) {
            if (lastWidth > width) {
                float dif = lastWidth - width;
                x += dif;
            } else if (lastWidth < width) {
                float dif = width - lastWidth;
                x -= dif;
            }
        }
        if (!this.isTopSide()) {
            if (lastHeight > height) {
                float dif = lastHeight - height;
                y += dif;
            } else if (lastHeight < height) {
                float dif = height - lastHeight;
                y -= dif;
            }
        }
        if (lastWidth != this.width || lastHeight != this.height) {
            checkCollisionsMoveOthers();
            bringOldCollisions();
        }
        this.lastWidth = this.width;
        this.lastHeight = this.height;
        this.lastX = this.x;
        this.lastY = this.y;
    }

    public void checkCollisions() {
        ArrayList<HudElement> colliding = getCollidingElements();
        if (!colliding.isEmpty()) {
            for (HudElement hudElement : colliding) {
                boolean right = hudElement.x > this.x;
                boolean top = hudElement.y > this.y;

                boolean side = Math.abs(hudElement.x - this.x) > Math.abs(hudElement.y - this.y);

                if (side) {
                    if (right) {
                        this.setX(hudElement.getX() - this.getWidth());
                    } else {
                        this.setX(hudElement.getX() + hudElement.getWidth());
                    }
                } else {
                    if (top) {
                        this.setY(hudElement.getY() - this.getHeight());
                    } else {
                        this.setY(hudElement.getY() + hudElement.getHeight());
                    }
                }
            }
        }
    }

    public void checkCollisionsMoveOthers() {
        ArrayList<HudElement> colliding = getCollidingElements();
        if (!colliding.isEmpty()) {
            ArrayList<HudElement> exempt = new ArrayList<>();
            exempt.add(this);
            for (HudElement hudElement : colliding) {
                boolean right = hudElement.x > this.x;
                boolean top = hudElement.y > this.y;

                boolean side = Math.abs(hudElement.x - this.x) > Math.abs(hudElement.y - this.y);

                if (side) {
                    if (right) {
                        hudElement.setX(this.getX() + this.getWidth());
                    } else {
                        hudElement.setX(this.getX() - hudElement.getWidth());
                    }
                    hudElement.checkCollisionsMoveOthers(exempt);
                } else {
                    if (top) {
                        hudElement.setY(this.getY() + this.getHeight());
                    } else {
                        hudElement.setY(this.getY() - hudElement.getHeight());
                    }
                    hudElement.checkCollisionsMoveOthers(exempt);
                }
            }
        }
    }

    public void checkCollisionsMoveOthers(ArrayList<HudElement> exempt) {
        ArrayList<HudElement> colliding = getCollidingElements();
        if (!colliding.isEmpty()) {
            for (HudElement hudElement : colliding) {
                if (exempt.contains(hudElement))
                    continue;
                boolean right = hudElement.x > this.x;
                boolean top = hudElement.y > this.y;

                boolean side = Math.abs(hudElement.x - this.x) > Math.abs(hudElement.y - this.y);

                if (side) {
                    if (right) {
                        hudElement.setX(this.getX() + this.getWidth());
                    } else {
                        hudElement.setX(this.getX() - hudElement.getWidth());
                    }
                    exempt.add(this);
                    hudElement.checkCollisionsMoveOthers(exempt);
                } else {
                    if (top) {
                        hudElement.setY(this.getY() + this.getHeight());
                    } else {
                        hudElement.setY(this.getY() - hudElement.getHeight());
                    }
                    exempt.add(this);
                    hudElement.checkCollisionsMoveOthers(exempt);
                }
            }
        }
    }

    public void bringOldCollisions() {
        ArrayList<HudElement> noLongerColliding = new ArrayList<>();
        ArrayList<HudElement> colliding = getCollidingElements();
        ArrayList<HudElement> wascolliding = getWereCollidingElements();
        ArrayList<HudElement> exempt = new ArrayList<>();
        exempt.add(this);
        for (HudElement hudElement : wascolliding) {
            if (!colliding.contains(hudElement))
                noLongerColliding.add(hudElement);
        }
        if (!noLongerColliding.isEmpty()) {
            for (HudElement hudElement : noLongerColliding) {
                boolean right = hudElement.x > this.x;
                boolean top = hudElement.y > this.y;

                boolean side = Math.abs(hudElement.x - this.x) > Math.abs(hudElement.y - this.y);

                if (side) {
                    if (right) {
                        hudElement.setX(this.getX() + this.getWidth());
                    } else {
                        hudElement.setX(this.getX() - hudElement.getWidth());
                    }
                    hudElement.bringOldCollisions(exempt);
                } else {
                    if (top) {
                        hudElement.setY(this.getY() + this.getHeight());
                    } else {
                        hudElement.setY(this.getY() - hudElement.getHeight());
                    }
                    hudElement.bringOldCollisions(exempt);
                }
            }
        }
    }

    public void bringOldCollisions(ArrayList<HudElement> exempt) {
        ArrayList<HudElement> noLongerColliding = new ArrayList<>();
        ArrayList<HudElement> colliding = getCollidingElements();
        ArrayList<HudElement> wascolliding = getWereCollidingElements();
        for (HudElement hudElement : wascolliding) {
            if (!colliding.contains(hudElement))
                noLongerColliding.add(hudElement);
        }
        if (!noLongerColliding.isEmpty()) {
            for (HudElement hudElement : noLongerColliding) {
                if (exempt.contains(hudElement))
                    continue;
                boolean right = hudElement.x > this.x;
                boolean top = hudElement.y > this.y;

                boolean side = Math.abs(hudElement.x - this.x) > Math.abs(hudElement.y - this.y);

                if (side) {
                    if (right) {
                        hudElement.setX(this.getX() + this.getWidth());
                    } else {
                        hudElement.setX(this.getX() - hudElement.getWidth());
                    }
                    exempt.add(this);
                    hudElement.bringOldCollisions(exempt);
                } else {
                    if (top) {
                        hudElement.setY(this.getY() + this.getHeight());
                    } else {
                        hudElement.setY(this.getY() - hudElement.getHeight());
                    }
                    exempt.add(this);
                    hudElement.bringOldCollisions(exempt);
                }
            }
        }
    }

    public ArrayList<HudElement> getCollidingElements() {
        ArrayList<HudElement> list = new ArrayList<>();
        for (HudElement hudElement : getHud().hudElements) {
            if (hudElement == this)
                continue;
            if (hudElement.getX() + hudElement.getWidth() >= this.getX() && hudElement.getX() <= this.getX() + this.getWidth()) {
                if (hudElement.getY() + hudElement.getHeight() >= this.getY() && hudElement.getY() <= this.getY() + this.getHeight())
                    list.add(hudElement);
            }
        }
        return list;
    }

    public ArrayList<HudElement> getWereCollidingElements() {
        ArrayList<HudElement> list = new ArrayList<>();
        for (HudElement hudElement : getHud().hudElements) {
            if (hudElement == this)
                continue;
            if (hudElement.getX() + hudElement.getWidth() >= this.lastX && hudElement.getX() <= lastX + this.lastWidth) {
                if (hudElement.getY() + hudElement.getHeight() >= this.lastY && hudElement.getY() <= lastY + this.lastHeight)
                    list.add(hudElement);
            }
        }
        return list;
    }

    public boolean isHovered() {
        return Render2DHelper.INSTANCE.isHovered(getX(), getY(), Math.max(getWidth(), getMinWidth()), Math.max(getHeight(), getMinHeight()));
    }

    public boolean isTopSide() {
        return topSide;
    }

    public void setTopSide(boolean topSide) {
        this.topSide = topSide;
    }

    public boolean isLeftSide() {
        return leftSide;
    }

    public void setLeftSide(boolean leftSide) {
        this.leftSide = leftSide;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(float minWidth) {
        this.minWidth = minWidth;
    }

    public float getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(float height) {
        this.minHeight = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public static Hud getHud() {
        if (hud == null)
            hud = (Hud)Feature.get(Hud.class);
        return hud;
    }
}
