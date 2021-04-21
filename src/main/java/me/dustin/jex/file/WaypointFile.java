package me.dustin.jex.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.feature.impl.world.Waypoints;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WaypointFile {

    private static String fileName = "Waypoints.json";

    public static void write() {

        JsonArray jsonArray = new JsonArray();
        for (Waypoints.Waypoint waypoint : Waypoints.waypoints) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Server", waypoint.getServer());
            jsonObject.addProperty("Name", waypoint.getName());
            jsonObject.addProperty("X", waypoint.getX());
            jsonObject.addProperty("Y", waypoint.getY());
            jsonObject.addProperty("Z", waypoint.getZ());
            jsonObject.addProperty("Dimension", waypoint.getDimension());
            jsonObject.addProperty("Color", waypoint.getColor());
            jsonArray.add(jsonObject);

            ArrayList<String> stringList = new ArrayList<>();
            for (String s : JsonHelper.INSTANCE.prettyGson.toJson(jsonArray).split("\n")) {
                stringList.add(s);
            }

            try {
                FileHelper.INSTANCE.writeFile(ModFileHelper.INSTANCE.getJexDirectory(), fileName, stringList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void read() {
        try {
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(ModFileHelper.INSTANCE.getJexDirectory(), fileName).getPath()), "UTF8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonArray.class);
            in.close();
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                String server = object.get("Server").getAsString();
                String name = object.get("Name").getAsString();
                float x = Float.parseFloat(object.get("X").getAsString());
                float y = Float.parseFloat(object.get("Y").getAsString());
                float z = Float.parseFloat(object.get("Z").getAsString());
                String dimension = object.get("Dimension").getAsString();
                int color = Integer.parseInt(object.get("Color").getAsString());
                Waypoints.waypoints.add(new Waypoints.Waypoint(name, server, x, y, z, dimension, color));
            }
        } catch (Exception e) {

        }
    }

}
