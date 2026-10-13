package com.sumirelabs.flexiblemarker.gui.mui;

import com.cleanroommc.modularui.value.sync.SyncHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import java.io.IOException;

/** Small server-to-client snapshots also refresh the existing world-space range renderer. */
final class MarkerStateSync extends SyncHandler {
    private final TileEntity tile;
    private NBTTagCompound previous;
    MarkerStateSync(TileEntity tile) { this.tile = tile; }
    @Override public void detectAndSendChanges(boolean init) {
        if (getSyncManager().isClient()) return;
        NBTTagCompound tag = tile.writeToNBT(new NBTTagCompound());
        if (init || !tag.equals(previous)) {
            previous = tag;
            syncToClient(0, buffer -> buffer.writeCompoundTag(tag));
        }
    }
    @Override public void readOnClient(int id, PacketBuffer buffer) throws IOException {
        if (id == 0) {
            NBTTagCompound tag = buffer.readCompoundTag();
            if (tag != null) tile.readFromNBT(tag);
        }
    }
    @Override public void readOnServer(int id, PacketBuffer buffer) {
        // Client snapshots must never replace the server's tile data.
    }
}
