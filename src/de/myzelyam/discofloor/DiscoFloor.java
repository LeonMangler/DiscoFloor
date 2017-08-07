package de.myzelyam.discofloor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

class DiscoFloor {

    private final DiscoFloorPlugin plugin;
    private final Location point1, point2;
    boolean taskStarted = false;
    private String id;
    private BukkitTask task;

    DiscoFloor(String id, Location point1, Location point2, DiscoFloorPlugin plugin) {
        this.plugin = plugin;
        this.id = id;
        this.point1 = point1;
        this.point2 = point2;
        if (!plugin.data.getStringList("DisabledFloors").contains(id))
            startTask();
    }

    void startTask() {
        if (taskStarted) return;
        task = new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    if (point1.getWorld() == null
                            || point1.getWorld().getName() == null) {
                        cancel();
                        return;
                    }
                    sendFakeBlockChanges(false);
                } catch (Exception e) {
                    plugin.logException(e);
                }
            }
        }.runTaskTimer(plugin, 0, plugin.taskPeriod);
        taskStarted = true;
    }

    void cancelTask() {
        if (!taskStarted)
            return;
        task.cancel();
        taskStarted = false;
    }

    void sendFakeBlockChanges(boolean replaceWithReal) {
        Location center = new Cuboid(point1, point2).getCenter();
        if (center == null) {
            center = getBlocks().get(0).getLocation();
        }
        // send client-side block change
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!center.getWorld().getName()
                    .equals(onlinePlayer.getWorld().getName()))
                continue;
            if (center.distanceSquared(onlinePlayer.getLocation()) > 1999.0)
                continue;
            if (plugin.getConfig().getBoolean(
                    "UseProtocolLibPackets")
                    && plugin.protocolLib) {
                plugin.blockChangePacketMgr
                        .sendAsyncMultiBlockChangePackets(onlinePlayer, getBlocks(), replaceWithReal);
            } else
                for (Block block : getBlocks()) {
                    MaterialData materialData = plugin
                            .getRandomFloorBlockData();
                    //noinspection deprecation
                    onlinePlayer.sendBlockChange(block.getLocation(), materialData.getItemType(),
                            materialData.getData());
                }
        }
    }

    private List<Block> getBlocks() {
        Cuboid cuboid = new Cuboid(point1, point2);
        return cuboid.getBlocks();
    }

    public String getId() {
        return id;
    }

    public Location getPoint1() {
        return point1;
    }

    public Location getPoint2() {
        return point2;
    }
}
