package com.zeitheron.solarflux;

import com.zeitheron.solarflux.api.SolarFluxAPI;
import com.zeitheron.solarflux.api.SolarInfo;
import com.zeitheron.solarflux.api.compat.ISolarFluxCompat;
import com.zeitheron.solarflux.api.compat.SFCompat;
import com.zeitheron.solarflux.block.BlockBaseSolar;
import com.zeitheron.solarflux.block.ItemBlockBaseSolar;
import com.zeitheron.solarflux.block.tile.TileBaseSolar;
import com.zeitheron.solarflux.gui.GuiHandlerSF;
import com.zeitheron.solarflux.init.ItemsSF;
import com.zeitheron.solarflux.init.RecipesSF;
import com.zeitheron.solarflux.init.SolarsSF;
import com.zeitheron.solarflux.net.NetworkSF;
import com.zeitheron.solarflux.proxy.ISFProxy;
import com.zeitheron.solarflux.utils.charging.ItemChargeHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod(modid = InfoSF.MOD_ID, name = "Solar Flux Reborn", version = InfoSF.VERSION, certificateFingerprint = "4d7b29cd19124e986da685107d16ce4b49bc0a97", updateJSON = "https://dccg.herokuapp.com/api/fmluc/246974", dependencies = "after:thaumcraft@[6.1.BETA26,);after:avaritia@[3.3.0,);after:draconicevolution@[2.3.18,)")
public class SolarFlux
{
	public static final Logger LOG = LogManager.getLogger(InfoSF.MOD_ID);

	@SidedProxy(clientSide = InfoSF.PROXY_CLIENT, serverSide = InfoSF.PROXY_SERVER)
	public static ISFProxy proxy;

	@Instance
	public static SolarFlux instance;

	@EventHandler
	public void construct(FMLConstructionEvent evt)
	{
		MinecraftForge.EVENT_BUS.register(proxy);
		MinecraftForge.EVENT_BUS.register(this);

		SolarFluxAPI.renderRenderer = proxy::render;

		proxy.construct();
	}

	public static final Set<ISolarFluxCompat> compats = new HashSet<>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		Map<String, ISolarFluxCompat> cmap = new HashMap<>();
		List<String> clist = new ArrayList<>();

		Configuration integrations = new Configuration();
		for(ASMData data : e.getAsmData().getAll(SFCompat.class.getCanonicalName()))
			try
			{
				String modid = data.getAnnotationInfo().get("modid").toString();

				if(!Loader.isModLoaded(modid))
				{
					LOG.debug("Skipped SolarFlux compat - " + data.getClassName() + " @" + modid + " not found!");
					continue;
				}

				Class c = Class.forName(data.getClassName());

				if(!ISolarFluxCompat.class.isAssignableFrom(c))
				{
					LOG.error("Found class that expects a compat from SolarFlux, but it doesn't implement " + ISolarFluxCompat.class.getName() + "!");
					continue;
				}

				clist.add(modid);

				ISolarFluxCompat icompat = ISolarFluxCompat.class.cast(c.newInstance());

				compats.add(icompat);

				if(cmap.get(modid) == null)
					cmap.put(modid, icompat);
				else
					cmap.put(modid, cmap.get(modid).merge(icompat));

				MinecraftForge.EVENT_BUS.register(icompat);

				LOG.info("Added SolarFlux compat - " + c.getCanonicalName());
			} catch(Throwable err)
			{
				if(err instanceof ClassNotFoundException || err instanceof NoClassDefFoundError)
				{
					LOG.debug("Skipped SolarFlux compat - " + data.getClassName() + ", requested mod not found!");
					continue;
				}

				err.printStackTrace();
			}

		{
			File modCfgFile = new File(e.getModConfigurationDirectory(), InfoSF.MOD_ID);
			if(!modCfgFile.isDirectory()) modCfgFile.mkdirs();
			modCfgFile = new File(modCfgFile, "compats.cfg");
			Configuration compatConfigs = new Configuration(modCfgFile);

			for(String modid : clist)
			{
				String modname = Loader.isModLoaded(modid) ? Loader.instance().getIndexedModList().get(modid).getName() : modid;

				boolean a = 1 == compatConfigs.getInt(modid, "States", modname != null ? 1 : 0, 0, 1, "Should Solar Flux Reborn enable compat for '" + modname + "'?\n1 - Enable, 0 - Disable.");
				if(!a && cmap.containsKey(modid))
				{
					compats.remove(cmap.get(modid));
					MinecraftForge.EVENT_BUS.unregister(cmap.get(modid));
					LOG.info("Disable SolarFlux compat - " + cmap.get(modid).getClass().getCanonicalName());
				}
			}

			compatConfigs.getCategory("States").setComment("If you are a pack developer, ensure that client and server have the same compats enabled/disabled to connect!");
			compatConfigs.getCategory("States").setRequiresMcRestart(true);

			if(compatConfigs.hasChanged())
				compatConfigs.save();
		}

		SolarsSF.preInit(new File(e.getModConfigurationDirectory(), InfoSF.MOD_ID));

