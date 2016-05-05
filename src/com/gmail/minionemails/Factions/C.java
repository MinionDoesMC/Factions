package com.gmail.minionemails.Factions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

public class C {

	public static List<String> factionIndex = new ArrayList<String>();
	public static List<UUID> playerIndex = new ArrayList<UUID>();
	public static List<String> boardIndex = new ArrayList<String>();
	public static List<String> playerIsIn_player = new ArrayList<String>();
	public static List<String> playerIsIn_faction = new ArrayList<String>();
	public static List<Location> playerIsIn_location = new ArrayList<Location>();
	public static JSONObject boardData = new JSONObject();
	public static JSONObject factionData = new JSONObject();
	public static JSONObject playerData = new JSONObject();
	public static JSONArray enemyData = new JSONArray();
	public static JSONArray allyData = new JSONArray();
	public static JSONArray truceData = new JSONArray();
	public static JSONArray inviteData = new JSONArray();

	private static boolean isRedirected(Map<String, List<String>> header) {
		for (String hv : header.get(null)) {
			if ((hv.contains(" 301 ")) || (hv.contains(" 302" ))) {
				return true;
			}
		}
		return false;
	}
	
	public static String getOnlineVersion() throws Throwable
	{
		String link = ""; //TODO: Add project to Github
		URL url = new URL(link);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
		http.setRequestProperty("User-Agen", "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");
		
		Map<String, List<String>> header = http.getHeaderFields();
		while (isRedirected(header)) {
			link = (String)((List<String>)header.get("Location")).get(0);
			url = new URL(link);
			http = (HttpURLConnection)url.openConnection();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
		String res = "";
		String someLine = "";
		
		while ((someLine = br.readLine()) != null) {
			res = res + someLine;
		}
		JSONObject onlineConfig = new JSONObject(res);
		
		String onlineVersion = "NULL";
		if (onlineConfig.has("pluginVersion")) {
			onlineVersion = onlineConfig.getString("pluginVersion");
		}
		br.close();
		return onlineVersion;
	}
	
	public static void checkForUpdates() {
		try
		{
			String onlineVersion = getOnlineVersion();
			if (!onlineVersion.equalsIgnoreCase(Factions.version)) {
				Bukkit.getConsoleSender().sendMessage("§c#############################################");
		        Bukkit.getConsoleSender().sendMessage("  ");
		        Bukkit.getConsoleSender().sendMessage("§c§lYOUR FACTIONS PLUGIN MIGHT BE OUT OF DATE.");
		        Bukkit.getConsoleSender().sendMessage("  ");
		        Bukkit.getConsoleSender().sendMessage("§cYour version: §c§l" + Factions.version + " §cGithub version: §c§l" + onlineVersion);
		        Bukkit.getConsoleSender().sendMessage("  ");
		        Bukkit.getConsoleSender().sendMessage("§c§lPLEASE GO TO ONE OF THE FOLLOWING URLS TO UPDATE IT!");
		        Bukkit.getConsoleSender().sendMessage("  ");
		        Bukkit.getConsoleSender().sendMessage("  §4 http://dev.bukkit.org/bukkit-plugins/simple-factions/");
		        Bukkit.getConsoleSender().sendMessage("  §4 http://www.spigotmc.org/threads/simplefactions.36766/");
		        Bukkit.getConsoleSender().sendMessage("  ");
		        Bukkit.getConsoleSender().sendMessage("§c#############################################");
			}
		}
		catch (MalformedURLException e) {
			Bukkit.getConsoleSender().sendMessage("§c[Factions] §c§lERROR§c: Malformed URL decteted while checking for updates");
		}
		catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage("§c[Factions] §c§lERROR§c: I/O Exception detected while checking for updates. Are you online?");
		}
		catch (Throwable e) {
			Bukkit.getConsoleSender().sendMessage("§c[Faction] §c§lERROR§c: Error checking for updates. You might be offline or Github might be experiencing heavy traffic.");
		}
	}
	
	public static void createDirectories() {
		File dir0 = new File(Factions.getInstance().getDataFolder() + "/");
		if (!dir0.exists()) {
			dir0.mkdir();
		}
		File fac = new File(Factions.getInstance().getDataFolder() + "/factionData");
		if (!fac.exists()) {
			fac.mkdir();
		}
		File player = new File(Factions.getInstance().getDataFolder() + "/playerData");
		if (!player.exists()) {
			player.mkdir();
		}
		File world = new File(Factions.getInstance().getDataFolder() + "/worldData");
		if (!world.exists()) {
			world.mkdir();
		}
	}

