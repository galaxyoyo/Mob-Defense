package fr.galaxyoyo.mobdefense;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MobDefenseExecutor implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (args.length != 1)
		{
			printUsage(sender);
			return true;
		}

		if (args[0].equalsIgnoreCase("start"))
		{
			MobDefense.instance().start(sender);
			return true;
		}

		printUsage(sender);
		return true;
	}

	public void printUsage(CommandSender sender)
	{
		sender.sendMessage(ChatColor.RED + "Usage : /mobdefense start");
	}
}
