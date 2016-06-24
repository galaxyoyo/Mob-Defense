package fr.galaxyoyo.mobdefense;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

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
		else if (args[0].equalsIgnoreCase("nextWave"))
		{
			if (!MobDefense.instance().isStarted())
			{
				sender.sendMessage(ChatColor.RED + "[MobDefense] Error: no game is started.");
				return true;
			}
			MobDefense.instance().startNextWave();
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
			loc.setYaw(NumberConversions.floor(loc.getYaw() / 45) * 45);
			loc.setPitch(NumberConversions.floor(loc.getPitch() / 45) * 45);

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
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> options;

		if (args.length == 1)
			options = Lists.newArrayList("start", "stop", "nextWave", "setloc");
		else if (args.length == 2 && args[0].equalsIgnoreCase("setloc"))
			options = Lists.newArrayList("spawn", "end", "playerSpawn", "npc");
		else if (args.length == 3 && args[0].equalsIgnoreCase("setloc") && args[1].equalsIgnoreCase("npc"))
			options = Lists.newArrayList("towers", "upgrades", "exchange");
		else
			options = Lists.newArrayList();

		String lastArg = args[args.length - 1];
		options.removeIf(option -> !option.startsWith(lastArg.toLowerCase()));
		options.removeIf(option -> MobDefense.instance().isStarted() ? option.equalsIgnoreCase("start") : option.equalsIgnoreCase("stop") || option.equalsIgnoreCase("nextWave"));

		return options;
	}
}