	public static void loadData() {
		createDirectories();

		Config.loadConfig();

		FileUtil fU = new FileUtil();

		factionIndex = new ArrayList<String>();
		playerIndex = new ArrayList<UUID>();
		boardIndex = new ArrayList<String>();

		List<String> factionIndexList = Arrays.asList(fU.listFiles(Factions.getInstance().getDataFolder() + "/factionData"));
		List<String> playerIndexList = Arrays.asList(fU.listFiles(Factions.getInstance().getDataFolder() + "/playerData"));
		List<String> boardIndexList = Arrays.asList(fU.listFiles(Factions.getInstance().getDataFolder() + "/worldData"));

		for (int i = 0; i < playerIndexList.size(); i++) {
			UUID uuid = UUID.fromString(((String) playerIndexList.get(i)).replaceAll(".json", ""));
			playerIndex.add(uuid);
		}
		Bukkit.getServer().getConsoleSender().sendMessage("§a  -Loaded all the players");
		for (int i = 0; i < factionIndexList.size(); i++) {
			String uuid = ((String) factionIndexList.get(i)).replaceFirst(".json", "");

			loadFaction(uuid);
			factionIndex.add(factionData.getString("name"));

			boolean exists = false;
			for (int j = 0; j < Data.Players.length(); j++) {
				if (Data.Players.getJSONObject(j).getString("faction")
						.equalsIgnoreCase(factionData.getString("name"))) {
					exists = true;
				}
			}
			if (!exists) {
				Bukkit.getServer().getConsoleSender().sendMessage(" §c    -> There are no players in the faction §4"
						+ factionData.getString("name") + "§c Removing the faction...");
				deleteFaction(factionData.getString("name"));
			}
		}
		Bukkit.getServer().getConsoleSender().sendMessage("§a  -Loaded all the factions");
		for (int i = 0; i < boardIndexList.size(); i++) {
			String name = ((String) boardIndexList.get(i)).replaceFirst(".json", "");
			Bukkit.getServer().getConsoleSender().sendMessage(" §a   ->Loading §f§l " + name);
			boardIndex.add(name);
		}
		Bukkit.getServer().getConsoleSender().sendMessage("§a  -Loaded all the worlds");
	}

	public static void loadPlayer(String name) {
		OfflinePlayer[] arrayOfOfflinePlayer;
		int j = (arrayOfOfflinePlayer = Bukkit.getOfflinePlayers()).length;
		for (int i = 0; i < j; i++) {
			OfflinePlayer player = arrayOfOfflinePlayer[i];
			if (player.getName().equalsIgnoreCase(name)) {
				loadPlayer(player.getUniqueId());
			}
		}
	}

	public static void loadPlayer(UUID uuid) {
		for (int i = 0; i < Data.Players.length(); i++) {
			if (Data.Players.getJSONObject(i).getString("ID").equalsIgnoreCase(uuid.toString())) {
				playerData = Data.Players.getJSONObject(i);
				return;
			}
		}
	}

