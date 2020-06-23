package de.myzelyam.discofloor;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionMgr implements Listener {

    private final Map<UUID, Location> playerFirstSelectionMap = new HashMap<>();
    private final Map<UUID, Location> playerSecondSelectionMap = new HashMap<>();
    private final DiscoFloorPlugin plugin;

    public SelectionMgr(DiscoFloorPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasValidSelection(Player p) {
        //noinspection SimplifiableIfStatement
        if (playerFirstSelectionMap.get(p.getUniqueId()) == null || playerSecondSelectionMap.get(p.getUniqueId()) == null)
            return false;
        return playerFirstSelectionMap.get(p.getUniqueId()).getWorld().getName().equals(playerSecondSelectionMap.get(p.getUniqueId())
                .getWorld().getName());
    }

    @EventHandler
    public void onSelect(PlayerInteractEvent e) {
        try {
            try {
                if (e.getHand() == EquipmentSlot.OFF_HAND) return;
            } catch (NoSuchMethodError | NoClassDefFoundError ignored) {
            }
            Player p = e.getPlayer();
            if (!p.hasPermission("discofloor.setup")) {
                return;
            }
            if (!((e.getAction() == Action.LEFT_CLICK_BLOCK) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)))
                return;
            @SuppressWarnings("deprecation") ItemStack itemInHand = p.getItemInHand();
            if (itemInHand == null
                    || itemInHand.getItemMeta() == null
                    || itemInHand.getItemMeta().getDisplayName() == null
                    || e.getClickedBlock() == null)
                return;
            if (!itemInHand.getItemMeta().getDisplayName()
                    .equalsIgnoreCase(ChatColor.GOLD + "D" + ChatColor.RED + "i" + ChatColor.GREEN + "s"
                            + ChatColor.DARK_PURPLE + "c" + ChatColor.AQUA + "o "
                            + ChatColor.YELLOW + "Selection Tool"))
                return;
            Location blockLocation = e.getClickedBlock().getLocation();
            if (e.getAction() == Action.LEFT_CLICK_BLOCK)
                playerFirstSelectionMap.put(p.getUniqueId(), blockLocation);
            else
                playerSecondSelectionMap.put(p.getUniqueId(), blockLocation);
            p.sendMessage(ChatColor.GOLD + "Location " + ChatColor.YELLOW + ""
                    + (e.getAction() == Action.LEFT_CLICK_BLOCK ? "1" : "2")
                    + ChatColor.GOLD + " at " + ChatColor.GOLD + "X"
                    + blockLocation.getBlockX() + ChatColor.YELLOW + ", " + ChatColor.GOLD + "Y"
                    + blockLocation.getBlockY() + ChatColor.YELLOW + ", " + ChatColor.GOLD + "Z"
                    + blockLocation.getBlockZ());
            e.setCancelled(true);
            e.setUseInteractedBlock(Result.DENY);
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    public Location getFirstSelection(UUID player) {
        return playerFirstSelectionMap.get(player);
    }

    public Location getSecondSelection(UUID player) {
        return playerSecondSelectionMap.get(player);
    }
}
