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
			if (!sender.hasPermission("mobdefense.command.start"))
				return noPerm(sender);

			if (!Wave.checkForPath())
			{
				sender.sendMessage(ChatColor.RED + "[MobDefense] " + Messages.getMessages(sender).getNoPath());
				return true;
			}
			MobDefense.instance().start(sender);
			return true;
		}
		else if (args[0].equalsIgnoreCase("stop"))
		{
			if (!sender.hasPermission("mobdefense.command.stop"))
				return noPerm(sender);

			MobDefense.instance().stop(sender);
			return true;
		}
		else if (args[0].equalsIgnoreCase("nextWave"))
		{
			if (!sender.hasPermission("mobdefense.command.nextWave"))
				return noPerm(sender);

			if (!MobDefense.instance().isStarted())
			{
				sender.sendMessage(ChatColor.RED + "[MobDefense] " + Messages.getMessages(sender).getNoGame());
				return true;
			}
			MobDefense.instance().startNextWave();
			return true;
		}
		else if (args[0].equalsIgnoreCase("setloc"))
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "[MobDefense] " + Messages.getMessages(sender).getOnlyPlayers());
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
			{
				if (!sender.hasPermission("mobdefense.command.setloc.spawn"))
					return noPerm(sender);

				MobDefense.instance().getConfiguration().setSpawn(loc);
			}
			else if (args[1].equalsIgnoreCase("end"))
			{
				if (!sender.hasPermission("mobdefense.command.setloc.end"))
					return noPerm(sender);

				MobDefense.instance().getConfiguration().setEnd(loc);
			}
			else if (args[1].equalsIgnoreCase("playerSpawn"))
			{
				if (!sender.hasPermission("mobdefense.command.setloc.playerSpawn"))
					return noPerm(sender);

				MobDefense.instance().getConfiguration().setPlayerSpawn(loc);
			}
			else if (args[1].equalsIgnoreCase("npc"))
			{
				if (args.length == 2)
				{
					printUsage(sender);
					return true;
				}

				if (args[2].equalsIgnoreCase("towers"))
				{
					if (!sender.hasPermission("mobdefense.command.setloc.npc.towers"))
						return noPerm(sender);

					MobDefense.instance().getConfiguration().setNpcTowerLoc(loc);
				}
				else if (args[2].equalsIgnoreCase("upgrades"))
				{
					if (!sender.hasPermission("mobdefense.command.setloc.npc.upgrades"))
						return noPerm(sender);

					MobDefense.instance().getConfiguration().setNpcUpgradesLoc(loc);
				}
				else if (args[2].equalsIgnoreCase("exchange"))
				{
					if (!sender.hasPermission("mobdefense.command.setloc.npc.exchange"))
						return noPerm(sender);

					MobDefense.instance().getConfiguration().setNpcExchangeLoc(loc);
				}
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

			sender.sendMessage("[MobDefense] " + Messages.getMessages(sender).getLocationsSuccessDefined());
			return true;
		}

		printUsage(sender);
		return true;
	}

	public void printUsage(CommandSender sender)
	{
		String cmd = "/mobdefense <";
		if (sender.hasPermission("mobdefense.command.start"))
			cmd += "start | ";
		if (sender.hasPermission("mobdefense.command.stop"))
			cmd += "stop | ";
		if (sender.hasPermission("mobdefense.command.nextWave"))
			cmd += "nextWave | ";
		boolean setloc = false;
		if (sender.hasPermission("mobdefense.command.setloc.spawn"))
		{
			setloc = true;
			cmd += "setloc <spawn | ";
		}
		if (sender.hasPermission("mobdefense.command.setloc.end"))
		{
			if (!setloc)
			{
				setloc = true;
				cmd += "setloc <";
			}
			cmd += "end | ";
		}
		if (sender.hasPermission("mobdefense.command.setloc.playerSpawn"))
		{
			if (!setloc)
			{
				setloc = true;
				cmd += "setloc <";
			}
			cmd += "playerSpawn | ";
		}
		boolean npc = false;
		if (sender.hasPermission("mobdefense.command.setloc.npc.towers"))
		{
			if (!setloc)
			{
				setloc = true;
				cmd += "setloc <";
			}
			npc = true;
			cmd += "npc <towers | ";
		}
		if (sender.hasPermission("mobdefense.command.setloc.npc.upgrades"))
		{
			if (!setloc)
			{
				setloc = true;
				cmd += "setloc <";
			}
			if (!npc)
			{
				npc = true;
				cmd += "npc <";
			}
			cmd += "upgrades | ";
		}
		if (sender.hasPermission("mobdefense.command.setloc.npc.exchange"))
		{
			if (!setloc)
			{
				setloc = true;
				cmd += "setloc <";
			}
			if (!npc)
			{
				npc = true;
				cmd += "npc <";
			}
			cmd += "exchange";
		}
		if (cmd.endsWith(" | "))
			cmd = cmd.substring(0, cmd.length() - 3);
		if (npc)
			cmd += ">";
		if (setloc)
			cmd += ">";
		if (!cmd.equals("/mobdefense <"))
			sender.sendMessage(ChatColor.RED + "Usage: " + cmd + ">");
		else
			noPerm(sender);
	}

	public boolean noPerm(CommandSender sender)
	{
		sender.sendMessage(Messages.getMessages(sender).getNoPermission());
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> options;

		if (args.length == 1)
		{
			options = Lists.newArrayList("start", "stop", "nextWave", "setloc");
			options.removeIf(option ->
			{
				if (option.equals("setloc"))
				{
					String[] subs = {"spawn", "end", "playerSpawn"};
					for (String sub : subs)
					{
						if (sender.hasPermission("mobdefense.command.setloc." + sub))
							return false;
					}
					subs = new String[]{"towers", "upgrades", "exchange"};
					for (String sub : subs)
					{
						if (sender.hasPermission("mobdefense.command.setloc.npc." + sub))
							return false;
					}
					return true;
				}
				return !sender.hasPermission("mobdefense.command." + option);
			});
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("setloc"))
		{
			options = Lists.newArrayList("spawn", "end", "playerSpawn", "npc");
			options.removeIf(option ->
			{
				if (option.equals("npc"))
				{
					String[] subs = {"towers", "upgrades", "exchange"};
					for (String sub : subs)
					{
						if (sender.hasPermission("mobdefense.command.setloc.npc." + sub))
							return false;
					}
				}
				return !sender.hasPermission("mobdefense.command." + option);
			});
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase("setloc") && args[1].equalsIgnoreCase("npc"))
		{
			options = Lists.newArrayList("towers", "upgrades", "exchange");
			options.removeIf(option -> !sender.hasPermission("mobdefense.command.setloc.npc." + option));
		}
		else
			options = Lists.newArrayList();

		String lastArg = args[args.length - 1];
		options.removeIf(option -> !option.startsWith(lastArg.toLowerCase()));
		options.removeIf(option -> MobDefense.instance().isStarted() ? option.equalsIgnoreCase("start") : option.equalsIgnoreCase("stop") || option.equalsIgnoreCase("nextWave"));

		return options;
	}
}
