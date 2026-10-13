package com.sumirelabs.flexiblemarker.dev;

import com.kotori316.marker.*;
import com.sumirelabs.flexiblemarker.gui.mui.MarkerGuiData;
import com.sumirelabs.flexiblemarker.gui.mui.MarkerGuiFactory;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.kotori316.marker.render.Box;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.Future;

/** Opt-in development-only regression run; never opens or modifies an existing world. */
@SideOnly(Side.CLIENT)
public final class MarkerSmokeTest {
    private static final Logger LOG = LogManager.getLogger("FlexibleMarkerSmoke");
    private static final BlockPos FLEX = new BlockPos(2, 64, 2), CHUNK = new BlockPos(6, 64, 2);
    private int stage, ticks;
    private Future<?> task;

    public static void install() {
        if (Boolean.getBoolean("flexiblemarker.smokeTest")
                && Boolean.TRUE.equals(net.minecraft.launchwrapper.Launch.blackboard.get("fml.deobfuscatedEnvironment")))
            MinecraftForge.EVENT_BUS.register(new MarkerSmokeTest());
    }

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || stage == 9) return;
        Minecraft mc = Minecraft.getMinecraft();
        mc.getToastGui().clear();
        try {
            if (stage == 0) {
                if (!(mc.currentScreen instanceof GuiMainMenu)) return;
                com.cleanroommc.modularui.ModularUIConfig.guiDebugMode = false;
                stage = 1;
                mc.launchIntegratedServer("flexiblemarker-smoke-" + System.currentTimeMillis(), "Flexible Marker Smoke",
                        new WorldSettings(1, GameType.CREATIVE, false, false, WorldType.FLAT).enableCommands());
                return;
            }
            if (mc.player == null || mc.world == null || mc.getIntegratedServer() == null) return;
            if (++ticks > 400) throw new IllegalStateException("Timed out at smoke stage " + stage);
            if (task != null) {
                if (!task.isDone()) return;
                task.get(); task = null;
            }
            IntegratedServer server = mc.getIntegratedServer();
            java.util.UUID uuid = mc.player.getUniqueID();
            if (stage == 1 && ticks >= 40) {
                task = server.addScheduledTask(() -> {
                    EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
                    for (int x = -1; x <= 9; x++) for (int z = -1; z <= 10; z++)
                        player.world.setBlockState(new BlockPos(x, 63, z), Blocks.STONE.getDefaultState());
                    player.world.setBlockState(FLEX, Marker.blockMarker.getDefaultState());
                    player.world.setBlockState(CHUNK, Marker.block16Marker.getDefaultState());
                    ((TileFlexMarker) player.world.getTileEntity(FLEX)).init(EnumFacing.NORTH);
                    ((Tile16Marker) player.world.getTileEntity(CHUNK)).init(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.POSITIVE);
                    player.inventory.setInventorySlotContents(9, new net.minecraft.item.ItemStack(Blocks.COBBLESTONE, 16));
                    player.connection.setPlayerLocation(4, 65, 8, 180, 20);
                });
                next(2);
            } else if (stage == 2 && ticks >= 30 && mc.world.getTileEntity(FLEX) instanceof TileFlexMarker marker
                    && marker.direction != null && marker.boxes != null
                    && mc.world.getTileEntity(CHUNK) instanceof Tile16Marker chunk && chunk.boxes != null) {
                geometry();
                check(Loader.isModLoaded("quarryplus") || !Loader.isModLoaded("scalar"), "Unexpected Scalar dependency");
                check(!MarkerGuiFactory.INSTANCE.canInteractWith(mc.player, new MarkerGuiData(mc.player, FLEX, 1)), "Wrong marker GUI type accepted");
                screenshot(mc, "world");
                task = server.addScheduledTask(() -> {
                    EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
                    MarkerGuiFactory.open(player, 0, player.world, FLEX);
                });
                next(3);
            } else if (stage == 3 && ticks >= 20 && nativeScreen(mc, 0)) {
                screenshot(mc, "flexible-gui");
                check(mc.player.openContainer.inventorySlots.size() == 36, "Unexpected native player slot count");
                press(mc, "forward_plus1");
                next(4);
            } else if (stage == 4 && ticks >= 20) {
                TileFlexMarker marker = (TileFlexMarker) mc.world.getTileEntity(FLEX);
                check(marker.min().getZ() == -9, "Flexible marker packet/client sync failed");
                number(mc, "forward_distance", 99); next(14);
            } else if (stage == 14 && ticks >= 20) {
                check(((TileFlexMarker) mc.world.getTileEntity(FLEX)).min().getZ() == -62, "Flexible numeric input was not bounded to 64");
                number(mc, "forward_distance", 11); next(15);
            } else if (stage == 15 && ticks >= 20) {
                check(((TileFlexMarker) mc.world.getTileEntity(FLEX)).min().getZ() == -9, "Flexible numeric input sync failed");
                task = server.addScheduledTask(() -> {
                    EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
                    TileFlexMarker original = (TileFlexMarker) player.world.getTileEntity(FLEX);
                    check(original.min().getZ() == -9, "Server marker range was not changed");
                    TileFlexMarker restored = new TileFlexMarker();
                    restored.readFromNBT(original.writeToNBT(new NBTTagCompound()));
                    check(restored.min().equals(original.min()) && restored.max().equals(original.max())
                            && restored.direction == original.direction, "Flexible marker NBT round-trip failed");
                    integrations(original);
                    MarkerGuiFactory.open(player, 1, player.world, CHUNK);
                });
                next(5);
            } else if (stage == 5 && ticks >= 20 && nativeScreen(mc, 1)) {
                screenshot(mc, "chunk-gui");
                number(mc, "chunk_count", 2);
                number(mc, "top_y", 68);
                number(mc, "bottom_y", 60);
                next(6);
            } else if (stage == 6 && ticks >= 20) {
                Tile16Marker marker = (Tile16Marker) mc.world.getTileEntity(CHUNK);
                check(marker.getSize() == 32 && marker.min().getY() == 60 && marker.max().getY() == 68,
                        "Chunk marker packet/client sync failed: size=" + marker.getSize() + " min=" + marker.min() + " max=" + marker.max());
                task = server.addScheduledTask(() -> {
                    EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
                    Tile16Marker original = (Tile16Marker) player.world.getTileEntity(CHUNK);
                    Tile16Marker restored = new Tile16Marker();
                    restored.readFromNBT(original.writeToNBT(new NBTTagCompound()));
                    check(restored.min().equals(original.min()) && restored.max().equals(original.max())
                            && original.max().getX() - original.min().getX() - 1 == 32, "Chunk marker NBT round-trip failed");
                    integrations(original);
                });
                next(7);
            } else if (stage == 7) {
                press(mc, "chunk_count_minus"); next(16);
            } else if (stage == 16 && ticks >= 20) {
                check(((Tile16Marker) mc.world.getTileEntity(CHUNK)).getConfiguredSize() == 16, "Chunk decrement failed");
                press(mc, "chunk_count_plus"); next(17);
            } else if (stage == 17 && ticks >= 20) {
                check(((Tile16Marker) mc.world.getTileEntity(CHUNK)).getConfiguredSize() == 32, "Chunk increment failed");
                action(mc, "top_y_plus", true, true); next(10);
            } else if (stage == 10 && ticks >= 20) {
                check(((Tile16Marker) mc.world.getTileEntity(CHUNK)).max().getY() == 132, "Ctrl+Shift Y increment failed");
                action(mc, "top_y_minus", true, true); next(11);
            } else if (stage == 11 && ticks >= 20) {
                check(((Tile16Marker) mc.world.getTileEntity(CHUNK)).max().getY() == 68, "Ctrl+Shift Y decrement failed");
                ModularContainer container = (ModularContainer) mc.player.openContainer;
                int from = playerSlot(container, 9), to = playerSlot(container, 10);
                mc.playerController.windowClick(container.windowId, from, 0, net.minecraft.inventory.ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(container.windowId, to, 0, net.minecraft.inventory.ClickType.PICKUP, mc.player);
                next(12);
            } else if (stage == 12 && ticks >= 20) {
                check(mc.player.inventory.getStackInSlot(9).isEmpty() && mc.player.inventory.getStackInSlot(10).getCount() == 16,
                        "Native client inventory move failed");
                task = server.addScheduledTask(() -> {
                    EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
                    check(player.inventory.getStackInSlot(9).isEmpty() && player.inventory.getStackInSlot(10).getCount() == 16
                            && player.inventory.getItemStack().isEmpty(), "Native server inventory move lost/duplicated items");
                });
                next(13);
            } else if (stage == 13) {
                mc.player.closeScreen(); next(8);
            } else if (stage == 8 && ticks >= 20) {
                screenshot(mc, "updated-world");
                LOG.info("Flexible Marker smoke passed: geometry, rendering, both ModularUI GUIs, buttons, numeric inputs, modifiers, inventory, network sync, NBT, optional integrations; BuildCraft={}, QuarryPlus={}, Scalar={}",
                        Loader.isModLoaded("buildcraftlib"), Loader.isModLoaded("quarryplus"), Loader.isModLoaded("scalar"));
                stage = 9; mc.shutdown();
            }
        } catch (Throwable error) {
            LOG.error("Flexible Marker smoke failed at stage " + stage, error);
            stage = 9; mc.shutdown();
        }
    }

    private static boolean nativeScreen(Minecraft mc, int id) {
        return mc.currentScreen instanceof GuiContainerWrapper && mc.player.openContainer instanceof ModularContainer container
                && container.getGuiData() instanceof MarkerGuiData data && data.id == id;
    }
    private static void press(Minecraft mc, String name) {
        ModularContainer container = (ModularContainer) mc.player.openContainer;
        for (IWidget widget : container.getScreen().getMainPanel().getChildren())
            if (widget.isName(name) && widget instanceof ButtonWidget<?> button) { button.onMousePressed(0); return; }
        throw new IllegalStateException("Missing native button " + name);
    }
    private static void action(Minecraft mc, String name, boolean shift, boolean ctrl) {
        ((ModularContainer) mc.player.openContainer).getSyncManager().getMainPSM().callSyncedAction(name, buffer -> {
            buffer.writeInt(0); buffer.writeBoolean(shift); buffer.writeBoolean(ctrl);
        });
    }
    private static void number(Minecraft mc, String name, int value) {
        IntSyncValue handler = (IntSyncValue) ((ModularContainer) mc.player.openContainer).getSyncManager().getMainPSM().findSyncHandlerNullable(name, 0);
        handler.setIntValue(value, true, true);
    }
    private static int playerSlot(ModularContainer container, int index) {
        for (int i = 0; i < container.inventorySlots.size(); i++)
            if (container.getModularSlot(i).getSlotIndex() == index) return i;
        throw new IllegalStateException("Missing player slot " + index);
    }

    private static void integrations(net.minecraft.tileentity.TileEntity marker) {
        if (Loader.isModLoaded("buildcraftlib")) {
            check(marker instanceof buildcraft.api.tiles.ITileAreaProvider, "BuildCraft interface missing");
            check(marker.getCapability(buildcraft.api.tiles.TilesAPI.CAP_TILE_AREA_PROVIDER, null) == marker, "BuildCraft capability missing");
        }
        if (Loader.isModLoaded("quarryplus")) {
            check(marker instanceof com.yogpc.qp.tile.IMarker, "QuarryPlus IMarker missing");
            check(((com.yogpc.qp.tile.IMarker) marker).hasLink(), "QuarryPlus marker link missing");
        }
    }

    private static void geometry() {
        TextureAtlasSprite sprite = new TextureAtlasSprite("smoke") {};
        sprite.setIconWidth(16); sprite.setIconHeight(16); sprite.initSprite(16, 16, 0, 0, false);
        double[][] ends = {{2,0,0},{0,2,0},{0,0,2},{2,0,2},{2,1,3},{0,0,0}};
        for (double[] end : ends) {
            Box box = Box.apply(1, 2, 3, 1 + end[0], 2 + end[1], 3 + end[2], .125, .125, .125, true, true);
            Recorder recorder = new Recorder(); box.render(recorder, sprite);
            int expected = 16 * (MathHelper.floor(Math.sqrt(end[0]*end[0]+end[1]*end[1]+end[2]*end[2]) / .125) + 1) + 8;
            check(recorder.vertices == expected, "Unexpected vertex count for " + box.getClass().getSimpleName());
        }
    }

    private static final class Recorder extends BufferBuilder {
        int vertices;
        Recorder() { super(256); }
        @Override public BufferBuilder pos(double x, double y, double z) {
            check(Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z), "Non-finite vertex"); return this;
        }
        @Override public BufferBuilder color(int r, int g, int b, int a) { return this; }
        @Override public BufferBuilder tex(double u, double v) { check(Double.isFinite(u) && Double.isFinite(v), "Non-finite UV"); return this; }
        @Override public BufferBuilder lightmap(int x, int y) { return this; }
        @Override public void endVertex() { vertices++; }
    }
    private void next(int stage) { this.stage = stage; ticks = 0; }
    private static void check(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static void screenshot(Minecraft mc, String name) {
        ScreenShotHelper.saveScreenshot(mc.gameDir, "flexiblemarker-" + name + ".png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
    }
}
