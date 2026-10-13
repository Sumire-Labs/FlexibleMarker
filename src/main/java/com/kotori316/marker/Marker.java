package com.kotori316.marker;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kotori316.marker.gui.GuiHandler;
import com.kotori316.marker.packet.PacketHandler;

@Mod(modid = Marker.modID, name = Marker.ModName, version = MarkerTags.VERSION,
    dependencies = "required-after:cleanroom;required-after:modularui@[3.1.6,);after:buildcraftlib;after:quarryplus", acceptedMinecraftVersions = "[1.12.2]")
public class Marker {
    public static final String modID = MarkerTags.MOD_ID;
    public static final String ModName = MarkerTags.MOD_NAME;
    public static final String NETWORK_CHANNEL = "FlexibleMarker";
    public static final BlockMarker blockMarker = new BlockMarker.BlockFlexMarker();
    public static final BlockMarker block16Marker = new BlockMarker.Block16Marker();
    private static final Marker instance;

    static {
        instance = new Marker();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        com.sumirelabs.flexiblemarker.gui.mui.MarkerGuiFactory.register();
        MinecraftForge.EVENT_BUS.register(getInstance());
        NetworkRegistry.INSTANCE.registerGuiHandler(getInstance(), new GuiHandler());
        PacketHandler.init();
    }

    @Mod.EventHandler
    @SideOnly(value = Side.CLIENT)
    public void preClientInit(FMLPreInitializationEvent event) {
        MarkerClient.preClientInit(event);
    }

    @Mod.InstanceFactory
    public static Marker getInstance() {
        return instance;
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(blockMarker);
        event.getRegistry().register(block16Marker);
        TileEntity.register(modID + ":flexiblemarker", TileFlexMarker.class);
        TileEntity.register(modID + ":16marker", Tile16Marker.class);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(blockMarker.itemBlock);
        event.getRegistry().register(block16Marker.itemBlock);
    }

}
