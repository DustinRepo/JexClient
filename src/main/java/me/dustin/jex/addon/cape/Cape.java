package me.dustin.jex.addon.cape;

import com.google.common.collect.Maps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

public class Cape {

    public static HashMap<String, Identifier> capes = Maps.newHashMap();
    public static String s = "6";

    public static void parseCape(String cape, String uuid) {
        NativeImage capeImage = readTexture(cape);
        int imageWidth = 64;
        int imageHeight = 32;

        for (int srcWidth = capeImage.getWidth(), srcHeight = capeImage.getHeight(); imageWidth < srcWidth || imageHeight < srcHeight; ) {
            imageWidth *= 2;
            imageHeight *= 2;
        }

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < capeImage.getWidth(); x++) {
            for (int y = 0; y < capeImage.getHeight(); y++) {
                imgNew.setPixelColor(x, y, capeImage.getPixelColor(x, y));
            }
        }

        capeImage.close();
        Identifier id = new Identifier("jex", "capes/" + uuid);
        applyTexture(id, imgNew);
        capes.put(uuid, id);
    }

    private static NativeImage readTexture(String textureBase64) {
        try {
            byte[] imgBytes = Base64.decodeBase64(textureBase64);
            ByteArrayInputStream bias = new ByteArrayInputStream(imgBytes);
            return NativeImage.read(bias);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    private static void applyTexture(Identifier identifier, NativeImage nativeImage) {
        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(nativeImage)));
    }

}
