package com.pengu.solarfluxreborn.config;

import net.minecraft.client.gui.GuiScreen;

import com.pengu.hammercore.cfg.gui.HCConfigGui;
import com.pengu.solarfluxreborn.reference.Reference;

public class ModConfigurationGui extends HCConfigGui
{
	public ModConfigurationGui(GuiScreen pGuiScreen)
	{
		super(pGuiScreen, ModConfiguration.getConfiguration(), Reference.MOD_ID);
	}
}
