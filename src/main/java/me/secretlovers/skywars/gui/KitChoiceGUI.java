package me.secretlovers.skywars.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.database.PlayerData;
import me.secretlovers.skywars.database.PlayerManager;
import me.secretlovers.skywars.game.kits.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KitChoiceGUI {

    public void open(Player player) {

        SGMenu menu = SkyWars.getSpiGUI().create("&cKits", 6);
        PlayerData data = PlayerManager.data.get(player);

        for(Kit kit : Kit.getKits().values().stream().sorted(Comparator.comparing(Kit::getCost)).collect(Collectors.toList())) {

            if(kit.getCost() <= 0 && !data.getKits().contains(kit.getName()))
                data.getKits().add(kit.getName());


            List<String> lore = new ArrayList<>();
            System.out.println(data.getSelectedKit());
            System.out.println(kit.getName());
            if(data.getKits().contains(kit.getName())) {
                lore.add("&aUnlocked");
                if(data.getSelectedKit().equals(kit.getName())) lore.add("&2Selected");
                else lore.add("&o&dLeft click to select");
            }
            else {
                lore.add("&e" + kit.getCost() + " Gold");
                if(data.getGold() >= kit.getCost()) lore.add("&o&dLeft click to buy");
                else lore.add("&cYou don't have enough money");
            }
            lore.add("&o&dRight click to see class");
            SGButton button = new SGButton(new ItemBuilder(kit.getItems().get(0).clone()).
                    name(kit.getName()).
                    lore(lore).
                    build()).withListener((InventoryClickEvent event) -> {
                Player p = (Player) event.getWhoClicked();
                InventoryAction action = event.getAction();
                if(action == InventoryAction.PICKUP_ALL) {
                    if (data.getKits().contains(kit.getName())) {
                        if (data.getSelectedKit().equals(kit.getName())) {
                            p.sendMessage("You already selected this kit");
                            p.closeInventory();
                        } else {
                            data.setSelectedKit(kit.getName());
                            open(p);
                            SkyWars.getInstance().getPlayerManager().savePlayer(data);
                        }
                    } else {
                        if (data.getGold() >= kit.getCost()) {
                            data.getKits().add(kit.getName());
                            data.setGold(data.getGold() - kit.getCost());
                            open(p);
                        } else {
                            p.sendMessage("You don't have enough money");
                            p.closeInventory();
                        }
                    }
                } else if(action == InventoryAction.PICKUP_HALF) {
                    KitShowGUI kitGui = new KitShowGUI();
                    kitGui.open(p, kit);
                }
                event.setCancelled(true);
            });

            menu.addButton(button);
        }

        player.openInventory(menu.getInventory());

    }

}
