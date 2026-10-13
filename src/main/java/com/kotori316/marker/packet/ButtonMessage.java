package com.kotori316.marker.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.kotori316.marker.TileFlexMarker;

/**
 * To server only.
 */
public class ButtonMessage implements IMessage {
    private BlockPos pos;
    private int dim;
    private TileFlexMarker.Movable movable;
    private int amount;

    @SuppressWarnings("unused")
    public ButtonMessage() {
    }

    public ButtonMessage(BlockPos pos, int dim, TileFlexMarker.Movable movable, int amount) {
        this.pos = pos;
        this.dim = dim;
        this.movable = movable;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer p = new PacketBuffer(buf);
        pos = p.readBlockPos();
        dim = p.readInt();
        movable = p.readEnumValue(TileFlexMarker.Movable.class);
        amount = p.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer p = new PacketBuffer(buf);
        p.writeBlockPos(pos).writeInt(dim);
        p.writeEnumValue(movable).writeInt(amount);
    }

    public static IMessage onReceive(ButtonMessage message, MessageContext ctx) {
        net.minecraft.entity.player.EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            World world = player.world;
            if (world.provider.getDimension() != message.dim || player.getDistanceSqToCenter(message.pos) > 64
                    || !world.isBlockLoaded(message.pos)) return;
            TileEntity entity = world.getTileEntity(message.pos);
            if (entity instanceof TileFlexMarker marker) {
                marker.move(message.movable, message.amount);
                marker.markDirty();
                PacketHandler.sendToClient(new AreaMessage(message.pos, message.dim, marker.min(), marker.max()), player);
            }
        });
        return null;
    }
}
