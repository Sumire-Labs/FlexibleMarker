package com.sumirelabs.flexiblemarker.gui.mui;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.kotori316.marker.Tile16Marker;
import com.kotori316.marker.TileFlexMarker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

final class MarkerPanels {
    private MarkerPanels() {}
    static ModularPanel build(MarkerGuiData data, TileEntity tile, PanelSyncManager sync) {
        return tile instanceof TileFlexMarker flex ? flexible(data, flex, sync) : chunk(data, (Tile16Marker) tile, sync);
    }
    private static ModularPanel panel(String name, String title, int height) {
        ModularPanel panel = ModularPanel.defaultPanel(name, 248, height);
        panel.child(IKey.lang(title).asWidget().pos(8, 6).size(220, 12));
        panel.child(ButtonWidget.panelCloseButton());
        panel.child(IKey.lang("container.inventory").asWidget().pos(43, height - 95).height(10));
        panel.child(SlotGroupWidget.playerInventory(7, true));
        return panel;
    }
    private static boolean editable(MarkerGuiData data, TileEntity tile, PanelSyncManager sync) {
        return !sync.isClient() && MarkerGuiFactory.INSTANCE.canInteractWith(data.getPlayer(), data) && data.getTileEntity() == tile;
    }
    private static IntSyncValue number(MarkerGuiData data, TileEntity tile, PanelSyncManager sync,
            String name, IntSupplier getter, IntConsumer setter) {
        IntSyncValue value = new IntSyncValue(getter, input -> { if (editable(data, tile, sync)) setter.accept(input); });
        sync.syncValue(name, 0, value); return value;
    }
    private static void button(MarkerGuiData data, TileEntity tile, PanelSyncManager sync, ModularPanel panel,
            String name, int x, int y, int width, String label, Consumer<PacketBuffer> action) {
        sync.registerSyncedAction(name, false, true, buffer -> {
            if (editable(data, tile, sync)) action.accept(buffer);
        });
        panel.child(new ButtonWidget<>().name(name).pos(x, y).size(width, 18).overlay(IKey.str(label))
                .onMousePressed(mouse -> {
                    if (mouse != 0) return false;
                    sync.callSyncedAction(name, buffer -> MarkerClientInput.write(buffer, mouse)); return true;
                }));
    }
    private static ModularPanel flexible(MarkerGuiData data, TileFlexMarker tile, PanelSyncManager sync) {
        ModularPanel panel = panel("flexible_marker", "tile.flexiblemarker.name", 222);
        int row = 0;
        for (TileFlexMarker.Movable direction : TileFlexMarker.Movable.values()) {
            int y = 25 + row++ * 20;
            String prefix = direction.name().toLowerCase(java.util.Locale.ROOT);
            panel.child(IKey.lang(direction.transName).asWidget().pos(8, y + 4).width(68));
            IntSyncValue value = number(data, tile, sync, prefix + "_distance", () -> MarkerSettings.distance(tile, direction),
                    input -> MarkerSettings.distance(tile, direction, input));
            panel.child(new TextFieldWidget().name(prefix + "_input").value(value).setNumbers(0, 64).setMaxLength(2)
                    .pos(136, y).size(36, 18));
            int[] amounts = {-16, -1, 1, 16}, positions = {80, 108, 178, 206};
            for (int i = 0; i < amounts.length; i++) {
                int amount = amounts[i];
                button(data, tile, sync, panel, prefix + (amount > 0 ? "_plus" : "_minus") + Math.abs(amount),
                        positions[i], y, 26, amount > 0 ? "+" + amount : String.valueOf(amount),
                        buffer -> MarkerSettings.move(tile, direction, amount));
            }
        }
        return panel;
    }
    private static ModularPanel chunk(MarkerGuiData data, Tile16Marker tile, PanelSyncManager sync) {
        ModularPanel panel = panel("chunk_marker", "tile.marker16.name", 204);
        int[] positions = {8, 88, 168};
        String[] names = {"bottom_y", "chunk_count", "top_y"};
        String[] titles = {"gui.mui.bottom_y", "gui.mui.chunks", "gui.mui.top_y"};
        IntSupplier[] getters = {() -> tile.min().getY(), () -> tile.getConfiguredSize() / 16, () -> tile.max().getY()};
        IntConsumer[] setters = {value -> MarkerSettings.bottom(tile, value), value -> MarkerSettings.chunks(tile, value), value -> MarkerSettings.top(tile, value)};
        for (int i = 0; i < 3; i++) {
            final int column = i;
            String name = names[i]; int x = positions[i];
            panel.child(IKey.lang(titles[i]).asWidget().pos(x, 26).size(72, 12));
            IntSyncValue value = number(data, tile, sync, name, getters[i], setters[i]);
            panel.child(new TextFieldWidget().name(name + "_input").value(value)
                    .setNumbers(i == 1 ? 1 : 0, i == 1 ? MarkerSettings.MAX_CHUNKS : MarkerSettings.maxY(tile))
                    .setMaxLength(i == 1 ? 9 : 4).pos(x, 43).size(72, 18));
            for (int sign : new int[]{1, -1}) {
                button(data, tile, sync, panel, name + (sign > 0 ? "_plus" : "_minus"),
                        x + (sign > 0 ? 0 : 38), 65, 34, sign > 0 ? "+" : "-", buffer -> {
                            buffer.readInt(); boolean shift = buffer.readBoolean(), ctrl = buffer.readBoolean();
                            int amount = column == 1 ? 1 : MarkerSettings.delta(shift, ctrl);
                            setters[column].accept(getters[column].getAsInt() + sign * amount);
                        });
            }
        }
        panel.child(IKey.lang("gui.mui.modifiers").asWidget().pos(8, 88).size(232, 10));
        return panel;
    }
}
