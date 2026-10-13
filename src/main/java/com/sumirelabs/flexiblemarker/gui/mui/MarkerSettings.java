package com.sumirelabs.flexiblemarker.gui.mui;

import com.kotori316.marker.Tile16Marker;
import com.kotori316.marker.TileFlexMarker;
import net.minecraft.tileentity.TileEntity;

/** Mutations use the original domain operations and leave registry/NBT schemas unchanged. */
final class MarkerSettings {
    static final int MAX_CHUNKS = (Integer.MAX_VALUE - 1) / 16;
    private MarkerSettings() {}
    static int delta(boolean shift, boolean ctrl) { return shift ? (ctrl ? 64 : 16) : (ctrl ? 4 : 1); }
    static int distance(TileFlexMarker tile, TileFlexMarker.Movable direction) {
        return direction.distanceFromOrigin(tile.getPos(), tile.min(), tile.max(), tile.direction);
    }
    static void distance(TileFlexMarker tile, TileFlexMarker.Movable direction, int target) {
        if (target == 0 && distance(tile, direction) == 0) return;
        move(tile, direction, Math.max(1, Math.min(64, target)) - distance(tile, direction));
    }
    static void move(TileFlexMarker tile, TileFlexMarker.Movable direction, int amount) {
        if (amount == 0) return;
        tile.move(direction, amount); tile.markDirty();
    }
    static int maxY(TileEntity tile) { return Math.max(0, tile.getWorld().getActualHeight() - 1); }
    static void chunks(Tile16Marker tile, int chunks) {
        int clamped = Math.max(1, Math.min(MAX_CHUNKS, chunks));
        long distance = (long) clamped * 16 + 1;
        // Reject coordinate overflow without imposing a new arbitrary gameplay-size limit.
        if (Math.abs((long) tile.getPos().getX()) + distance > Integer.MAX_VALUE
                || Math.abs((long) tile.getPos().getZ()) + distance > Integer.MAX_VALUE) return;
        tile.changeSize(clamped * 16, tile.max().getY(), tile.min().getY()); tile.markDirty();
    }
    static void top(Tile16Marker tile, int value) {
        tile.changeSize(tile.getConfiguredSize(), Math.max(tile.min().getY(), Math.min(maxY(tile), value)), tile.min().getY());
        tile.markDirty();
    }
    static void bottom(Tile16Marker tile, int value) {
        tile.changeSize(tile.getConfiguredSize(), tile.max().getY(), Math.max(0, Math.min(tile.max().getY(), value)));
        tile.markDirty();
    }
}
