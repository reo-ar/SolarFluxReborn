package com.pengu.solarfluxreborn.init;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.google.common.collect.Lists;
import com.pengu.solarfluxreborn.blocks.AbstractSolarPanelBlock;
import com.pengu.solarfluxreborn.blocks.BlockCable320;
import com.pengu.solarfluxreborn.blocks.BlockCable3200;
import com.pengu.solarfluxreborn.blocks.BlockCable320000;
import com.pengu.solarfluxreborn.blocks.SolarPanelBlock;
import com.pengu.solarfluxreborn.config.BlackHoleStorageConfigs;
import com.pengu.solarfluxreborn.config.DraconicEvolutionConfigs;
import com.pengu.solarfluxreborn.config.ModConfiguration;
import com.pengu.solarfluxreborn.items.CableItemBlock;
import com.pengu.solarfluxreborn.items.SolarPanelItemBlock;
import com.pengu.solarfluxreborn.reference.Reference;
import com.pengu.solarfluxreborn.utility.MetricUnits;

public class BlocksSFR
{
	private static final List<Block> mSolarPanels = Lists.newArrayList();
	
	public static final Block cable1 = new BlockCable320();
	public static final Block cable2 = new BlockCable3200();
	public static final Block cable3 = new BlockCable320000();
	
	public static AbstractSolarPanelBlock draconicSolar, chaoticSolar;
	public static AbstractSolarPanelBlock darkMatterSolar;
	
	private BlocksSFR()
	{
	}
	
	public static void initialize()
	{
		mSolarPanels.clear();
		for(int tierIndex = 0; tierIndex < ModConfiguration.getTierConfigurations().size(); tierIndex++)
		{
			SolarPanelBlock block = new SolarPanelBlock("solar" + tierIndex, tierIndex);
			register(block, "solar" + tierIndex, new SolarPanelItemBlock(block));
			mSolarPanels.add(block);
		}
		
		if(DraconicEvolutionConfigs.canIntegrate)
		{
			if(DraconicEvolutionConfigs.draconicSolar)
			{
				AbstractSolarPanelBlock block = new AbstractSolarPanelBlock("solardraconic", 512 * MetricUnits.MEGA, 1024 * MetricUnits.KILO, 131072);
				register(draconicSolar = block, "solardraconic", new SolarPanelItemBlock(block));
				mSolarPanels.add(block);
			}
			
			if(DraconicEvolutionConfigs.chaoticSolar)
			{
				AbstractSolarPanelBlock block = new AbstractSolarPanelBlock("solarchaotic", 2048 * MetricUnits.MEGA, 4096 * MetricUnits.KILO, 524288);
				register(chaoticSolar = block, "solarchaotic", new SolarPanelItemBlock(block));
				mSolarPanels.add(block);
			}
		}
		
		if(BlackHoleStorageConfigs.canIntegrate)
		{
			if(BlackHoleStorageConfigs.darkMatterSolar)
			{
				AbstractSolarPanelBlock block = new AbstractSolarPanelBlock("solardarkmatter", 512 * MetricUnits.MEGA, 1024 * MetricUnits.KILO, 131072).setUseConnectedTextures();
				register(darkMatterSolar = block, "solardarkmatter", new SolarPanelItemBlock(block));
				mSolarPanels.add(block);
			}
		}
		
		register(cable1, "wire_1", new CableItemBlock(cable1));
		register(cable2, "wire_2", new CableItemBlock(cable2));
		register(cable3, "wire_3", new CableItemBlock(cable3));
	}
	
	public static List<Block> getSolarPanels()
	{
		return mSolarPanels;
	}
	
	public static Block register(Block block, String name, ItemBlock ib)
	{
		ib.setRegistryName(Reference.MOD_ID, name);
		block.setRegistryName(Reference.MOD_ID, name);
		GameRegistry.findRegistry(Block.class).register(block);
		GameRegistry.findRegistry(Item.class).register(ib);
		com.pengu.hammercore.init.ItemsHC.items.add(ib);
		return block;
	}
}
