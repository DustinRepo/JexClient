package me.dustin.jex.gui.minecraft.blocklist;


import me.dustin.jex.addon.hat.Hat;
import me.dustin.jex.helper.file.files.XrayFile;
import me.dustin.jex.gui.jex.JexOptionsScreen;
import me.dustin.jex.gui.minecraft.blocklist.button.BlockButton;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.world.xray.Xray;
import me.dustin.jex.helper.render.Scrollbar;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class XraySelectScreen extends Screen {

    private ArrayList<BlockButton> allowedBlocks = new ArrayList<>();
    private ArrayList<BlockButton> notAllowedBlocks = new ArrayList<>();
    private TextFieldWidget searchField;
    private ButtonWidget searchButton;
    private ButtonWidget addXrayButton;
    private ButtonWidget removeXrayButton;
    private ButtonWidget doneButton;
    private Scrollbar leftScrollbar;
    private Scrollbar rightScrollbar;
    public XraySelectScreen() {
        super(new LiteralText("Xray Selection"));
    }

    @Override
    protected void init() {
        float allowedLeftX = Render2DHelper.INSTANCE.getScaledWidth() / 2 - 200;
        float notAllowedLeftX = Render2DHelper.INSTANCE.getScaledWidth() / 2 + 2;
        float startY = Render2DHelper.INSTANCE.getScaledHeight() / 2 - 125;
        float buttonWidth = 198;
        loadBlocks();
        searchField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (int) allowedLeftX, (int) startY - 25, 350, 20, new LiteralText(""));
        searchField.setVisible(true);
        searchField.setEditable(true);
        searchButton = new ButtonWidget(Render2DHelper.INSTANCE.getScaledWidth() / 2 + 155, (int) startY - 25, 45, 20, new LiteralText("Search"), button -> {
            if (searchField.getText().isEmpty())
                loadBlocks();
            else
                loadBlocks(searchField.getText());
        });

        removeXrayButton = new ButtonWidget((int) allowedLeftX, (int) startY + 255, (int) buttonWidth, 20, new LiteralText("Remove From Xray"), button -> {
            getSelectedAllowed().forEach(blockButton -> {
                Xray.blockList.remove(blockButton.getBlock());
                allowedBlocks.remove(blockButton);
                notAllowedBlocks.add(blockButton);
            });
            if (searchField.getText().isEmpty())
                loadBlocks();
            else
                loadBlocks(searchField.getText());
            XrayFile.write();
            if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null && Feature.get(Xray.class).getState())
                Wrapper.INSTANCE.getMinecraft().worldRenderer.reload();
        });
        addXrayButton = new ButtonWidget((int) notAllowedLeftX, (int) startY + 255, (int) buttonWidth, 20, new LiteralText("Add To Xray"), button -> {
            getSelectedNotAllowed().forEach(blockButton -> {
                Xray.blockList.add(blockButton.getBlock());
                allowedBlocks.add(blockButton);
                notAllowedBlocks.remove(blockButton);
            });
            if (searchField.getText().isEmpty())
                loadBlocks();
            else
                loadBlocks(searchField.getText());
            XrayFile.write();
            if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null && Feature.get(Xray.class).getState())
                Wrapper.INSTANCE.getMinecraft().worldRenderer.reload();
        });

        doneButton = new ButtonWidget((int) (Render2DHelper.INSTANCE.getScaledWidth() / 2 - 100), height - 22, 200, 20, new LiteralText("Done"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(new JexOptionsScreen());
        });

        this.addSelectableChild(searchField);
        this.addDrawableChild(searchButton);
        this.addDrawableChild(addXrayButton);
        this.addDrawableChild(removeXrayButton);
        this.addDrawableChild(doneButton);
        super.init();
    }

    @Override
    public void tick() {
        searchField.tick();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        this.addXrayButton.active = !getSelectedNotAllowed().isEmpty();
        this.removeXrayButton.active = !getSelectedAllowed().isEmpty();

        float allowedLeftX = Render2DHelper.INSTANCE.getScaledWidth() / 2 - 200;
        float startY = Render2DHelper.INSTANCE.getScaledHeight() / 2 - 125;

        Render2DHelper.INSTANCE.fill(matrices, allowedLeftX, startY, allowedLeftX + 400, startY + 250, 0x60000000);
        Render2DHelper.INSTANCE.fill(matrices, Render2DHelper.INSTANCE.getScaledWidth() / 2 - 1, startY, Render2DHelper.INSTANCE.getScaledWidth() / 2, startY + 250, ColorHelper.INSTANCE.getClientColor());

        Scissor.INSTANCE.cut(0, (int) startY, width, 250);
        this.allowedBlocks.forEach(button -> {
            if (button.getY() + button.getHeight() > startY && button.getY() < startY + 250)
                button.draw(matrices);
        });
        this.notAllowedBlocks.forEach(button -> {
            if (button.getY() + button.getHeight() > startY && button.getY() < startY + 250)
                button.draw(matrices);
        });
        Scissor.INSTANCE.seal();
        if (leftScrollbar != null)
            leftScrollbar.render(matrices);
        if (rightScrollbar != null)
            rightScrollbar.render(matrices);
        searchField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHoveredAllowed()) {
            for (BlockButton button1 : allowedBlocks) {
                if (button1.isHovered()) {
                    button1.setSelected(!button1.isSelected());
                } else if (!KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                    button1.setSelected(false);
                }
            }
        } else if (isHoveredNotAllowed()) {
            for (BlockButton button1 : notAllowedBlocks) {
                if (button1.isHovered()) {
                    button1.setSelected(!button1.isSelected());
                } else if (!KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                    button1.setSelected(false);
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchField.isFocused() && keyCode == GLFW.GLFW_KEY_ENTER) {
            if (searchField.getText().isEmpty())
                loadBlocks();
            else
                loadBlocks(searchField.getText());
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double double_1, double double_2, double amount) {
        if (amount > 0) {
            if (isHoveredAllowed()) {
                if (allowedBlocks.isEmpty())
                    return false;
                BlockButton topButton = allowedBlocks.get(0);
                if (topButton.getY() < ((height / 2) - 125)) {
                    for (int i = 0; i < 40; i++) {
                        if (topButton.getY() < ((height / 2) - 125)) {
                            for (BlockButton button : allowedBlocks) {
                                button.setY(button.getY() + 1);
                            }
                            if (leftScrollbar != null)
                                leftScrollbar.moveUp();
                        }
                    }
                }
            } else if (isHoveredNotAllowed()) {
                if (notAllowedBlocks.isEmpty())
                    return false;
                BlockButton topButton = notAllowedBlocks.get(0);
                if (topButton.getY() < ((height / 2) - 125)) {
                    for (int i = 0; i < 40; i++) {
                        if (topButton.getY() < ((height / 2) - 125)) {
                            for (BlockButton button : notAllowedBlocks) {
                                button.setY(button.getY() + 1);
                            }
                            if (rightScrollbar != null)
                                rightScrollbar.moveUp();
                        }
                    }
                }
            }
        } else if (amount < 0) {
            if (isHoveredAllowed()) {
                if (allowedBlocks.isEmpty())
                    return false;
                BlockButton bottomButton = allowedBlocks.get(allowedBlocks.size() - 1);
                if (bottomButton.getY() + bottomButton.getHeight() > ((height / 2) + 125)) {
                    for (int i = 0; i < 40; i++) {
                        if (bottomButton.getY() + bottomButton.getHeight() > ((height / 2) + 125)) {
                            for (BlockButton button : allowedBlocks) {
                                button.setY(button.getY() - 1);
                            }
                            if (leftScrollbar != null)
                                leftScrollbar.moveDown();
                        }
                    }
                }
            } else if (isHoveredNotAllowed()) {
                if (notAllowedBlocks.isEmpty())
                    return false;
                BlockButton bottomButton = notAllowedBlocks.get(notAllowedBlocks.size() - 1);
                if (bottomButton.getY() + bottomButton.getHeight() > ((height / 2) + 125)) {
                    for (int i = 0; i < 40; i++) {
                        if (bottomButton.getY() + bottomButton.getHeight() > ((height / 2) + 125)) {
                            for (BlockButton button : notAllowedBlocks) {
                                button.setY(button.getY() - 1);
                            }
                            if (rightScrollbar != null)
                                rightScrollbar.moveDown();
                        }
                    }
                }
            }
        }
        return false;
    }

    private ArrayList<BlockButton> getSelectedAllowed() {
        ArrayList<BlockButton> blockButtons = new ArrayList<>();
        for (BlockButton blockButton : allowedBlocks) {
            if (blockButton.isSelected())
                blockButtons.add(blockButton);
        }
        return blockButtons;
    }

    private ArrayList<BlockButton> getSelectedNotAllowed() {
        ArrayList<BlockButton> blockButtons = new ArrayList<>();
        for (BlockButton blockButton : notAllowedBlocks) {
            if (blockButton.isSelected())
                blockButtons.add(blockButton);
        }
        return blockButtons;
    }

    private boolean isHoveredAllowed() {
        float allowedLeftX = Render2DHelper.INSTANCE.getScaledWidth() / 2 - 200;
        float startY = Render2DHelper.INSTANCE.getScaledHeight() / 2 - 125;
        float buttonWidth = 198;
        return Render2DHelper.INSTANCE.isHovered(allowedLeftX, startY, buttonWidth, 250);
    }

    private boolean isHoveredNotAllowed() {
        float notAllowedLeftX = Render2DHelper.INSTANCE.getScaledWidth() / 2 + 2;
        float startY = Render2DHelper.INSTANCE.getScaledHeight() / 2 - 125;
        float buttonWidth = 198;
        return Render2DHelper.INSTANCE.isHovered(notAllowedLeftX, startY, buttonWidth, 250);
    }

    private void loadBlocks() {
        allowedBlocks.clear();
        notAllowedBlocks.clear();
        int allowedCount = 0;
        int notAllowedCount = 0;

        float allowedLeftX = Render2DHelper.INSTANCE.getScaledWidth() / 2 - 200;
        float notAllowedLeftX = Render2DHelper.INSTANCE.getScaledWidth() / 2 + 2;
        float startY = Render2DHelper.INSTANCE.getScaledHeight() / 2 - 125;
        float buttonWidth = 198;
        float buttonHeight = 20;

        for (Block block : Registry.BLOCK) {
            if (block == Blocks.AIR || block == Hat.cowboyHat || block == Hat.halo || block == Hat.topHat || block == Hat.crown || block == Hat.propeller)
                continue;
            if (Xray.blockList.contains(block)) {
                float y = startY + (buttonHeight * allowedCount);
                allowedBlocks.add(new BlockButton(block, block.getTranslationKey(), allowedLeftX, y + 1, buttonWidth, buttonHeight, null));
                allowedCount++;
            } else {
                float y = startY + (buttonHeight * notAllowedCount);
                notAllowedBlocks.add(new BlockButton(block, block.getTranslationKey(), notAllowedLeftX, y + 1, buttonWidth, buttonHeight, null));
                notAllowedCount++;
            }
        }
        if (!allowedBlocks.isEmpty()) {
            float contentHeight = (allowedBlocks.get(allowedBlocks.size() - 1).getY() + (allowedBlocks.get(allowedBlocks.size() - 1).getHeight())) - allowedBlocks.get(0).getY();
            float viewportHeight = 250;
            this.leftScrollbar = new Scrollbar((width / 2.f) - 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f - 126, 2, 200, viewportHeight, contentHeight, ColorHelper.INSTANCE.getClientColor());
        }
        if (!notAllowedBlocks.isEmpty()) {
            float contentHeight = (notAllowedBlocks.get(notAllowedBlocks.size() - 1).getY() + (notAllowedBlocks.get(notAllowedBlocks.size() - 1).getHeight())) - notAllowedBlocks.get(0).getY();
            float viewportHeight = 250;
            this.rightScrollbar = new Scrollbar((width / 2.f) + 200, Render2DHelper.INSTANCE.getScaledHeight() / 2.f - 126, 2, 200, viewportHeight, contentHeight, ColorHelper.INSTANCE.getClientColor());
        }
    }

    private void loadBlocks(String name) {
        allowedBlocks.clear();
        notAllowedBlocks.clear();
        int allowedCount = 0;
        int notAllowedCount = 0;

        float allowedLeftX = Render2DHelper.INSTANCE.getScaledWidth() / 2 - 200;
        float notAllowedLeftX = Render2DHelper.INSTANCE.getScaledWidth() / 2 + 2;
        float startY = Render2DHelper.INSTANCE.getScaledHeight() / 2 - 125;
        float buttonWidth = 198;
        float buttonHeight = 20;

        for (Block block : Registry.BLOCK) {
            if (block == Blocks.AIR || block == Hat.cowboyHat || block == Hat.halo || block == Hat.topHat || block == Hat.crown || block == Hat.propeller)
                continue;
            String blockName = Registry.BLOCK.getId(block).toString();
            if (blockName.contains(":"))
                blockName = blockName.split(":")[1];
            if (!blockName.replace("_", " ").toLowerCase().contains(searchField.getText().toLowerCase()))
                continue;
            if (Xray.blockList.contains(block)) {
                float y = startY + (buttonHeight * allowedCount);
                allowedBlocks.add(new BlockButton(block, block.getTranslationKey(), allowedLeftX, y + 1, buttonWidth, buttonHeight, null));
                allowedCount++;
            } else {
                float y = startY + (buttonHeight * notAllowedCount);
                notAllowedBlocks.add(new BlockButton(block, block.getTranslationKey(), notAllowedLeftX, y + 1, buttonWidth, buttonHeight, null));
                notAllowedCount++;
            }
        }
        if (!allowedBlocks.isEmpty()) {
            float contentHeight = (allowedBlocks.get(allowedBlocks.size() - 1).getY() + (allowedBlocks.get(allowedBlocks.size() - 1).getHeight())) - allowedBlocks.get(0).getY();
            float viewportHeight = 250;
            this.leftScrollbar = new Scrollbar((width / 2.f) - 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f - 126, 2, 200, viewportHeight, contentHeight, ColorHelper.INSTANCE.getClientColor());
        }
        if (!notAllowedBlocks.isEmpty()) {
            float contentHeight = (notAllowedBlocks.get(notAllowedBlocks.size() - 1).getY() + (notAllowedBlocks.get(notAllowedBlocks.size() - 1).getHeight())) - notAllowedBlocks.get(0).getY();
            float viewportHeight = 250;
            this.rightScrollbar = new Scrollbar((width / 2.f) + 200, Render2DHelper.INSTANCE.getScaledHeight() / 2.f - 126, 2, 200, viewportHeight, contentHeight, ColorHelper.INSTANCE.getClientColor());
        }
    }
}
