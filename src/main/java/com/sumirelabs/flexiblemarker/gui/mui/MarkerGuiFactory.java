package com.sumirelabs.flexiblemarker.gui.mui;

import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.kotori316.marker.Marker;
import com.kotori316.marker.Tile16Marker;
import com.kotori316.marker.TileFlexMarker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** One native, server-authoritative factory for both marker screens. */
public final class MarkerGuiFactory implements UIFactory<MarkerGuiData> {
    public static final MarkerGuiFactory INSTANCE = new MarkerGuiFactory();
    private static boolean registered;
    private MarkerGuiFactory() {}
    public static void register() {
        if (!registered) { GuiManager.registerFactory(INSTANCE); registered = true; }
    }
    public static void open(EntityPlayer player, int id, World world, BlockPos pos) {
        if (world.isRemote || world != player.world || player instanceof FakePlayer) return;
        MarkerGuiData data = new MarkerGuiData(player, pos, id);
        if (player instanceof EntityPlayerMP serverPlayer && INSTANCE.canInteractWith(player, data))
            GuiManager.open(INSTANCE, data, serverPlayer);
    }
    @Override public String getFactoryName() { return "flexiblemarker:marker"; }
    @Override public boolean canInteractWith(EntityPlayer player, MarkerGuiData data) {
        if (player != data.getPlayer() || player.isDead || !player.isEntityAlive()
                || !data.getWorld().isBlockLoaded(data.getBlockPos()) || data.getSquaredDistance(player) > 64) return false;
        TileEntity tile = data.getTileEntity();
        return tile != null && !tile.isInvalid()
                && ((data.id == 0 && tile instanceof TileFlexMarker) || (data.id == 1 && tile instanceof Tile16Marker));
    }
    @Override public ModularPanel createPanel(MarkerGuiData data, PanelSyncManager sync, UISettings settings) {
        if (!canInteractWith(data.getPlayer(), data)) throw new IllegalArgumentException("Invalid marker GUI target");
        TileEntity tile = data.getTileEntity();
        settings.canInteractWith(player -> canInteractWith(player, data) && data.getTileEntity() == tile);
        sync.bindPlayerInventory(data.getPlayer());
        sync.syncValue("marker_state", 0, new MarkerStateSync(tile));
        return MarkerPanels.build(data, tile, sync);
    }
    @SideOnly(Side.CLIENT)
    @Override public ModularScreen createScreen(MarkerGuiData data, ModularPanel panel) {
        return new ModularScreen(Marker.modID, panel);
    }
    @Override public void writeGuiData(MarkerGuiData data, PacketBuffer buffer) {
        buffer.writeBlockPos(data.getBlockPos()); buffer.writeVarInt(data.id);
    }
    @Override public MarkerGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new MarkerGuiData(player, buffer.readBlockPos(), buffer.readVarInt());
    }
}
