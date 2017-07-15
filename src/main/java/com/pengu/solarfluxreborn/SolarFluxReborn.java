package com.pengu.solarfluxreborn;

import java.io.File;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.pengu.hammercore.ext.TeslaAPI;
import com.pengu.solarfluxreborn.config.BlackHoleStorageConfigs;
import com.pengu.solarfluxreborn.config.DraconicEvolutionConfigs;
import com.pengu.solarfluxreborn.config.ModConfiguration;
import com.pengu.solarfluxreborn.err.NoSolarsRegisteredException;
import com.pengu.solarfluxreborn.gui.GuiHandler;
import com.pengu.solarfluxreborn.init.BlocksSFR;
import com.pengu.solarfluxreborn.init.ItemsSFR;
import com.pengu.solarfluxreborn.init.RecipeIO;
import com.pengu.solarfluxreborn.proxy.CommonProxy;
import com.pengu.solarfluxreborn.reference.Reference;
import com.pengu.solarfluxreborn.te.AbstractSolarPanelTileEntity;
import com.pengu.solarfluxreborn.te.SolarPanelTileEntity;
import com.pengu.solarfluxreborn.te.cable.TileCustomCable;
import com.pengu.solarfluxreborn.utility.SFRLog;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, guiFactory = "com.pengu.solarfluxreborn.config.ConfigurationGuiFactory", dependencies = "required-after:redstoneflux;required-after:hammercore;after:blackholestorage")
public class SolarFluxReborn
{
	@Mod.Instance(Reference.MOD_ID)
	public static SolarFluxReborn instance;
	
	@SidedProxy(clientSide = "com.pengu.solarfluxreborn.proxy.ClientProxy", serverSide = "com.pengu.solarfluxreborn.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	public static File cfgFolder;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent pEvent)
	{
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(proxy);
		String cfg = pEvent.getSuggestedConfigurationFile().getAbsolutePath();
		cfg = cfg.substring(0, cfg.lastIndexOf("."));
		cfgFolder = new File(cfg);
		cfgFolder.mkdirs();
		File main_cfg = new File(cfgFolder, "main.cfg");
		File draconicevolution = new File(cfgFolder, "DraconicEvolution.cfg");
		File blackholestorage = new File(cfgFolder, "BlackHoleStorage.cfg");
		File version_file = new File(cfgFolder, "version.dat");
		ModConfiguration.initialize(main_cfg, version_file);
		DraconicEvolutionConfigs.initialize(draconicevolution);
		BlackHoleStorageConfigs.initialize(blackholestorage);
		
		GameRegistry.registerTileEntity(SolarPanelTileEntity.class, Reference.MOD_ID + ":solar");
		GameRegistry.registerTileEntity(AbstractSolarPanelTileEntity.class, Reference.MOD_ID + ":abstractsolar");
		GameRegistry.registerTileEntity(TileCustomCable.class, Reference.MOD_ID + ":cable_custom");
		BlocksSFR.initialize();
		
		if(BlocksSFR.getSolarPanels().isEmpty())
		{
			boolean deleted = main_cfg.delete();
			throw new NoSolarsRegisteredException("No solar panels was registered in config file." + (deleted ? "\nSolarFluxReborn configs were removed." : "Please remove file \"" + main_cfg.getAbsolutePath() + "\" manually.") + "\nTry restarting game.", false);
		}
		
		ItemsSFR.initialize();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent pEvent)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
		FMLInterModComms.sendMessage("waila", "register", "com.pengu.solarfluxreborn.intr.waila.WailaIntegrar.registerWAIA");
		proxy.init();
		
		RecipeIO.reload();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		proxy.postInit();
	}
	
	@EventHandler
	public void loadWorld(FMLServerStartingEvent e)
	{
		SFRLog.info("Loading TeslaAPI...");
		int classesLoaded = TeslaAPI.refreshTeslaClassData();
		SFRLog.info("TeslaAPI loaded " + classesLoaded + "/" + TeslaAPI.allClasses.size() + " required classes.");
	}
	
	@EventHandler
	public void printMessage(FMLServerStartedEvent e)
	{
		if(ModConfiguration.willNotify && proxy.getClass() != CommonProxy.class)
		{
			SFRLog.bigWarning(TextFormatting.RED + "WARNING: Your configs have been replaced.");
			ModConfiguration.updateNotification(false);
		}
	}
}