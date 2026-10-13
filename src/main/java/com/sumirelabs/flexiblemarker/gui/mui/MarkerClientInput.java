package com.sumirelabs.flexiblemarker.gui.mui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class MarkerClientInput {
    private MarkerClientInput() {}
    static void write(PacketBuffer buffer, int mouseButton) {
        buffer.writeInt(mouseButton);
        buffer.writeBoolean(GuiScreen.isShiftKeyDown());
        buffer.writeBoolean(GuiScreen.isCtrlKeyDown());
    }
}
