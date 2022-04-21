package me.dustin.jex.helper.network;

import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

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

    public HttpResponse httpRequest(String url, Object data, Map<String, String> headers, String requestMethod) {
        try {
            URL theURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) theURL.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setConnectTimeout(10 * 1000);
            connection.setDoInput(true);
            if (headers != null)
                headers.forEach(connection::setRequestProperty);
            if (data != null) {
                connection.setDoOutput(true);
                byte[] bytes = new byte[0];
                if (data instanceof Map<?, ?> m) {
                    String encoded = encode((Map<Object, Object>) m);
                    bytes = encoded.getBytes();
                } else if (data instanceof String s) {
                    bytes = s.getBytes();
                }
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(bytes);
                }
            }
            StringBuilder sb = new StringBuilder();
            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                for (String line; (line = input.readLine()) != null; ) {
                    sb.append(line);
                    sb.append("\n");
                }
            }
            connection.disconnect();
            return new HttpResponse(sb.toString(), code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HttpResponse("", 404);
    }

    private String encode(Map<Object, Object> map) {
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<?, ?> entry : map.entrySet())
            sj.add(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        return sj.toString();
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
                String imgurApi = "https://api.imgur.com/3/image";
                Map<String, String> map = new HashMap<>();
                map.put("image", encodedImage);
                map.put("title", "Jex Screenshot");
                Map<String, String> requestProperties = new HashMap<>();
                requestProperties.put("Authorization", "Client-ID 57e0280fe5e3a5e");
                requestProperties.put("Content-Type", "application/x-www-form-urlencoded");
                String resp = httpRequest(imgurApi, map, requestProperties, "POST").data;

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
        if (os.contains("win")) {
            Runtime rt = Runtime.getRuntime();
            rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
        } else if (os.contains("mac")) {
            Runtime rt = Runtime.getRuntime();
            rt.exec("open " + url);
        } else if (os.contains("nix") || os.contains("nux")) {
            Runtime rt = Runtime.getRuntime();
            String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links", "lynx"};

            StringBuilder cmd = new StringBuilder();
            for (int i = 0; i < browsers.length; i++)
                if (i == 0)
                    cmd.append(String.format("%s \"%s\"", browsers[i], url));
                else
                    cmd.append(String.format(" || %s \"%s\"", browsers[i], url));

            rt.exec(new String[]{"sh", "-c", cmd.toString()});

        }
    }

    public record HttpResponse(String data, int responseCode){}
}
