package me.dustin.jex.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.helper.file.YamlHelper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "Friends.yml", folder = "config")
public class FriendFile extends ConfigFile {

	@Override
	public void write() {
		Map<String, Object> yamlMap = new HashMap<>();
		FriendHelper.INSTANCE.getFriendsList().forEach(friend -> {
			Map<String, Object> friendData = new HashMap<>();
			friendData.put("alias", friend.alias());
			yamlMap.put(friend.name(), friendData);
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
			Map<String, Object> friendData = (Map<String, Object>) o;
			String alias = (String)friendData.get("alias");
			FriendHelper.INSTANCE.addFriend(new FriendHelper.Friend(s, alias));
		});
	}

	public void convertJson() {
		File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Friends.json");
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
				String name = object.get("name").getAsString();
				String nickname = object.get("nickname").getAsString();
				FriendHelper.INSTANCE.addFriend(name, nickname);
			}
		} catch (Exception e) {

		}
		file.delete();
		write();
	}

}
