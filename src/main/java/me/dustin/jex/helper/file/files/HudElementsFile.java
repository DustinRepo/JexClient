package me.dustin.jex.helper.file.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.feature.mod.impl.render.hud.Hud;
import me.dustin.jex.feature.mod.impl.render.hud.elements.HudElement;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.util.math.MathHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class HudElementsFile {

    private static String fileName = "HudElements.json";

    public static void write() {
        JsonArray jsonArray = new JsonArray();
        for (HudElement hudElement : HudElement.getHud().hudElements) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Name", hudElement.getName());
            jsonObject.addProperty("X", hudElement.isLeftSide() ? hudElement.getX() : hudElement.getX() + hudElement.getWidth() - hudElement.getMinWidth());
            jsonObject.addProperty("Y", hudElement.isTopSide() ? hudElement.getY() : hudElement.getY() + hudElement.getHeight() - hudElement.getMinHeight());
            jsonObject.addProperty("TopSide", hudElement.isTopSide());
            jsonObject.addProperty("LeftSide", hudElement.isLeftSide());
            jsonObject.addProperty("Width", hudElement.getWidth());
            jsonObject.addProperty("Height", hudElement.getHeight());
            jsonArray.add(jsonObject);

            ArrayList<String> stringList = new ArrayList<>(Arrays.asList(JsonHelper.INSTANCE.prettyGson.toJson(jsonArray).split("\n")));

            try {
                FileHelper.INSTANCE.writeFile(ModFileHelper.INSTANCE.getJexDirectory(), fileName, stringList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void read(Hud hud) {
        File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), fileName);
        if (!file.exists())
            return;
        try {
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
                HudElement hudElement = hud.getElement(name);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
