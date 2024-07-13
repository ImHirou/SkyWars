package me.secretlovers.skywars.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class TeamChoiceGUI {

    public void open(Player player) {

        SGMenu menu = SkyWars.getSpiGUI().create("&cTeam Choice", 3);
        Game game = SkyWars.getInstance().getGameManager().getPlayerToGame().get(player);

        for(Team team : game.getTeams()) {

            List<String> lore = new ArrayList<>();
            lore.add("Players: ");
            for(Player p : team.getPlayers()) {
                lore.add(p.getDisplayName());
            }

            SGButton button = new SGButton(new ItemBuilder(team.getMaterial())
                    .name(team.getColor())
                    .lore(lore)
                    .build())
                    .withListener((InventoryClickEvent event) -> {
                Player p = (Player) event.getWhoClicked();
                if(game.getPlayerTeam().containsKey(p)) {
                    System.out.println("CONTAINS");
                    Team pastTeam = game.getPlayerTeam().get(p);
                    System.out.println(pastTeam.getColor());
                    game.getPlayerTeam().remove(p);
                    if(team.addPlayer(p)) {
                        System.out.println("Added to " + team.getColor());
                        pastTeam.removePlayer(p);
                        System.out.println("Removed from " + pastTeam.getColor());
                        System.out.println("Players past team " + pastTeam.getPlayers());
                        game.getPlayerTeam().put(p, team);
                    } else {
                        game.getPlayerTeam().put(p, pastTeam);
                    }
                }
                else {
                    if(team.addPlayer(p))
                        game.getPlayerTeam().put(p, team);
                }
                open(p);
            });

            menu.addButton(button);

        }

        player.openInventory(menu.getInventory());

    }

}
