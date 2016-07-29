package de.myzelyam.discofloor;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionMgr implements Listener {

    public Map<UUID, Location> firstSelections = new HashMap<>();
    public Map<UUID, Location> secondSelections = new HashMap<>();
    private Map<UUID, Long> clickInteractionCoolDowns = new HashMap<>();
    private DiscoFloorPlugin plugin;

    public SelectionMgr(DiscoFloorPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasValidSelection(Player p) {
        //noinspection SimplifiableIfStatement
        if (firstSelections.get(p.getUniqueId()) == null || secondSelections.get(p.getUniqueId()) == null)
            return false;
        return firstSelections.get(p.getUniqueId()).getWorld().getName().equals(secondSelections.get(p.getUniqueId())
                .getWorld().getName());
    }

    @EventHandler
    public void onSelect(PlayerInteractEvent e) {
        try {
            Player p = e.getPlayer();
            if (!p.hasPermission("discofloor.setup")) {
                return;
            }
            if (!((e.getAction() == Action.LEFT_CLICK_BLOCK) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)))
                return;
            if (clickInteractionCoolDowns.containsKey(p.getUniqueId())) {
                if (clickInteractionCoolDowns.get(p.getUniqueId()) < (System.currentTimeMillis() - 20L)) {
                    clickInteractionCoolDowns.remove(p.getUniqueId());
                } else return;
            } else clickInteractionCoolDowns.put(p.getUniqueId(), System.currentTimeMillis());
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
                firstSelections.put(p.getUniqueId(), blockLocation);
            else
                secondSelections.put(p.getUniqueId(), blockLocation);
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

}
