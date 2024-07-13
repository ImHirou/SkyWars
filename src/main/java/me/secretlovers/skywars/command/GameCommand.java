package me.secretlovers.skywars.command;

import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.GameManager;
import me.secretlovers.skywars.game.GameState;
import me.secretlovers.skywars.gui.GamesGUI;
import me.secretlovers.skywars.gui.TeamChoiceGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommand implements CommandExecutor {


    public GameCommand() {

    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player player)) return true;

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
                gameManager.getGames().get(id).changeGameState(GameState.STARTING);
        }
        if (args[0].equals("leave")) {
            if (!gameManager.getPlayerToGame().containsKey(player)) return true;
            GameState gameState = gameManager.getPlayerToGame().get(player).getGameState();
            if (gameState != GameState.STARTING && gameState != GameState.PLAYING)
                gameManager.getPlayerToGame().get(player).removePlayer(player);
        }

        if (args[0].equals("create")) {
            if (args[1] == null) return true;
            gameManager.addGame(args[1]);
        }
        if (args[0].equals("team")) {
            TeamChoiceGUI gui = new TeamChoiceGUI();
            gui.open(player);
        }
        if (args[0].equals("menu")) {
            GamesGUI gui = new GamesGUI();
            gui.open(player);
        }

        return true;
    }



}
