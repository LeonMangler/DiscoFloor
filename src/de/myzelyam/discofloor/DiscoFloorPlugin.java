package de.myzelyam.discofloor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class DiscoFloorPlugin extends JavaPlugin {

    public File dataFile = new File(getDataFolder().getPath() + File.separator + "data.yml");
    public FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
    public int taskPeriod;
    public boolean protocolLib;
    public BlockChangePacketMgr blockChangePacketMgr;
    private final List<DiscoFloor> discoFloors = new ArrayList<>();
    private SelectionMgr selectionMgr;
    private boolean materialsEmptyWarned = false;
    private String version;
    private List<Material> configuredFloorBlockTypes = null;

    @Override
    public void onEnable() {
        try {
            version = getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            saveDefaultConfig();
            configuredFloorBlockTypes = getConfiguredFloorBlockTypes();
            selectionMgr = new SelectionMgr(this);
            getCommand("df").setExecutor(new DiscoFloorCommandExecutor(this));
            taskPeriod = getConfig().getInt("ColorSwitchTime");
            saveData();
            loadDiscoFloors();
            getServer().getPluginManager().registerEvents(selectionMgr, this);
            protocolLib = getServer().getPluginManager().isPluginEnabled("ProtocolLib")
                    && getConfig().getBoolean("UseProtocolLibPackets");
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

    public Material getRandomFloorBlockType() {
        if (configuredFloorBlockTypes.isEmpty()) {
            if (!materialsEmptyWarned) {
                getLogger().severe("Your config file isnt configured correctly - there are no valid Blocks. " +
                        "Please be sure to not use numeric IDs since they arent supported anymore. " +
                        "In this case regenerating your config file by deleting it will help.");
                materialsEmptyWarned = true;
            }
            return Material.STONE;
        }
        return configuredFloorBlockTypes.get(ThreadLocalRandom.current().nextInt(configuredFloorBlockTypes.size()));
    }

    private List<Material> getConfiguredFloorBlockTypes() {
        List<Material> blockTypeList = new CopyOnWriteArrayList<>();
        for (String blockInfo : getConfig().getStringList("Blocks")) {
            Material material = Material.getMaterial(blockInfo);
            if (material != null) blockTypeList.add(material);
        }
        return blockTypeList;
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

    public boolean isOneDotXOrHigher(int majorRelease) {
        for (int i = majorRelease; i < 20; i++)
            if (version.contains("v1_" + i + "_R")) return true;
        return version.contains("v2_");
    }
}
