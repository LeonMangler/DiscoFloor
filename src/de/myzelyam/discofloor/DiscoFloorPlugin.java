package de.myzelyam.discofloor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class DiscoFloorPlugin extends JavaPlugin {

    public File dataFile = new File(this.getDataFolder().getPath()
            + File.separator + "data.yml");
    public FileConfiguration data = YamlConfiguration
            .loadConfiguration(dataFile);
    public int taskPeriod;
    public boolean protocolLib;
    public ProtocolLibPacketMgr protocolLibPacketMgr;
    private List<DiscoFloor> discoFloors = new ArrayList<>();
    private SelectionMgr selectionMgr;
    private CommandMgr commandMgr;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            selectionMgr = new SelectionMgr(this);
            commandMgr = new CommandMgr(this);
            taskPeriod = getConfig().getInt("ColorSwitchTime");
            saveData();
            loadDiscoFloors();
            getServer().getPluginManager().registerEvents(selectionMgr, this);
            protocolLib = getServer().getPluginManager().isPluginEnabled("ProtocolLib");
            if (protocolLib)
                protocolLibPacketMgr = new ProtocolLibPacketMgr(this);
        } catch (Exception e) {
            logException(e);
        }
    }

    @Override
    public void onDisable() {
        discoFloors.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        commandMgr.execute(sender, args);
        return true;
    }

    private void loadDiscoFloors() {
        for (String info : data.getStringList("DiscoFloors")) {
            discoFloors.add(Serialization.deserializeFloor(info, this));
        }
    }

    public BlockInfo getRandomFloorBlockType() {
        List<BlockInfo> list = getPossibleFloorBlockTypes();
        int random = new Random().nextInt(list.size());
        return list.get(random);
    }

    @SuppressWarnings("deprecation")
    private List<BlockInfo> getPossibleFloorBlockTypes() {
        List<BlockInfo> blockInfoList = new ArrayList<>();
        for (String type : getConfig().getStringList("Blocks")) {
            try {
                if (type.contains(":")) {
                    String[] split = type.split(":");
                    blockInfoList.add(new BlockInfo(Material
                            .getMaterial(Integer.parseInt(split[0])), (byte) Integer
                            .parseInt(split[1])));
                } else {
                    blockInfoList.add(new BlockInfo(Material
                            .getMaterial(Integer.parseInt(type)), (byte) 0));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return blockInfoList;
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logException(Exception e) {
        try {
            Logger logger = Bukkit.getLogger();
            logger.warning("[DiscoFloor] Unknown Exception occurred!");
            logger.warning("[DiscoFloor] Please report this issue!");
            logger.warning("Message: ");
            logger.warning("  " + e.getMessage());
            logger.warning("General information: ");
            String pluginInfo = "";
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                if (plugin.getName().equalsIgnoreCase("DiscoFloor"))
                    continue;
                pluginInfo = pluginInfo + plugin.getName();
                pluginInfo = pluginInfo + " v"
                        + plugin.getDescription().getVersion();
                pluginInfo = pluginInfo + ", ";
            }
            logger.warning("  ServerVersion: "
                    + this.getServer().getVersion());
            logger.warning("  PluginVersion: "
                    + this.getDescription().getVersion());
            logger.warning("  ServerPlugins: " + pluginInfo);
            logger.warning("StackTrace: ");
            e.printStackTrace();
            logger.warning("[DiscoFloor] Please include this information");
            logger.warning("[DiscoFloor] if you report the issue.");
        } catch (Exception e2) {
            Bukkit.getLogger().warning("[DiscoFloor] An exception occurred while trying to print a detailed " +
                    "stacktrace, printing an undetailed stacktrace of both exceptions:");
            Bukkit.getLogger().warning("ORIGINAL EXCEPTION:");
            e.printStackTrace();
            Bukkit.getLogger().warning("EXCEPTION WHILE PRINTING DETAILED STACKTRACE:");
            e2.printStackTrace();
        }
    }

    public List<DiscoFloor> getDiscoFloors() {
        return discoFloors;
    }

    public SelectionMgr getSelectionMgr() {
        return selectionMgr;
    }

    public class BlockInfo {

        public byte data;
        public Material material;

        public BlockInfo(Material material, byte data) {
            this.data = data;
            this.material = material;
        }
    }
}
