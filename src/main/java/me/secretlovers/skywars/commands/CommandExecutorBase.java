package me.secretlovers.skywars.commands;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.secretlovers.skywars.utils.ArrayUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommandExecutorBase implements TabExecutor {

    Map<String, SubCommand> alliasToCommandMap = new HashMap<>();
    List<SubCommand> subCommands = new ArrayList<>();
    String commandPermission;

    public CommandExecutorBase(String commandPermission) {
        this.commandPermission = commandPermission;
    }

    public final void addSubCommand(SubCommand subCommand) {
        Validate.notNull(subCommand);
        subCommands.add(subCommand);
        alliasToCommandMap.put(subCommand.getCommandName(), subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        SubCommand subCommand = getSubCommand(sender, label, args);
        if(subCommand != null) {
            String[] subCommandArgs = ArrayUtil.getSubArray(args, 1, args.length -1);
            if(checkFilters(sender, cmd, subCommand, label, args[0], subCommandArgs)) {
                subCommand.runCommand(sender, cmd, label, args[0], subCommandArgs);
            }
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0 || args[0].isEmpty()) {
            ArrayList<String> result = new ArrayList<>();
            for(Map.Entry<String, SubCommand> entry : alliasToCommandMap.entrySet())
                if(hasHelpConditions(sender, entry.getValue()))
                    result.add(entry.getKey());
            return result;
        } else if(args.length == 1) {
            ArrayList<String> result = new ArrayList<>();
            for(Map.Entry<String, SubCommand> entry : alliasToCommandMap.entrySet())
                if(entry.getKey().startsWith(args[0].toLowerCase()))
                    if(hasHelpConditions(sender, entry.getValue()))
                        result.add(entry.getKey());
            return result;
        } else {
            SubCommand subCommand = alliasToCommandMap.get(args[0].toLowerCase());
            if(subCommand != null && hasHelpConditions(sender, subCommand))
                return subCommand.tabComplete(sender, cmd, label, subCommand, args[0], ArrayUtil.getSubArray(args, 1, args.length - 1));
            else
                return Collections.emptyList();
        }
    }

    private void sendInvalidSubCommandMessage(CommandSender sender, String label, String[] args) {
        sender.sendMessage("Invalid subCommand " + label + " " + Arrays.toString(args));
    }

    private void sendHelpMessage(CommandSender sender, String baseCommandLabel) {
        sender.sendMessage("Help: ");
        for(SubCommand subCommandVar : subCommands) {
            if(hasHelpConditions(sender, subCommandVar)) {
                sender.sendMessage(getHelpMessage(subCommandVar, baseCommandLabel));
            }
        }
    }

    private void sendNoPermissionMessage(CommandSender sender, String label) {
        sender.sendMessage("No permission " + label);
    }

    SubCommand getSubCommand(CommandSender sender, String label, String[] args) {
        if (!hasPermission(sender)) {
            sendNoPermissionMessage(sender, label);
            return null;
        }
        if(args.length < 1) {
            sendHelpMessage(sender, label);
            return null;
        }
        SubCommand command = alliasToCommandMap.get(args[0].toLowerCase());
        if(command == null) {
            sendInvalidSubCommandMessage(sender, label, args);
            return null;
        }
        return command;
    }

    boolean hasPermission(CommandSender sender) {
        return (commandPermission == null || sender.hasPermission(commandPermission) || !(sender instanceof Player));
    }

    public static String getHelpMessage(SubCommand subCommand, String baseCommandLabel) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(subCommand.getCommandName());
        if (!subCommand.getArgumentNames().isEmpty()) {
            for (String argument : subCommand.getArgumentNames()) {
                resultBuilder.append(argument);
            }
        }
        resultBuilder.append(subCommand.getHelp());
        return resultBuilder.toString();
    }

    public static boolean hasHelpConditions(CommandSender sender, SubCommand subCommand) {
        for (CommandPreCondition condition : subCommand.getHelpConditions()) {
            if (!condition.canContinue(sender, subCommand)) {
                return false;
            }
        }
        return true;
    }

    static boolean checkFilters(CommandSender sender, Command baseCommand, SubCommand subCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs) {
        for(CommandFilter filter : subCommand.getCommandFilters()) {
            if(!filter.canContinue(sender, baseCommand, subCommand, baseCommandLabel, subCommandLabel, subCommandArgs)) {
                sender.sendMessage(filter.getDeclineMessage(sender, baseCommand, subCommand, baseCommandLabel, subCommandLabel, subCommandArgs));
                return false;
            }
        }
        return true;
    }

}
