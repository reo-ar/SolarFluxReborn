package com.pengu.solarfluxreborn.net;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.pengu.hammercore.common.InterItemStack;
import com.pengu.hammercore.net.packetAPI.IPacket;
import com.pengu.hammercore.net.packetAPI.IPacketListener;

public class PacketSyncItemStack implements IPacket, IPacketListener<PacketSyncItemStack, IPacket>
{
	public ItemStack stack = InterItemStack.NULL_STACK;
	
	@Override
	public IPacket onArrived(PacketSyncItemStack packet, MessageContext context)
	{
		if(context.side == Side.CLIENT)
			packet.sendClient();
		else
			context.getServerHandler().player.inventory.setItemStack(packet.stack);
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public void sendClient()
	{
		Minecraft.getMinecraft().player.inventory.setItemStack(stack);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setBoolean("null", InterItemStack.isStackNull(stack));
		if(!InterItemStack.isStackNull(stack))
			stack.writeToNBT(nbt);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		if(!nbt.getBoolean("null"))
			stack = new ItemStack(nbt);
	}
}