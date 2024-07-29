package me.secretlovers.skywars.commands;

import org.bukkit.command.CommandSender;

public interface CommandPreCondition {

    boolean canContinue(CommandSender sender, SubCommand subCommand);

}
