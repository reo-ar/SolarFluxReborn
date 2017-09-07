package com.pengu.solarfluxreborn.net;

import com.pengu.hammercore.common.InterItemStack;
import com.pengu.hammercore.common.utils.ItemStackUtil;
import com.pengu.hammercore.net.packetAPI.iPacket;
import com.pengu.hammercore.net.packetAPI.iPacketListener;
import com.pengu.solarfluxreborn.items.UpgradeItem;
import com.pengu.solarfluxreborn.te.SolarPanelTileEntity;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandleUpgradeClick implements iPacket, iPacketListener<PacketHandleUpgradeClick, iPacket>
{
	private BlockPos pos;
	private int slot;
	
	public PacketHandleUpgradeClick()
	{
	}
	
	public PacketHandleUpgradeClick(SolarPanelTileEntity tile, int slot)
	{
		this.slot = slot;
		this.pos = tile.getPos();
	}
	
	@Override
	public iPacket onArrived(PacketHandleUpgradeClick packet, MessageContext context)
	{
		if(context.side == Side.SERVER)
		{
			EntityPlayerMP mp = context.getServerHandler().player;
			InventoryPlayer pinv = mp.inventory;
			SolarPanelTileEntity solar = null;
			if(packet.pos != null)
				solar = mp.world.getTileEntity(pos) instanceof SolarPanelTileEntity ? (SolarPanelTileEntity) mp.world.getTileEntity(pos) : null;
			if(solar != null)
			{
				ItemStack stack = pinv.getItemStack();
				if(!InterItemStack.isStackNull(stack) && stack.getItem() instanceof UpgradeItem)
				{
					int max = Math.min(solar.additionalUpgradeAllowed(stack), InterItemStack.getStackSize(stack));
					if(max == 0)
						return null;
					ItemStack ss = solar.getStackInSlot(packet.slot);
					if(InterItemStack.isStackNull(ss))
					{
						ItemStack n = stack.copy();
						InterItemStack.setStackSize(n, max);
						solar.setInventorySlotContents(packet.slot, n);
						InterItemStack.setStackSize(stack, InterItemStack.getStackSize(stack) - max);
						
						stack = pinv.getItemStack();
						if(ItemStackUtil.shouldReturn(stack))
							pinv.setItemStack(InterItemStack.NULL_STACK);
					} else if(ItemStackUtil.itemsEqual(stack, ss))
					{
						int add = Math.min(max + InterItemStack.getStackSize(ss), ss.getMaxStackSize());
						int delta = add - max;
						InterItemStack.setStackSize(ss, add);
						InterItemStack.setStackSize(stack, InterItemStack.getStackSize(stack) - delta);
						
						stack = pinv.getItemStack();
						if(ItemStackUtil.shouldReturn(stack))
							pinv.setItemStack(InterItemStack.NULL_STACK);
					}
				} else if(InterItemStack.isStackNull(stack))
				{
					ItemStack ss = solar.getStackInSlot(packet.slot);
					if(!InterItemStack.isStackNull(ss))
					{
						pinv.setItemStack(ss.copy());
						solar.setInventorySlotContents(packet.slot, InterItemStack.NULL_STACK);
					}
				}
				solar.sync();
				PacketSyncItemStack pkt = new PacketSyncItemStack();
				pkt.stack = pinv.getItemStack();
				return pkt;
			}
		}
		return null;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setLong("p", pos.toLong());
		nbt.setInteger("s", slot);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		pos = BlockPos.fromLong(nbt.getLong("p"));
		slot = nbt.getInteger("s");
	}
}