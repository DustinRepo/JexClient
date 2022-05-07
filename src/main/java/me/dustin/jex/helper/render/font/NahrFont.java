package me.dustin.jex.helper.render.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author Nahr.
 * @author nuf
 */
//NahrFont in 2021????
public class NahrFont {

    private Font theFont;
    private Graphics2D theGraphics;
    private FontMetrics theMetrics;
    private float fontSize;
    private int startChar, endChar;
    private float[] xPos, yPos;
    private BufferedImage bufferedImage;
    private ResourceLocation resourceLocation;
    private final Pattern patternControlCode = Pattern.compile("(?i)\\u00A7[0-9A-FK-OG]"), patternUnsupported = Pattern.compile("(?i)\\u00A7[L-O]");

    public NahrFont(Object font, float size) {
        this(font, size, 0F);
    }

    public NahrFont(Object font) {
        this(font, 18F, 0F);
    }

    public NahrFont(Object font, float size, float spacing) {
        this.fontSize = size;
        this.startChar = 32;
        this.endChar = 255;
        this.xPos = new float[this.endChar - this.startChar];
        this.yPos = new float[this.endChar - this.startChar];
        setupGraphics2D();
        createFont(font, size);
    }

    private final void setupGraphics2D() {
        this.bufferedImage = new BufferedImage(256, 256, 2);
        this.theGraphics = ((Graphics2D) this.bufferedImage.getGraphics());
        this.theGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private final void createFont(Object font, float size) {
        try {
            if ((font instanceof Font))
                this.theFont = ((Font) font);
            else if ((font instanceof File))
                this.theFont = Font.createFont(0, (File) font).deriveFont(size);
            else if ((font instanceof InputStream))
                this.theFont = Font.createFont(0, (InputStream) font).deriveFont(size);
            else if ((font instanceof String)) {
                if (((String)font).toLowerCase().endsWith("ttf") || ((String)font).toLowerCase().endsWith("otf"))
                    this.theFont = Font.createFont(0, new File(ModFileHelper.INSTANCE.getJexDirectory() + File.separator + "fonts", (String)font)).deriveFont(size);
                else
                    this.theFont = new Font((String) font, 0, Math.round(size));
            } else {
                this.theFont = new Font("Verdana", 0, Math.round(size));
            }
            this.theGraphics.setFont(this.theFont);
        } catch (Exception e) {
            e.printStackTrace();
            this.theFont = new Font("Verdana", 0, Math.round(size));
            this.theGraphics.setFont(this.theFont);
        }
        this.theGraphics.setColor(new Color(255, 255, 255, 0));
        this.theGraphics.fillRect(0, 0, 256, 256);
        this.theGraphics.setColor(Color.white);
        this.theMetrics = this.theGraphics.getFontMetrics();

        float x = 5.0F;
        float y = 5.0F;
        for (int i = this.startChar; i < this.endChar; i++) {
            this.theGraphics.drawString(Character.toString((char) i), x, y + this.theMetrics.getAscent());
            this.xPos[(i - this.startChar)] = x;
            this.yPos[(i - this.startChar)] = (y - this.theMetrics.getMaxDescent());
            x += this.theMetrics.stringWidth(Character.toString((char) i)) + 2.0F;
            if (x >= 250 - this.theMetrics.getMaxAdvance()) {
                x = 5.0F;
                y += this.theMetrics.getMaxAscent() + this.theMetrics.getMaxDescent() + this.fontSize / 2.0F;
            }
        }
        String base64 = imageToBase64String(bufferedImage, "png");
        this.setResourceLocation(base64, theFont, size);
    }

    private String imageToBase64String(BufferedImage image, String type) {
        String ret = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, type, bos);
            byte[] bytes = bos.toByteArray();
            Base64 encoder = new Base64();
            ret = encoder.encodeAsString(bytes);
            ret = ret.replace(System.lineSeparator(), "");
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return ret;
    }

