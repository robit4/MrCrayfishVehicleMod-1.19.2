package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageFuelVehicle implements IMessage<MessageFuelVehicle>
{
    protected int entityId;
    private InteractionHand hand;

    public MessageFuelVehicle()
    {
    }

    public MessageFuelVehicle(int entityId, InteractionHand hand)
    {
        this.entityId = entityId;
        this.hand = hand;
    }

    @Override
    public void encode(MessageFuelVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeEnum(message.hand);
    }

    @Override
    public MessageFuelVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessageFuelVehicle(buffer.readInt(), buffer.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(MessageFuelVehicle message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayerEntity player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleFuelVehicleMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public InteractionHand getHand()
    {
        return this.hand;
    }
}