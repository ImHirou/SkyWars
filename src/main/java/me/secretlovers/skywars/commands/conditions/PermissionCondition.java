package me.secretlovers.skywars.commands.conditions;

import me.secretlovers.skywars.commands.CommandPreCondition;
import me.secretlovers.skywars.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class PermissionCondition implements CommandPreCondition {
    @Override
    public boolean canContinue(CommandSender sender, SubCommand subCommand) {
        String permission = subCommand.getPermission();
        return permission == null || sender instanceof ConsoleCommandSender || sender.hasPermission(permission);
    }
}
