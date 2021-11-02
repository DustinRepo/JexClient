package me.dustin.jex.frame;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JexFrame {
    private JFrame frame;
    private JPanel panel;
    private JButton loadOptifineSupportButton;
    private JLabel warnLabel1;
    private JLabel warnLabel2;

    public JexFrame() {
        this.frame = new JFrame("Jex Client");
        this.frame.setSize(500, 120);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setVisible(true);
        this.frame.setResizable(true);
        this.frame.setLocationRelativeTo(null);
        this.warnLabel1 = new JLabel("This screen is for changing Jex to support Optifine.");
        this.warnLabel2 = new JLabel("If you just want to run Jex normally, drop this jar into your mods folder with Fabric-API.");
        this.loadOptifineSupportButton = new JButton("Force Optifine Support");

        this.panel = new JPanel();
        this.panel.setSize(this.frame.getSize());
        this.panel.add(warnLabel1);
        this.panel.add(warnLabel2);
        this.panel.add(loadOptifineSupportButton);
        this.frame.add(this.panel);
        this.frame.show();

        loadOptifineSupportButton.addActionListener(e -> {
            try {
                File thisjar = new File(JexFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File tempMixinsFile = new File("jex.mixins.json");
                extractMixinsFile(thisjar, tempMixinsFile);
                Thread.sleep(250);
                String jsondata = readFile(tempMixinsFile);
                System.out.println("jsonData from file: " + jsondata);
                if (jsondata.contains("\"minecraft.MixinShader\"")) {
                    jsondata = jsondata.replace("\"minecraft.MixinShader\"", "\"minecraft.MixinShaderWithOptifine\"");
                }
                ArrayList<String> stringList = new ArrayList<>(Arrays.asList(jsondata.split("\n")));
                writeFile(tempMixinsFile, stringList);

                System.out.println("Rezipping: " + jsondata);
                reZip(thisjar.getAbsolutePath());
                tempMixinsFile.delete();
                JOptionPane.showMessageDialog(frame, "Done! There is now a JexClient-Optifine.jar in the same folder as this jar.\nYou can drop that in your mods folder. Don't forget to also put the Optifabric and Optifine jars in there too!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void reZip(String zipFilePath) {
        File tempMixinsFile = new File("jex.mixins.json");
        try (ZipFile srcFile = new ZipFile(zipFilePath)) {
            try (ZipOutputStream destFile = new ZipOutputStream(Files.newOutputStream(Paths.get(new File("JexClient-Optifine.jar").toURI())))) {
                Enumeration<? extends ZipEntry> entries = srcFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry src = entries.nextElement();
                    ZipEntry dest = new ZipEntry(src.getName());
                    destFile.putNextEntry(dest);


                    if (src.getName().contains("jex.mixins.json")) {
                        try (InputStream content = new FileInputStream(tempMixinsFile)) {
                            content.transferTo(destFile);
                        }
                    } else {
                        try (InputStream content = srcFile.getInputStream(src)) {
                            content.transferTo(destFile);
                        }
                    }
                    destFile.closeEntry();
                }
                destFile.finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void extractMixinsFile(File file, File tempMixinsFile) {
        byte[] buffer = new byte[1024];
        try {
            if (!tempMixinsFile.exists())
                tempMixinsFile.createNewFile();
            FileInputStream fileInputStream = new FileInputStream(file);
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry ze = zipInputStream.getNextEntry();
            while (ze != null) {
                if (ze.getName().contains("jex.mixins.json")) {
                    FileOutputStream fos = new FileOutputStream(tempMixinsFile);
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                ze = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch (Exception ex) {

        }
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
            content.forEach(string -> {
                stringBuilder.append(string + "\r\n");
            });
            printWriter.print(stringBuilder);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        if (System.getProperty("os.name").contains("linux")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }
        new JexFrame();
    }
}
