package me.secretlovers.skywars.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.game.GameManager;
import me.secretlovers.skywars.game.kits.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;


public class KitShowGUI {

    public void open(Player player, Kit kit) {

        SGMenu menu = SkyWars.getSpiGUI().create("&c" + kit.getName(), 4);

        for (ItemStack itemStack : kit.getItems().values()) {
            if(itemStack == null) continue;

            SGButton button = new SGButton(itemStack);

            menu.addButton(button);
        }

        SGButton button = new SGButton(new ItemBuilder(Material.RED_STAINED_GLASS_PANE).
                name("&5Return to kits").
                build()).withListener((InventoryClickEvent event) -> {
                new KitChoiceGUI().open((Player) event.getWhoClicked());
        });

        menu.setButton(31 ,button);

        player.openInventory(menu.getInventory());

    }
}
