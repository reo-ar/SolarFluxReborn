package com.pengu.solarfluxreborn;

import com.pengu.hammercore.annotations.MCFBus;
import com.pengu.hammercore.net.HCNetwork;
import com.pengu.solarfluxreborn.net.PacketMakeRemoteConfigs;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

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