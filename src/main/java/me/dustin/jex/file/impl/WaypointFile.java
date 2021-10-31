package me.dustin.jex.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.feature.mod.impl.world.Waypoints;
import me.dustin.jex.helper.file.YamlHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "Waypoints.yml", folder = "config")
public class WaypointFile extends ConfigFile{

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        Waypoints.waypoints.forEach(waypoint -> {
            Map<String, Object> waypointData = new HashMap<>();
            waypointData.put("Server", waypoint.getServer());
            waypointData.put("X", waypoint.getX());
            waypointData.put("Y", waypoint.getY());
            waypointData.put("Z", waypoint.getZ());
            waypointData.put("Dimension", waypoint.getDimension());
            waypointData.put("Color", waypoint.getColor());
            waypointData.put("Hidden", waypoint.isHidden());
            waypointData.put("Beacon", waypoint.isDrawBeacon());
            waypointData.put("Tracer", waypoint.isDrawTracer());
            waypointData.put("Nametag", waypoint.isDrawNametag());
            yamlMap.put(waypoint.getName(), waypointData);
        });
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    @Override
    public void read() {
        convertJson();
        Map<String, Object> parsedyaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedyaml == null || parsedyaml.isEmpty())
            return;
        parsedyaml.forEach((s, o) -> {
            Map<String, Object> waypointData = (Map<String, Object>) o;
            String server = (String)waypointData.get("Server");
            String dimension = (String)waypointData.get("Dimension");
            boolean hidden = (boolean)waypointData.get("Hidden");
            boolean nametag = (boolean)waypointData.get("Nametag");
            boolean beacon = (boolean)waypointData.get("Beacon");
            boolean tracer = (boolean)waypointData.get("Tracer");
            float x = (float)(double)waypointData.get("X");
            float y = (float)(double)waypointData.get("Y");
            float z = (float)(double)waypointData.get("Z");
            int color = Integer.parseInt(String.valueOf(waypointData.get("Color")));
            Waypoints.Waypoint waypoint = new Waypoints.Waypoint(s, server, x, y, z, dimension, color);
            waypoint.setHidden(hidden);
            waypoint.setDrawNametag(nametag);
            waypoint.setDrawBeacon(beacon);
            waypoint.setDrawTracer(tracer);
            Waypoints.waypoints.add(waypoint);
        });
    }

    private void convertJson() {
        File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Waypoints.json");
        try {
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
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
                boolean hidden = object.get("Hidden").getAsBoolean();
                boolean nametag = object.get("Nametag").getAsBoolean();
                boolean beacon = object.get("Beacon").getAsBoolean();
                boolean tracer = object.get("Tracer").getAsBoolean();
                Waypoints.Waypoint waypoint = new Waypoints.Waypoint(name, server, x, y, z, dimension, color);
                waypoint.setHidden(hidden);
                waypoint.setDrawNametag(nametag);
                waypoint.setDrawBeacon(beacon);
                waypoint.setDrawTracer(tracer);
                Waypoints.waypoints.add(waypoint);
            }
            file.delete();
            write();
        } catch (Exception e) {

        }
    }

}
