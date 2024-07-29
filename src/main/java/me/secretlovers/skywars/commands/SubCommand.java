package me.secretlovers.skywars.commands;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.secretlovers.skywars.commands.conditions.PermissionCondition;
import me.secretlovers.skywars.commands.conditions.PlayerOnlyCondition;
import me.secretlovers.skywars.commands.filters.PermissionFilter;
import me.secretlovers.skywars.commands.filters.PlayerOnlyFilter;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SubCommand {

    @Getter
    String commandName;
    @Getter
    String help;
    @Getter
    String permission;
    List<String> argumentNames;
    List<CommandFilter> commandFilters;
    List<CommandPreCondition> helpConditions;

    public SubCommand(String commandName, boolean canConsoleExecute, String permission, String helpMessage) {
        Validate.notNull(commandName);
        this.commandName = commandName;
        this.help = (helpMessage == null ? "" : helpMessage);
        this.permission = permission;
        this.argumentNames = new ArrayList<>();
        this.commandFilters = new ArrayList<>();
        this.helpConditions = new ArrayList<>();
        this.helpConditions.add(new PermissionCondition());
        this.commandFilters.add(new PermissionFilter());
        if(!canConsoleExecute) {
            this.helpConditions.add(new PlayerOnlyCondition());
            this.commandFilters.add(new PlayerOnlyFilter());
        }

    }

    public SubCommand addArgumentNames(String... args) {
        argumentNames.addAll(Arrays.asList(args));
        return this;
    }

    public SubCommand addCommandFilter(CommandFilter filter) {
        commandFilters.add(filter);
        return this;
    }

    public SubCommand addCommandPreCondition(CommandPreCondition helpCondition) {
        helpConditions.add(helpCondition);
        return this;
    }

    public List<String> getArgumentNames() {
        return Collections.unmodifiableList(argumentNames);
    }

    public List<CommandFilter> getCommandFilters() {
        return Collections.unmodifiableList(commandFilters);
    }

    public List<CommandPreCondition> getHelpConditions() {
        return Collections.unmodifiableList(helpConditions);
    }

    public String getHelpMessage(String baseCommandLabel) {
        return CommandExecutorBase.getHelpMessage(this, baseCommandLabel);
    }

    public void runCommand(CommandSender sender, Command baseCommand, String baseCommandLabel, String subCommandLabel, String[] subCommandArgs) {
        
    }

    public List<String> tabComplete(CommandSender sender, Command baseCommand, String baseCommandLabel, SubCommand subCommand, String subCommandLabel, String[] subCommandArgs) {
        return Collections.emptyList();
    }

}