		// Register plugin's panels
		List<SolarInfo> subs = new ArrayList<>();
		compats.forEach(i ->
		{
			List<SolarInfo> lo = new ArrayList<>();
			i.registerSolarInfos(lo);
			i.registerInvListers(ItemChargeHelper.playerInvListers);
			lo.forEach(si -> si.setCompatMod(i.getClass().getAnnotation(SFCompat.class).modid()));
			subs.addAll(lo);
		});
		subs.forEach(si ->
		{
			SolarFluxAPI.SOLAR_PANELS.register(si);
			BlockBaseSolar block = si.getBlock();
			ForgeRegistries.BLOCKS.register(block);
			Item ib = new ItemBlockBaseSolar(block);
			ib.setRegistryName(block.getRegistryName());
			ForgeRegistries.ITEMS.register(ib);
			SolarFluxAPI.renderRenderer.accept(ib);
			proxy.onPanelRegistered(si);
		});
		subs.clear();

		ItemsSF.preInit();

		compats.forEach(ISolarFluxCompat::preInit);

		TileEntity.register(InfoSF.MOD_ID + ":base_solar", TileBaseSolar.class);

		StringBuilder en_us_lang = new StringBuilder("# Builtin & Generated by Solar Flux Reborn lang file.\n");
		for(SolarInfo si : SolarFluxAPI.SOLAR_PANELS.getValuesCollection())
			if(si.localizations != null && si.localizations.containsKey("en_us"))
				en_us_lang.append("\n" + si.getBlock().getTranslationKey() + ".name=" + si.localizations.get("en_us"));
		LanguageMap.inject(new ByteArrayInputStream(en_us_lang.toString().getBytes(StandardCharsets.UTF_8)));

		proxy.preInit();
	}

	@SubscribeEvent
	public void createRegistries(RegistryEvent.NewRegistry e)
	{
		SolarFluxAPI.SOLAR_PANELS = new RegistryBuilder<SolarInfo>().setName(new ResourceLocation(InfoSF.MOD_ID, "panels")).setType(SolarInfo.class).create();
	}

	@SubscribeEvent
	public void registerRecipesEvent(RegistryEvent.Register<IRecipe> event)
	{
		RecipesSF.register(event.getRegistry());
		compats.forEach(s -> s.registerRecipes(event.getRegistry()));
	}

	@EventHandler
	public void init(FMLInitializationEvent e)
	{
		FinalFieldHelper.setStaticFinalField(NetworkSF.class, "INSTANCE", new NetworkSF());

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandlerSF());
		proxy.init();

		compats.forEach(ISolarFluxCompat::init);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		compats.forEach(ISolarFluxCompat::postInit);
		proxy.postInit();
	}

	@EventHandler
	public void certificateViolation(FMLFingerprintViolationEvent e)
	{
		LOG.warn("*****************************");
		LOG.warn("WARNING: Somebody has been tampering with SolarFluxReborn jar!");
		LOG.warn("It is highly recommended that you redownload mod from https://www.curseforge.com/projects/246974 !");
		LOG.warn("*****************************");

		try
		{
			Class HammerCore = Class.forName("com.zeitheron.hammercore.HammerCore");
			Map<String, String> invalidCertificates = (Map<String, String>) HammerCore.getDeclaredField("invalidCertificates").get(null);
			invalidCertificates.put(InfoSF.MOD_ID, "https://www.curseforge.com/projects/246974");
		} catch(Throwable err)
		{
			if(err instanceof ClassNotFoundException || err instanceof NoClassDefFoundError)
				return;
			err.printStackTrace();
		}
	}

	public static class FinalFieldHelper
	{
		private static Field modifiersField;
		private static Object reflectionFactory;
		private static Method newFieldAccessor;
		private static Method fieldAccessorSet;

		static boolean setStaticFinalField(Class<?> cls, String var, Object val)
		{
			try
			{
				Field f = cls.getDeclaredField(var);
				if(Modifier.isStatic(f.getModifiers()))
					return setFinalField(f, null, val);
				return false;
			} catch(Throwable err)
			{
				err.printStackTrace();
			}
			return false;
		}

		static Field makeWritable(Field f) throws ReflectiveOperationException
		{
			f.setAccessible(true);
			if(modifiersField == null)
			{
				Method getReflectionFactory = Class.forName("sun.reflect.ReflectionFactory").getDeclaredMethod("getReflectionFactory");
				reflectionFactory = getReflectionFactory.invoke(null);
				newFieldAccessor = Class.forName("sun.reflect.ReflectionFactory").getDeclaredMethod("newFieldAccessor", Field.class, boolean.class);
				fieldAccessorSet = Class.forName("sun.reflect.FieldAccessor").getDeclaredMethod("set", Object.class, Object.class);
				modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
			}
			modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			return f;
		}

		public static boolean setFinalField(Field f, @Nullable Object instance, Object thing) throws ReflectiveOperationException
		{
			if(Modifier.isFinal(f.getModifiers()))
			{
				makeWritable(f);
				Object fieldAccessor = newFieldAccessor.invoke(reflectionFactory, f, false);
				fieldAccessorSet.invoke(fieldAccessor, instance, thing);
				return true;
			}
			return false;
		}
	}
}