package com.pengu.solarfluxreborn.utility;

import net.minecraft.util.text.translation.I18n;

import com.pengu.solarfluxreborn.reference.Reference;

public final class Lang
{
	public static final String MOD_PREFIX = "info." + Reference.MOD_ID.toLowerCase() + ".";
	
	private Lang()
	{
	}
	
	public static String localise(String text)
	{
		return localise(text, true);
	}
	
	public static String localise(String text, boolean appendModPrefix)
	{
		if(appendModPrefix)
			text = MOD_PREFIX + text;
		return I18n.translateToLocal(text);
	}
}
