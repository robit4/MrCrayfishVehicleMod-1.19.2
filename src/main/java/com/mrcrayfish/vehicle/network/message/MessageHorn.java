package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageHorn implements IMessage<MessageHorn>
{
	private boolean horn;

	public MessageHorn() {}

	public MessageHorn(boolean horn)
	{
		this.horn = horn;
	}

	@Override
	public void encode(MessageHorn message, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(message.horn);
	}

	@Override
	public MessageHorn decode(FriendlyByteBuf buffer)
	{
		return new MessageHorn(buffer.readBoolean());
	}

	@Override
	public void handle(MessageHorn message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				ServerPlayHandler.handleHornMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public boolean isHorn()
	{
		return this.horn;
	}
}
