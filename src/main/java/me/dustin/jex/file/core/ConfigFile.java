package me.dustin.jex.file.core;

import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.ModFileHelper;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class ConfigFile {

    private final File file;
    private final boolean bootRead;

    public ConfigFile() {
        String fileName = this.getClass().getAnnotation(CFG.class).fileName();
        String folder = this.getClass().getAnnotation(CFG.class).folder();
        File parentFolder = folder.isEmpty() ? ModFileHelper.INSTANCE.getJexDirectory() : new File(ModFileHelper.INSTANCE.getJexDirectory(), folder);
        if (!parentFolder.exists())
            parentFolder.mkdirs();
        this.file = new File(parentFolder, fileName);
        this.bootRead = this.getClass().getAnnotation(CFG.class).bootRead();
        if (!file.exists())
            FileHelper.INSTANCE.createFile(parentFolder, fileName);
    }

    public abstract void read();
    public abstract void write();

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CFG {
        String fileName();
        String folder() default "";
        boolean bootRead() default true;
    }

    public File getFile() {
        return file;
    }

    public boolean doesReadOnBoot() {
        return bootRead;
    }
}
