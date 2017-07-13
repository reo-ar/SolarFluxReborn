package com.pengu.solarfluxreborn.client.tesr;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.pengu.hammercore.client.DestroyStageTexture;
import com.pengu.hammercore.client.render.tesr.TESR;
import com.pengu.hammercore.client.utils.RenderBlocks;
import com.pengu.solarfluxreborn.blocks.BlockAbstractCable;
import com.pengu.solarfluxreborn.reference.Reference;
import com.pengu.solarfluxreborn.te.cable.TileCustomCable;

public class RenderCustomCable extends TESR<TileCustomCable>
{
	private static final Predicate<EnumFacing> Y_AX = new Predicate<EnumFacing>()
	{
		@Override
		public boolean apply(EnumFacing input)
		{
			return input.getAxis() != Axis.Z;
		}
	};
	
	@Override
	public void renderItem(ItemStack item)
	{
		ResourceLocation txt = null;
		if(item.getItem() instanceof ItemBlock && ((ItemBlock) item.getItem()).getBlock() instanceof BlockAbstractCable)
			txt = ((BlockAbstractCable) ((ItemBlock) item.getItem()).getBlock()).getConnection();
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		renderWire(Y_AX, 0, 0, 0, Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(txt + ""), getBrightnessForRB(null, RenderBlocks.forMod(Reference.MOD_ID)));
	}
	
	@Override
	public void renderTileEntityAt(TileCustomCable te, double x, double y, double z, float partialTicks, ResourceLocation destroyStage, float alpha)
	{
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite spr = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(te.getResourceConnection());
		// if(mc.player.isSneaking())
		// spr =
		// Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(te.getResourceConnection()
		// + "1");
		renderWire(te, x, y, z, spr, getBrightnessForRB(te, RenderBlocks.forMod(Reference.MOD_ID)));
		if(destroyProgress > 0F)
			renderWire(te, x, y, z, DestroyStageTexture.getAsSprite(destroyProgress), getBrightnessForRB(te, RenderBlocks.forMod(Reference.MOD_ID)));
	}
	
	private static Map<EnumFacing, Boolean> conns = new HashMap<EnumFacing, Boolean>();
	
