package me.secretlovers.skywars.commands.filters;

import me.secretlovers.skywars.commands.CommandFilter;
import me.secretlovers.skywars.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class PermissionFilter implements CommandFilter {
    @Override
    public boolean canContinue(CommandSender sender, Command baseCommand, SubCommand subCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs) {
        String permission = subCommand.getPermission();
        return permission == null || sender instanceof ConsoleCommandSender || sender.hasPermission(permission);
    }

    @Override
    public String[] getDeclineMessage(CommandSender sender, Command baseCommand, SubCommand subCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs) {
        return new String[] {
                ChatColor.RED + "You haven't permission"
        };
    }
}
