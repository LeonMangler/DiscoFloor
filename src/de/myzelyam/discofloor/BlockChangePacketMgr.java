package de.myzelyam.discofloor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BlockChangePacketMgr implements Listener {

    private DiscoFloorPlugin plugin;
    private Map<Player, Long> playerTimeForMorePacketsMap;

    public BlockChangePacketMgr(DiscoFloorPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        playerTimeForMorePacketsMap = new HashMap<>();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        playerTimeForMorePacketsMap.put(e.getPlayer(),
                System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playerTimeForMorePacketsMap.remove(e.getPlayer());
    }

    public void sendAsyncMultiBlockChangePackets(final Player p, final List<Block> blocks,
                                                 final boolean sendCurrentRealBlocksInstead) {
        new BukkitRunnable() {

            @Override
            public void run() {
                // don't send multiBlockChange packets when the player joins
                if (playerTimeForMorePacketsMap.containsKey(p)
                        && playerTimeForMorePacketsMap.get(p) > System.currentTimeMillis())
                    return;

                // arrange block changes to chunk batches
                Map<Chunk, List<Block>> chunks = new HashMap<>();
                for (Block block : blocks) {
                    Chunk chunk = block.getChunk();
                    if (chunks.get(chunk) == null) {
                        chunks.put(chunk, new LinkedList<>(Collections.singletonList(block)));
                    } else {
                        List<Block> chunkBlocks = chunks.get(chunk);
                        chunkBlocks.add(block);
                        chunks.put(chunk, chunkBlocks);
                    }
                }

                for (Chunk chunk : chunks.keySet()) {
                    List<Block> chunkBlocks = chunks.get(chunk);

                    PacketContainer packet = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
                    packet.getChunkCoordIntPairs().write(0,
                            new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));

                    MultiBlockChangeInfo[] packetInfo = new MultiBlockChangeInfo[chunkBlocks.size()];
                    for (int i = 0; i < chunkBlocks.size(); i++) {
                        Block chunkBlock = chunkBlocks.get(i);
                        MaterialData nextBlockData = plugin.getRandomFloorBlockData();
                        if (sendCurrentRealBlocksInstead) {
                            //noinspection deprecation
                            nextBlockData = new MaterialData(chunkBlock.getType(), chunkBlock.getData());
                        }
                        //noinspection deprecation
                        WrappedBlockData data = WrappedBlockData.createData(
                                nextBlockData.getItemType(), nextBlockData.getData());
                        packetInfo[i] = new MultiBlockChangeInfo(chunkBlock.getLocation(), data);
                    }
                    packet.getMultiBlockChangeInfoArrays().write(0, packetInfo);

                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Cannot send packet "
                                + packet, e);
                    }
                }

            }
        }.runTaskAsynchronously(plugin);
    }
}