	public static void renderWire(Predicate<EnumFacing> te, double x, double y, double z, TextureAtlasSprite texture, int bright)
	{
		GL11.glPushMatrix();
		GL11.glDisable(2884);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glTranslated(x, y + 2.001D, z + 1D);
		GL11.glRotatef(180F, 1F, 0F, 0F);
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glEnable(GL11.GL_BLEND);
		
		RenderBlocks renderBlocks = RenderBlocks.forMod(Reference.MOD_ID);
		
		Tessellator t = Tessellator.getInstance();
		
		boolean renderCore = true;
		
		conns.clear();
		for(EnumFacing f : EnumFacing.VALUES)
			if(te.apply(f))
				conns.put(f, true);
		
		EnumFacing dir = null;
		
		if(conns.size() > 0)
		{
			dir = conns.keySet().toArray(new EnumFacing[0])[0];
			if(conns.size() == 2 && conns.get(dir.getOpposite()) == Boolean.TRUE)
				renderCore = false;
			else
				dir = null;
		}
		t.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		
		renderBlocks.setRenderBounds(0D, 0D, 0D, 4D / 16D, 1D, 4D / 16D);
		
		renderBlocks.renderFaceYPos(6D / 16D, 6F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
		renderBlocks.renderFaceYPos(6D / 16D, 10F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
		
		renderBlocks.setRenderBounds(0, 12D / 16D, 0, 1, 1, 4D / 16D);
		
		renderBlocks.renderFaceXPos(-6D / 16D, 10F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
		renderBlocks.renderFaceXPos(-10D / 16D, 10F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
		
		renderBlocks.setRenderBounds(0, 12D / 16D, 0, 4D / 16D, 1, 1);
		
		renderBlocks.renderFaceZPos(6D / 16D, 10F / 16F, -6D / 16D, texture, 1F, 1F, 1F, bright);
		renderBlocks.renderFaceZPos(6D / 16D, 10F / 16F, -10D / 16D, texture, 1F, 1F, 1F, bright);
		
		if(conns.get(EnumFacing.UP) == Boolean.TRUE)
		{
			renderBlocks.setRenderBounds(0, 6D / 16D, 0, 1, 12D / 16D, 4D / 16D);
			
			renderBlocks.renderFaceXPos(-6D / 16D, 10F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceXPos(-10D / 16D, 10F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			
			renderBlocks.setRenderBounds(0, 6D / 16D, 0, 4D / 16D, 12D / 16D, 1);
			
			renderBlocks.renderFaceZPos(6D / 16D, 10F / 16F, -6D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceZPos(6D / 16D, 10F / 16F, -10D / 16D, texture, 1F, 1F, 1F, bright);
		}
		
		if(conns.get(EnumFacing.DOWN) == Boolean.TRUE)
		{
			renderBlocks.setRenderBounds(0, 6D / 16D, 0, 1D, 12D / 16D, 4D / 16D);
			
			renderBlocks.renderFaceXPos(-6D / 16D, 20F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceXPos(-10D / 16D, 20F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			
			renderBlocks.setRenderBounds(0, 6D / 16D, 0, 4D / 16D, 12D / 16D, 1);
			
			renderBlocks.renderFaceZPos(6D / 16D, 20F / 16F, -6D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceZPos(6D / 16D, 20F / 16F, -10D / 16D, texture, 1F, 1F, 1F, bright);
		}
		
		if(conns.get(EnumFacing.EAST) == Boolean.TRUE)
		{
			renderBlocks.setRenderBounds(4D / 16D, 12D / 16D, 0, 10D / 16D, 1D, 4D / 16D);
			
			renderBlocks.renderFaceYPos(6D / 16D, 6F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceYPos(6D / 16D, 10F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			
			renderBlocks.setRenderBounds(4D / 16D, 12D / 16D, 0, 10D / 16D, 1D, 1);
			
			renderBlocks.renderFaceZPos(6D / 16D, 10F / 16F, -6D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceZPos(6D / 16D, 10F / 16F, -10D / 16D, texture, 1F, 1F, 1F, bright);
		}
		
		if(conns.get(EnumFacing.WEST) == Boolean.TRUE)
		{
			renderBlocks.setRenderBounds(4D / 16D, 12D / 16D, 0, 10D / 16D, 1D, 4D / 16D);
			
			renderBlocks.renderFaceYPos(-4D / 16D, 6F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceYPos(-4D / 16D, 10F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			
			renderBlocks.setRenderBounds(4D / 16D, 12D / 16D, 0, 10D / 16D, 1D, 1);
			
			renderBlocks.renderFaceZPos(-4D / 16D, 10F / 16F, -6D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceZPos(-4D / 16D, 10F / 16F, -10D / 16D, texture, 1F, 1F, 1F, bright);
		}
		
		if(conns.get(EnumFacing.SOUTH) == Boolean.TRUE)
		{
			renderBlocks.setRenderBounds(0, 0, 4D / 16D, 4D / 16D, 1, 10D / 16D);
			
			renderBlocks.renderFaceYPos(6D / 16D, 6F / 16F, -4D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceYPos(6D / 16D, 10F / 16F, -4D / 16D, texture, 1F, 1F, 1F, bright);
			
			renderBlocks.setRenderBounds(0, 12D / 16D, 4 / 16D, 1, 1, 10 / 16D);
			
			renderBlocks.renderFaceXPos(-6D / 16D, 10F / 16F, -4D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceXPos(-10D / 16D, 10F / 16F, -4D / 16D, texture, 1F, 1F, 1F, bright);
		}
		
		if(conns.get(EnumFacing.NORTH) == Boolean.TRUE)
		{
			renderBlocks.setRenderBounds(0, 0, 4D / 16D, 4D / 16D, 1, 10D / 16D);
			
			renderBlocks.renderFaceYPos(6D / 16D, 6F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceYPos(6D / 16D, 10F / 16F, 6D / 16D, texture, 1F, 1F, 1F, bright);
			
			renderBlocks.setRenderBounds(0, 12D / 16D, 4D / 16D, 1, 1, 10 / 16D);
			
			renderBlocks.renderFaceXPos(-6D / 16D, 10F / 16F, 6D / 16, texture, 1F, 1F, 1F, bright);
			renderBlocks.renderFaceXPos(-10D / 16D, 10F / 16F, 6D / 16, texture, 1F, 1F, 1F, bright);
		}
		
		t.draw();
		
		GL11.glEnable(2884);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}
	
	public static class TextureAtlasSpriteFull extends TextureAtlasSprite
	{
		public static final TextureAtlasSprite instance = new TextureAtlasSpriteFull();
		
		public TextureAtlasSpriteFull()
		{
			super("full");
		}
		
		@Override
		public float getMinU()
		{
			return 0;
		}
		
		@Override
		public float getMinV()
		{
			return 0;
		}
		
		@Override
		public float getMaxU()
		{
			return 1;
		}
		
		@Override
		public float getMaxV()
		{
			return 1;
		}
		
		/**
		 * Gets a V coordinate on the icon. 0 returns vMin and 16 returns vMax.
		 * Other arguments return in-between values.
		 */
		public float getInterpolatedV(double v)
		{
			return 1 * (float) v / 16F;
		}
		
		/**
		 * The opposite of getInterpolatedV. Takes the return value of that
		 * method and returns the input to it.
		 */
		public float getUnInterpolatedV(float p_188536_1_)
		{
			return p_188536_1_ * 16F;
		}
		
		/**
		 * Gets a U coordinate on the icon. 0 returns uMin and 16 returns uMax.
		 * Other arguments return in-between values.
		 */
		public float getInterpolatedU(double u)
		{
			return 1 * (float) u / 16F;
		}
		
		/**
		 * The opposite of getInterpolatedU. Takes the return value of that
		 * method and returns the input to it.
		 */
		public float getUnInterpolatedU(float u)
		{
			return u * 16F;
		}
	}
}