package com.pengu.solarfluxreborn.net;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.pengu.hammercore.net.packetAPI.IPacket;
import com.pengu.hammercore.net.packetAPI.IPacketListener;
import com.pengu.solarfluxreborn.config.RemoteConfigs;

public class PacketMakeRemoteConfigs implements IPacket, IPacketListener<PacketMakeRemoteConfigs, IPacket>
{
	@Override
	public IPacket onArrived(PacketMakeRemoteConfigs packet, MessageContext context)
	{
		return null;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setByteArray("cfg", RemoteConfigs.pack());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		RemoteConfigs.unpack(nbt.getByteArray("cfg"));
	}
}