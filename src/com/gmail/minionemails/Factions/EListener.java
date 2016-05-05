package com.gmail.minionemails.Factions;

import java.io.File;
import java.util.Random;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;

public class EListener implements Listener {
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		File playerFile = new File(Factions.getInstance().getDataFolder() + "/playerData/"
				+ event.getPlayer().getUniqueId().toString() + ".json");
		if (!playerFile.exists()) {
			C.createPlayer(event.getPlayer());
		}
	}

	@EventHandler
	public void respawnEvent(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		C.loadPlayer(player.getUniqueId());
		if (!C.playerData.getString("faction").equals("")) {
			C.loadFaction(C.playerData.getString("faction"));
			String home = C.factionData.getString("home");
			if (!home.equalsIgnoreCase("")) {
				Scanner scan = new Scanner(home);
				String world = scan.next();
				double x = scan.nextDouble();
				double y = scan.nextDouble();
				double z = scan.nextDouble();
				scan.close();

				Location loc = new Location(Bukkit.getWorld(world), x, y, z);
				Block block = loc.getBlock();
				int i = 0;
				while ((block.getRelative(BlockFace.UP).getType() != Material.AIR)
						|| (block.getType() != Material.AIR)) {
					Random generater = new Random(
							System.currentTimeMillis() + block.getX() + block.getY() + block.getZ());
					block = block.getRelative(generater.nextInt(10) * i, generater.nextInt(10) * i,
							generater.nextInt(10) * i);

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
						loc = player.getLocation().getWorld().getSpawnLocation();
					}
				}
				loc = block.getLocation();
				event.setRespawnLocation(loc);
			}
		}
	}

	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event) {
		Entity entityAttacking = event.getDamager();
		Entity entityAttacked = event.getEntity();

		EntityDamageEvent.DamageCause damagedCause = event.getCause();
		if (((entityAttacking.getType() == EntityType.PLAYER) || (entityAttacking.getType() == EntityType.ARROW))
				&& (entityAttacked.getType() == EntityType.PLAYER)) {
			if (entityAttacking.getType() == EntityType.ARROW) {
				Arrow arrow = (Arrow) entityAttacking;
				if ((arrow instanceof Player)) {
					Player playerAttacked = (Player) entityAttacked;
					Player playerAttacking = (Player) arrow.getShooter();
					C.loadPlayer(playerAttacking.getUniqueId());
					String factionAttacking = C.playerData.getString("faction");
					C.loadPlayer(playerAttacked.getUniqueId());
					String factionAttacked = C.playerData.getString("faction");
					if (Config.configData.getString("friendly fire projectile (arrows)").equalsIgnoreCase("false")) {
						playerAttacking.sendMessage("§cYou cannot shoot members of §f" + " "
								+ Factions.getFactionRelationColor(factionAttacking, factionAttacked) + factionAttacked
								+ "§c!");
						event.setCancelled(true);
						return;
					}
				}
				return;
			}
			Player playerAttacking = (Player) entityAttacking;
			Player playerAttacked = (Player) entityAttacked;

			C.loadPlayer(playerAttacking.getUniqueId());
			String factionAttacking = C.playerData.getString("faction");
			C.loadPlayer(playerAttacked.getUniqueId());
			String factionAttacked = C.playerData.getString("faction");

			String inFactionLand = "neutral";

			Location loc = playerAttacked.getLocation();
			C.loadWorld(loc.getWorld().getName());
			int posX = loc.getBlockX();
			int posY = loc.getBlockY();
			int posZ = loc.getBlockZ();
			int chunkSizeX = Config.chunkSizeX;
			int chunkSizeY = Config.chunkSizeY;
			int chunkSizeZ = Config.chunkSizeZ;
			posX = Math.round(posX / chunkSizeX) * chunkSizeX;
			posY = Math.round(posY / chunkSizeY) * chunkSizeY;
			posZ = Math.round(posZ / chunkSizeZ) * chunkSizeZ;
			if (C.boardData.has("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ)) {
				inFactionLand = C.boardData.getString("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ);
				C.loadFaction(inFactionLand);
				if (C.factionData.has("peaceful")) {
					if (C.factionData.getString("peaceful").equalsIgnoreCase("true")) {
						playerAttacking.sendMessage("§cYou cannot hurt players in peaceful land");
						event.setCancelled(true);
					}
				} else {
					C.factionData.put("peaceful", "false");
					C.saveFaction(C.factionData);
				}
				if (C.factionData.has("safezone")) {
					if (C.factionData.getString("safezone").equalsIgnoreCase("true")) {
						playerAttacking.sendMessage("§cYou cannot hurt players in SafeZone");
						event.setCancelled(true);
					}
				} else {
					C.factionData.put("safezone", "false");
					C.saveFaction(C.factionData);
				}
			}
			C.loadFaction(factionAttacking);
			if (C.factionData.has("peaceful")) {
				if (C.factionData.getString("peaceful").equalsIgnoreCase("true")) {
					playerAttacking.sendMessage("§cPeaceful players cannot attack other players.");
				}
			} else {
				C.factionData.put("peaceful", "false");
				C.saveFaction(C.factionData);
			}
			C.loadFaction(factionAttacked);
			if (C.factionData.has("peaceful")) {
				if (C.factionData.getString("peaceful").equalsIgnoreCase("true")) {
					playerAttacking.sendMessage("§cYou cannot hurt peaceful players");
				}
			} else {
				C.factionData.put("peaceful", "false");
				C.saveFaction(C.factionData);
			}
			if (factionAttacking.equalsIgnoreCase(factionAttacked)) {
				C.loadFaction(inFactionLand);
				if ((damagedCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
						&& ((Config.configData.getString("friendly fire melee").equalsIgnoreCase("true"))
								|| (C.factionData.getString("warzone").equalsIgnoreCase("true")))) {
					playerAttacking.sendMessage("§a§lHit player!");
					return;
				}
				if (Config.configData.getString("friendly fire other").equalsIgnoreCase("false")) {
					playerAttacking.sendMessage("§cYou cannot hurt members of §f" + " "
							+ Factions.getFactionRelationColor(factionAttacking, factionAttacked) + factionAttacked);
					event.setCancelled(true);
					return;
				}
				return;
			}
			return;
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Location loc = e.getTo().getBlock().getLocation();
		Player player = e.getPlayer();
		World world = player.getWorld();
		String inFaction = "nneutral territory";

		C.loadWorld(world.getName());
		C.loadPlayer(player.getUniqueId());

		String playerFaction = C.playerData.getString("faction");

		int posX = loc.getBlockX();
		int posY = loc.getBlockY();
		int posZ = loc.getBlockZ();
		int chunkSizeX = Config.chunkSizeX;
		int chunkSizeY = Config.chunkSizeY;
		int chunkSizeZ = Config.chunkSizeZ;
		posX = Math.round(posX / chunkSizeX) * chunkSizeX;
		posY = Math.round(posY / chunkSizeY) * chunkSizeY;
		posZ = Math.round(posZ / chunkSizeZ) * chunkSizeZ;
		if (C.boardData.has("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ)) {
			inFaction = C.boardData.getString("chunkX" + posX + " chunkY" + posY + " chunkZ" + posZ);
		}
		int k = -1;
		for (int i = 0; i < C.playerIsIn_player.size(); i++) {
			if (((String) C.playerIsIn_player.get(i)).equalsIgnoreCase(player.getName())) {
				k = i;
			}
		}
		Location location = new Location(loc.getWorld(), posX, posY, posZ);
		if (k == -1) {
			C.playerIsIn_player.add(player.getName());
			C.playerIsIn_faction.add(inFaction);
			C.playerIsIn_location.add(location);
			k = 0;
		}
		if ((!((String) C.playerIsIn_faction.get(k)).equalsIgnoreCase(inFaction)) && (C.playerData.has("autoclaim"))
				&& (C.playerData.getString("autoclaim").equalsIgnoreCase("false")) && (C.playerData.has("autounclaim"))
				&& (C.playerData.getString("autounclaim").equalsIgnoreCase("false"))) {
			player.sendMessage("§a" + "You have traveled from §f" + " "
					+ Factions.getFactionRelationColor(playerFaction, (String) C.playerIsIn_faction.get(k))
					+ (String) C.playerIsIn_faction.get(k) + "§a " + "to §f" + " "
					+ Factions.getFactionRelationColor(playerFaction, inFaction) + inFaction + "§7.");
			C.playerIsIn_faction.set(k, inFaction);
		}
		if ((!((Location) C.playerIsIn_location.get(k)).equals(location))
				&& (!C.playerData.getString("faction").equalsIgnoreCase(inFaction))) {
			C.playerIsIn_location.set(k, location);
			if ((C.playerData.has("autoclaim")) && (C.playerData.getString("autoclaim").equalsIgnoreCase("true"))) {
				F.tryClaim(player);
			}
		}
		if ((!((Location) C.playerIsIn_location.get(k)).equals(location))
				&& (C.playerData.getString("faction").equalsIgnoreCase(inFaction))
				&& (!inFaction.equalsIgnoreCase("nneutral territory")) && (!inFaction.equalsIgnoreCase(""))
				&& (C.playerData.has("autounclaim"))
				&& (C.playerData.getString("autounclaim").equalsIgnoreCase("true"))) {
			F.tryUnClaim(player);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		event.setMessage(event.getMessage().replace(">", "§a>"));
		event.setMessage(event.getMessage().replaceAll("&", "§"));

		String playerName = event.getPlayer().getName();

		boolean hasEssentialsUtils = false;

		Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();
		Plugin[] arrayOfPlugin1;
		int j = (arrayOfPlugin1 = plugins).length;
		for (int i = 0; i < j; i++) {
			Plugin plugin = arrayOfPlugin1[i];
			if (plugin.getName().toLowerCase().contains("essentials")) {
				hasEssentialsUtils = true;
			}
			String playerNickname = "";
			if (playerNickname.equalsIgnoreCase("")) {
				playerNickname = playerName;
			}
			if (Config.configData.getString("enable simplefaction chat").equalsIgnoreCase("false")) {
				return;
			}
			int posX_talk = event.getPlayer().getLocation().getBlockX();
			int posZ_talk = event.getPlayer().getLocation().getBlockZ();

			C.loadPlayer(event.getPlayer().getUniqueId());
			String chatChannel_talk = C.playerData.getString("chat channel");
			String factionRank = C.playerData.getString("factionRank");
			String title = C.playerData.getString("factionTitle") + " ";
			String faction = C.playerData.getString("faction");
			String factionString = C.playerData.getString("faction").replaceAll("\\$", "\\\\\\$");
			factionRank = factionRank + " ";
			if (factionRank.contains("leader")) {
				factionRank = Config.configData.getString("faction leader tag");
			}
			if (factionRank.contains("officer")) {
				factionRank = Config.configData.getString("faction officer tag");
			}
			if (factionRank.contains("member")) {
				factionRank = "";
			}
			if (factionRank.equalsIgnoreCase(" ")) {
				factionRank = "";
			}
			String factionRelation = "";
			String faction2 = "";
			if (!Config.configData.getString("allow player titles").equalsIgnoreCase("true")) {
				title = "";
			}
			if (event.getMessage().charAt(0) == '!') {
				chatChannel_talk = "global";
				event.setMessage(event.getMessage().replace("!", ""));
			}
			if ((chatChannel_talk.equals("global"))
					&& (Config.configData.getString("disable simplefaction chat in global").equals("true"))) {
				return;
			}
			for (Player player : event.getRecipients()) {
				World world = player.getWorld();
				C.loadPlayer(event.getPlayer().getUniqueId());
				factionString = C.playerData.getString("faction");
				C.loadPlayer(player.getUniqueId());
				faction2 = C.playerData.getString("faction");
				String chatChannel_listen = C.playerData.getString("chat channel");
				if ((!faction.equalsIgnoreCase("")) && (!faction2.equalsIgnoreCase(""))) {
					factionRelation = Factions.getFactionRelationColor(faction2, faction);
				} else {
					factionRelation = Config.Rel_Other;
				}
				if (!faction.equalsIgnoreCase("")) {
					factionString = faction.replaceAll("\\$", "\\\\\\$") + " ";
				}
				if ((!faction.equalsIgnoreCase("")) && (!faction2.equalsIgnoreCase(""))) {
					C.loadFaction(faction);
				}
				JSONArray ignoreWorlds = Config.configData.getJSONArray("disable global chat in these worlds");
				JSONArray ignoreFactionChatWorlds = Config.configData
						.getJSONArray("disable faction(and ally/enemy/truce/etc) chat in these worlds");
				boolean skipThisPlayer = false;
				for (int w = 0; w < ignoreWorlds.length(); w++) {
					String ignoreWorld = ignoreWorlds.getString(w);
					if (ignoreWorld.equalsIgnoreCase(world.getName())) {
						skipThisPlayer = true;
					}
				}
				if (!chatChannel_talk.equalsIgnoreCase("global")) {
					for (int w = 0; w < ignoreFactionChatWorlds.length(); w++) {
						String ignoreWorld = ignoreWorlds.getString(w);
						if (ignoreWorld.equalsIgnoreCase(world.getName())) {
							skipThisPlayer = true;
						}
					}
				}
				if (!skipThisPlayer) {
					if (chatChannel_talk.equalsIgnoreCase("global")) {
						String message = "";
						if (Config.configData.getString("show faction data in global chat").equalsIgnoreCase("true")) {
							message = message + factionRelation + factionRank + factionString;
						}
						message = message + " §f(" + factionRelation + playerNickname + "§f): " + event.getMessage();
						if (Config.configData
								.getString("inject faction into message instead of replacing whole message")
								.equalsIgnoreCase("true")) {
							String format = event.getFormat();

							message = format.replaceFirst("%1\\$s",
									factionRelation + factionRank + factionString + playerNickname + "§f");
							message = message.replaceFirst("%2\\$s",
									"§f" + event.getMessage().replaceAll("\\$", "\\\\\\$"));
							message = message.replaceAll("\\$s", "§f");
						}
						player.sendMessage(message);
					} else if (chatChannel_talk.equalsIgnoreCase("faction")) {
						if (faction.equalsIgnoreCase(faction2)) {
							player.sendMessage(Config.Rel_Faction + "(" + faction + ") " + factionRelation + title
									+ factionRank + factionString + " §f(" + factionRelation + playerNickname + "§f): "
									+ event.getMessage());
						}
					} else if (chatChannel_talk.equalsIgnoreCase("ally")) {
						C.allyData = C.factionData.getJSONArray("allies");
						C.loadFaction(faction2);
						JSONArray allyData2 = C.factionData.getJSONArray("allies");
						C.loadFaction(faction);
						for (int l = 0; l < C.allyData.length(); l++) {
							if (C.allyData.getString(i).equalsIgnoreCase(faction2)) {
								for (int k = 0; k < allyData2.length(); k++) {
									if (allyData2.getString(k).equalsIgnoreCase(faction)) {
										player.sendMessage(Config.Rel_Ally + "(" + "ally" + ") " + factionRelation
												+ title + factionRank + factionString + " §f(" + factionRelation
												+ playerNickname + "§f): " + event.getMessage());
									}
								}
							}
						}
						if (faction.equalsIgnoreCase(faction2)) {
							player.sendMessage(Config.Rel_Ally + "(" + "ally" + ") " + factionRelation + title
									+ factionRank + factionString + " §f(" + factionRelation + playerNickname + "§f): "
									+ event.getMessage());
						}
					} else if (chatChannel_talk.equalsIgnoreCase("truce")) {
						C.truceData = C.factionData.getJSONArray("truce");

						C.truceData = C.factionData.getJSONArray("truce");
						C.loadFaction(faction2);
						JSONArray truceData2 = C.factionData.getJSONArray("truce");
						C.loadFaction(faction);
						for (int l = 0; l < C.truceData.length(); l++) {
							if (C.truceData.getString(l).equalsIgnoreCase(faction2)) {
								for (int k = 0; k < truceData2.length(); k++) {
									if (truceData2.getString(k).equalsIgnoreCase(faction)) {
										player.sendMessage(Config.Rel_Truce + "(" + "truce" + ") " + factionRelation
												+ title + factionRank + factionString + " §f(" + factionRelation
												+ playerNickname + "§f): " + event.getMessage());
									}
								}
							}
						}
						if (faction.equalsIgnoreCase(faction2)) {
							player.sendMessage(Config.Rel_Truce + "(" + "truce" + ") " + factionRelation + title
									+ factionRank + factionString + " §f(" + factionRelation + playerNickname + "§f): "
									+ event.getMessage());
						}
					} else if (chatChannel_talk.equalsIgnoreCase("enemy")) {
						C.enemyData = C.factionData.getJSONArray("enemies");
						for (int m = 0; m < C.enemyData.length(); m++) {
							if (C.enemyData.getString(m).equalsIgnoreCase(faction2)) {
								player.sendMessage(Config.Rel_Enemy + "(" + "enemy" + ") " + factionRelation + title
										+ factionRank + factionString + " §f(" + factionRelation + playerNickname
										+ "§f): " + event.getMessage());
							}
						}
						if (faction.equalsIgnoreCase(faction2)) {
							player.sendMessage(Config.Rel_Enemy + "(" + "enemy" + ") " + factionRelation + title
									+ factionRank + factionString + " §f(" + factionRelation + playerNickname + "§f): "
									+ event.getMessage());
						}
					} else {
						boolean treatchatlikeradio = false;
						if ((Config.configData.has("treat all chat like a radio")) && (Config.configData
								.getString("treat all chat like a radio").equalsIgnoreCase("true"))) {
							treatchatlikeradio = true;
						}
						if ((chatChannel_talk.equalsIgnoreCase("local")) || (treatchatlikeradio)) {
							String Direction = "";
							int posX_listen = player.getLocation().getBlockX();
							int posZ_listen = player.getLocation().getBlockZ();

							int distance = (int) Math.sqrt(
									Math.pow(posX_talk - posX_listen, 2.0D) + Math.pow(posZ_talk - posZ_listen, 2.0D));
							double direction = Math
									.toDegrees(Math.atan2(posZ_talk - posZ_listen, posX_talk - posX_listen + 0.001D));
							if ((direction > -15.0D) && (direction < 15.0D)) {
								Direction = "E";
							}
							if ((direction > -75.0D) && (direction < -14.0D)) {
								Direction = "NE";
							}
							if ((direction > -105.0D) && (direction < -74.0D)) {
								Direction = "N";
							}
							if ((direction > -165.0D) && (direction < -104.0D)) {
								Direction = "NW";
							}
							if ((direction < -164.0D) || (direction > 165.0D)) {
								Direction = "W";
							}
							if ((direction < 165.0D) && (direction > 105.0D)) {
								Direction = "SW";
							}
							if ((direction < 106.0D) && (direction > 75.0D)) {
								Direction = "S";
							}
							if ((direction < 76.0D) && (direction > 15.0D)) {
								Direction = "SE";
							}
							if ((distance < Config.configData.getInt("local chat distance"))
									&& (!player.getName().equalsIgnoreCase(event.getPlayer().getName()))) {
								String message_ = Config.Rel_Neutral + "(" + distance + Direction + ") ";
								if (Config.configData.getString("show faction data in local chat")
										.equalsIgnoreCase("true")) {
									message_ = message_ + factionRelation + title + factionRank + factionString;
								}
								message_ = message_ + " §f(" + factionRelation + playerNickname + "§f): "
										+ event.getMessage();
								player.sendMessage(message_);
							}
							if ((distance >= Config.configData.getInt("local chat distance"))
									&& (distance < Config.configData.getInt("local chat distance") * 1.5D)
									&& (!player.getName().equalsIgnoreCase(event.getPlayer().getName()))) {
								String message_ = Config.Rel_Neutral + "(you hear something " + distance + " blocks "
										+ Direction + " from you)";
								player.sendMessage(message_);
							}
							if (player.getName().equalsIgnoreCase(event.getPlayer().getName())) {
								String _message = Config.Rel_Neutral + "(local) ";
								if (Config.configData.getString("show faction data in local chat")
										.equalsIgnoreCase("true")) {
									_message = _message + factionRelation + title + " " + factionRank + factionString;
								}
								_message = _message + " §f(" + factionRelation + playerNickname + "§f): "
										+ event.getMessage();
								player.sendMessage(_message);
							}
						} else if (chatChannel_talk.equalsIgnoreCase(chatChannel_listen)) {
							player.sendMessage(Config.Rel_Other + "(" + chatChannel_talk + ") " + factionRelation
									+ factionRank + factionString + " §f(" + factionRelation + playerNickname + "§f): "
									+ event.getMessage());
						}
					}
				}
			}
			String loggerMessage = "[" + chatChannel_talk + "]" + " " + factionString + " (" + playerNickname + "): "
					+ event.getMessage();
			Bukkit.getLogger().info(loggerMessage);

			event.setCancelled(true);
		}
	}
}
