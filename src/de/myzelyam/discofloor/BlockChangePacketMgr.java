package de.myzelyam.discofloor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BlockChangePacketMgr implements Listener {

    private final DiscoFloorPlugin plugin;
    private final Map<Player, Long> playerTimeForMorePacketsMap;

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
        // arrange block changes to chunk batches
        final Map<Chunk, RelevantChunkInfo> chunks = new HashMap<>();

        for (Block block : blocks) {
            Chunk chunk = block.getChunk();
            RelevantBlockInfo blockInfo = new RelevantBlockInfo(block.getLocation(), block.getType());

            if (chunks.get(chunk) == null) {
                RelevantChunkInfo chunkInfo = new RelevantChunkInfo(
                        new LinkedList<>(Collections.singletonList(blockInfo)),
                        chunk.getX(), chunk.getZ());
                chunks.put(chunk, chunkInfo);
            } else {
                RelevantChunkInfo chunkInfo = chunks.get(chunk);
                chunkInfo.getBlockInfo().add(blockInfo);
                chunks.put(chunk, chunkInfo);
            }
        }
        new BukkitRunnable() {

            @Override
            public void run() {
                // don't send multiBlockChange packets when the player joins
                if (playerTimeForMorePacketsMap.containsKey(p)
                        && playerTimeForMorePacketsMap.get(p) > System.currentTimeMillis())
                    return;

                for (Chunk chunk : chunks.keySet()) {
                    RelevantChunkInfo chunkInfo = chunks.get(chunk);
                    List<RelevantBlockInfo> chunkBlocks = chunkInfo.getBlockInfo();

                    PacketContainer packet = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
                    packet.getChunkCoordIntPairs().write(0,
                            new ChunkCoordIntPair(chunkInfo.getX(), chunkInfo.getZ()));

                    MultiBlockChangeInfo[] packetInfo = new MultiBlockChangeInfo[chunkBlocks.size()];
                    for (int i = 0; i < chunkBlocks.size(); i++) {
                        RelevantBlockInfo chunkBlock = chunkBlocks.get(i);
                        Material nextBlockType = plugin.getRandomFloorBlockType();
                        if (sendCurrentRealBlocksInstead) {
                            nextBlockType = chunkBlock.getType();
                        }
                        WrappedBlockData data = WrappedBlockData.createData(nextBlockType);
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

    /**
     * Used to wrap relevant information about a chunk to avoid interfering with the server asynchronously
     */
    private static class RelevantChunkInfo {
        private final List<RelevantBlockInfo> blocks;
        private final int x, z;

        private RelevantChunkInfo(List<RelevantBlockInfo> blocks, int x, int z) {
            this.blocks = blocks;
            this.x = x;
            this.z = z;
        }

        public List<RelevantBlockInfo> getBlockInfo() {
            return blocks;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }
    }

    /**
     * Used to wrap relevant information about a block to avoid interfering with the server asynchronously
     */
    private static class RelevantBlockInfo {
        private final Location location;
        private final Material type;

        private RelevantBlockInfo(Location location, Material type) {
            this.location = location;
            this.type = type;
        }

        public Location getLocation() {
            return location;
        }

        public Material getType() {
            return type;
        }
    }
}
