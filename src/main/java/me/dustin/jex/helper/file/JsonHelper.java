package me.dustin.jex.helper.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public enum JsonHelper {
    INSTANCE;
    public final Gson gson = new Gson();
    public final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

}
