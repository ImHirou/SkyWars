package me.secretlovers.skywars.lobby;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import me.secretlovers.skywars.SkyWars;
import me.secretlovers.skywars.utils.LocationUtil;
import me.secretlovers.skywars.utils.PlayerUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.UUID;

@Getter
public class Lobby implements Listener {

    World world;
    EntityPlayer gameMenuNPC;

    public Lobby() {
        File lobbyWorldFolder = new File(
                Bukkit.getWorldContainer(),
                "lobby"
        );
        if (!lobbyWorldFolder.exists()) lobbyWorldFolder.mkdirs();
        try {
            FileUtils.copyDirectory(new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/maps/lobby"), lobbyWorldFolder);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        Bukkit.getScheduler().runTask(SkyWars.getInstance(), new Runnable() {
            @Override
            public void run() {

                WorldCreator creator = new WorldCreator(lobbyWorldFolder.getName());
                world = creator.createWorld();
                world.setKeepSpawnInMemory(false);
                world.setAutoSave(false);
                world.setDifficulty(Difficulty.PEACEFUL);

            }

        });
        setUpNPCs();
    }

    private void setUpNPCs() {

        new BukkitRunnable() {

            @Override
            public void run() {
                if(world != null) {
                    MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
                    WorldServer nmsWorld = ((CraftWorld) world).getHandle();

                    GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "Play");

                    gameMenuNPC = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));
                    JsonObject npc = SkyWars.getInstance().getJsonConfig().getAsJsonObject("lobby").getAsJsonObject("npc");
                    JsonObject gmLoc = npc.getAsJsonObject("gameMenuNPC").getAsJsonObject("location");
                    gameMenuNPC.setLocation(gmLoc.get("x").getAsDouble(), gmLoc.get("y").getAsDouble(), gmLoc.get("z").getAsDouble(), 0, 0);

                    SkyWars.getInstance().getProtocolManager().addPacketListener(new PacketAdapter(SkyWars.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
                        @Override
                        public void onPacketReceiving(PacketEvent event) {

                            Player player = event.getPlayer();
                            PacketContainer packet = event.getPacket();

                            if (packet.getType() == PacketType.Play.Client.USE_ENTITY) {
                                int entityId = packet.getIntegers().read(0);

                                if (entityId == gameMenuNPC.getId()) {
                                    Bukkit.getScheduler().runTask(SkyWars.getInstance(), () -> player.performCommand("game menu"));
                                }
                            }
                        }
                    });
                    cancel();
                }
            }

        }.runTaskTimer(SkyWars.getInstance(), 20, 20);


    }

    @EventHandler
    public void onEvent(PlayerJoinEvent event) {
        teleportPlayer(event.getPlayer());
    }

    @EventHandler
    public void onEvent(PlayerMoveEvent event) {
        if(gameMenuNPC == null) return;
        if(!world.getPlayers().contains(event.getPlayer())) return;
        Player player = event.getPlayer();

        double dx = player.getLocation().getX() - gameMenuNPC.locX();
        double dy = player.getLocation().getY() + player.getEyeHeight() - gameMenuNPC.locY() - gameMenuNPC.getHeadHeight();
        double dz = player.getLocation().getZ() - gameMenuNPC.locZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distanceXZ));

        PlayerConnection connection = ((CraftPlayer)event.getPlayer()).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(gameMenuNPC, (byte) (yaw * 256 / 360)));
        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(gameMenuNPC.getId(), (byte) (yaw * 256 / 360), (byte) (pitch * 256 / 360), true));
    }

    @EventHandler
    public void onEvent(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if(!world.getPlayers().contains(event.getPlayer())) return;
        if (p.getItemInHand().getType() == Material.IRON_SWORD &&
                        (event.getAction() == Action.RIGHT_CLICK_AIR ||
                                event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            p.performCommand("game kits");
        }
    }
    @EventHandler
    public void onEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player )) return;
        Player player = (Player) event.getEntity();
        if (!world.getPlayers().contains(player)) return;
        player.setHealth(player.getMaxHealth());
    }

    public void teleportPlayer(Player p) {
        JsonObject spawn = SkyWars.getInstance().getJsonConfig().getAsJsonObject("lobby").getAsJsonObject("spawn");
        p.teleport(LocationUtil.fromJson(spawn, world));
        PlayerUtil.clearPlayer(p);
        p.setGameMode(GameMode.ADVENTURE);
        if(gameMenuNPC != null) {
            PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, gameMenuNPC));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(gameMenuNPC));
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, gameMenuNPC));
        }
        giveKitChoiceSword(p);
    }

    public void giveKitChoiceSword(Player p) {
        org.bukkit.inventory.ItemStack itemStack = new ItemStack(Material.IRON_SWORD);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "Kit Choice");
        itemStack.setItemMeta(itemMeta);
        p.getInventory().setItem(4, itemStack);
    }

}
