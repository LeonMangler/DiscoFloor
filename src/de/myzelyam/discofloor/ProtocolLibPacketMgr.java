package de.myzelyam.discofloor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.myzelyam.discofloor.DiscoFloorPlugin.BlockInfo;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ProtocolLibPacketMgr implements Listener {

    private DiscoFloorPlugin plugin;
    private Map<Player, Integer> packetDelays;

    public ProtocolLibPacketMgr(DiscoFloorPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        packetDelays = new HashMap<>();
        startPacketDelaysTask();
    }

    private BukkitTask startPacketDelaysTask() {
        return new BukkitRunnable() {

            @Override
            public void run() {
                for (Player p : new LinkedList<>(packetDelays.keySet())) {
                    if (packetDelays.get(p) == 0) {
                        packetDelays.remove(p);
                        continue;
                    }
                    packetDelays.put(p, (packetDelays.get(p) - 1));
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        packetDelays.put(e.getPlayer(), 5);
    }

    public void sendMultiBlockChangePacket(final Player p,
                                           final List<Block> blocks) {
        new BukkitRunnable() {

            @Override
            public void run() {
                // don't send multiblockchange packets when the player joins
                if (packetDelays.get(p) != null)
                    return;
                // arrange blocks
                Map<Chunk, List<Block>> chunks = new HashMap<>();
                for (Block block : blocks) {
                    Chunk c = block.getChunk();
                    if (chunks.get(c) == null) {
                        chunks.put(c,
                                new LinkedList<>(Collections.singletonList(block)));
                    } else {
                        List<Block> chunkBlocks = chunks.get(c);
                        chunkBlocks.add(block);
                        chunks.put(c, chunkBlocks);
                    }
                }
                for (Chunk c : chunks.keySet()) {
                    List<Block> chunkBlocks = chunks.get(c);
                    // add information to packets
                    PacketContainer packet = new PacketContainer(
                            PacketType.Play.Server.MULTI_BLOCK_CHANGE);
                    packet.getChunkCoordIntPairs().write(0,
                            new ChunkCoordIntPair(c.getX(), c.getZ()));
                    MultiBlockChangeInfo[] infos = new MultiBlockChangeInfo[chunkBlocks
                            .size()];
                    for (int i = 0; i < chunkBlocks.size(); i++) {
                        Block chunkBlock = chunkBlocks.get(i);
                        BlockInfo newBlockCombi = plugin
                                .getRandomFloorBlockType();
                        WrappedBlockData data = WrappedBlockData.createData(
                                newBlockCombi.material, newBlockCombi.data);
                        infos[i] = new MultiBlockChangeInfo(
                                chunkBlock.getLocation(), data);
                    }
                    packet.getMultiBlockChangeInfoArrays().write(0, infos);
                    // send packets
                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(
                                p, packet);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Cannot send packet "
                                + packet, e);
                    }
                }

            }
        }.runTaskAsynchronously(plugin);
    }
}
