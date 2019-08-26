package com.MoofIT.Minecraft.Cenotaph;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CenotaphCommand implements CommandExecutor {
	private Cenotaph plugin;

	public CenotaphCommand(Cenotaph instance) {
		this.plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		Player p = (Player)sender;
		String cmd = command.getName();
		if (cmd.equalsIgnoreCase("cenlist")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphlist")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}
			ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(p.getName());
			if (pList == null) {
				plugin.sendMessage(p, "You have no cenotaphs.");
				return true;
			}
			plugin.sendMessage(p, "Cenotaph List:");
			int i = 0;
			long cTime = System.currentTimeMillis() / 1000;
			for (TombBlock tomb : pList) {
				i++;
				if (tomb.getBlock() == null) continue;
				String message;
				message = " " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + tomb.getBlock().getX() + "," + tomb.getBlock().getY() + "," + tomb.getBlock().getZ() + ")";
				if (tomb.getLocketteSign() != null) {
					message = message + " [Locked " + plugin.convertTime( (int) (CenotaphSettings.securityTimeOut() - (cTime - tomb.getTime())) ) + "]";
				}				
				plugin.sendMessage(p, message);
			}
			return true;
		} else if (cmd.equalsIgnoreCase("cenfind")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphfind")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}

			TombBlock tBlock = getBlockByIndex(p.getName(), args.length == 0 ? "last" : args[0]);
			if (tBlock == null) {
				plugin.sendMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
				return true;
			}
			double degrees = (plugin.getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
			p.setCompassTarget(tBlock.getBlock().getLocation());
			plugin.sendMessage(p, "Course: " + degrees + " (" + Cenotaph.getDirection(degrees) + "). Your compass has been set to point that direction. Use /cenreset to reset it to your spawn point.");
			return true;
		} else if (cmd.equalsIgnoreCase("ceninfo")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphinfo") && !p.hasPermission("cenotaph.cmd.cenotaphtime")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}
			TombBlock tBlock = getBlockByIndex(p.getName(), args.length == 0 ? "last" : args[0]);
			if (tBlock == null) {
				plugin.sendMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
				return true;
			}

			plugin.sendMessage(p, centimeMsg(tBlock));
			return true;
		} else if (cmd.equalsIgnoreCase("cenreset")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphreset")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}
			p.setCompassTarget(p.getWorld().getSpawnLocation());
			return true;
		}
		else if (cmd.equalsIgnoreCase("cenadmin")) {
			String playerName = null;
			if (!p.hasPermission("cenotaph.admin")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}
			if (args.length == 0) {
				plugin.sendMessage(p, "Usage: /cenadmin list");
				plugin.sendMessage(p, "Usage: /cenadmin list <player>");
				plugin.sendMessage(p, "Usage: /cenadmin find <player> <#>");
				plugin.sendMessage(p, "Usage: /cenadmin remove <player> <#>");
				plugin.sendMessage(p, "Usage: /cenadmin version");
				plugin.sendMessage(p, "Usage: /cenadmin reload");
				return true;
			}
			if (args.length > 1) {
				try {
					playerName = plugin.getServer().getPlayer(args[1]).getName();
				} catch (NullPointerException e) {
					plugin.sendMessage(p, "Player" + args[1] + " not found.");
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("list")) {
				if (!p.hasPermission("cenotaph.admin.list")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				if (args.length < 2) {
					if (Cenotaph.playerTombList.keySet().isEmpty()) {
						plugin.sendMessage(p, "There are no cenotaphs.");
						return true;
					}
					plugin.sendMessage(p, "Players with cenotaphs:");
					for (String player : Cenotaph.playerTombList.keySet()) {
						plugin.sendMessage(p, player);
					}
					return true;
				}
				ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(playerName);
				if (pList == null) {
					plugin.sendMessage(p, "No cenotaphs found for " + playerName + ".");
					return true;
				}
				plugin.sendMessage(p, "Cenotaph List:");
				int i = 0;
				for (TombBlock tomb : pList) {
					i++;
					if (tomb.getBlock() == null) continue;
					plugin.sendMessage(p, " " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + tomb.getBlock().getX() + "," + tomb.getBlock().getY() + "," + tomb.getBlock().getZ() + ")");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("find")) {
				if (!p.hasPermission("cenotaph.admin.find")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				TombBlock tBlock = getBlockByIndex(playerName, args.length < 3 ? "last" : args[2]);
				if (tBlock == null) {
					plugin.sendMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
					return true;
				}

				double degrees = (plugin.getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
				int X = tBlock.getBlock().getX();
				int Y = tBlock.getBlock().getY();
				int Z = tBlock.getBlock().getZ();
				plugin.sendMessage(p,"Location:" + X + "," + Y + "," + Z + ", to the " + Cenotaph.getDirection(degrees) + ".");
				return true;
			} else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("time")) {
				if (!p.hasPermission("cenotaph.admin.cenotaphinfo") && !p.hasPermission("cenotaph.admin.cenotaphtime")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				TombBlock tBlock = getBlockByIndex(playerName, args.length < 3 ? "last" : args[2]);
				if (tBlock == null) {
					plugin.sendMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
					return true;
				}

				plugin.sendMessage(p, centimeMsg(tBlock));
				return true;
			} else if (args[0].equalsIgnoreCase("version")) {
				String message;
				message = plugin.getVersion();
				plugin.sendMessage(p, message);

			} else if (args[0].equalsIgnoreCase("remove")) {
				if (!p.hasPermission("cenotaph.admin.remove")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(playerName);
				if (pList == null) {
					plugin.sendMessage(p, "No cenotaphs found for " + playerName + ".");
					return true;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					plugin.sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					plugin.sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				TombBlock tBlock = pList.get(slot);
				plugin.destroyCenotaph(tBlock);
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!p.hasPermission("cenotaph.admin.reload")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				plugin.loadSettings();
				Cenotaph.log.info("[Cenotaph] Configuration reloaded from file.");
				plugin.sendMessage(p, "Configuration reloaded from file.");
			} else {
				plugin.sendMessage(p, "Usage: /cenadmin list");
				plugin.sendMessage(p, "Usage: /cenadmin list <player>");
				plugin.sendMessage(p, "Usage: /cenadmin find <player> <#>");
				plugin.sendMessage(p, "Usage: /cenadmin remove <player> <#>");
				plugin.sendMessage(p, "Usage: /cenadmin version");
				return true;
			}
			return true;
		}
		return false;
	}

	private TombBlock getBlockByIndex(String playerName,String index) {
		ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(playerName);
		int slot = 0;

		if (pList == null) return null;

		try {
			slot = Integer.parseInt(index);
		} catch (NumberFormatException e) {
			slot = pList.size();
		}
		slot -= 1;

		if (slot < 0 || slot >= pList.size()) return null;

		return pList.get(slot);
	}

	private String centimeMsg(TombBlock tBlock) {
		long cTime = System.currentTimeMillis() / 1000;

		int breakTime = (CenotaphSettings.levelBasedRemoval() ? Math.min(tBlock.getOwnerLevel() + 1 * CenotaphSettings.levelBasedTime(), CenotaphSettings.cenotaphRemoveTime()) : CenotaphSettings.cenotaphRemoveTime());
		int secTimeLeft = (int)((tBlock.getTime() + CenotaphSettings.securityTimeOut()) - cTime);
		int remTimeLeft = (int)((tBlock.getTime() + breakTime) - cTime);

		String msg = ChatColor.YELLOW + "Security: " + ChatColor.WHITE;
		if (tBlock.getLwcEnabled()) msg += "LWC ";
		else if (tBlock.getLocketteSign() != null) msg += "Lockette ";
		else msg += "None ";
		if (CenotaphSettings.securityRemove()) msg += ChatColor.YELLOW + "SecTime: " + ChatColor.WHITE + (CenotaphSettings.securityTimeOut() < breakTime && CenotaphSettings.cenotaphRemove() && !CenotaphSettings.keepUntilEmpty() ? plugin.convertTime(secTimeLeft) : "Inf" ) + " ";
		msg += ChatColor.YELLOW + "BreakTime: " + ChatColor.WHITE + (CenotaphSettings.cenotaphRemove() ? plugin.convertTime(remTimeLeft) : "Inf") + " ";
		if (CenotaphSettings.removeWhenEmpty() || CenotaphSettings.keepUntilEmpty()) {
			msg += ChatColor.YELLOW + "BreakOverride: " + ChatColor.WHITE;
			if (CenotaphSettings.removeWhenEmpty()) msg += "Break on empty";
			if (CenotaphSettings.removeWhenEmpty() && CenotaphSettings.keepUntilEmpty()) msg += " & ";
			if (CenotaphSettings.keepUntilEmpty()) msg += "Keep until empty";
		}

		return msg;
	}
}
