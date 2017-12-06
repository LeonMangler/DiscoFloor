package de.myzelyam.discofloor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class DiscoFloorPlugin extends JavaPlugin {

    public File dataFile = new File(getDataFolder().getPath() + File.separator + "data.yml");
    public FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
    public int taskPeriod;
    public boolean protocolLib;
    public BlockChangePacketMgr blockChangePacketMgr;
    private List<DiscoFloor> discoFloors = new ArrayList<>();
    private SelectionMgr selectionMgr;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            selectionMgr = new SelectionMgr(this);
            getCommand("df").setExecutor(new DiscoFloorCommandExecutor(this));
            taskPeriod = getConfig().getInt("ColorSwitchTime");
            saveData();
            loadDiscoFloors();
            getServer().getPluginManager().registerEvents(selectionMgr, this);
            protocolLib = getServer().getPluginManager().isPluginEnabled("ProtocolLib");
            if (protocolLib) blockChangePacketMgr = new BlockChangePacketMgr(this);
        } catch (Exception e) {
            logException(e);
        }
    }

    @Override
    public void onDisable() {
        discoFloors.clear();
    }

    private void loadDiscoFloors() {
        for (String info : data.getStringList("DiscoFloors")) {
            discoFloors.add(DiscoFloorFactory.createDiscoFloor(info, this));
        }
    }

    public MaterialData getRandomFloorBlockData() {
        List<MaterialData> list = getPossibleFloorBlockData();
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    @SuppressWarnings("deprecation")
    private List<MaterialData> getPossibleFloorBlockData() {
        List<MaterialData> blockInfoList = new ArrayList<>();
        for (String blockInfo : getConfig().getStringList("Blocks")) {
            if (blockInfo.contains(":")) {
                String[] split = blockInfo.split(":");
                String type = split[0];
                Material material = Material.getMaterial(type);
                try {
                    if (material == null) material = Material.getMaterial(Integer.parseInt(type));
                } catch (NumberFormatException | NoSuchMethodError e) {
                    continue;
                }
                blockInfoList.add(new MaterialData(material, Byte.parseByte(split[1])));
            } else {
                Material material = Material.getMaterial(blockInfo);
                try {
                    if (material == null) material = Material.getMaterial(Integer.parseInt(blockInfo));
                } catch (NumberFormatException | NoSuchMethodError e) {
                    continue;
                }
                blockInfoList.add(new MaterialData(material, (byte) 0));
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
            StringBuilder pluginInfo = new StringBuilder();
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                if (plugin.getName().equalsIgnoreCase("DiscoFloor"))
                    continue;
                pluginInfo.append(plugin.getName());
                pluginInfo.append(" v").append(plugin.getDescription().getVersion());
                pluginInfo.append(", ");
            }
            logger.warning("  ServerVersion: " + getServer().getVersion());
            logger.warning("  PluginVersion: " + getDescription().getVersion());
            logger.warning("  ServerPlugins: " + pluginInfo);
            logger.warning("StackTrace: ");
            e.printStackTrace();
            logger.warning("[DiscoFloor] Please include this information");
            logger.warning("[DiscoFloor] if you report the issue.");
        } catch (Exception e2) {
            Bukkit.getLogger().warning("[DiscoFloor] An exception occurred while trying to print a " +
                    "detailed stacktrace, printing an undetailed stacktrace of both exceptions:");
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
}
