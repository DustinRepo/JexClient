package me.dustin.jex.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.feature.mod.impl.render.hud.elements.HudElement;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.util.math.MathHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "HudElements.yml", bootRead = false, folder = "config")
public class HudElementsFile extends ConfigFile {

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        HudElement.getHud().hudElements.forEach(hudElement -> {
            Map<String, Object> elementData = new HashMap<>();
            elementData.put("X", hudElement.isLeftSide() ? hudElement.getX() : hudElement.getX() + hudElement.getWidth() - hudElement.getMinWidth());
            elementData.put("Y", hudElement.isTopSide() ? hudElement.getY() : hudElement.getY() + hudElement.getHeight() - hudElement.getMinHeight());
            elementData.put("TopSide", hudElement.isTopSide());
            elementData.put("LeftSide", hudElement.isLeftSide());
            elementData.put("Width", hudElement.getWidth());
            elementData.put("Height", hudElement.getHeight());
            yamlMap.put(hudElement.getName(), elementData);
        });
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    @Override
    public void read() {
        convertJson();
        Map<String, Object> parsedYaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedYaml == null || parsedYaml.isEmpty())
            return;
        parsedYaml.forEach((s, o) -> {
            HudElement hudElement = HudElement.getHud().getElement(s);
            if (hudElement == null)
                return;
            Map<String, Object> elementData = (Map<String, Object>) o;
            float x = (float)(double)elementData.get("X");
            float y = (float)(double)elementData.get("Y");
            boolean topSide = (boolean)elementData.get("TopSide");
            boolean leftSide = (boolean)elementData.get("LeftSide");
            hudElement.setX(x);
            hudElement.setY(y);
            hudElement.setLeftSide(leftSide);
            hudElement.setTopSide(topSide);
            if (elementData.get("Height") != null) {
                float height = (float)(double)elementData.get("Height");
                float width = (float)(double)elementData.get("Width");
                hudElement.setWidth(width);
                hudElement.setHeight(height);

                if (!hudElement.isLeftSide()) {
                    if (hudElement.getLastWidth() > width) {
                        float dif = hudElement.getLastWidth() - width;
                        x += dif;
                        hudElement.setX(x);
                        hudElement.setLastX(x);
                        hudElement.setLastWidth(width);
                    } else if (hudElement.getLastWidth() < width) {
                        float dif = width - hudElement.getLastWidth();
                        x -= dif;
                        hudElement.setX(x);
                        hudElement.setLastX(x);
                        hudElement.setLastWidth(width);
                    }
                }
                if (!hudElement.isTopSide()) {
                    if (hudElement.getLastHeight() > height) {
                        float dif = hudElement.getLastHeight() - height;
                        y += dif;
                        hudElement.setY(y);
                        hudElement.setLastY(y);
                        hudElement.setLastHeight(height);
                    } else if (hudElement.getLastHeight() < height) {
                        float dif = height - hudElement.getLastHeight();
                        y -= dif;
                        hudElement.setY(y);
                        hudElement.setLastY(y);
                        hudElement.setLastHeight(height);
                    }
                }
                hudElement.setLastWidth(hudElement.getWidth());
                hudElement.setLastHeight(hudElement.getHeight());
                hudElement.setX(MathHelper.clamp(hudElement.getX(), 0, Render2DHelper.INSTANCE.getScaledWidth() - hudElement.getWidth()));
                hudElement.setY(MathHelper.clamp(hudElement.getY(), 0, Render2DHelper.INSTANCE.getScaledHeight() - hudElement.getHeight()));
                hudElement.setLastX(hudElement.getX());
                hudElement.setLastY(hudElement.getY());
            }
        });
    }

    public void convertJson() {
        try {
            File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "HudElements.json");
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath()), "UTF8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonArray.class);
            in.close();
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                String name = object.get("Name").getAsString();
                float x = object.get("X").getAsFloat();
                float y = object.get("Y").getAsFloat();
                boolean topSide = object.get("TopSide").getAsBoolean();
                boolean leftSide = object.get("LeftSide").getAsBoolean();
                HudElement hudElement = HudElement.getHud().getElement(name);
                if (hudElement != null) {
                    hudElement.setX(x);
                    hudElement.setY(y);
                    hudElement.setLeftSide(leftSide);
                    hudElement.setTopSide(topSide);
                    if (object.get("Height") != null) {
                        float width = object.get("Width").getAsFloat();
                        float height = object.get("Height").getAsFloat();
                        hudElement.setWidth(width);
                        hudElement.setHeight(height);

                        if (!hudElement.isLeftSide()) {
                            if (hudElement.getLastWidth() > width) {
                                float dif = hudElement.getLastWidth() - width;
                                x += dif;
                                hudElement.setX(x);
                                hudElement.setLastX(x);
                                hudElement.setLastWidth(width);
                            } else if (hudElement.getLastWidth() < width) {
                                float dif = width - hudElement.getLastWidth();
                                x -= dif;
                                hudElement.setX(x);
                                hudElement.setLastX(x);
                                hudElement.setLastWidth(width);
                            }
                        }
                        if (!hudElement.isTopSide()) {
                            if (hudElement.getLastHeight() > height) {
                                float dif = hudElement.getLastHeight() - height;
                                y += dif;
                                hudElement.setY(y);
                                hudElement.setLastY(y);
                                hudElement.setLastHeight(height);
                            } else if (hudElement.getLastHeight() < height) {
                                float dif = height - hudElement.getLastHeight();
                                y -= dif;
                                hudElement.setY(y);
                                hudElement.setLastY(y);
                                hudElement.setLastHeight(height);
                            }
                        }
                        hudElement.setLastWidth(hudElement.getWidth());
                        hudElement.setLastHeight(hudElement.getHeight());
                        hudElement.setX(MathHelper.clamp(hudElement.getX(), 0, Render2DHelper.INSTANCE.getScaledWidth() - hudElement.getWidth()));
                        hudElement.setY(MathHelper.clamp(hudElement.getY(), 0, Render2DHelper.INSTANCE.getScaledHeight() - hudElement.getHeight()));
                        hudElement.setLastX(hudElement.getX());
                        hudElement.setLastY(hudElement.getY());
                    }
                }
            }
            file.delete();
            write();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
