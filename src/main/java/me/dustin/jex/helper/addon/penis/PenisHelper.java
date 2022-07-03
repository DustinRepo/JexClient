package me.dustin.jex.helper.addon.penis;

import com.google.common.collect.Maps;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.render.EventInitPlayerModel;
import me.dustin.jex.event.render.EventPlayerEntityTexturedModelData;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.render.GifDecoder;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

public enum PenisHelper {
    INSTANCE;
    private static final HashMap<String, Identifier> penises = Maps.newHashMap();
    private static final HashMap<String, GifInfo> gifPenises = Maps.newHashMap();
    private static boolean selfAnimated;
    private ModelPart penis;

    @EventPointer
    private final EventListener<EventPlayerEntityTexturedModelData> eventPlayerEntityTexturedModelDataEventListener = new EventListener<>(event -> {
        event.getModelData().getRoot().addChild("penis",
        ModelPartBuilder.create()//texture scale = image dimensions / skin dimensions (64x64)
                .uv(12, 0).cuboid(-4.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F, event.getDilation(), 24.f / 64.f, 16.f / 64.f)
                .uv(9, 10).cuboid(1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F, event.getDilation(), 24.f / 64.f, 16.f / 64.f)
                .uv(0, 0).cuboid(-1.5F, -10.0F, -1.5F, 3.0F, 10.0F, 3.0F, event.getDilation(), 24.f / 64.f, 16.f / 64.f),
        ModelTransform.NONE);
    });

    @EventPointer
    private final EventListener<EventInitPlayerModel> eventInitPlayerModelEventListener = new EventListener<>(event -> {
        penis = event.getRoot().getChild("penis");
    });

    public void setPersonalPenis(File file) {
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
                        Identifier id = new Identifier("jex", "penises/self/%s".formatted(i));
                        FileHelper.INSTANCE.applyTexture(id, capeFrame);
                        gifPenises.put("self", new GifInfo(decoder, new StopWatch()));
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
                Identifier id = new Identifier("jex", "penises/self.png");
                FileHelper.INSTANCE.applyTexture(id, imgNew);
                if (penises.containsKey("self"))
                    penises.replace("self", id);
                else
                    penises.put("self", id);
                selfAnimated = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void parsePenis(String cape, String uuid) {
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
                    NativeImage penisesFrame = NativeImage.read(is);
                    Identifier id = new Identifier("jex", "penises/%s/%s".formatted(uuid, i));
                    FileHelper.INSTANCE.applyTexture(id, penisesFrame);
                }
                JexClient.INSTANCE.getLogger().info("Gif penis loaded. %d frames".formatted(decoder.getFrameCount()));
                gifPenises.put(uuid, new GifInfo(decoder, new StopWatch()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        NativeImage capeImage = FileHelper.INSTANCE.readTexture(cape);
        Identifier id = new Identifier("jex", "penises/" + uuid);
        FileHelper.INSTANCE.applyTexture(id, capeImage);
        penises.put(uuid, id);
    }

    public void clear() {
        penises.clear();
        gifPenises.clear();
    }

    public boolean hasPenis(String uuid) {
        return penises.containsKey(uuid) || gifPenises.containsKey(uuid);
    }

    public Identifier getPenis(String uuid) {
        if (!hasPenis(uuid))
            return null;
        GifInfo gifInfo = gifPenises.get(uuid);
        if (uuid.equals("self") && selfAnimated) {
            if (gifInfo.stopWatch.hasPassed(gifInfo.gifDecoder.getDelay(gifInfo.lastFrame))) {
                int current = (gifInfo.lastFrame + 1 > gifInfo.gifDecoder.getFrameCount() - 1) ? 0 : (gifInfo.lastFrame + 1);
                gifInfo.lastFrame = current;
                gifInfo.stopWatch.reset();
                return new Identifier("jex", "penises/self/%s".formatted(current));
            }
            return new Identifier("jex", "penises/self/%s".formatted(gifInfo.lastFrame));
        }
        if (gifPenises.containsKey(uuid)) {
            if (gifInfo.stopWatch.hasPassed(gifInfo.gifDecoder.getDelay(gifInfo.lastFrame))) {
                int current = (gifInfo.lastFrame + 1 > gifInfo.gifDecoder.getFrameCount()  - 1) ? 0 : (gifInfo.lastFrame + 1);
                gifInfo.lastFrame = current;
                gifInfo.stopWatch.reset();
                return new Identifier("jex", "penises/%s/%s".formatted(uuid, current));
            }
            return new Identifier("jex", "penises/%s/%s".formatted(uuid, gifInfo.lastFrame));
        }
        return penises.get(uuid);
    }

    public void renderPenis(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        penis.render(matrices, vertices, light, overlay);
    }

    public ModelPart getPenis() {
        return penis;
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
