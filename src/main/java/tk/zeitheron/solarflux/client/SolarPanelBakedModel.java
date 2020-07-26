package tk.zeitheron.solarflux.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import tk.zeitheron.solarflux.InfoSF;
import tk.zeitheron.solarflux.block.SolarPanelBlock;
import tk.zeitheron.solarflux.block.SolarPanelTile;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class SolarPanelBakedModel
		implements IDynamicBakedModel
{
	public static final FaceBakery COOKER = new FaceBakery();
	public final SolarPanelBlock block;

	final ResourceLocation modelName = new ModelResourceLocation(InfoSF.MOD_ID, "solar_panel");

	public SolarPanelBakedModel(SolarPanelBlock spb)
	{
		this.block = spb;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction sideIn, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		List<BakedQuad> quads = new ArrayList<>();
		Direction[] sides = sideIn == null ? Direction.values() : new Direction[]{ sideIn };
		for(Direction side : sides)
			if(side != null)
			{
				World world = extraData.getData(SolarPanelTile.WORLD_PROP);
				BlockPos pos = extraData.getData(SolarPanelTile.POS_PROP);

				TextureAtlasSprite top = t_top(), base = t_base();

				float h = block.panel.getPanelData().height * 16F;

				quads.add(COOKER.bakeQuad( //
						new Vector3f(0, 0, 0), new Vector3f(16, h, 16), //
						new BlockPartFace(null, 0, "#0", new BlockFaceUV(new float[]{
								0,
								side.getAxis() == Axis.Y ? 0 : (16F - h),
								16,
								16
						}, 4)), //
						side == Direction.UP ? top : base, side, ZERO_SPRITE, null, true, modelName));

				// world/pos not set? no connected textures == no crash!
				if(world == null || pos == null)
					return quads;

				boolean west, east, north, south;

				if(west = world.getBlockState(pos.west()).getBlock() != block)
					quads.add(COOKER.bakeQuad( //
							new Vector3f(0, h, 1), new Vector3f(1, h + 0.25F, 15), //
							new BlockPartFace(null, 0, "#0", new BlockFaceUV(side != Direction.UP ? new float[]{
									0,
									0,
									16,
									1
							} : new float[]{
									0,
									0,
									1,
									16
							}, 4)), //
							base, side, ZERO_SPRITE, null, true, modelName));

				if(east = world.getBlockState(pos.east()).getBlock() != block)
					quads.add(COOKER.bakeQuad( //
							new Vector3f(15, h, 1), new Vector3f(16, h + 0.25F, 15), //
							new BlockPartFace(null, 0, "#0", new BlockFaceUV(side != Direction.UP ? new float[]{
									0,
									0,
									16,
									1
							} : new float[]{
									15,
									0,
									16,
									16
							}, 4)), //
							base, side, ZERO_SPRITE, null, true, modelName));

				if(north = world.getBlockState(pos.north()).getBlock() != block)
					quads.add(COOKER.bakeQuad( //
							new Vector3f(1, h, 0), new Vector3f(15, h + 0.25F, 1), //
							new BlockPartFace(null, 0, "#0", new BlockFaceUV(new float[]{
									0,
									0,
									16,
									1
							}, 4)), //
							base, side, ZERO_SPRITE, null, true, modelName));

				if(south = world.getBlockState(pos.south()).getBlock() != block)
					quads.add(COOKER.bakeQuad( //
							new Vector3f(1, h, 15), new Vector3f(15, h + 0.25F, 16), //
							new BlockPartFace(null, 0, "#0", new BlockFaceUV(new float[]{
									0,
									0,
									16,
									1
							}, 4)), //
							base, side, ZERO_SPRITE, null, true, modelName));

				if(west || north || world.getBlockState(pos.west().north()).getBlock() != block)
					quads.add(COOKER.bakeQuad( //
							new Vector3f(0, h, 0), new Vector3f(1, h + 0.25F, 1), //
							new BlockPartFace(null, 0, "#0", new BlockFaceUV(new float[]{
									0,
									0,
									1,
									1
							}, 4)), //
							base, side, ZERO_SPRITE, null, true, modelName));

				if(east || north || world.getBlockState(pos.east().north()).getBlock() != block)
					quads.add(COOKER.bakeQuad( //
							new Vector3f(15, h, 0), new Vector3f(16, h + 0.25F, 1), //
							new BlockPartFace(null, 0, "#0", new BlockFaceUV(new float[]{
									15,
									0,
									16,
									1
							}, 4)), //
							base, side, ZERO_SPRITE, null, true, modelName));

				if(south || east || world.getBlockState(pos.south().east()).getBlock() != block)
					quads.add(COOKER.bakeQuad( //
							new Vector3f(15, h, 15), new Vector3f(16, h + 0.25F, 16), //
							new BlockPartFace(null, 0, "#0", new BlockFaceUV(new float[]{
									15,
									15,
									16,
									16
							}, 4)), //
							base, side, ZERO_SPRITE, null, true, modelName));

				if(west || south || world.getBlockState(pos.west().south()).getBlock() != block)
					quads.add(COOKER.bakeQuad( //
							new Vector3f(0, h, 15), new Vector3f(1, h + 0.25F, 16), //
							new BlockPartFace(null, 0, "#0", new BlockFaceUV(new float[]{
									0,
									15,
									1,
									16
							}, 4)), //
							base, side, ZERO_SPRITE, null, true, modelName));
			}
		return quads;
	}

	public static final IModelTransform ZERO_SPRITE = new IModelTransform()
	{
		@Override
		public boolean isUvLock()
		{
			return false;
		}
	};

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return new ItemCameraTransforms(getTransform(TransformType.THIRD_PERSON_LEFT_HAND), getTransform(TransformType.THIRD_PERSON_RIGHT_HAND), getTransform(TransformType.FIRST_PERSON_LEFT_HAND), getTransform(TransformType.FIRST_PERSON_RIGHT_HAND), getTransform(TransformType.HEAD), getTransform(TransformType.GUI), getTransform(TransformType.GROUND), getTransform(TransformType.FIXED));
	}

	@SuppressWarnings("deprecation")
	public ItemTransformVec3f getTransform(TransformType type)
	{
		if (type == TransformType.GUI) {
			return new ItemTransformVec3f(new Vector3f(30, 45, 0), new Vector3f(0, 0, 0), new Vector3f(0.625F, 0.625F, 0.625F));
		}
		return ItemTransformVec3f.DEFAULT;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return ThreadLocalRandom.current().nextInt(5) > 0 ? t_base() : t_top();
	}

	final Function<ResourceLocation, TextureAtlasSprite> spriteGetter = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

	ResourceLocation baseTx, topTx;

	public TextureAtlasSprite t_base()
	{
		if(baseTx == null)
			baseTx = new ResourceLocation(Objects.requireNonNull(block.getRegistryName()).getNamespace(), "blocks/" + block.getRegistryName().getPath() + "_base");
		return spriteGetter.apply(baseTx);
	}

	public TextureAtlasSprite t_top()
	{
		if(topTx == null)
			topTx = new ResourceLocation(Objects.requireNonNull(block.getRegistryName()).getNamespace(), "blocks/" + block.getRegistryName().getPath() + "_top");
		return spriteGetter.apply(topTx);
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return false;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean func_230044_c_()
	{
		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.EMPTY;
	}
}