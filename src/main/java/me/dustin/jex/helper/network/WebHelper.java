package me.dustin.jex.helper.network;

import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.codec.binary.Base64;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public enum WebHelper {
    INSTANCE;

    public String readURL(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10 * 1000);
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder buffer = new StringBuilder();
        for (String line; (line = input.readLine()) != null; ) {
            buffer.append(line);
            buffer.append("\n");
        }
        input.close();
        return buffer.toString();
    }

    public String sendPOST(URL url, Map<?, ?> args) {
        return sendPOST(url, args, new HashMap<>());
    }

    public String sendPOST(URL url, Map<?, ?> args, Map<?, ?> requestProperties) {
        String response = "";
        try {
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<?, ?> entry : args.entrySet())
                sj.add(URLEncoder.encode(entry.getKey().toString(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8"));

            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            if (requestProperties.isEmpty()) {
                http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            } else {
                for (Object key : requestProperties.keySet()) {
                    String str = (String)key;
                    String str2 = (String)requestProperties.get(key);
                    http.setRequestProperty(str, str2);
                }
            }
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(http.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            for (String line; (line = input.readLine()) != null; ) {
                buffer.append(line);
                buffer.append("\n");
            }
            input.close();
            response = buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public void uploadToImgur(File file) {
        ChatHelper.INSTANCE.addClientMessage("Uploading image to Imgur");
        if (!file.exists()) {
            JexClient.INSTANCE.getLogger().info("file does not exist");
            return;
        }
        new Thread(() -> {
            try {
                FileInputStream fileInputStreamReader = new FileInputStream(file);
                byte[] bytes = new byte[(int)file.length()];
                fileInputStreamReader.read(bytes);
                String encodedImage = Base64.encodeBase64String(bytes);
                fileInputStreamReader.close();
                encodedImage = encodedImage.replace(System.lineSeparator(), "");
                final URL imgurApi = new URL("https://api.imgur.com/3/image");
                Map<String, String> map = new HashMap<>();
                map.put("image", encodedImage);
                map.put("title", "Jex Screenshot");
                Map<String, String> requestProperties = new HashMap<>();
                requestProperties.put("Authorization", "Client-ID 57e0280fe5e3a5e");
                requestProperties.put("Content-Type", "application/x-www-form-urlencoded");
                String resp = sendPOST(imgurApi, map, requestProperties);

                JsonObject responseJson = JsonHelper.INSTANCE.gson.fromJson(resp, JsonObject.class);
                JsonObject data = responseJson.getAsJsonObject("data");
                String id = data.get("id").getAsString();
                String imgURL = "http://i.imgur.com/" + id + ".png";

                Wrapper.INSTANCE.getMinecraft().keyboard.setClipboard(imgURL);
                ChatHelper.INSTANCE.addClientMessage("URL: \247b" + imgURL + " \2477Copied to clipboard");
            } catch (Exception e) {
                ChatHelper.INSTANCE.addClientMessage("Error");
                e.printStackTrace();
            }
            if (!file.delete()) {
                ChatHelper.INSTANCE.addClientMessage("Couldn't delete file");
            }
        }).start();
    }

    public boolean openLink(String url) {
        try {
            openLinkOnOS(url);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openLinkOnOS(String url) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            Runtime rt = Runtime.getRuntime();
            rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
        } else if (os.indexOf("mac") >= 0) {
            Runtime rt = Runtime.getRuntime();
            rt.exec("open " + url);
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            Runtime rt = Runtime.getRuntime();
            String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
                    "netscape", "opera", "links", "lynx"};

            StringBuffer cmd = new StringBuffer();
            for (int i = 0; i < browsers.length; i++)
                if (i == 0)
                    cmd.append(String.format("%s \"%s\"", browsers[i], url));
                else
                    cmd.append(String.format(" || %s \"%s\"", browsers[i], url));

            rt.exec(new String[]{"sh", "-c", cmd.toString()});

        }
    }

}
