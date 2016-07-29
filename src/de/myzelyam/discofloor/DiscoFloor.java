package de.myzelyam.discofloor;

import de.myzelyam.discofloor.DiscoFloorPlugin.BlockInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

class DiscoFloor {

    private final DiscoFloorPlugin plugin;
    private final Location firstSelection, secondSelection;
    boolean taskStarted = false;
    private String id;
    private BukkitTask task;

    DiscoFloor(String id, Location firstSelection, Location secondSelection, DiscoFloorPlugin plugin) {
        this.plugin = plugin;
        this.id = id;
        this.firstSelection = firstSelection;
        this.secondSelection = secondSelection;
        if (!plugin.data.getStringList("DisabledFloors").contains(id))
            startTask();
    }

    void createBukkitBlocks() {
        for (Block b : getBlocks()) {
            BlockInfo combi = plugin.getRandomFloorBlockType();
            b.setType(combi.material);
            //noinspection deprecation
            b.setData(combi.data);
        }
    }

    void startTask() {
        if (taskStarted)
            return;
        task = new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    if (firstSelection.getWorld() == null
                            || firstSelection.getWorld().getName() == null) {
                        cancel();
                        return;
                    }
                    List<Block> blocks = getBlocks();
                    Location center = new Cuboid(firstSelection, secondSelection).getCenter();
                    if (center == null) {
                        center = blocks.get(0).getLocation();
                    }
                    // send client-side block change
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!center.getWorld().getName()
                                .equals(p.getWorld().getName()))
                            continue;
                        if (center.distanceSquared(p.getLocation()) > 1999.0)
                            continue;
                        if (plugin.getConfig().getBoolean(
                                "UseProtocolLibPackets")
                                && plugin.protocolLib) {
                            plugin.protocolLibPacketMgr
                                    .sendMultiBlockChangePacket(p, blocks);
                        } else
                            for (Block b : blocks) {
                                BlockInfo blockInfo = plugin
                                        .getRandomFloorBlockType();
                                //noinspection deprecation
                                p.sendBlockChange(b.getLocation(), blockInfo.material,
                                        blockInfo.data);
                            }
                    }
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

    private List<Block> getBlocks() {
        Cuboid c = new Cuboid(firstSelection, secondSelection);
        return c.getBlocks();
    }

    public String getId() {
        return id;
    }

    public Location getFirstSelection() {
        return firstSelection;
    }

    public Location getSecondSelection() {
        return secondSelection;
    }
}
