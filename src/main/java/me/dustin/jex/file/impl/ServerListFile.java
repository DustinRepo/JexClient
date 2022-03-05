package me.dustin.jex.file.impl;

import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.network.ServerScrubber;

@ConfigFile.CFG(fileName = "scrub.txt", folder = "")
public class ServerListFile extends ConfigFile {

    @Override
    public void read() {
        if (!getFile().exists()) {
            ServerScrubber.INSTANCE.loadDefaultList();
            write();
            return;
        }

        ServerScrubber.INSTANCE.getServers().clear();
        String fileStr = FileHelper.INSTANCE.readFile(getFile());
        for (String ip : fileStr.split("\n")) {
            ServerScrubber.INSTANCE.getServers().add(ip);
        }
    }

    @Override
    public void write() {
        FileHelper.INSTANCE.writeFile(getFile(), ServerScrubber.INSTANCE.getServers());
    }
}
