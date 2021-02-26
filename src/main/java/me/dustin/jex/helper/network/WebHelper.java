package me.dustin.jex.helper.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public enum WebHelper {
    INSTANCE;

    public String readURL(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder buffer = new StringBuilder();
        for (String line; (line = input.readLine()) != null; ) {
            buffer.append(line);
            buffer.append("\n");
        }
        input.close();
        return buffer.toString();
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
