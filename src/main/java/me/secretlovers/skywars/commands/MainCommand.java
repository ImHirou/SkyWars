package me.secretlovers.skywars.commands;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.PluginCommand;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MainCommand {

    CommandExecutorBase base;

    public MainCommand() {
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
