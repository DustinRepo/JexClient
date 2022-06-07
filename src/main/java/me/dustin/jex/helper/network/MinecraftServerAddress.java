package me.dustin.jex.helper.network;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class MinecraftServerAddress {

    private final String ip;
    private final int port;

    public MinecraftServerAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static MinecraftServerAddress resolve(String ip, int port) {
        InitialDirContext ctx;
        try {
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable<String, String> hashtable = new Hashtable();
            hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            hashtable.put("java.naming.provider.url", "dns:");
            hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
            ctx = new InitialDirContext(hashtable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (port == 25565) {
            try {
                Attributes attributes = ctx.getAttributes("_minecraft._tcp." + ip, new String[]{"SRV"});
                Attribute attribute = attributes.get("srv");
                if (attribute != null) {
                    String[] strings = attribute.get().toString().split(" ", 4);
                    try {
                        port = Integer.parseInt(strings[2]);
                    } catch (NumberFormatException numberFormatException) {}
                    return new MinecraftServerAddress(strings[3], port);
                }
            } catch (Throwable var5) {
            }
        }
        return new MinecraftServerAddress(ip, port);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
