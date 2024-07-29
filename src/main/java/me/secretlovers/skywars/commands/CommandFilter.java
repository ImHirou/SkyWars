package me.secretlovers.skywars.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CommandFilter {

    boolean canContinue(CommandSender sender, Command baseCommand, SubCommand subCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs);

    String[] getDeclineMessage(CommandSender sender, Command baseCommand, SubCommand subCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs);

}
