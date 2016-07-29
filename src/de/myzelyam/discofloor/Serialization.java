package de.myzelyam.discofloor;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Serialization {

    static String serializeFloor(Location loc, Location loc2, String id) {
        String w = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        String w2 = loc2.getWorld().getName();
        int x2 = loc2.getBlockX();
        int y2 = loc2.getBlockY();
        int z2 = loc2.getBlockZ();
        return (id + "%" + w + "%" + x + "%" + y + "%" + z + "%" + w2 + "%"
                + x2 + "%" + y2 + "%" + z2);
    }

    static DiscoFloor deserializeFloor(String string, DiscoFloorPlugin plugin) {
        String[] values = string.split("%");
        double x, y, z, x2, y2, z2;
        String w, w2;
        String id;
        id = values[0];
        //
        w = values[1];
        x = Integer.parseInt(values[2]);
        y = Integer.parseInt(values[3]);
        z = Integer.parseInt(values[4]);
        //
        w2 = values[5];
        x2 = Integer.parseInt(values[6]);
        y2 = Integer.parseInt(values[7]);
        z2 = Integer.parseInt(values[8]);
        return new DiscoFloor(id, new Location(Bukkit.getWorld(w), x, y, z),
                new Location(Bukkit.getWorld(w2), x2, y2, z2), plugin);
    }
}
