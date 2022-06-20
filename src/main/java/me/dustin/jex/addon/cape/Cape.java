package me.dustin.jex.addon.cape;

import com.google.common.collect.Maps;
import me.dustin.jex.JexClient;
import me.dustin.jex.addon.Addon;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.render.GifDecoder;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Cape {
    private static final HashMap<String, Identifier> capes = Maps.newHashMap();
    private static final HashMap<String, GifInfo> gifCapes = Maps.newHashMap();
    private static boolean selfAnimated;

    public static void setPersonalCape(File file) {
        if (!file.exists())
            return;
        new Thread(() -> {
            try {
                if (file.getName().endsWith(".gif")) {
                    GifDecoder decoder = new GifDecoder();
                    decoder.read(new FileInputStream(file));
                    for (int i = 0; i < decoder.getFrameCount(); i++) {
                        BufferedImage frame = decoder.getFrame(i);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageIO.write(frame, "png", os);
                        InputStream is = new ByteArrayInputStream(os.toByteArray());
                        NativeImage capeFrame = NativeImage.read(is);
                        Identifier id = new Identifier("jex", "capes/self/%s".formatted(i));
                        FileHelper.INSTANCE.applyTexture(id, capeFrame);
                        gifCapes.put("self", new GifInfo(decoder, new StopWatch()));
                        selfAnimated = true;
                    }
                    return;
                }
                BufferedImage in = ImageIO.read(file);
                BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = newImage.createGraphics();
                g.drawImage(in, 0, 0, null);
                g.dispose();
                NativeImage capeImage = FileHelper.INSTANCE.readTexture(FileHelper.INSTANCE.imageToBase64String(newImage, "png"));
                int imageWidth = 64;
                int imageHeight = 32;

                for (int srcWidth = capeImage.getWidth(), srcHeight = capeImage.getHeight(); imageWidth < srcWidth || imageHeight < srcHeight; ) {
                    imageWidth *= 2;
                    imageHeight *= 2;
                }

                NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
                for (int x = 0; x < capeImage.getWidth(); x++) {
                    for (int y = 0; y < capeImage.getHeight(); y++) {
                        imgNew.setColor(x, y, capeImage.getColor(x, y));
                    }
                }

                capeImage.close();
                Identifier id = new Identifier("jex", "capes/self.png");
                FileHelper.INSTANCE.applyTexture(id, imgNew);
                if (capes.containsKey("self"))
                    capes.replace("self", id);
                else
                    capes.put("self", id);
                selfAnimated = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void parseCape(String cape, String uuid) {
        byte[] bytes = Base64.decodeBase64(cape);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GifDecoder decoder = new GifDecoder();
        decoder.read(bais);
        if (decoder.getFrameCount() > 0) {
            try {
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    BufferedImage frame = decoder.getFrame(i);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(frame, "png", os);
                    InputStream is = new ByteArrayInputStream(os.toByteArray());
                    NativeImage capeFrame = NativeImage.read(is);
                    Identifier id = new Identifier("jex", "capes/%s/%s".formatted(uuid, i));
                    FileHelper.INSTANCE.applyTexture(id, capeFrame);
                }
                JexClient.INSTANCE.getLogger().info("Gif cape loaded. %d frames".formatted(decoder.getFrameCount()));
                gifCapes.put(uuid, new GifInfo(decoder, new StopWatch()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        NativeImage capeImage = FileHelper.INSTANCE.readTexture(cape);
        int imageWidth = 64;
        int imageHeight = 32;

        for (int srcWidth = capeImage.getWidth(), srcHeight = capeImage.getHeight(); imageWidth < srcWidth || imageHeight < srcHeight; ) {
            imageWidth *= 2;
            imageHeight *= 2;
        }

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < capeImage.getWidth(); x++) {
            for (int y = 0; y < capeImage.getHeight(); y++) {
                imgNew.setColor(x, y, capeImage.getColor(x, y));
            }
        }

        capeImage.close();
        Identifier id = new Identifier("jex", "capes/" + uuid);
        FileHelper.INSTANCE.applyTexture(id, imgNew);
        capes.put(uuid, id);
    }

    public static void clear() {
        capes.clear();
        gifCapes.clear();
    }

    public static boolean hasCape(String uuid) {
        return capes.containsKey(uuid) || gifCapes.containsKey(uuid);
    }

    public static Identifier getCape(String uuid) {
        if (!hasCape(uuid))
            return null;
        GifInfo gifInfo = gifCapes.get(uuid);
        if (uuid.equals("self") && selfAnimated) {
            if (gifInfo.stopWatch.hasPassed(gifInfo.gifDecoder.getDelay(gifInfo.lastFrame))) {
                int current = (gifInfo.lastFrame + 1 > gifInfo.gifDecoder.getFrameCount() - 1) ? 0 : (gifInfo.lastFrame + 1);
                gifInfo.lastFrame = current;
                gifInfo.stopWatch.reset();
                return new Identifier("jex", "capes/self/%s".formatted(current));
            }
            return new Identifier("jex", "capes/self/%s".formatted(gifInfo.lastFrame));
        }
        if (gifCapes.containsKey(uuid)) {
            if (gifInfo.stopWatch.hasPassed(gifInfo.gifDecoder.getDelay(gifInfo.lastFrame))) {
                int current = (gifInfo.lastFrame + 1 > gifInfo.gifDecoder.getFrameCount()  - 1) ? 0 : (gifInfo.lastFrame + 1);
                gifInfo.lastFrame = current;
                gifInfo.stopWatch.reset();
                return new Identifier("jex", "capes/%s/%s".formatted(uuid, current));
            }
            return new Identifier("jex", "capes/%s/%s".formatted(uuid, gifInfo.lastFrame));
        }
        return capes.get(uuid);
    }

    private static class GifInfo{
        private final GifDecoder gifDecoder;
        private final StopWatch stopWatch;
        private int lastFrame;
        public GifInfo(GifDecoder gifDecoder, StopWatch stopWatch) {
            this.gifDecoder = gifDecoder;
            this.stopWatch = stopWatch;
        }
    }
}
