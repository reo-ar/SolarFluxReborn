package com.zeitheron.solarflux.proxy;

import net.minecraft.item.Item;

public interface ISFProxy
{
	default void init()
	{
	}
	
	default void render(Item item)
	{
	}
}