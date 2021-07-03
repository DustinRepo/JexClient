package me.dustin.jex.helper.file;


import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public enum FileHelper {

    INSTANCE;

    public List<String> readFile(File path, String name) {
        File file = new File(path, name);
        ArrayList<String> s = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String inString;
            while ((inString = in.readLine()) != null) {
                s.add(inString);
            }
            in.close();
            return s;
        } catch (IOException e) {
            createFile(path, name);
        }
        return new ArrayList<>();
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

    public void writeFile(File path, String name, List<String> content) {
        try {
            File file = new File(path, name);
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

    public void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            e.printStackTrace();
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
