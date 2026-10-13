package com.kotori316.marker.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.kotori316.marker.Tile16Marker;

/**
 * To server only.
 */
public class Button16Message implements IMessage {
    private BlockPos pos;
    private int dim;
    private int amount;
    private int yMax;
    private int yMin;

    @SuppressWarnings("unused")
    public Button16Message() {
    }

    public Button16Message(BlockPos pos, int dim, int amount, int yMax, int yMin) {
        this.pos = pos;
        this.dim = dim;
        this.amount = amount;
        this.yMax = yMax;
        this.yMin = yMin;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer p = new PacketBuffer(buf);
        pos = p.readBlockPos();
        dim = p.readInt();
        amount = p.readInt();
        yMax = p.readInt();
        yMin = p.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer p = new PacketBuffer(buf);
        p.writeBlockPos(pos).writeInt(dim);
        p.writeInt(amount);
        p.writeInt(yMax).writeInt(yMin);
    }

    public static IMessage onReceive(Button16Message message, MessageContext ctx) {
        net.minecraft.entity.player.EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            World world = player.world;
            if (world.provider.getDimension() != message.dim || player.getDistanceSqToCenter(message.pos) > 64
                    || !world.isBlockLoaded(message.pos) || message.amount < 16 || message.yMin < 0 || message.yMax < message.yMin) return;
            TileEntity entity = world.getTileEntity(message.pos);
            if (entity instanceof Tile16Marker marker) {
                marker.changeSize(message.amount, message.yMax, message.yMin);
                marker.markDirty();
                PacketHandler.sendToClient(new AreaMessage(message.pos, message.dim, marker.min(), marker.max()), player);
            }
        });
        return null;
    }
}
