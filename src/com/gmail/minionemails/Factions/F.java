package com.gmail.minionemails.Factions;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

public class F {

	public F() {
	}

	public static void messageFaction(String faction, String message) {
		Collection<? extends Player> on = Bukkit.getOnlinePlayers();
		for (Player player : on) {
			C.loadPlayer(player.getUniqueId());
			if (C.playerData.getString("faction").equalsIgnoreCase(faction)) {
				player.getPlayer().sendMessage(message);
			}
		}
	}

	public static boolean factionCheck(String name) {
		for (int i = 0; i < Data.Factions.length(); i++) {
			if (Data.Factions.getJSONObject(i).getString("name").equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isFactionOnline(String faction) {
		C.loadFaction(faction);

		for (Player player : Bukkit.getOnlinePlayers()) {
			C.loadPlayer(player.getUniqueId());
			if ((C.playerData.getString("faction").equalsIgnoreCase(faction)) && (player.isOnline())) {
				C.factionData.put("lastOnline", System.currentTimeMillis());
				C.saveFaction(C.factionData);
				return true;
			}
		}
		if (C.factionData.getLong("lastOnline") + 300 * 1000 > System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	public static boolean setFactionFlag(CommandSender sender, String faction, String flag, String tr) {
		if ((sender.isOp()) || (sender.hasPermission("factions.admin"))) {
			C.loadFaction(faction);
			if ((flag.equalsIgnoreCase("peaceful") || (flag.equalsIgnoreCase("warzone"))
					|| (flag.equalsIgnoreCase("safezone")))) {
				if ((tr.equalsIgnoreCase("true")) || (tr.equalsIgnoreCase("false"))) {
					String ntr = "false";
					if (tr.equalsIgnoreCase("false")) {
						ntr = "true";
					}
					C.factionData.put("peaceful", ntr);
					C.factionData.put("warzone", ntr);
					C.factionData.put("safezone", ntr);
					C.factionData.put(flag, tr);
					C.saveFaction(C.factionData);
					if (tr.equalsIgnoreCase("true")) {
						sender.sendMessage("§aThe faction §a§l" + faction + " §ais now a §a§l" + flag + "§afaction.");
						return true;
					}
					if (tr.equalsIgnoreCase("false")) {
						sender.sendMessage(
								"§The faction §a§l" + faction + " §ais no longer a §a§l" + flag + " §afaction.");
						return true;
					}
				} else {
					sender.sendMessage("§cPlease specify weither you want §a§l" + faction + " §ato be §a§l" + flag
							+ " §awith true or false at the end.");
					return true;
				}
			} else {
				sender.sendMessage("§cPlease use either [§c§lPeacful|Warzone|Safzone §c]");
				return true;
			}
		} else {
			sender.sendMessage(
					"§cYou must either be OP or have the permission: §c§lfactions.admin  §cto do this command");
			return true;
		}
		return true;
	}

	public static boolean setDesc(CommandSender sender, String[] args) {
		C.loadPlayer(((Player) sender).getUniqueId());
		String faction = C.playerData.getString("faction");
		if (faction.equalsIgnoreCase("")) {
			sender.sendMessage("§cYou're not in a faction.");
			return true;
		}
		if (args.length > 1) {
			String desc = "";
			for (int i = 0; i < args.length; i++) {
				desc = desc + args[i] + " ";
			}
			C.loadFaction(faction);
			C.factionData.put("desc", desc);
			C.saveFaction(C.factionData);
			messageFaction(faction, "§aThe description was changed to: §a§l" + desc);
			return true;
		}
		sender.sendMessage("§cPlease provide a description. Example: /f desc We are the best!");
		return true;
	}

	public static boolean setRelation(CommandSender sender, String[] args, String relation) {
		if (args.length > 1) {
			C.loadPlayer(((Player) sender).getUniqueId());
			String faction = C.playerData.getString("faction");
			String otherFaction = args[1];
			if (factionCheck(otherFaction)) {
				String relString = "";
				if (relation.equalsIgnoreCase("enemies")) {
					relString = "§c";
				}
				if (relation.equalsIgnoreCase("allies")) {
					relString = "§2";
				}
				if (relation.equalsIgnoreCase("truce")) {
					relString = "§a";
				}
				if (relation.equalsIgnoreCase("neutral")) {
					relString = "§f";
				}
				if (!relation.equalsIgnoreCase("neutral")) {
					C.loadPlayer(((Player) sender).getUniqueId());
					C.loadFaction(faction);
					C.enemyData = C.factionData.getJSONArray(relation);
					int k = 0;
					for (int i = 0; i < C.enemyData.length(); i++) {
						if (C.enemyData.getString(i).equalsIgnoreCase(otherFaction)) {
							k++;
						}
					}
					if (k < 1) {
						C.enemyData.put(otherFaction);
						C.factionData.put(relation, C.enemyData);
						C.saveFaction(C.factionData);

						C.loadFaction(faction);
						C.enemyData = C.factionData.getJSONArray("enemies");
						C.allyData = C.factionData.getJSONArray("allies");
						C.truceData = C.factionData.getJSONArray("truce");

						if (!relation.equalsIgnoreCase("enemies")) {
							for (int i = 0; i < C.enemyData.length(); i++) {
								if (C.enemyData.getString(i).equalsIgnoreCase(otherFaction)) {
									C.enemyData.remove(i);
								}
							}
						}
						if (!relation.equalsIgnoreCase("allies")) {
							for (int i = 0; i < C.allyData.length(); i++) {
								if (C.allyData.getString(i).equalsIgnoreCase(otherFaction)) {
									C.allyData.remove(i);
								}
							}
						}
						if (!relation.equalsIgnoreCase("truce")) {
							for (int i = 0; i < C.truceData.length(); i++) {
								if (C.truceData.getString(i).equalsIgnoreCase(otherFaction)) {
									C.truceData.remove(i);
								}
							}
						}
						C.factionData.put("enemies", C.enemyData);
						C.factionData.put("allies", C.allyData);
						C.factionData.put("truce", C.truceData);
						C.saveFaction(C.factionData);
					} else {
						sender.sendMessage("§cYou already have this relation set with that faction.");
						return true;
					}
					C.loadFaction(otherFaction);
					C.enemyData = C.factionData.getJSONArray("enemies");
					C.allyData = C.factionData.getJSONArray("allies");
					C.truceData = C.factionData.getJSONArray("truce");

					if (!relation.equalsIgnoreCase("allies")) {
						for (int i = 0; i < C.allyData.length(); i++) {
							if (C.allyData.getString(i).equalsIgnoreCase(faction)) {
								C.allyData.remove(i);
							}
						}
					}
					if (!relation.equalsIgnoreCase("truce")) {
						for (int i = 0; i < C.truceData.length(); i++) {
							if (C.truceData.getString(i).equalsIgnoreCase(faction)) {
								C.truceData.remove(i);
							}
						}
					}
					if (!relation.equalsIgnoreCase("enemies")) {
						int m = 0;
						for (int i = 0; i < C.enemyData.length(); i++) {
							if (C.enemyData.getString(i).equalsIgnoreCase(faction)) {
								m++;
							}
						}
						if (m < 1) {
							C.enemyData.put(faction);
						}
					}
					C.factionData.put("enemies", C.enemyData);
					C.factionData.put("allies", C.allyData);
					C.factionData.put("truce", C.truceData);
					C.saveFaction(C.factionData);

					int j = 0;
					if (relation.equalsIgnoreCase("enemies")) {
						for (int i = 0; i < C.enemyData.length(); i++) {
							if (C.enemyData.getString(i).equalsIgnoreCase(faction)) {
								j++;
							}
						}
					}
					if (relation.equalsIgnoreCase("allies")) {
						for (int i = 0; i < C.allyData.length(); i++) {
							if (C.allyData.getString(i).equalsIgnoreCase(faction)) {
								j++;
							}
						}
					}
					if (relation.equalsIgnoreCase("truce")) {
						for (int i = 0; i < C.truceData.length(); i++) {
							if (C.truceData.getString(i).equalsIgnoreCase(faction)) {
								j++;
							}
						}
					}
					if ((j > 0) || ((j == 0) && (relation.equalsIgnoreCase("neutral")))) {
						messageFaction(faction, "§cYou're now " + relString + relation + " §cwith "
								+ Factions.getFactionRelationColor(faction, otherFaction) + otherFaction);
						messageFaction(otherFaction, "§cYou're now " + relString + relation + " §cwith "
								+ Factions.getFactionRelationColor(faction, otherFaction) + faction);
						return true;
					}
					if (j == 0) {
						messageFaction(faction,
								"§cYou have asked " + Factions.getFactionRelationColor(faction, otherFaction)
										+ otherFaction + " §cto become " + relString + relation);
						messageFaction(otherFaction, Factions.getFactionRelationColor(faction, otherFaction) + faction
								+ " §chave asked to become " + relString + relation);
						return true;
					}
				} else {
					C.loadPlayer(((Player) sender).getUniqueId());
					C.loadFaction(faction);
					C.enemyData = C.factionData.getJSONArray("enemies");
					C.allyData = C.factionData.getJSONArray("allies");
					C.truceData = C.factionData.getJSONArray("truce");

					int k = 0;
					for (int i = 0; i < C.enemyData.length(); i++) {
						if (C.enemyData.getString(i).equalsIgnoreCase(otherFaction)) {
							C.enemyData.remove(i);
							k++;
						}
					}
					for (int i = 0; i < C.allyData.length(); i++) {
						if (C.allyData.getString(i).equalsIgnoreCase(otherFaction)) {
							C.allyData.remove(i);
							k++;
						}
					}
					for (int i = 0; i < C.truceData.length(); i++) {
						if (C.truceData.getString(i).equalsIgnoreCase(otherFaction)) {
							C.truceData.remove(i);
							k++;
						}
					}
					if (k == 0) {
						sender.sendMessage("§cYou are already neutral with "
								+ Factions.getFactionRelationColor(faction, otherFaction) + otherFaction);
						return true;
					}
					C.factionData.put("enemies", C.enemyData);
					C.factionData.put("allies", C.allyData);
					C.factionData.put("truce", C.truceData);
					C.saveFaction(C.factionData);

					C.loadFaction(otherFaction);
					C.enemyData = C.factionData.getJSONArray("enemies");
					C.allyData = C.factionData.getJSONArray("allies");
					C.truceData = C.factionData.getJSONArray("truce");

					int j = 0;
					for (int i = 0; i < C.enemyData.length(); i++) {
						if (C.enemyData.getString(i).equalsIgnoreCase(faction)) {
							j++;
						}
					}
					for (int i = 0; i < C.allyData.length(); i++) {
						if (C.allyData.getString(i).equalsIgnoreCase(faction)) {
							j++;
						}
					}
					for (int i = 0; i < C.truceData.length(); i++) {
						if (C.truceData.getString(i).equalsIgnoreCase(faction)) {
							j++;
						}
					}
					if ((k > 0) && (j == 0)) {
						sender.sendMessage("§aYou are now neutral with "
								+ Factions.getFactionRelationColor(faction, otherFaction) + otherFaction);
						return true;
					}
					if ((k > 0) && (j > 0)) {
						sender.sendMessage("§aYou have asked" + Factions.getFactionRelationColor(faction, otherFaction)
								+ otherFaction + " §aif they would like to become §fNeutral");
						return true;
					}
				}
			} else {
				sender.sendMessage("§cThat faction does not exist");
				return true;
			}
		} else {
			sender.sendMessage(
					"§cYou need to enter the name of the faction you would like to enemy. Example: /f enemy Minions");
			return true;
		}
		return true;
	}

	public static boolean tryCreateFaction(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("§cPlease enter a name");
			return true;
		} else {
			if ((args[1].contains("/")) || (args[1].contains("\\")) || (args[1].contains("."))
					|| (args[1].contains("\"")) || (args[1].contains(",")) || (args[1].contains("?"))
					|| (args[1].contains("'")) || (args[1].contains("*")) || (args[1].contains("|"))
					|| (args[1].contains("<")) || (args[1].contains(":")) || (args[1].contains("$"))) {
				sender.sendMessage("§cYour faction name cannot contain special characters");
				return true;
			}
			for (int i = 0; i < C.factionIndex.size(); i++) {
				if (((String) C.factionIndex.get(i)).equalsIgnoreCase(args[1].toString())) {
					sender.sendMessage("§cThe faction name §f" + args[1].toString() + " §chas already been taken");
					return true;
				}
			}
			C.loadPlayer(((Player) sender).getUniqueId());

			String factionName = C.playerData.getString("faction");
			if (!factionName.equalsIgnoreCase("")) {
				sender.sendMessage("§cYou're already in a faction: " + factionName);
				sender.sendMessage(
						"§cTo create a new faction you'll have to leave your faction by doing the command: /f leave");
				return true;
			}
			createFaction(args[1].toString());
			C.loadPlayer(((Player) sender).getUniqueId());
			C.playerData.put("factionRank", "leader");
			C.playerData.put("faction", args[1].toString());
			C.savePlayer(C.playerData);

			for (Player player : Bukkit.getOnlinePlayers()) {
				player.sendMessage("§a" + sender.getName() + " has created the faction §f" + args[1].toString());
			}
		}
		return true;
	}

	public static void createFaction(String faction) {
		C.enemyData = new JSONArray();
		C.allyData = new JSONArray();
		C.truceData = new JSONArray();
		C.inviteData = new JSONArray();
		C.factionData = new JSONObject();
		C.factionData.put("name", faction);
		C.factionData.put("peaceful", "false");
		C.factionData.put("warzone", "false");
		C.factionData.put("safezone", "false");
		C.factionData.put("ID", UUID.randomUUID().toString());
		C.factionData.put("shekels", 0.0D);
		C.factionData.put("enemies", C.enemyData);
		C.factionData.put("allies", C.allyData);
		C.factionData.put("truce", C.truceData);
		C.factionData.put("invited", C.inviteData);
		C.factionData.put("lastOnline", System.currentTimeMillis());
		C.factionData.put("home", "");
		C.factionData.put("desc", "do /f desc to change this description");
		C.factionData.put("open", "false");
		C.saveFaction(C.factionData);

		for (int i = 0; i < C.factionIndex.size(); i++) {
			if (((String) C.factionIndex.get(i)).equals(faction)) {
				C.factionIndex.remove(i);
			}
		}
		C.factionIndex.add(faction);
		for (int i = 0; i < Data.Factions.length(); i++) {
			if (Data.Factions.getJSONObject(i).getString("ID").equals(C.factionData.getString("ID"))) {
				Data.Factions.remove(i);
			}
		}
		Data.Factions.put(C.factionData);
	}

	public static void deleteFaction(String factionName) {
		String uuid = "";
		for (int i = 0; i < Data.Factions.length(); i++) {
			if (Data.Factions.getJSONObject(i).getString("name").equalsIgnoreCase(factionName)) {
				uuid = Data.Factions.getJSONObject(i).getString("ID");
				Data.Factions.remove(i);
			}
		}
		File file = new File(Factions.getInstance().getDataFolder() + "/factionData/" + uuid + ".json");
		if (file.exists()) {
			file.delete();
		}
		int k = -1;
		for (int i = 0; i < C.factionIndex.size(); i++) {
			if (((String) C.factionIndex.get(i)).equalsIgnoreCase(factionName)) {
				k = i;
			}
		}
		if (k >= 0) {
			C.factionIndex.remove(k);
			Bukkit.getServer().getConsoleSender().sendMessage("removed");
		}
		for (int j = 0; j < Data.Worlds.length(); j++) {
			loadWorld(Data.Worlds.getJSONObject(j).getString("name"));
			JSONArray array = C.boardData.names();
			for (int m = 0; m < array.length(); m++) {
				String name = array.getString(m);
				if (C.boardData.getString(name).equalsIgnoreCase(factionName)) {
					C.boardData.remove(name);
				}
			}
			C.saveWorld(C.boardData);
		}
	}

	public static boolean tryDisband(CommandSender sender, String faction) {
		C.loadPlayer(((Player) sender).getUniqueId());

		if (C.playerData.getString("faction").equalsIgnoreCase("")) {
			sender.sendMessage("§cYou're not in a faction");
			return true;
		}

		if ((!C.playerData.getString("factionRank").equalsIgnoreCase("leader")) && (!sender.isOp())) {
			sender.sendMessage("§cOnly leaders can set the faction's home");
			return true;
		}

		if (faction.equalsIgnoreCase("")) {
			faction = C.playerData.getString("faction");
		}

		for (UUID player : C.playerIndex) {
			C.loadPlayer(player);
			if (C.playerData.getString("faction").equalsIgnoreCase(faction)) {
				C.playerData.put("faction", "");
				C.playerData.put("factionRank", "member");
				C.savePlayer(C.playerData);
			}
		}
		deleteFaction(faction);

		sender.sendMessage("§aThe faction has been disbanded");
		return true;
	}

	public static boolean trySetHome(CommandSender sender) {
		C.loadPlayer(((Player) sender).getUniqueId());
		Player player = (Player) sender;
		String factionName = C.playerData.getString("faction");

		if (factionName.equalsIgnoreCase("")) {
			sender.sendMessage("§cYou're not in a faction");
			return true;
		}
		if ((!C.playerData.getString("factionRank").equalsIgnoreCase("officer"))
				&& (!C.playerData.getString("factionRank").equalsIgnoreCase("leader"))) {
			sender.sendMessage(
					"§cYou have to be either an §c§lOfficer §c or a §c§lLeader §c to set the faction's home");
			return true;
		}
		String world = player.getLocation().getWorld().getName().toString();
		double posX = player.getLocation().getX();
		double posY = player.getLocation().getY();
		double posZ = player.getLocation().getZ();

		Location location = new Location(Bukkit.getWorld(world), posX, posY, posZ);
		Block block = location.getBlock();

		if (block.getRelative(BlockFace.UP).getType() != Material.AIR) {
			sender.sendMessage("§cCan't set home due to a block above you.");
			return true;
		}
		C.loadPlayer(player.getUniqueId());
		C.loadFaction(C.playerData.getString("faction"));
		C.factionData.put("home", world + " " + posX + " " + posY + " " + posZ);
		C.saveFaction(C.factionData);

		messageFaction(C.playerData.getString("faction"),
				"§7" + sender.getName() + " §ahas set the faction home. You can now use /f home");
		return true;
	}

	public static Location getHome(String faction) {
		C.loadFaction(faction);
		String home = C.factionData.getString("home");

		if (home.equalsIgnoreCase("")) {
			return null;
		}
		Scanner scan = new Scanner(home);
		String world = scan.next();
		double x = scan.nextDouble();
		double y = scan.nextDouble();
		double z = scan.nextDouble();
		scan.close();

		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	public static boolean tryHome(CommandSender sender) {
		C.loadPlayer(((Player) sender).getUniqueId());
		Player player = (Player) sender;
		String factionName = C.playerData.getString("faction");

		if (factionName.equalsIgnoreCase("")) {
			sender.sendMessage("§cYou're not in a faction");
		}
		C.loadPlayer(((Player) sender).getUniqueId());
		C.loadFaction(C.playerData.getString("faction"));
		Location loc = getHome(factionName);

		if (loc == null) {
			sender.sendMessage("§cYour faction does not have a home");
			return true;
		}
		Block block = loc.getBlock();
		int i = 0;
		while ((block.getRelative(BlockFace.UP).getType() != Material.AIR) || (block.getType() != Material.AIR)) {
			Random generater = new Random(System.currentTimeMillis() + block.getX() + block.getY() + block.getZ());
			block = block.getRelative(generater.nextInt(10) * i, generater.nextInt(10) * i, generater.nextInt(10) * i);

			int l = 0;
			while (block.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
				l++;
				if (l > 9) {
					break;
				}
				block = block.getRelative(BlockFace.DOWN);
			}
			i++;
			if (i > 9) {
				sender.sendMessage("§cThe area is unsafe to be teleported to");
				return true;
			}
		}
		loc = block.getLocation();
		player.teleport(loc);
		sender.sendMessage("§aYou have been teleported to your faction home");
		return true;

	}

	public static boolean tryOpen(CommandSender sender) {
		C.loadPlayer(((Player) sender).getUniqueId());
		if ((!C.playerData.getString("factionRank").equalsIgnoreCase("officer"))
				&& (!C.playerData.getString("factionRank").equalsIgnoreCase("leader"))) {
			sender.sendMessage(
					"§cYou have to be either an §c§lOfficer §c or a §c§lLeader §c to either open or close the faction");
			return true;
		}
		C.loadFaction(C.playerData.getString("faction"));
		String open = C.factionData.getString("open");

		if (open.equalsIgnoreCase("true")) {
			open = "false";
			C.factionData.put("open", "false");
		} else {
			open = "true";
			C.factionData.put("open", "true");
		}
		C.saveFaction(C.factionData);
		messageFaction(C.playerData.getString("faction"), "§aYour faction is now set to: §a§l" + open);

		return true;
	}

	public static void loadWorld(String name) {
		boolean exists = false;
		for (int i = 0; i < Data.Worlds.length(); i++) {
			if (Data.Worlds.getJSONObject(i).getString("name").equalsIgnoreCase(name)) {
				C.boardData = Data.Worlds.getJSONObject(i);
				exists = true;
			}
		}
		if (!exists) {
			C.boardData = new JSONObject();
			C.boardData.put("name", name);
			C.saveWorld(C.boardData);
		}
	}

	// CLAIM|MAP METHODS DOWN HERE \\

	public static boolean drawMap(CommandSender sender) {
		String mapKey = "§7Unclaimed = #";
		String map = "";

		Player player = (Player) sender;
		loadWorld(player.getWorld().getName());
		C.loadPlayer(((Player) sender).getUniqueId());
		String faction = C.playerData.getString("faction");

		int x = player.getLocation().getBlockX();
		int cX = Config.chunkSizeX;
		int y = player.getLocation().getBlockY();
		int cY = Config.chunkSizeY;
		int z = player.getLocation().getBlockZ();
		int cZ = Config.chunkSizeZ;

		x = Math.round(x / cX) * cX;
		y = Math.round(y / cY) * cY;
		z = Math.round(z / cZ) * cZ;

		String[] factionsFoundArray = new String[64];
		String[] mapSymbols = { "/", "]", "[", "}", "{", ";", ",", "-", "0", "_", "=", "*", "&", "^", "%", "$", "!",
				"@", "\\" };
		int factionsFound = 0;
		String factionsStandingOn = "neutral";

		for (int i = z - 3 * cZ; i < z + 3 * cZ; i++) {
			if (i % cZ == 0) {
				map = map + "\n";
				for (int j = x - 12 * cX; j < x + 6 * cX; j++) {
					if (j % cX == 0) {
						if ((i == z) && (j == x)) {
							map = map + "§a+§7";
							if ((C.boardData.has("chunkX" + j + " chunkY" + y + " chunkZ" + i)) && !C.boardData
									.getString("chunkX" + j + " chunkY" + y + " chunkZ" + i).equalsIgnoreCase("")) {
								factionsStandingOn = C.boardData.get("chunkX" + j + " chunkY" + y + " chunkZ" + i)
										.toString();
							}
						} else if (C.boardData.has("chunkX" + j + " chunkY" + y + " chunkZ" + i)) {
							String mapFaction = C.boardData.get("chunkX" + j + " chunkY" + y + " chunkZ" + i)
									.toString();
							factionsFoundArray[factionsFound] = mapFaction;
							boolean newOne = true;
							int useKey = factionsFound + 1;

							for (int k = 0; k <= factionsFound; k++) {
								if (factionsFoundArray[k].equalsIgnoreCase(mapFaction)) {
									newOne = false;
									useKey = k;
								}
							}
							if (newOne) {
								factionsFound++;
								mapKey = mapKey + ", " + Factions.getFactionRelationColor(faction, mapFaction)
										+ mapFaction + " = " + mapSymbols[useKey];
							}
							map = map + Factions.getFactionRelationColor(faction, mapFaction) + mapSymbols[useKey];
						} else {
							map = map + "§7#§7";
						}
					}
				}
			}
		}
		String dashThing = "";
		String spaceThing = " ";

		for (int i = 36; i > factionsStandingOn.length(); i--) {
			dashThing = dashThing + "-";
		}
		sender.sendMessage(
				"§7" + spaceThing + dashThing + " §f[" + Factions.getFactionRelationColor(faction, factionsStandingOn)
						+ factionsStandingOn + "§f]§7 " + dashThing + " ");
		sender.sendMessage(map + "\n§3Layer: y" + y + "§f -- " + mapKey);
		return true;
	}

	private static JSONArray array;
	private static int i;

	public static int getFactionClaimedLand(String faction) {
		int claimedLand = 0;

		for (Iterator<World> localIterator = Bukkit.getServer().getWorlds().iterator(); localIterator.hasNext(); array
				.length()) {
			World w = (World) localIterator.next();
			loadWorld(w.getName());
			array = C.boardData.names();
			i = 0;
			String name = array.getString(i);
			if (C.boardData.getString(name).equalsIgnoreCase(faction)) {
				claimedLand++;
			}
			i++;
		}
		return claimedLand;
	}

	public static double getFactionPower(String faction) {
		double factionPower = 0.0D;

		OfflinePlayer[] off = Bukkit.getOfflinePlayers();
		Collection<? extends Player> on = Bukkit.getOnlinePlayers();
		for (int i = 0; i < off.length; i++) {
			if (!off[i].isOnline()) {
				C.loadPlayer(off[i].getUniqueId());
				if (C.playerData.getString("faction").equalsIgnoreCase(faction)) {
					if ((off.length + on.size() >= 30)
							&& (Config.configData.getString("power cap type").equalsIgnoreCase("soft"))) {
						factionPower = factionPower + 3 * Config.configData.getInt("power cap max power")
								* Math.exp(-(off.length + on.size()))
								/ (2.0D * Math.pow(10.0D * Math.exp(-(off.length + on.size())) + 1.0D, 2.0D))
								* (C.playerData.getDouble("power") / Config.configData.getDouble("max player power"));
					} else {
						factionPower += C.playerData.getDouble("power");
					}
				}
			}
		}
		for (Player player : on) {
			if (player.isOnline()) {
				C.loadPlayer(player.getUniqueId());
				if (C.playerData.getString("faction").equalsIgnoreCase(faction)) {
					if ((off.length + on.size() >= 30)
							&& (Config.configData.getString("power cap type").equalsIgnoreCase("soft"))) {
						factionPower = factionPower + 3 * Config.configData.getInt("power cap max power")
								* Math.exp(-(off.length + on.size()))
								/ (2.0D * Math.pow(10.0D * Math.exp(-(off.length + on.size())) + 1.0D, 2.0D))
								* (C.playerData.getDouble("power") / Config.configData.getDouble("max player power"));
					} else {
						factionPower += C.playerData.getDouble("power");
					}
				}
			}
		}
		String temp = C.factionData.getString("name");

		C.loadFaction(faction);
		if (C.factionData.getString("safezone").equalsIgnoreCase("true")) {
			factionPower = 9999999.0D;
		}
		if (C.factionData.getString("warzone").equalsIgnoreCase("true")) {
			factionPower = 9999999.0D;
		}
		C.loadFaction(temp);
		return factionPower;
	}

	public static boolean tryClaim(CommandSender sender) {
		Player player = (Player) sender;
		C.loadWorld(player.getWorld().getName());
		C.loadPlayer(((Player) sender).getUniqueId());
		String faction = C.playerData.getString("faction");

		if (faction.equalsIgnoreCase("")) {
			sender.sendMessage("§cYou're not in a faction");
			return true;
		}
		if ((!C.playerData.getString("factionRank").equalsIgnoreCase("leader"))
				&& (!C.playerData.getString("factionRank").equalsIgnoreCase("officer"))) {
			sender.sendMessage("§cYou have to be either an §c§lOfficer §c or a §c§lLeader §c to claim land");
			return true;
		}
		if (getFactionClaimedLand(faction) >= getFactionPower(faction)) {
			sender.sendMessage(
					"§cYou need more power. Staying online and having more members increase your faction power. Do §c§l/f help§c for more information");
			return true;
		}
		loadWorld(player.getWorld().getName());

		Config.loadConfig();

		int posX = player.getLocation().getBlockX();
		int chunkSizeX = Config.chunkSizeX;
		int posY = player.getLocation().getBlockY();
		int chunkSizeY = Config.chunkSizeY;
		int posZ = player.getLocation().getBlockZ();
		int chunkSizeZ = Config.chunkSizeZ;

		posX = Math.round(posX / chunkSizeX) * chunkSizeX;
		posY = Math.round(posY / chunkSizeY) * chunkSizeY;
		posZ = Math.round(posZ / chunkSizeZ) * chunkSizeZ;
		if (C.boardData.has("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ)) {
			if (C.boardData.getString("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ)
					.equalsIgnoreCase(faction)) {
				sender.sendMessage("§cYou already own this land.");
				return true;
			}
			String faction2 = C.boardData.getString("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ);

			C.loadFaction(faction2);
			if (C.factionData.getString("safezone").equalsIgnoreCase("true")) {
				sender.sendMessage("§cYou cannot claim over §c§lSafeZone");
				return true;
			}
			if (C.factionData.getString("warzone").equalsIgnoreCase("true")) {
				sender.sendMessage("§cYou cannot claim over a §c§lWarZone");
				return true;
			}
			if ((!faction2.equalsIgnoreCase("")) && (getFactionPower(faction2) >= getFactionClaimedLand(faction2))) {
				sender.sendMessage(Factions.getFactionRelationColor(faction, faction2) + faction2
						+ "§cowns this chunk.§c If you want it, you need to lower their power.");
				return true;
			}
		}
		C.boardData.put("name", player.getWorld().getName());
		C.boardData.put("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ, faction);

		C.saveWorld(C.boardData);
		messageFaction(faction, Config.Rel_Faction + sender.getName() + "§a claimed some land for " + Config.Rel_Faction
				+ faction + "§a");

		return true;
	}

	public static boolean tryUnClaim(CommandSender sender) {
		Player player = (Player) sender;
		loadWorld(player.getWorld().getName());
		C.loadPlayer(((Player) sender).getUniqueId());
		String factionName = C.playerData.getString("faction");
		Config.loadConfig();
		if (factionName.equalsIgnoreCase("")) {
			sender.sendMessage("§cYou're in a faction.");
			return true;
		}
		int posX = player.getLocation().getBlockX();
		int chunkSizeX = Config.chunkSizeX;
		int posY = player.getLocation().getBlockY();
		int chunkSizeY = Config.chunkSizeY;
		int posZ = player.getLocation().getBlockZ();
		int chunkSizeZ = Config.chunkSizeZ;

		posX = Math.round(posX / chunkSizeX) * chunkSizeX;
		posY = Math.round(posY / chunkSizeY) * chunkSizeY;
		posZ = Math.round(posZ / chunkSizeZ) * chunkSizeZ;
		if (C.boardData.has("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ)) {
			if (C.boardData.getString("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ).equalsIgnoreCase("")) {
				sender.sendMessage("§cThis area is not claimed");
				return true;
			}
			if (C.boardData.getString("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ)
					.equalsIgnoreCase(factionName)) {
				C.boardData.remove("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ);

				messageFaction(factionName, Config.Rel_Faction + sender.getName() + "§6 unclaimed " + "chunkX" + posX
						+ " chunkY" + posY + " chunkZ" + posZ);
				C.saveWorld(C.boardData);
				return true;
			}
			String faction2 = C.boardData.getString("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ);
			if (getFactionPower(faction2) < getFactionClaimedLand(faction2)) {
				messageFaction(factionName,
						Config.Rel_Faction + sender.getName() + "§6 unclaimed "
								+ Factions.getFactionRelationColor(factionName, faction2)
								+ Config.configData.getString("faction symbol left") + faction2
								+ Config.configData.getString("faction symbol right") + "§6's land!");
				messageFaction(faction2,
						Factions.getFactionRelationColor(faction2, factionName)
								+ Config.configData.getString("faction symbol left") + factionName
								+ Config.configData.getString("faction symbol right") + " " + sender.getName()
								+ "§6 unclaimed your land!");

				C.boardData.remove("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ);
				C.saveWorld(C.boardData);
				return true;
			}
			sender.sendMessage("§cYou could not unclaim " + Factions.getFactionRelationColor(factionName, faction2)
					+ Config.configData.getString("faction symbol left") + faction2
					+ Config.configData.getString("faction symbol right") + "§c's land!§7 They have too much power!");
			return true;
		}
		if ((C.playerData.has("autounclaim")) && (C.playerData.getString("autounclaim").equalsIgnoreCase("false"))) {
			sender.sendMessage("§cCould not unclaim chunk!");
		}
		return true;
	}
}
