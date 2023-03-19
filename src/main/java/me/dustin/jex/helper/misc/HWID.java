package me.dustin.jex.helper.misc;

import org.apache.commons.codec.digest.DigestUtils;

import java.net.InetAddress;

public enum HWID {
    INSTANCE;

    private String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
        }
        return "UNKNOWN";
   }

    public String getHWID() {
       return DigestUtils.md5Hex(System.getenv("JAVA_HOME"));
   }
}
