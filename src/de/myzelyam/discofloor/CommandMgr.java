package de.myzelyam.discofloor;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CommandMgr {

    private DiscoFloorPlugin plugin;

    public CommandMgr(DiscoFloorPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        try {
            if (args.length == 0
                    || (args.length == 1 && !(args[0].equalsIgnoreCase("list") || args[0]
                    .equalsIgnoreCase("wand")))) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.help")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return;
                }
                sender.sendMessage(ChatColor.RED + "Invalid usage! /discoFloor <create|delete|on|off|list|wand> [name]");
                if ((args.length == 1 && !(args[0].equalsIgnoreCase("list") || args[0]
                        .equalsIgnoreCase("wand")))) {
                    sender.sendMessage(ChatColor.YELLOW + "Every action except 'list' and 'wand' requires a length of two arguments!");
                }
                return;
            }
            if (args[0].equalsIgnoreCase("list")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.list")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return;
                }
                String floorsString = "";
                for (DiscoFloor discoFloor : plugin.getDiscoFloors()) {
                    floorsString = floorsString.concat(ChatColor.GREEN + ""
                            + discoFloor.getId() + ChatColor.YELLOW + ", ");
                }
                sender.sendMessage(ChatColor.YELLOW + "DiscoFloors: " + floorsString);
                return;
            }
            if (args[0].equalsIgnoreCase("create")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.setup")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to create a disco floor!");
                    return;
                }
                if (!plugin.getSelectionMgr().hasValidSelection(((Player) sender))) {
                    sender.sendMessage(ChatColor.RED + "Your selection is invalid!\n" + ChatColor.YELLOW +
                            "Get the selection-tool by typing '" + ChatColor.GREEN + "/discoFloor wand"
                            + ChatColor.YELLOW + "' and\nright click two corners of your prospective disco floor.");
                    return;
                }
                String id = args[1].toLowerCase();
                for (DiscoFloor floor : plugin.getDiscoFloors()) {
                    if (floor.getId().equalsIgnoreCase(id)) {
                        sender.sendMessage(ChatColor.RED + "That disco floor does already exist! " +
                                "Please use a different identifier.");
                        return;
                    }
                }
                // create new disco floor
                DiscoFloor discoFloor = new DiscoFloor(id,
                        plugin.getSelectionMgr().firstSelections.get(((Player) sender).getUniqueId()),
                        plugin.getSelectionMgr().secondSelections.get(((Player) sender).getUniqueId()), plugin);
                discoFloor.createBukkitBlocks();
                List<String> currentFloors = plugin.data
                        .getStringList("DiscoFloors");
                currentFloors.add(Serialization.serializeFloor(discoFloor.getFirstSelection(),
                        discoFloor.getSecondSelection(), id));
                plugin.data.set("DiscoFloors", currentFloors);
                plugin.saveData();
                plugin.getDiscoFloors().add(discoFloor);
                sender.sendMessage(ChatColor.GREEN + "Successfully created a disco floor!");
                return;
            }
            if (args[0].equalsIgnoreCase("delete")
                    || args[0].equalsIgnoreCase("remove")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.setup")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return;
                }
                String id = args[1].toLowerCase();
                DiscoFloor discoFloor = null;
                for (DiscoFloor floor : plugin.getDiscoFloors()) {
                    if (floor.getId().equalsIgnoreCase(id)) {
                        discoFloor = floor;
                        break;
                    }
                }
                boolean success = false;
                if (!(discoFloor == null)) {
                    List<String> currentFloors = plugin.data
                            .getStringList("DiscoFloors");
                    success = currentFloors.remove(Serialization
                            .serializeFloor(discoFloor.getFirstSelection(), discoFloor.getSecondSelection(), id));
                    plugin.data.set("DiscoFloors", currentFloors);
                    plugin.saveData();
                    discoFloor.cancelTask();
                    plugin.getDiscoFloors().remove(discoFloor);
                }
                if (success)
                    sender.sendMessage(ChatColor.GREEN + "Successfully deleted the disco floor '" + ChatColor.YELLOW
                            + discoFloor.getId() + ChatColor.GREEN + "'!");
                else
                    sender.sendMessage(ChatColor.RED + "Failure while trying to remove the disco floor '"
                            + ChatColor.YELLOW + id + ChatColor.RED + "'!");
                return;
            }
            if (args[0].equalsIgnoreCase("wand")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.setup")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to get the selection tool!");
                    return;
                }
                Player p = (Player) sender;
                ItemStack wand = new ItemStack(Material.GOLD_AXE);
                ItemMeta meta = wand.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "D" + ChatColor.RED + "i" + ChatColor.GREEN + "s"
                        + ChatColor.DARK_PURPLE + "c" + ChatColor.AQUA + "o " + ChatColor.YELLOW + "Selection Tool");
                wand.setItemMeta(meta);
                p.getInventory().addItem(wand);
                return;
            }
            if (args[0].equalsIgnoreCase("on")
                    || args[0].equalsIgnoreCase("enable")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.toggle")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return;
                }
                String id = args[1].toLowerCase();
                DiscoFloor discoFloor = null;
                for (DiscoFloor floor : plugin.getDiscoFloors()) {
                    if (floor.getId().equalsIgnoreCase(id)) {
                        discoFloor = floor;
                        break;
                    }
                }
                if (discoFloor == null) {
                    sender.sendMessage(ChatColor.RED + "This floor doesn't exist!");
                    return;
                }
                List<String> currentDisabledFloors = plugin.data
                        .getStringList("DisabledFloors");
                boolean success = currentDisabledFloors.remove(discoFloor.getId());
                if (success)
                    sender.sendMessage(ChatColor.YELLOW + "Successfully turned the disco floor '" + ChatColor.GREEN
                            + discoFloor.getId() + ChatColor.YELLOW + "' on!");
                else {
                    sender.sendMessage(ChatColor.RED + "Failure while trying to turn the disco floor '"
                            + ChatColor.YELLOW + discoFloor.getId() + ChatColor.RED + "' on!");
                    return;
                }
                plugin.data.set("DisabledFloors", currentDisabledFloors);
                plugin.saveData();
                discoFloor.createBukkitBlocks();
                discoFloor.startTask();
                return;
            }
            if (args[0].equalsIgnoreCase("off")
                    || args[0].equalsIgnoreCase("disable")
                    || args[0].equalsIgnoreCase("of")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.toggle")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return;
                }
                String id = args[1].toLowerCase();
                DiscoFloor discoFloor = null;
                for (DiscoFloor floor : plugin.getDiscoFloors()) {
                    if (floor.getId().equalsIgnoreCase(id)) {
                        discoFloor = floor;
                        break;
                    }
                }
                if (discoFloor == null) {
                    sender.sendMessage(ChatColor.RED + "This floor doesn't exist!");
                    return;
                }
                discoFloor.cancelTask();
                List<String> currentDisabledFloors = plugin.data
                        .getStringList("DisabledFloors");
                if (currentDisabledFloors.contains(discoFloor.getId())) {
                    sender.sendMessage(ChatColor.RED + "Failure while trying to turn the disco floor '"
                            + ChatColor.YELLOW + discoFloor.getId() + ChatColor.RED + "' off!");
                    return;
                }
                currentDisabledFloors.add(discoFloor.getId());
                plugin.data.set("DisabledFloors", currentDisabledFloors);
                plugin.saveData();
                sender.sendMessage(ChatColor.YELLOW + "Successfully turned the disco floor '" + ChatColor.GREEN
                        + discoFloor.getId() + ChatColor.YELLOW + "' off!");
                return;
            }
            if (sender instanceof Player
                    && !sender.hasPermission("discofloor.help")) {
                sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                return;
            }
            sender.sendMessage(ChatColor.RED + "Invalid usage! /discoFloor <create|delete|on|off|list|wand> [name]");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred, more detail is in console.");
            plugin.logException(e);
        }
    }
}
