package me.secretlovers.skywars.command;

import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.GameManager;
import me.secretlovers.skywars.game.GameState;
import me.secretlovers.skywars.game.phases.Phase;
import me.secretlovers.skywars.gui.GamesGUI;
import me.secretlovers.skywars.gui.KitChoiceGUI;
import me.secretlovers.skywars.gui.TeamChoiceGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class GameCommand implements CommandExecutor, TabCompleter {


    public GameCommand() {

    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player )) return true;

        Player player = (Player) sender;

        GameManager gameManager = SkyWars.getInstance().getGameManager();

        if (args[0].equals("join")) {
            if (args[1] == null) return true;
            int id = Integer.parseInt(args[1]);
            if(gameManager.getGames().containsKey(id))
                gameManager.getGames().get(id).addPlayer(player);
        }
        if (args[0].equals("start")) {
            if (args[1] == null) return true;
            int id = Integer.parseInt(args[1]);
            if(gameManager.getGames().containsKey(id))
                gameManager.getGames().get(id).nextPhase();
        }
        if (args[0].equals("leave")) {
            if (!gameManager.getPlayerToGame().containsKey(player)) return true;
            gameManager.getPlayerToGame().get(player).removePlayer(player);
        }
        if(args[0].equals("send")) {
            int id = Integer.parseInt(args[1]);
            Player p = Bukkit.getPlayer(args[2]);
            if(gameManager.getPlayerToGame().containsKey(p))
                gameManager.getPlayerToGame().get(p).removePlayer(p);
            gameManager.getGames().get(id).addPlayer(p);
        }

        if (args[0].equals("create")) {
            if (args[1] == null) return true;
            gameManager.addGame(args[1]);
        }
        if (args[0].equals("team")) {
            if(!SkyWars.getInstance().getGameManager().getPlayerToGame().containsKey(player)) return true;
            TeamChoiceGUI gui = new TeamChoiceGUI();
            gui.open(player);
        }
        if (args[0].equals("menu")) {
            GamesGUI gui = new GamesGUI();
            gui.open(player);
        }
        if (args[0].equals("kits")) {
            KitChoiceGUI gui = new KitChoiceGUI();
            gui.open(player);
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if(args.length==1) {
            return Arrays.asList(
                    "menu",
                    "leave");
        }
        else {
            return null;
        }
    }
}
