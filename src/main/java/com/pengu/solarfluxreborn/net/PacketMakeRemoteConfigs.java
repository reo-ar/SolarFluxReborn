package com.pengu.solarfluxreborn.net;

import com.pengu.hammercore.net.packetAPI.iPacket;
import com.pengu.hammercore.net.packetAPI.iPacketListener;
import com.pengu.solarfluxreborn.config.RemoteConfigs;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMakeRemoteConfigs implements iPacket, iPacketListener<PacketMakeRemoteConfigs, iPacket>
{
	@Override
	public iPacket onArrived(PacketMakeRemoteConfigs packet, MessageContext context)
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