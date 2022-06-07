package me.dustin.jex.helper.file;


import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public enum FileHelper {

    INSTANCE;

    public NativeImage readTexture(String textureBase64) {
        try {
            byte[] imgBytes = Base64.decodeBase64(textureBase64);
            ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes);
            return NativeImage.read(bais);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    public void applyTexture(Identifier identifier, NativeImage nativeImage) {
        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(nativeImage)));
    }

    public String imageToBase64String(BufferedImage image, String type) {
        String ret;
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

    public String readFile(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String inString;
            while ((inString = in.readLine()) != null) {
                sb.append(inString);
                sb.append("\n");
            }
            in.close();
            return sb.toString();
        } catch (IOException e) {
        }
        return "";
    }

    public void writeFile(File file, List<String> content) {
        try {
            PrintWriter printWriter = new PrintWriter(file);
            StringBuilder stringBuilder = new StringBuilder();
            content.forEach(string -> stringBuilder.append(string).append("\r\n"));
            printWriter.print(stringBuilder);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createFile(File path, String name) {
        try {
            File file = new File(path, name);
            if (!file.exists()) {
                PrintWriter printWriter = new PrintWriter(new FileWriter(file));
                printWriter.println();
                printWriter.close();
            }
            FileWriter fw = new FileWriter(file);
            fw.close();
        } catch (Exception e) {
        }
    }

    public void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis = null;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);

                // create all non exists folders
                // else you will hit FileNotFoundException for compressed folder
                if (ze.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    if (!newFile.exists())
                        newFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (fis != null)
                try {
                    fis.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
        }

    }

}
