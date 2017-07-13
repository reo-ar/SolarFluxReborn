package com.pengu.solarfluxreborn.proxy;

import java.io.IOException;
import java.util.Scanner;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

import com.pengu.hammercore.client.render.item.ItemRenderingHandler;
import com.pengu.hammercore.client.render.tesr.TESR;
import com.pengu.solarfluxreborn.client.tesr.RenderCustomCable;
import com.pengu.solarfluxreborn.client.tesr.RenderSolarPanelTile;
import com.pengu.solarfluxreborn.config.ModConfiguration;
import com.pengu.solarfluxreborn.config.RemoteConfigs;
import com.pengu.solarfluxreborn.init.BlocksSFR;
import com.pengu.solarfluxreborn.reference.Reference;
import com.pengu.solarfluxreborn.te.SolarPanelTileEntity;
import com.pengu.solarfluxreborn.te.cable.TileCustomCable;
import com.pengu.solarfluxreborn.utility.SFRLog;

public class ClientProxy extends CommonProxy
{
	@Override
	public void init()
	{
		RenderSolarPanelTile solarRender = new RenderSolarPanelTile();
		RenderCustomCable cableRender = new RenderCustomCable();
		
		ClientRegistry.bindTileEntitySpecialRenderer(SolarPanelTileEntity.class, solarRender);
		ClientRegistry.bindTileEntitySpecialRenderer(TileCustomCable.class, cableRender);
		
		for(Block solar : BlocksSFR.getSolarPanels())
			ItemRenderingHandler.INSTANCE.bindItemRender(Item.getItemFromBlock(solar), solarRender);
		
		ItemRenderingHandler.INSTANCE.bindItemRender(Item.getItemFromBlock(BlocksSFR.cable1), cableRender);
		ItemRenderingHandler.INSTANCE.bindItemRender(Item.getItemFromBlock(BlocksSFR.cable2), cableRender);
		ItemRenderingHandler.INSTANCE.bindItemRender(Item.getItemFromBlock(BlocksSFR.cable3), cableRender);
	}
	
	private <T extends TileEntity> void registerRender(Class<T> tileEntityClass, Block block, TESR<? super T> specialRenderer)
	{
		ClientRegistry.bindTileEntitySpecialRenderer(tileEntityClass, specialRenderer);
		ItemRenderingHandler.INSTANCE.bindItemRender(Item.getItemFromBlock(block), specialRenderer);
	}
	
	@SubscribeEvent
	public void pte(RenderGameOverlayEvent e)
	{
		if(ModConfiguration.willNotify)
		{
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("[" + Reference.MOD_NAME + "] WARNING: Your configs have been replaced."));
			ModConfiguration.updateNotification(false);
		}
	}
	
	@SubscribeEvent
	public void disconnect(PlayerLoggedOutEvent evt)
	{
		if(evt.player.world.isRemote)
			RemoteConfigs.reset();
	}
	
	@Override
	public void postInit()
	{
		textureStitch(new TextureStitchEvent.Pre(Minecraft.getMinecraft().getTextureMapBlocks()));
	}
	
	@SubscribeEvent
	public void textureStitch(TextureStitchEvent.Pre evt)
	{
		try
		{
			int sprite = 0;
			SFRLog.info("Loadeding sprites...");
			Scanner s = new Scanner(Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(Reference.MOD_ID, "textures/blocks/_sprites.txt")).getInputStream());
			while(s.hasNextLine())
			{
				String ln = s.nextLine();
				if(ln.isEmpty())
					continue;
				evt.getMap().registerSprite(new ResourceLocation(Reference.MOD_ID, "blocks/" + ln));
				sprite++;
			}
			s.close();
			SFRLog.info("Loaded " + sprite + " sprites!");
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}