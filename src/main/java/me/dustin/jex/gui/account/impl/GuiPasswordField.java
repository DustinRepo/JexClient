package me.dustin.jex.gui.account.impl;

import com.mojang.blaze3d.platform.GlStateManager.LogicOp;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class GuiPasswordField extends ClickableWidget implements Drawable, Element {
    public static final int field_32194 = -1;
    public static final int field_32195 = 1;
    public static final int DEFAULT_EDITABLE_COLOR = 14737632;
    private final TextRenderer textRenderer;
    private String text;
    private int maxLength;
    private int focusedTicks;
    private boolean drawsBackground;
    private boolean focusUnlocked;
    private boolean editable;
    private boolean selecting;
    private int firstCharacterIndex;
    private int selectionStart;
    private int selectionEnd;
    private int editableColor;
    private int uneditableColor;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> changedListener;
    private Predicate<String> textPredicate;
    private BiFunction<String, Integer, OrderedText> renderTextProvider;

    public GuiPasswordField(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        this(textRenderer, x, y, width, height, (TextFieldWidget)null, text);
    }

    public GuiPasswordField(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(x, y, width, height, text);
        this.text = "";
        this.maxLength = 32;
        this.drawsBackground = true;
        this.focusUnlocked = true;
        this.editable = true;
        this.editableColor = 14737632;
        this.uneditableColor = 7368816;
        this.textPredicate = Objects::nonNull;
        this.renderTextProvider = (string, integer) -> {
            return OrderedText.styledForwardsVisitedString(string, Style.EMPTY);
        };
        this.textRenderer = textRenderer;
        if (copyFrom != null) {
            this.setText(copyFrom.getText());
        }

    }

    public void setChangedListener(Consumer<String> changedListener) {
        this.changedListener = changedListener;
    }

    public void setRenderTextProvider(BiFunction<String, Integer, OrderedText> renderTextProvider) {
        this.renderTextProvider = renderTextProvider;
    }

    public void tick() {
        ++this.focusedTicks;
    }

    protected MutableText getNarrationMessage() {
        Text text = this.getMessage();
        return Text.translatable("gui.narrate.editBox", text, this.text);
    }

    public void setText(String text) {
        if (this.textPredicate.test(text)) {
            if (text.length() > this.maxLength) {
                this.text = text.substring(0, this.maxLength);
            } else {
                this.text = text;
            }

            this.setCursorToEnd();
            this.setSelectionEnd(this.selectionStart);
            this.onChanged(text);
        }
    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.text.substring(i, j);
    }

    public void setTextPredicate(Predicate<String> textPredicate) {
        this.textPredicate = textPredicate;
    }

    public void write(String text) {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        int k = this.maxLength - this.text.length() - (i - j);
        String string = SharedConstants.stripInvalidChars(text);
        int l = string.length();
        if (k < l) {
            string = string.substring(0, k);
            l = k;
        }

        String string2 = (new StringBuilder(this.text)).replace(i, j, string).toString();
        if (this.textPredicate.test(string2)) {
            this.text = string2;
            this.setSelectionStart(i + l);
            this.setSelectionEnd(this.selectionStart);
            this.onChanged(this.text);
        }
    }

    private void onChanged(String newText) {
        if (this.changedListener != null) {
            this.changedListener.accept(newText);
        }

    }

    private void erase(int offset) {
        if (Screen.hasControlDown()) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }

    }

    public void eraseWords(int wordOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                this.eraseCharacters(this.getWordSkipPosition(wordOffset) - this.selectionStart);
            }
        }
    }

    public void eraseCharacters(int characterOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                int i = this.getCursorPosWithOffset(characterOffset);
                int j = Math.min(i, this.selectionStart);
                int k = Math.max(i, this.selectionStart);
                if (j != k) {
                    String string = (new StringBuilder(this.text)).delete(j, k).toString();
                    if (this.textPredicate.test(string)) {
                        this.text = string;
                        this.setCursor(j);
                    }
                }
            }
        }
    }

    public int getWordSkipPosition(int wordOffset) {
        return this.getWordSkipPosition(wordOffset, this.getCursor());
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition) {
        return this.getWordSkipPosition(wordOffset, cursorPosition, true);
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
        int i = cursorPosition;
        boolean bl = wordOffset < 0;
        int j = Math.abs(wordOffset);

        for(int k = 0; k < j; ++k) {
            if (!bl) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while(skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while(skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while(i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursor(int offset) {
        this.setCursor(this.getCursorPosWithOffset(offset));
    }

    private int getCursorPosWithOffset(int offset) {
        return Util.moveCursor(this.text, this.selectionStart, offset);
    }

    public void setCursor(int cursor) {
        this.setSelectionStart(cursor);
        if (!this.selecting) {
            this.setSelectionEnd(this.selectionStart);
        }

        this.onChanged(this.text);
    }

    public void setSelectionStart(int cursor) {
        this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
    }

    public void setCursorToStart() {
        this.setCursor(0);
    }

    public void setCursorToEnd() {
        this.setCursor(this.text.length());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isNarratable()) {
            return false;
        } else {
            this.selecting = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode)) {
                this.setCursorToEnd();
                this.setSelectionEnd(0);
                return true;
            } else if (Screen.isCopy(keyCode)) {
                MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                return true;
            } else if (Screen.isPaste(keyCode)) {
                if (this.editable) {
                    this.write(MinecraftClient.getInstance().keyboard.getClipboard());
                }

                return true;
            } else if (Screen.isCut(keyCode)) {
                MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                if (this.editable) {
                    this.write("");
                }

                return true;
            } else {
                switch(keyCode) {
                    case 259:
                        if (this.editable) {
                            this.selecting = false;
                            this.erase(-1);
                            this.selecting = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.editable) {
                            this.selecting = false;
                            this.erase(1);
                            this.selecting = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.setCursorToStart();
                        return true;
                    case 269:
                        this.setCursorToEnd();
                        return true;
                }
            }
        }
    }

    public boolean isNarratable() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!this.isNarratable()) {
            return false;
        } else if (SharedConstants.isValidChar(chr)) {
            if (this.editable) {
                this.write(Character.toString(chr));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean bl = mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
            if (this.focusUnlocked) {
                this.setTextFieldFocused(bl);
            }

            if (this.isFocused() && bl && button == 0) {
                int i = MathHelper.floor(mouseX) - this.x;
                if (this.drawsBackground) {
                    i -= 4;
                }

                String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
                this.setCursor(this.textRenderer.trimToWidth(string, i).length() + this.firstCharacterIndex);
                return true;
            } else {
                return false;
            }
        }
    }

    public void setTextFieldFocused(boolean focused) {
        this.setFocused(focused);
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.isVisible()) {
            int j;
            if (this.drawsBackground()) {
                j = this.isFocused() ? -1 : -6250336;
                fill(matrices, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, j);
                fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            }

            j = this.editable ? this.editableColor : this.uneditableColor;
            int k = this.selectionStart - this.firstCharacterIndex;
            int l = this.selectionEnd - this.firstCharacterIndex;
            //String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
            String string = "";
            for (int i5 = 0; i5 < this.text.length(); i5++) {
                string = string + "*";
            }
            boolean bl = k >= 0 && k <= string.length();
            boolean bl2 = this.isFocused() && this.focusedTicks / 6 % 2 == 0 && bl;
            int m = this.drawsBackground ? this.x + 4 : this.x;
            int n = this.drawsBackground ? this.y + (this.height - 8) / 2 : this.y;
            int o = m;
            if (l > string.length()) {
                l = string.length();
            }

            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, k) : string;
                o = this.textRenderer.drawWithShadow(matrices, (OrderedText)this.renderTextProvider.apply(string2, this.firstCharacterIndex), (float)m, (float)n, j);
            }

            boolean bl3 = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
            int p = o;
            if (!bl) {
                p = k > 0 ? m + this.width : m;
            } else if (bl3) {
                p = o - 1;
                --o;
            }

            if (!string.isEmpty() && bl && k < string.length()) {
                this.textRenderer.drawWithShadow(matrices, (OrderedText)this.renderTextProvider.apply(string.substring(k), this.selectionStart), (float)o, (float)n, j);
            }

            if (!bl3 && this.suggestion != null) {
                this.textRenderer.drawWithShadow(matrices, this.suggestion, (float)(p - 1), (float)n, -8355712);
            }

            int var10002;
            int var10003;
            int var10004;
            if (bl2) {
                if (bl3) {
                    var10002 = n - 1;
                    var10003 = p + 1;
                    var10004 = n + 1;
                    Objects.requireNonNull(this.textRenderer);
                    DrawableHelper.fill(matrices, p, var10002, var10003, var10004 + 9, -3092272);
                } else {
                    this.textRenderer.drawWithShadow(matrices, "_", (float)p, (float)n, j);
                }
            }

            if (l != k) {
                int q = m + this.textRenderer.getWidth(string.substring(0, l));
                var10002 = n - 1;
                var10003 = q - 1;
                var10004 = n + 1;
                Objects.requireNonNull(this.textRenderer);
                this.drawSelectionHighlight(p, var10002, var10003, var10004 + 9);
            }

        }
    }

    private void drawSelectionHighlight(int x1, int y1, int x2, int y2) {
        int j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        if (x2 > this.x + this.width) {
            x2 = this.x + this.width;
        }

        if (x1 > this.x + this.width) {
            x1 = this.x + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(LogicOp.OR_REVERSE);
        bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex((double)x1, (double)y2, 0.0D).next();
        bufferBuilder.vertex((double)x2, (double)y2, 0.0D).next();
        bufferBuilder.vertex((double)x2, (double)y1, 0.0D).next();
        bufferBuilder.vertex((double)x1, (double)y1, 0.0D).next();
        tessellator.draw();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (this.text.length() > maxLength) {
            this.text = this.text.substring(0, maxLength);
            this.onChanged(this.text);
        }

    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursor() {
        return this.selectionStart;
    }

    private boolean drawsBackground() {
        return this.drawsBackground;
    }

    public void setDrawsBackground(boolean drawsBackground) {
        this.drawsBackground = drawsBackground;
    }

    public void setEditableColor(int color) {
        this.editableColor = color;
    }

    public void setUneditableColor(int color) {
        this.uneditableColor = color;
    }

    public boolean changeFocus(boolean lookForwards) {
        return this.visible && this.editable ? super.changeFocus(lookForwards) : false;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
    }

    protected void onFocusedChanged(boolean newFocused) {
        if (newFocused) {
            this.focusedTicks = 0;
        }

    }

    private boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public int getInnerWidth() {
        return this.drawsBackground() ? this.width - 8 : this.width;
    }

    public void setSelectionEnd(int index) {
        int i = this.text.length();
        this.selectionEnd = MathHelper.clamp(index, 0, i);
        if (this.textRenderer != null) {
            if (this.firstCharacterIndex > i) {
                this.firstCharacterIndex = i;
            }

            int j = this.getInnerWidth();
            String string = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), j);
            int k = string.length() + this.firstCharacterIndex;
            if (this.selectionEnd == this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.textRenderer.trimToWidth(this.text, j, true).length();
            }

            if (this.selectionEnd > k) {
                this.firstCharacterIndex += this.selectionEnd - k;
            } else if (this.selectionEnd <= this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.firstCharacterIndex - this.selectionEnd;
            }

            this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, i);
        }

    }

    public void setFocusUnlocked(boolean focusUnlocked) {
        this.focusUnlocked = focusUnlocked;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    public int getCharacterX(int index) {
        return index > this.text.length() ? this.x : this.x + this.textRenderer.getWidth(this.text.substring(0, index));
    }

    public void setX(int x) {
        this.x = x;
    }

    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, Text.translatable("narration.edit_box", this.getText()));
    }
}
