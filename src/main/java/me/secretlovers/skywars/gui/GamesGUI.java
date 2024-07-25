package me.secretlovers.skywars.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.Game;
import me.secretlovers.skywars.game.GameManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class GamesGUI {

    public void open(Player player) {

        SGMenu menu = SkyWars.getSpiGUI().create("&cGame Choice", 6);
        GameManager gameManager = SkyWars.getInstance().getGameManager();

        for(Game game : gameManager.getGames().values()) {

            List<String> lore = new ArrayList<>();
            lore.add("&fPlayers: " + game.getPlayers().size() + "/" + game.getTeams().size() * game.getTeams().get(0).getMaxPlayers());
            for(Player p : game.getPlayers()) {
                lore.add("&f" + p);
            }

            SGButton button = new SGButton(new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                    .name("&a" + game.getMapName() + "-" + game.getId())
                    .lore(lore)
                    .build())
                    .withListener((InventoryClickEvent event) -> {
                Player p = (Player) event.getWhoClicked();
                if(game.getMaxPlayers() != game.getPlayers().size()) {
                    game.addPlayer(p);
                } else {
                    open(p);
                }
            });

            menu.addButton(button);

        }

        player.openInventory(menu.getInventory());

    }

}
