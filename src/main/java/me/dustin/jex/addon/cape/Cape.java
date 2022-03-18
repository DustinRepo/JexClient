package me.dustin.jex.addon.cape;

import com.google.common.collect.Maps;
import me.dustin.jex.helper.file.FileHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

public class Cape {

    public static HashMap<String, Identifier> capes = Maps.newHashMap();

    public static void setPersonalCape(File file) {
        if (!file.exists())
            return;
        try {
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
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseCape(String cape, String uuid) {
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

}
