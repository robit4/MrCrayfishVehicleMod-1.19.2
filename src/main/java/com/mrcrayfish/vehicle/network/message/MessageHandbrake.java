package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageHandbrake implements IMessage<MessageHandbrake>
{
	private boolean handbrake;

	public MessageHandbrake() {}

	public MessageHandbrake(boolean handbrake)
	{
		this.handbrake = handbrake;
	}

	@Override
	public void encode(MessageHandbrake message, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(message.handbrake);
	}

	@Override
	public MessageHandbrake decode(FriendlyByteBuf buffer)
	{
		return new MessageHandbrake(buffer.readBoolean());
	}

	@Override
	public void handle(MessageHandbrake message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				ServerPlayHandler.handleHandbrakeMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public boolean isHandbrake()
	{
		return this.handbrake;
	}
}
