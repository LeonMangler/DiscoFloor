package de.myzelyam.discofloor;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class DiscoFloorFactory {

    public static String serializeDiscoFloorData(Location point1, Location point2, String id) {
        String w = point1.getWorld().getName();
        int x = point1.getBlockX();
        int y = point1.getBlockY();
        int z = point1.getBlockZ();

        String w2 = point2.getWorld().getName();
        int x2 = point2.getBlockX();
        int y2 = point2.getBlockY();
        int z2 = point2.getBlockZ();

        return (id + "%" + w + "%" + x + "%" + y + "%" + z + "%" + w2 + "%"
                + x2 + "%" + y2 + "%" + z2);
    }

    public static DiscoFloor createDiscoFloor(String serializedFloorData, DiscoFloorPlugin plugin) {
        String[] values = serializedFloorData.split("%");
        double x, y, z, x2, y2, z2;
        String w, w2;
        String id;
        id = values[0];

        w = values[1];
        x = Integer.parseInt(values[2]);
        y = Integer.parseInt(values[3]);
        z = Integer.parseInt(values[4]);

        w2 = values[5];
        x2 = Integer.parseInt(values[6]);
        y2 = Integer.parseInt(values[7]);
        z2 = Integer.parseInt(values[8]);

        return new DiscoFloor(id, new Location(Bukkit.getWorld(w), x, y, z),
                new Location(Bukkit.getWorld(w2), x2, y2, z2), plugin);
    }
}
