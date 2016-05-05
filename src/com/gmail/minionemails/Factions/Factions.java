package com.gmail.minionemails.Factions;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Factions 
	extends JavaPlugin {

	private static Factions instance;
	static String version = "1.0";
	
	public static Factions getInstance()
	{
		return instance;
	}
	
	public void onEnable()
	{
		instance = this;
		
		this.getServer().getConsoleSender().sendMessage("§2[Factions is loading...]");
		
		this.getServer().getPluginManager().registerEvents(new EListener(), this);
		this.getServer().getConsoleSender().sendMessage("§2[Factions has registered the events]");
		
		C.loadData();
		this.getServer().getConsoleSender().sendMessage("§2[Factions has loaded the necessary data]");
		
		this.getServer().getConsoleSender().sendMessage("§2[Factions has been loaded!]");
		
	}
	
	public void onDisable() {
		C.createDirectories();
		C.saveAllPlayersToDisk();
		C.saveAllFactionsToDisk();
		C.saveAllWorldsToDisk();
		
		Bukkit.getServer().getConsoleSender().sendMessage("§2[Factions has finished saving]");
		
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String cmd = command.getName().toLowerCase();
		
		if ((cmd.equalsIgnoreCase("f"))) {
			if ((sender instanceof Player)) {
				if (args.length < 1) {
					sender.sendMessage("§cInvaild command. Use /f help for commands");
					return true;
				}
				if (args[0].equalsIgnoreCase("create")) {
					return F.tryCreateFaction(sender, args);
				}
				if (args[0].equalsIgnoreCase("disband")) {
					if (args.length > 1) {
						C.loadPlayer(((Player)sender).getUniqueId());
						if (C.playerData.getString("faction").equalsIgnoreCase(args[1])) {
							return F.tryDisband(sender, args[1]);
						}
						if ((sender.isOp()) || (sender.hasPermission("factions.admin"))) {
							return F.tryDisband(sender, args[1]);
						}
						sender.sendMessage("§cYou are not allowed to run this command");
						return true;
					}
					C.loadPlayer(((Player)sender).getUniqueId());
					if (args.length > 1) {
						if (C.playerData.getString("faction").equalsIgnoreCase(args[1])) {
							sender.sendMessage("§cYou're not a member of that faction");
							return true;
						}
						return F.tryDisband(sender, args[1]);
					}
				}
				if (args[0].equalsIgnoreCase("map")) {
					return F.drawMap(sender);
				}
				if (args[0].equalsIgnoreCase("claim")) {
					return F.tryClaim(sender);
				}
				if (args[0].equalsIgnoreCase("ally")) {
					return F.setRelation(sender, args, "allies");
				}
				if (args[0].equalsIgnoreCase("enemy")) {
					return F.setRelation(sender, args, "enemies");
				}
				if (args[0].equalsIgnoreCase("truce")) {
					return F.setRelation(sender, args, "truce");
				}
				if (args[0].equalsIgnoreCase("neutral")) {
					return F.setRelation(sender, args, "neutral");
				}
			}
		}
		
		return true;
	}

	//     Getters are down here     \\
	public static String getFactionRelationColor(String senderFaction, String reviewedFaction)
	  {
	    String relation = "§";
	    String relation2 = "";
	    if (!Config.configData.getString("enforce relations").equalsIgnoreCase(""))
	    {
	      String rel = Config.configData.getString("enforce relations");
	      if (rel.equalsIgnoreCase("enemies")) {
	        return Config.Rel_Enemy;
	      }
	      if (rel.equalsIgnoreCase("ally")) {
	        return Config.Rel_Ally;
	      }
	      if (rel.equalsIgnoreCase("truce")) {
	        return Config.Rel_Truce;
	      }
	      if (rel.equalsIgnoreCase("neutral")) {
	        return Config.Rel_Other;
	      }
	      if (rel.equalsIgnoreCase("other")) {
	        return Config.Rel_Other;
	      }
	    }
	    if (senderFaction.equalsIgnoreCase(""))
	    {
	      if (relation.equalsIgnoreCase("enemy")) {
	        return Config.Rel_Enemy;
	      }
	      if (relation.equalsIgnoreCase("truce")) {
	        return Config.Rel_Truce;
	      }
	    }
	    if (senderFaction.equalsIgnoreCase(reviewedFaction)) {
	      return Config.Rel_Faction;
	    }
	    if ((!senderFaction.equalsIgnoreCase("")) && (!reviewedFaction.equalsIgnoreCase("")) && (!reviewedFaction.equalsIgnoreCase("neutral territory")) && 
	      (!senderFaction.equalsIgnoreCase("neutral territory")))
	    {
	      C.loadFaction(senderFaction);
	      C.enemyData = C.factionData.getJSONArray("enemies");
	      for (int i = 0; i < C.enemyData.length(); i++) {
	        if (C.enemyData.getString(i).equalsIgnoreCase(reviewedFaction)) {
	          relation = "enemy";
	        }
	      }
	      C.allyData = C.factionData.getJSONArray("allies");
	      for (int i = 0; i < C.allyData.length(); i++) {
	        if (C.allyData.getString(i).equalsIgnoreCase(reviewedFaction)) {
	          relation = "ally";
	        }
	      }
	      C.truceData = C.factionData.getJSONArray("truce");
	      for (int i = 0; i < C.truceData.length(); i++) {
	        if (C.truceData.getString(i).equalsIgnoreCase(reviewedFaction)) {
	          relation = "truce";
	        }
	      }
	      C.loadFaction(reviewedFaction);
	      
	      C.enemyData = C.factionData.getJSONArray("enemies");
	      for (int i = 0; i < C.enemyData.length(); i++) {
	        if (C.enemyData.getString(i).equalsIgnoreCase(senderFaction)) {
	          relation2 = "enemy";
	        }
	      }
	      C.allyData = C.factionData.getJSONArray("allies");
	      for (int i = 0; i < C.allyData.length(); i++) {
	        if (C.allyData.getString(i).equalsIgnoreCase(senderFaction)) {
	          relation2 = "ally";
	        }
	      }
	      C.truceData = C.factionData.getJSONArray("truce");
	      for (int i = 0; i < C.truceData.length(); i++) {
	        if (C.truceData.getString(i).equalsIgnoreCase(senderFaction)) {
	          relation2 = "truce";
	        }
	      }
	      if (C.factionData.getString("peaceful").equalsIgnoreCase("true")) {
	        return Config.Rel_Truce;
	      }
	      if (C.factionData.getString("safezone").equalsIgnoreCase("true")) {
	        return Config.Rel_Truce;
	      }
	      if (C.factionData.getString("warzone").equalsIgnoreCase("true")) {
	        return Config.Rel_Enemy;
	      }
	      C.loadFaction(senderFaction);
	      if (C.factionData.getString("peaceful").equalsIgnoreCase("true")) {
	        return Config.Rel_Truce;
	      }
	      if (C.factionData.getString("safezone").equalsIgnoreCase("true")) {
	        return Config.Rel_Truce;
	      }
	      if (C.factionData.getString("warzone").equalsIgnoreCase("true")) {
	        return Config.Rel_Enemy;
	      }
	      if ((relation.equalsIgnoreCase("enemy")) || (relation2.equalsIgnoreCase("enemy"))) {
	        return Config.Rel_Enemy;
	      }
	      if ((relation.equalsIgnoreCase("ally")) && (relation2.equalsIgnoreCase("ally"))) {
	        return Config.Rel_Ally;
	      }
	      if ((relation.equalsIgnoreCase("truce")) && (relation2.equalsIgnoreCase("truce"))) {
	        return Config.Rel_Truce;
	      }
	      C.loadFaction(senderFaction);
	      if (relation.equalsIgnoreCase("enemy")) {
	        return Config.Rel_Enemy;
	      }
	      if (relation.equalsIgnoreCase("truce")) {
	        return Config.Rel_Truce;
	      }
	      return relation;
	    }
	    if (relation.equalsIgnoreCase("enemy")) {
	      return Config.Rel_Enemy;
	    }
	    if (relation.equalsIgnoreCase("truce")) {
	      return Config.Rel_Truce;
	    }
	    return relation;
	  }
}
