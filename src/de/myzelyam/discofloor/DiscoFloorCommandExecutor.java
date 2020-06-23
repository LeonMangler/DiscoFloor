package de.myzelyam.discofloor;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class DiscoFloorCommandExecutor implements CommandExecutor {

    private final DiscoFloorPlugin plugin;

    public DiscoFloorCommandExecutor(DiscoFloorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length == 0
                    || (args.length == 1 && !(args[0].equalsIgnoreCase("list") || args[0]
                    .equalsIgnoreCase("wand")))) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.help")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return true;
                }
                sender.sendMessage(ChatColor.RED + "Invalid usage! /discoFloor <create|delete|on|off|list|wand> [name]");
                if ((args.length == 1 && !(args[0].equalsIgnoreCase("list") || args[0]
                        .equalsIgnoreCase("wand")))) {
                    sender.sendMessage(ChatColor.YELLOW + "Every action except 'list' and 'wand' requires a length of two arguments!");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.list")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return true;
                }
                String floorsString = "";
                for (DiscoFloor discoFloor : plugin.getDiscoFloors()) {
                    floorsString = floorsString.concat(ChatColor.GREEN + ""
                            + discoFloor.getId() + ChatColor.YELLOW + ", ");
                }
                sender.sendMessage(ChatColor.YELLOW + "DiscoFloors: " + floorsString);
                return true;
            }
            if (args[0].equalsIgnoreCase("create")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.setup")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to create a disco floor!");
                    return true;
                }
                if (!plugin.getSelectionMgr().hasValidSelection(((Player) sender))) {
                    sender.sendMessage(ChatColor.RED + "Your selection is invalid!\n" + ChatColor.YELLOW +
                            "Get the selection-tool by typing '" + ChatColor.GREEN + "/discoFloor wand"
                            + ChatColor.YELLOW + "' and\nright click two corners of your prospective disco floor.");
                    return true;
                }
                String id = args[1].toLowerCase();
                for (DiscoFloor floor : plugin.getDiscoFloors()) {
                    if (floor.getId().equalsIgnoreCase(id)) {
                        sender.sendMessage(ChatColor.RED + "That disco floor does already exist! " +
                                "Please use a different identifier.");
                        return true;
                    }
                }
                DiscoFloor discoFloor = new DiscoFloor(id,
                        plugin.getSelectionMgr().getFirstSelection(((Player) sender).getUniqueId()),
                        plugin.getSelectionMgr().getSecondSelection(((Player) sender).getUniqueId()), plugin);
                List<String> currentSerializedFloorData = plugin.data.getStringList("DiscoFloors");
                currentSerializedFloorData.add(DiscoFloorFactory.serializeDiscoFloorData(discoFloor.getPoint1(),
                        discoFloor.getPoint2(), id));
                plugin.data.set("DiscoFloors", currentSerializedFloorData);
                plugin.saveData();
                plugin.getDiscoFloors().add(discoFloor);
                sender.sendMessage(ChatColor.GREEN + "Successfully created a disco floor!");
                return true;
            }
            if (args[0].equalsIgnoreCase("delete")
                    || args[0].equalsIgnoreCase("remove")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.setup")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return true;
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
                    List<String> currentSerializedFloorData = plugin.data.getStringList("DiscoFloors");
                    success = currentSerializedFloorData.remove(DiscoFloorFactory
                            .serializeDiscoFloorData(discoFloor.getPoint1(), discoFloor.getPoint2(), id));
                    plugin.data.set("DiscoFloors", currentSerializedFloorData);
                    plugin.saveData();
                    discoFloor.cancelTask();
                    discoFloor.sendFakeBlockChanges(true);
                    plugin.getDiscoFloors().remove(discoFloor);
                }
                if (success)
                    sender.sendMessage(ChatColor.GREEN + "Successfully deleted the disco floor '" + ChatColor.YELLOW
                            + discoFloor.getId() + ChatColor.GREEN + "'!");
                else
                    sender.sendMessage(ChatColor.RED + "Failure while trying to remove the disco floor '"
                            + ChatColor.YELLOW + id + ChatColor.RED + "'!");
                return true;
            }
            if (args[0].equalsIgnoreCase("wand")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.setup")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to get the selection tool!");
                    return true;
                }
                Player p = (Player) sender;
                Material goldAxeMaterial = Material.getMaterial("GOLDEN_AXE") != null
                        ? Material.getMaterial("GOLDEN_AXE")
                        : Material.getMaterial("GOLD_AXE");
                ItemStack wand = new ItemStack(goldAxeMaterial);
                ItemMeta meta = wand.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "D" + ChatColor.RED + "i" + ChatColor.GREEN + "s"
                        + ChatColor.DARK_PURPLE + "c" + ChatColor.AQUA + "o " + ChatColor.YELLOW + "Selection Tool");
                wand.setItemMeta(meta);
                p.getInventory().addItem(wand);
                return true;
            }
            if (args[0].equalsIgnoreCase("on")
                    || args[0].equalsIgnoreCase("enable")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.toggle")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return true;
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
                    return true;
                }
                List<String> currentDisabledSerializedFloorData = plugin.data.getStringList("DisabledFloors");
                boolean success = currentDisabledSerializedFloorData.remove(discoFloor.getId());
                if (success)
                    sender.sendMessage(ChatColor.YELLOW + "Successfully turned the disco floor '" + ChatColor.GREEN
                            + discoFloor.getId() + ChatColor.YELLOW + "' on!");
                else {
                    sender.sendMessage(ChatColor.RED + "Failure while trying to turn the disco floor '"
                            + ChatColor.YELLOW + discoFloor.getId() + ChatColor.RED + "' on!");
                    return true;
                }
                plugin.data.set("DisabledFloors", currentDisabledSerializedFloorData);
                plugin.saveData();
                discoFloor.startTask();
                return true;
            }
            if (args[0].equalsIgnoreCase("off")
                    || args[0].equalsIgnoreCase("disable")
                    || args[0].equalsIgnoreCase("of")) {
                if (sender instanceof Player
                        && !sender.hasPermission("discofloor.toggle")) {
                    sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                    return true;
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
                    return true;
                }
                discoFloor.cancelTask();
                List<String> currentDisabledSerializedFloorData = plugin.data
                        .getStringList("DisabledFloors");
                if (currentDisabledSerializedFloorData.contains(discoFloor.getId())) {
                    sender.sendMessage(ChatColor.RED + "Failure while trying to turn the disco floor '"
                            + ChatColor.YELLOW + discoFloor.getId() + ChatColor.RED + "' off!");
                    return true;
                }
                currentDisabledSerializedFloorData.add(discoFloor.getId());
                plugin.data.set("DisabledFloors", currentDisabledSerializedFloorData);
                discoFloor.sendFakeBlockChanges(true);
                plugin.saveData();
                sender.sendMessage(ChatColor.YELLOW + "Successfully turned the disco floor '" + ChatColor.GREEN
                        + discoFloor.getId() + ChatColor.YELLOW + "' off!");
                return true;
            }
            if (sender instanceof Player
                    && !sender.hasPermission("discofloor.help")) {
                sender.sendMessage(ChatColor.DARK_RED + "Denied access! You are not allowed to do this.");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Invalid usage! /discoFloor <create|delete|on|off|list|wand> [name]");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred, more detail is in the console.");
            plugin.logException(e);
        }
        return true;
    }
}
