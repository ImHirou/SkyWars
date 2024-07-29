package me.secretlovers.skywars.commands;

import org.bukkit.command.PluginCommand;

public class SetupCommand {

    CommandExecutorBase base;

    public SetupCommand() {
        this.base = new CommandExecutorBase(null);
        initCommands();
    }

    private void initCommands() {

    }

    public void latchOnto(PluginCommand command) {
        if(command != null) {
            command.setDescription("desc");
            command.setExecutor(base);
            command.setUsage("</command>");
            command.setPermission(null);
        }
    }

}
