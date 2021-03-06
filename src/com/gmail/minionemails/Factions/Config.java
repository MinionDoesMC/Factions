package com.gmail.minionemails.Factions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.json.JSONArray;
import org.json.JSONObject;

public class Config {

	  public static JSONObject configData = new JSONObject();
	  public static int chunkSizeX = 16;
	  public static int chunkSizeY = 16;
	  public static int chunkSizeZ = 16;
	  public static int powerCapMax = 750;
	  public static String Rel_Faction = "�7";
	  public static String Rel_Ally = "�2";
	  public static String Rel_Enemy = "�c";
	  public static String Rel_Neutral = "�f";
	  public static String Rel_Other = "�f";
	  public static String Rel_Truce = "�a";
	  public static String powerCapType = "none";
	  public static JSONArray neutralBreakData = new JSONArray();
	  public static JSONArray allyBreakData = new JSONArray();
	  public static JSONArray truceBreakData = new JSONArray();
	  public static JSONArray otherBreakData = new JSONArray();
	  public static JSONArray enemyBreakData = new JSONArray();
	  public static JSONArray neutralPlaceData = new JSONArray();
	  public static JSONArray allyPlaceData = new JSONArray();
	  public static JSONArray trucePlaceData = new JSONArray();
	  public static JSONArray otherPlaceData = new JSONArray();
	  public static JSONArray enemyPlaceData = new JSONArray();
	  public static JSONArray neutralItemData = new JSONArray();
	  public static JSONArray allyItemData = new JSONArray();
	  public static JSONArray truceItemData = new JSONArray();
	  public static JSONArray otherItemData = new JSONArray();
	  public static JSONArray enemyItemData = new JSONArray();
	  public static JSONArray claimsDisabledInTheseWorlds = new JSONArray();
	
	  public static String createConfigData()
	  {
	    String configFile = "";
	    InputStream input = Factions.class.getResourceAsStream("/configFile.json");
	    Scanner scan = new Scanner(input).useDelimiter("\\Z");
	    configFile = scan.next();
	    scan.close();
	    
	    configData = new JSONObject(configFile);
	    
	    loadConfigData();
	    return configFile;
	  }
	  
	  public static void loadConfig()
	  {
	    File dataFolder = Factions.getInstance().getDataFolder();
	    String version = "1.0";
	    String oldVersion = "1.0";
	    
	    File configFile = new File(dataFolder + "/config.json");
	    if (!configFile.exists()) {
	      try
	      {
	        FileWriter fw = new FileWriter(configFile);
	        BufferedWriter bw = new BufferedWriter(fw);
	        
	        bw.write(createConfigData());
	        bw.newLine();
	        bw.close();
	      }
	      catch (IOException e)
	      {
	        e.printStackTrace();
	      }
	    }
	    try
	    {
	      FileReader filereader = new FileReader(dataFolder + "/config.json");
	      Scanner scan = new Scanner(filereader).useDelimiter("\\Z");
	      configData = new JSONObject(scan.next());
	      loadConfigData();
	      scan.close();
	      if (!oldVersion.equals(version))
	      {
	        Bukkit.getServer().getConsoleSender().sendMessage("�cConfig file is out of date! Backing up old config file and creating a new one! Please go and redo your configs with the new format!");
	        try
	        {
	          File backupFile = new File(configFile.getAbsoluteFile() + ".backup");
	          FileWriter filew = new FileWriter(backupFile);
	          BufferedWriter baw = new BufferedWriter(filew);
	          baw.write(createConfigData());
	          baw.newLine();
	          baw.close();
	        }
	        catch (IOException e)
	        {
	          e.printStackTrace();
	        }
	        FileWriter fw = new FileWriter(configFile);
	        BufferedWriter bw = new BufferedWriter(fw);
	        bw.write(createConfigData());
	        bw.newLine();
	        bw.close();
	      }
	    }
	    catch (FileNotFoundException e)
	    {
	      e.printStackTrace();
	    }
	    catch (IOException e)
	    {
	      e.getCause();
	    }
	  }
	  
	  public static void loadConfigData()
	  {
	    chunkSizeX = configData.getInt("claim size x");
	    chunkSizeY = configData.getInt("claim size y");
	    chunkSizeZ = configData.getInt("claim size z");
	    if (configData.has("power cap max power")) {
	      powerCapMax = configData.getInt("power cap max power");
	    }
	    if (configData.has("power cap type (none/soft/hard)")) {
	      powerCapType = configData.getString("power cap type (none/soft/hard)");
	    }
	    neutralBreakData = configData.getJSONArray("block break protection in neutral land");
	    allyBreakData = configData.getJSONArray("block break protection in ally land");
	    truceBreakData = configData.getJSONArray("block break protection in truce land");
	    otherBreakData = configData.getJSONArray("block break protection in other land");
	    enemyBreakData = configData.getJSONArray("block break protection in enemy land");
	    
	    neutralPlaceData = configData.getJSONArray("block place protection in neutral land");
	    allyPlaceData = configData.getJSONArray("block place protection in ally land");
	    trucePlaceData = configData.getJSONArray("block place protection in truce land");
	    otherPlaceData = configData.getJSONArray("block place protection in other land");
	    enemyPlaceData = configData.getJSONArray("block place protection in enemy land");
	    
	    neutralItemData = configData.getJSONArray("item protection in neutral land");
	    allyItemData = configData.getJSONArray("item protection in ally land");
	    truceItemData = configData.getJSONArray("item protection in truce land");
	    otherItemData = configData.getJSONArray("item protection in other land");
	    enemyItemData = configData.getJSONArray("item protection in enemy land");
	    
	    claimsDisabledInTheseWorlds = configData.getJSONArray("disable claims and homes in these worlds");
	  }
}
