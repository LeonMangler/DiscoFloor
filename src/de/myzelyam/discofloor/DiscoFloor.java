package de.myzelyam.discofloor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

class DiscoFloor {

    private final DiscoFloorPlugin plugin;
    private final Location point1, point2;
    boolean taskStarted = false;
    private final String id;
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
                    if (point1.getWorld() == null) {
                        cancel();
                        return;
                    } else {
                        point1.getWorld().getName();
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
        for (Player worldPlayer : center.getWorld().getPlayers()) {
            if (center.distanceSquared(worldPlayer.getLocation()) > 1999.0)
                continue;
            if (plugin.getConfig().getBoolean("UseProtocolLibPackets") && plugin.protocolLib) {
                plugin.blockChangePacketMgr
                        .sendAsyncMultiBlockChangePackets(worldPlayer, getBlocks(), replaceWithReal);
            } else {
                for (Block block : getBlocks()) {
                    Material material = plugin.getRandomFloorBlockType();
                    if (replaceWithReal)
                        material = block.getType();
                    worldPlayer.sendBlockChange(block.getLocation(), material.createBlockData());
                }
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
