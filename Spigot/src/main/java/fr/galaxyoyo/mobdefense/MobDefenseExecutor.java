package fr.galaxyoyo.mobdefense;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class MobDefenseExecutor implements CommandExecutor, TabCompleter
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (args.length == 0)
		{
			printUsage(sender);
			return true;
		}

		if (args[0].equalsIgnoreCase("start"))
		{
			if (!Wave.checkForPath())
			{
				sender.sendMessage(ChatColor.RED + "[MobDefense] Error: no path can be found. Please update the map.");
				return true;
			}
			MobDefense.instance().start(sender);
			return true;
		}
		else if (args[0].equalsIgnoreCase("stop"))
		{
			MobDefense.instance().stop(sender);
			return true;
		}
		else if (args[0].equalsIgnoreCase("setloc"))
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "[MobDefense] Only players are able set positions.");
				return true;
			}

			if (args.length == 1)
			{
				printUsage(sender);
				return true;
			}

			Player player = (Player) sender;
			Location loc = player.getLocation().clone();
			loc.setX(loc.getBlockX() + 0.5D);
			loc.setY(loc.getBlockY());
			loc.setZ(loc.getBlockZ() + 0.5D);
			loc.setYaw(((int) (loc.getYaw() - 20) / 4) * 4 + 20);
			loc.setPitch(((int) (loc.getYaw() - 20) / 4) * 4 + 20);

			if (args[1].equalsIgnoreCase("spawn"))
				MobDefense.instance().setSpawn(loc);
			else if (args[1].equalsIgnoreCase("end"))
				MobDefense.instance().setEnd(loc);
			else if (args[1].equalsIgnoreCase("playerSpawn"))
				MobDefense.instance().setPlayerSpawn(loc);
			else if (args[1].equalsIgnoreCase("npc"))
			{
				if (args.length == 2)
				{
					printUsage(sender);
					return true;
				}

				if (args[2].equalsIgnoreCase("towers"))
					MobDefense.instance().setNpcTowerLoc(loc);
				else if (args[2].equalsIgnoreCase("upgrades"))
					MobDefense.instance().setNpcUpgradesLoc(loc);
				else if (args[2].equalsIgnoreCase("exchange"))
					MobDefense.instance().setNpcExchangeLoc(loc);
				else
				{
					printUsage(sender);
					return true;
				}
			}
			else
			{
				printUsage(sender);
				return true;
			}

			sender.sendMessage("[MobDefense] Location successfully defined!");
			MobDefense.instance().saveConfig();
			return true;
		}

		printUsage(sender);
		return true;
	}

	public void printUsage(CommandSender sender)
	{
		sender.sendMessage(ChatColor.RED + "Usage : /mobdefense <start | stop | setloc <spawn | end | playerSpawn | npc <towers | " +
				"upgrades | exchange>>>");
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings)
	{
		return null;
	}
}
