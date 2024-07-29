package me.secretlovers.skywars.commands.filters;

import me.secretlovers.skywars.commands.CommandFilter;
import me.secretlovers.skywars.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerOnlyFilter implements CommandFilter {
    @Override
    public boolean canContinue(CommandSender sender, Command baseCommand, SubCommand subCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs) {
        return sender instanceof Player;
    }

    @Override
    public String[] getDeclineMessage(CommandSender sender, Command baseCommand, SubCommand subCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs) {
        return new String[] {
                "You're not a Player!"
        };
    }
}
