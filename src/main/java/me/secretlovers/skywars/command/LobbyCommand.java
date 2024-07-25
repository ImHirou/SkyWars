package me.secretlovers.skywars.command;

import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        GameManager gameManager = SkyWars.getInstance().getGameManager();
        Player player = (Player) sender;
        if(gameManager.getPlayerToGame().containsKey(player))
            gameManager.getPlayerToGame().get(player).removePlayer(player);
        if(!SkyWars.getInstance().getLobby().getWorld().getPlayers().contains(player)) SkyWars.getInstance().getLobby().teleportPlayer((Player) sender);
        return true;
    }
}
