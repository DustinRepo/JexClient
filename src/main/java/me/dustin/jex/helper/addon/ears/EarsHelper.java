package me.dustin.jex.helper.addon.ears;

import com.google.common.collect.Maps;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.render.EventInitPlayerModel;
import me.dustin.jex.event.render.EventPlayerEntityTexturedModelData;
import me.dustin.jex.event.render.EventRenderFeature;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.render.GifDecoder;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

public enum EarsHelper {
    INSTANCE;
    private static final HashMap<String, Identifier> ears = Maps.newHashMap();
    private static final HashMap<String, GifInfo> gifEars = Maps.newHashMap();
    private static boolean selfAnimated;

    @EventPointer
    private final EventListener<EventPlayerEntityTexturedModelData> eventPlayerEntityTexturedModelDataEventListener = new EventListener<>(event -> {
        event.getModelData().getRoot().addChild("ear",
                ModelPartBuilder.create()
                    .uv(0, 0).cuboid(1.5F, -10.5F, -1.0F, 6.0F, 6.0F, 1.0F, event.getDilation(), 14.f / 64.f, 7.f / 64.f)
                    .uv(0, 0).cuboid(-7.5F, -10.5F, -1.0F, 6.0F, 6.0F, 1.0F, event.getDilation(), 14.f / 64.f, 7.f / 64.f),
                ModelTransform.NONE);
    });

    public void setPersonalEars(File file) {
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
                        Identifier id = new Identifier("jex", "ears/self/%s".formatted(i));
                        FileHelper.INSTANCE.applyTexture(id, capeFrame);
                        gifEars.put("self", new GifInfo(decoder, new StopWatch()));
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
                Identifier id = new Identifier("jex", "ears/self.png");
                FileHelper.INSTANCE.applyTexture(id, imgNew);
                if (ears.containsKey("self"))
                    ears.replace("self", id);
                else
                    ears.put("self", id);
                selfAnimated = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void parseEars(String cape, String uuid) {
        byte[] bytes = Base64.decodeBase64(cape);
        GifDecoder decoder = new GifDecoder();
        decoder.read(new ByteArrayInputStream(bytes));
        if (decoder.getFrameCount() > 0) {
            try {
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    BufferedImage frame = decoder.getFrame(i);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(frame, "png", os);
                    InputStream is = new ByteArrayInputStream(os.toByteArray());
                    NativeImage earsFrame = NativeImage.read(is);
                    Identifier id = new Identifier("jex", "ears/%s/%s".formatted(uuid, i));
                    FileHelper.INSTANCE.applyTexture(id, earsFrame);
                }
                JexClient.INSTANCE.getLogger().info("Gif ears loaded. %d frames".formatted(decoder.getFrameCount()));
                gifEars.put(uuid, new GifInfo(decoder, new StopWatch()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        NativeImage capeImage = FileHelper.INSTANCE.readTexture(cape);
        Identifier id = new Identifier("jex", "ears/" + uuid);
        FileHelper.INSTANCE.applyTexture(id, capeImage);
        ears.put(uuid, id);
    }

    public void clear() {
        ears.clear();
        gifEars.clear();
    }

    public boolean hasEars(String uuid) {
        return ears.containsKey(uuid) || gifEars.containsKey(uuid);
    }

    public Identifier getEars(String uuid) {
        if (!hasEars(uuid))
            return null;
        GifInfo gifInfo = gifEars.get(uuid);
        if (uuid.equals("self") && selfAnimated) {
            if (gifInfo.stopWatch.hasPassed(gifInfo.gifDecoder.getDelay(gifInfo.lastFrame))) {
                int current = (gifInfo.lastFrame + 1 > gifInfo.gifDecoder.getFrameCount() - 1) ? 0 : (gifInfo.lastFrame + 1);
                gifInfo.lastFrame = current;
                gifInfo.stopWatch.reset();
                return new Identifier("jex", "ears/self/%s".formatted(current));
            }
            return new Identifier("jex", "ears/self/%s".formatted(gifInfo.lastFrame));
        }
        if (gifEars.containsKey(uuid)) {
            if (gifInfo.stopWatch.hasPassed(gifInfo.gifDecoder.getDelay(gifInfo.lastFrame))) {
                int current = (gifInfo.lastFrame + 1 > gifInfo.gifDecoder.getFrameCount()  - 1) ? 0 : (gifInfo.lastFrame + 1);
                gifInfo.lastFrame = current;
                gifInfo.stopWatch.reset();
                return new Identifier("jex", "ears/%s/%s".formatted(uuid, current));
            }
            return new Identifier("jex", "ears/%s/%s".formatted(uuid, gifInfo.lastFrame));
        }
        return ears.get(uuid);
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
