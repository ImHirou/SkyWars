package me.secretlovers.skywars.commands.conditions;

import me.secretlovers.skywars.commands.CommandPreCondition;
import me.secretlovers.skywars.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerOnlyCondition implements CommandPreCondition {
    @Override
    public boolean canContinue(CommandSender sender, SubCommand subCommand) {
        return sender instanceof Player;
    }
}