    public void setResourceLocation(String base64, Object font, float size) {
        NativeImage image = readTexture(base64);
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                imgNew.setPixelRGBA(x, y, image.getPixelRGBA(x, y));
            }
        }

        image.close();
        this.resourceLocation = new ResourceLocation("jex", "font" + getFont().getFontName().toLowerCase().replace(" ", "-") + size);
        applyTexture(resourceLocation, imgNew);
    }

    private static NativeImage readTexture(String textureBase64) {
        try {
            byte[] imgBytes = Base64.decodeBase64(textureBase64);
            ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes);
            return NativeImage.read(bais);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    private void applyTexture(ResourceLocation identifier, NativeImage nativeImage) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().getTextureManager().register(identifier, new DynamicTexture(nativeImage)));
    }

    public final void drawString(PoseStack matrixStack, String text, float x, float y, FontType fontType, int color, int color2) {
        text = stripUnsupported(text);

        Render2DHelper.INSTANCE.setup2DRender(false);
        String text2 = stripControlCodes(text);
        switch (fontType.ordinal()) {
            case 1:
                drawer(matrixStack, text2, x + 0.5F, y, color2);
                drawer(matrixStack, text2, x - 0.5F, y, color2);
                drawer(matrixStack, text2, x, y + 0.5F, color2);
                drawer(matrixStack, text2, x, y - 0.5F, color2);
                break;
            case 2:
                drawer(matrixStack, text2, x + 0.5F, y + 0.5F, color2);
                break;
            case 3:
                drawer(matrixStack, text2, x + 0.5F, y + 1.0F, color2);
                break;
            case 4:
                drawer(matrixStack, text2, x, y + 0.5F, color2);
                break;
            case 5:
                drawer(matrixStack, text2, x, y - 0.5F, color2);
                break;
            case 6:
                break;
        }

        drawer(matrixStack, text, x, y, color);
        Render2DHelper.INSTANCE.end2DRender();
    }

    public void drawCenteredString(PoseStack matrixStack, String text, float x, float y, int color) {
        drawString(matrixStack, text, (x - getStringWidth(text) / 2), y, FontType.SHADOW_THIN, color);
    }

    public final void drawString(PoseStack matrixStack, String text, float x, float y, FontType fontType, int color) {
        matrixStack.scale(0.5f, 0.5f, 1);
        drawString(matrixStack, text, x, y, fontType, color, 0xBB000000);
        matrixStack.scale(2f, 2f, 1);
    }

    private final void drawer(PoseStack matrixStack, String text, float x, float y, int color) {
        x *= 2.0F;
        y *= 2.0F;
        Render2DHelper.INSTANCE.setup2DRender(false);
        Render2DHelper.INSTANCE.bindTexture(this.resourceLocation);

        if ((color & -67108864) == 0)
        {
            color |= -16777216;
        }

        int newColor = color;
        float startX = x;
        boolean scramble = false;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int i = 0; i < text.length(); i++)
            if ((text.charAt(i) == '\247') && (i + 1 < text.length())) {
                char oneMore = Character.toLowerCase(text.charAt(i + 1));
                if (oneMore == 'n') {
                    y += this.theMetrics.getAscent() + 2;
                    x = startX;
                }else if (oneMore == 'k') {
                    scramble = true;
                } else if (oneMore == 'r')
                    newColor = color;
                else {
                    newColor = getColorFromCode(oneMore);
                }
                i++;
            } else {
                try {
                    String obfText = "\\:><&%$@!/?";
                    char c = scramble ? obfText.charAt((int)(new Random().nextFloat() * (obfText.length() - 1))) : text.charAt(i);
                    drawChar(matrixStack, c, x, y, newColor);
                    x += getStringWidth(Character.toString(c)) * 2.0F;
                } catch (ArrayIndexOutOfBoundsException indexException) {
                }
            }
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        Render2DHelper.INSTANCE.shaderColor(0xffffffff);
    }

    public final float getStringWidth(String text) {
        return (float) (getBounds(text).getWidth()) / 2.0F;
    }

    public final float getStringHeight(String text) {
        return (float) getBounds(text).getHeight() / 2.0F;
    }

    private final Rectangle2D getBounds(String text) {
        return this.theMetrics.getStringBounds(text, this.theGraphics);
    }

    private final void drawChar(PoseStack matrixStack, char character, float x, float y, int color) throws ArrayIndexOutOfBoundsException {
        Rectangle2D bounds = this.theMetrics.getStringBounds(Character.toString(character), this.theGraphics);
        drawTexturedModalRect(matrixStack, x, y, this.xPos[(character - this.startChar)], this.yPos[(character - this.startChar)], (float) bounds.getWidth(), (float) bounds.getHeight() + this.theMetrics.getMaxDescent() + 1.0F, color);
    }

    private final List<String> listFormattedStringToWidth(String s, int width) {
        return Arrays.asList(wrapFormattedStringToWidth(s, width).split("\n"));
    }

    private final String wrapFormattedStringToWidth(String s, float width) {
        int wrapWidth = sizeStringToWidth(s, width);

        if (s.length() <= wrapWidth) {
            return s;
        }
        String split = s.substring(0, wrapWidth);
        String split2 = getFormatFromString(split)
                + s.substring(wrapWidth + ((s.charAt(wrapWidth) == ' ') || (s.charAt(wrapWidth) == '\n') ? 1 : 0));
        try {
            return split + "\n" + wrapFormattedStringToWidth(split2, width);
        } catch (Exception e) {
            JexClient.INSTANCE.getLogger().error("Cannot wrap string to width.");
        }
        return "";
    }

    private final int sizeStringToWidth(String par1Str, float par2) {
        int var3 = par1Str.length();
        float var4 = 0.0F;
        int var5 = 0;
        int var6 = -1;

        for (boolean var7 = false; var5 < var3; var5++) {
            char var8 = par1Str.charAt(var5);

            switch (var8) {
                case '\n':
                    var5--;
                    break;
                case '\247':
                    if (var5 < var3 - 1) {
                        var5++;
                        char var9 = par1Str.charAt(var5);

                        if ((var9 != 'l') && (var9 != 'L')) {
                            if ((var9 == 'r') || (var9 == 'R') || (isFormatColor(var9)))
                                var7 = false;
                        } else
                            var7 = true;
                    }
                    break;
                case ' ':
                    var6 = var5;
                case '-':
                    var6 = var5;
                case '_':
                    var6 = var5;
                case ':':
                    var6 = var5;
                default:
                    String text = String.valueOf(var8);
                    var4 += getStringWidth(text);

                    if (var7) {
                        var4 += 1.0F;
                    }
                    break;
            }
            if (var8 == '\n') {
                var5++;
                var6 = var5;
            } else {
                if (var4 > par2) {
                    break;
                }
            }
        }
        return (var5 != var3) && (var6 != -1) && (var6 < var5) ? var6 : var5;
    }

    private final String getFormatFromString(String par0Str) {
        String var1 = "";
        int var2 = -1;
        int var3 = par0Str.length();

        while ((var2 = par0Str.indexOf('\247', var2 + 1)) != -1) {
            if (var2 < var3 - 1) {
                char var4 = par0Str.charAt(var2 + 1);

                if (isFormatColor(var4))
                    var1 = "\247" + var4;
                else if (isFormatSpecial(var4)) {
                    var1 = var1 + "\247" + var4;
                }
            }
        }

        return var1;
    }

    private final boolean isFormatColor(char par0) {
        return ((par0 >= '0') && (par0 <= '9')) || ((par0 >= 'a') && (par0 <= 'f')) || ((par0 >= 'A') && (par0 <= 'F'));
    }

    private final boolean isFormatSpecial(char par0) {
        return ((par0 >= 'k') && (par0 <= 'o')) || ((par0 >= 'K') && (par0 <= 'O')) || (par0 == 'r') || (par0 == 'R');
    }

    private final void drawTexturedModalRect(PoseStack matrixStack, float x, float y, float u, float v, float width, float height, int color) {
        Matrix4f matrix4f = matrixStack.last().pose();
        float scale = 0.0039063F;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        float f = (color >> 24 & 0xFF) / 255.0F;
        float f1 = (color >> 16 & 0xFF) / 255.0F;
        float f2 = (color >> 8 & 0xFF) / 255.0F;
        float f3 = (color & 0xFF) / 255.0F;
        bufferBuilder.vertex(matrix4f, x + 0.0F, y + height, 0.0f).uv((u + 0.0F) * scale, (v + height) * scale).color(f1, f2, f3, f).endVertex();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0f).uv((u + width) * scale, (v + height) * scale).color(f1, f2, f3, f).endVertex();
        bufferBuilder.vertex(matrix4f, x + width, y + 0.0F, 0.0f).uv((u + width) * scale, (v + 0.0F) * scale).color(f1, f2, f3, f).endVertex();
        bufferBuilder.vertex(matrix4f, x + 0.0F, y + 0.0F, 0.0f).uv((u + 0.0F) * scale, (v + 0.0F) * scale).color(f1, f2, f3, f).endVertex();
    }

    public final String stripControlCodes(String s) {
        for (ChatFormatting value : ChatFormatting.values()) {
            s = s.replace("\247" + value.getChar(), "");
        }
        return s;
    }

    public final String stripUnsupported(String s) {
        return this.patternUnsupported.matcher(s).replaceAll("");
    }

    public final Graphics2D getGraphics() {
        return this.theGraphics;
    }

    public final Font getFont() {
        return theFont;
    }

    private int getColorFromCode(char code) {
        switch (code) {
            case '0': return Color.BLACK.getRGB();
            case '1': return 0xff0000AA;
            case '2': return 0xff00AA00;
            case '3': return 0xff00AAAA;
            case '4': return 0xffAA0000;
            case '5': return 0xffAA00AA;
            case '6': return 0xffFFAA00;
            case '7': return 0xffAAAAAA;
            case '8': return 0xff555555;
            case '9': return 0xff5555FF;
            case 'a': return 0xff55FF55;
            case 'b': return 0xff55FFFF;
            case 'c': return 0xffFF5555;
            case 'd': return 0xffFF55FF;
            case 'e': return 0xffFFFF55;
            case 'f': return 0xffffffff;
            case 'g': return 0xffDDD605;
        }
        return -1;
    }

    public enum FontType {
        NORMAL, SHADOW_THICK, SHADOW_THIN, OUTLINE_THIN, EMBOSS_TOP, EMBOSS_BOTTOM;
    }

}