	public static void loadPlayerDisk(String uuid) {
		createDirectories();

		File playerFile = new File(Factions.getInstance().getDataFolder() + "/playerData/" + uuid + ".json");
		if (!playerFile.exists()) {
			try {
				FileWriter fw = new FileWriter(playerFile);
				BufferedWriter bw = new BufferedWriter(fw);
				if (Bukkit.getOfflinePlayer(UUID.fromString(uuid)) == null) {
					createPlayer(Bukkit.getPlayer(UUID.fromString(uuid)));
				} else {
					createPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
				}
				bw.write(playerData.toString(8));
				bw.newLine();
				bw.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Scanner scan = new Scanner(
					new FileReader(Factions.getInstance().getDataFolder() + "/playerData/" + uuid + ".json"));
			scan.useDelimiter("\\Z");
			if (scan.hasNext()) {
				JSONObject player = new JSONObject(scan.next());
				for (int i = 0; i < Data.Players.length(); i++) {
					if (Data.Players.getJSONObject(i).getString("ID").equalsIgnoreCase(uuid)) {
						Data.Players.remove(i);
					}
				}
				Data.Players.put(player);

				scan.close();
			} else {
				scan.close();
				if (Bukkit.getOfflinePlayer(UUID.fromString(uuid)) == null) {
					createPlayer(Bukkit.getPlayer(UUID.fromString(uuid)));
				} else {
					createPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
				}
				scan = new Scanner(
						new FileReader(Factions.getInstance().getDataFolder() + "/playerData/" + uuid + ".json"));
				playerData = new JSONObject(scan.next());
				savePlayer(playerData);
				scan.close();
				loadData();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void loadFaction(String name) {
		for (int i = 0; i < Data.Factions.length(); i++) {
			if (Data.Factions.getJSONObject(i).getString("name").equalsIgnoreCase(name)) {
				factionData = Data.Factions.getJSONObject(i);
				return;
			}
		}
		F.createFaction(name);
	}

	public static void loadFactionDisk(String uuid) {
		createDirectories();

		File factionFile = new File(Factions.getInstance().getDataFolder() + "/factionData/" + uuid + ".json");
		if (!factionFile.exists()) {
			try {
				FileWriter fw = new FileWriter(factionFile);
				BufferedWriter bw = new BufferedWriter(fw);

				F.createFaction(uuid);
				bw.write(factionData.toString(8));
				bw.newLine();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Scanner scan = new Scanner(
					new FileReader(Factions.getInstance().getDataFolder() + "/factionData/" + uuid + ".json"));
			scan.useDelimiter("\\Z");
			factionData = new JSONObject(scan.next());
			for (int i = 0; i < Data.Factions.length(); i++) {
				if (Data.Factions.getJSONObject(i).getString("ID").equalsIgnoreCase(uuid)) {
					Data.Factions.remove(i);
				}
			}
			Data.Factions.put(factionData);
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void loadWorld(String name) {
		boolean exists = false;
		for (int i = 0; i < Data.Worlds.length(); i++) {
			if (Data.Worlds.getJSONObject(i).getString("name").equalsIgnoreCase(name)) {
				boardData = Data.Worlds.getJSONObject(i);
				exists = true;
			}
		}
		if (!exists) {
			boardData = new JSONObject();
			boardData.put("name", name);
			saveWorld(boardData);
		}
	}

	public static void loadWorldDisk(String name) {
		createDirectories();

		File worldFile = new File(Factions.getInstance().getDataFolder() + "/worldData/" + name + ".json");
		if (!worldFile.exists()) {
			try {
				FileWriter fw = new FileWriter(worldFile);
				BufferedWriter bw = new BufferedWriter(fw);
				boardData = new JSONObject();
				boardData.put("name", name);
				bw.write(boardData.toString(8));
				bw.newLine();
				bw.close();
				Bukkit.getLogger().info("[Debug] " + name + ".json doesn't exist, so we just created one.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Scanner scan = new Scanner(
					new FileReader(Factions.getInstance().getDataFolder() + "/worldData/" + name + ".json"));
			scan.useDelimiter("\\Z");
			boardData = new JSONObject(scan.next());
			for (int i = 0; i < Data.Worlds.length(); i++) {
				if (Data.Worlds.getJSONObject(i).getString("name").equalsIgnoreCase(name)) {
					Data.Worlds.remove(i);
				}
			}
			Data.Worlds.put(boardData);
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void savePlayer(JSONObject player) {
		String id = player.getString("ID");
		for (int i = 0; i < Data.Players.length(); i++) {
			if (Data.Players.getJSONObject(i).getString("ID").equalsIgnoreCase(id)) {
				Data.Players.remove(i);
			}
		}
		Data.Players.put(player);
	}

	public static void saveAllPlayersToDisk() {
		for (int i = 0; i < Data.Players.length(); i++) {
			savePlayerDisk(Data.Players.getJSONObject(i));
		}
	}

	public static void saveAllFactionsToDisk() {
		for (int i = 0; i < Data.Factions.length(); i++) {
			saveFactionDisk(Data.Factions.getJSONObject(i));
		}
	}

	public static void saveAllWorldsToDisk() {
		for (int i = 0; i < Data.Worlds.length(); i++) {
			saveWorldDisk(Data.Worlds.getJSONObject(i));
		}
	}

	public static void savePlayerDisk(JSONObject pData) {
		createDirectories();

		String saveString = pData.toString(8);
		try {
			FileWriter fw = new FileWriter(
					Factions.getInstance().getDataFolder() + "/playerData/" + pData.getString("ID") + ".json");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(saveString);
			bw.newLine();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveFaction(JSONObject faction) {
		String id = faction.getString("ID");
		for (int i = 0; i < Data.Factions.length(); i++) {
			if (Data.Factions.getJSONObject(i).getString("ID").equalsIgnoreCase(id)) {
				Data.Factions.remove(i);
			}
		}
		Data.Factions.put(faction);
	}

	public static void saveFactionDisk(JSONObject fData) {
		createDirectories();

		String saveString = fData.toString(8);
		try {
			FileWriter fw = new FileWriter(
					Factions.getInstance().getDataFolder() + "/factionData/" + fData.getString("ID") + ".json");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(saveString);
			bw.newLine();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveWorld(JSONObject world) {
		String name = world.getString("name");
		for (int i = 0; i < Data.Worlds.length(); i++) {
			if (Data.Worlds.getJSONObject(i).getString("name").equalsIgnoreCase(name)) {
				Data.Worlds.remove(i);
			}
		}
		Data.Worlds.put(world);
	}

	public static void saveWorldDisk(JSONObject wData) {
		createDirectories();

		String saveString = wData.toString(8);
		try {
			FileWriter fw = new FileWriter(Factions.getInstance().getDataFolder() + "/worldData/"
					+ wData.getString("name").toString() + ".json");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(saveString);
			bw.newLine();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteFaction(String factionName)
	  {
	    String uuid = "";
	    for (int i = 0; i < Data.Factions.length(); i++) {
	      if (Data.Factions.getJSONObject(i).getString("name").equalsIgnoreCase(factionName))
	      {
	        uuid = Data.Factions.getJSONObject(i).getString("ID");
	        Data.Factions.remove(i);
	      }
	    }
	    File file = new File(Factions.getInstance().getDataFolder() + "/factionData/" + uuid + ".json");
	    if (file.exists()) {
	      file.delete();
	    }
	    int k = -1;
	    for (int i = 0; i < factionIndex.size(); i++) {
	      if (((String)factionIndex.get(i)).equalsIgnoreCase(factionName)) {
	        k = i;
	      }
	    }
	    if (k >= 0)
	    {
	      factionIndex.remove(k);
	      Bukkit.getServer().getConsoleSender().sendMessage("removed");
	    }
	    for (int j = 0; j < Data.Worlds.length(); j++)
	    {
	      loadWorld(Data.Worlds.getJSONObject(j).getString("name"));
	      JSONArray array = boardData.names();
	      for (int m = 0; m < array.length(); m++)
	      {
	        String name = array.getString(m);
	        if (boardData.getString(name).equalsIgnoreCase(factionName)) {
	          boardData.remove(name);
	        }
	      }
	      saveWorld(boardData);
	    }
	  }
	
	public static void createPlayer(Player player)
	  {
	    playerData = new JSONObject();
	    playerData.put("name", player.getName());
	    playerData.put("ID", player.getUniqueId().toString());
	    playerData.put("faction", "");
	    playerData.put("autoclaim", "false");
	    playerData.put("autounclaim", "false");
	    playerData.put("factionRank", "member");
	    playerData.put("factionTitle", "");
	    playerData.put("shekels", "1000");
	    playerData.put("power", 25);
	    playerData.put("deaths", 0);
	    playerData.put("kills", 0);
	    playerData.put("time online", 0L);
	    playerData.put("chat channel", "global");
	    playerData.put("last online", System.currentTimeMillis());
	    savePlayer(playerData);
	    for (int i = 0; i < Data.Players.length(); i++) {
	      if (Data.Players.getJSONObject(i).getString("ID").equals(player.getUniqueId().toString())) {
	        Data.Players.remove(i);
	      }
	    }
	    for (int i = 0; i < playerIndex.size(); i++) {
	      if (((UUID)playerIndex.get(i)).equals(player.getUniqueId().toString())) {
	        playerIndex.remove(i);
	      }
	    }
	    Data.Players.put(playerData);
	    playerIndex.add(player.getUniqueId());
	  }
	  
	  public static void createPlayer(OfflinePlayer player)
	  {
	    playerData = new JSONObject();
	    playerData.put("name", player.getName());
	    playerData.put("ID", player.getUniqueId().toString());
	    playerData.put("faction", "");
	    playerData.put("autoclaim", "false");
	    playerData.put("autounclaim", "false");
	    playerData.put("factionRank", Config.configData.getString("default player factionRank"));
	    playerData.put("factionTitle", Config.configData.getString("default player title"));
	    playerData.put("shekels", Config.configData.getInt("default player money"));
	    playerData.put("power", Config.configData.getInt("default player power"));
	    playerData.put("deaths", 0);
	    playerData.put("kills", 0);
	    playerData.put("time online", 0L);
	    playerData.put("chat channel", "global");
	    playerData.put("last online", System.currentTimeMillis());
	    savePlayer(playerData);
	    for (int i = 0; i < Data.Players.length(); i++) {
	      if (Data.Players.getJSONObject(i).getString("ID").equals(player.getUniqueId().toString())) {
	        Data.Players.remove(i);
	      }
	    }
	    for (int i = 0; i < playerIndex.size(); i++) {
	      if (((UUID)playerIndex.get(i)).equals(player.getUniqueId().toString())) {
	        playerIndex.remove(i);
	      }
	    }
	    Data.Players.put(playerData);
	    playerIndex.add(player.getUniqueId());
	  }
}