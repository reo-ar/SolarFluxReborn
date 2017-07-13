package com.pengu.solarfluxreborn;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import com.pengu.hammercore.annotations.MCFBus;
import com.pengu.hammercore.net.HCNetwork;
import com.pengu.solarfluxreborn.net.PacketMakeRemoteConfigs;

@MCFBus
public class SyncManager
{
	@SubscribeEvent
	public void playerLoggedIn(PlayerLoggedInEvent evt)
	{
		if(!evt.player.world.isRemote && evt.player instanceof EntityPlayerMP)
			HCNetwork.manager.sendTo(new PacketMakeRemoteConfigs(), (EntityPlayerMP) evt.player);
	}
}