package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.groups.PlayManager;
import net.aeronica.mods.mxtune.gui.GuiBandAmp;
import net.aeronica.mods.mxtune.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

import static net.aeronica.mods.mxtune.blocks.BlockPiano.spawnEntityItem;

@SuppressWarnings("deprecation")
public class BlockBandAmp extends BlockHorizontal implements IMusicPlayer
{
    public BlockBandAmp()
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(0.0F);
        this.setSoundType(SoundType.WOOD);
        this.disableStats();
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) { return ModItems.ITEM_BAND_AMP; }

    @Deprecated
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) { return new ItemStack(ModItems.ITEM_BAND_AMP); }

    /**
     * Called when the block is right clicked by a player.
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote && playerIn.capabilities.allowEdit)
        {


            if (!playerIn.isSneaking())
            {
                canPlayOrStopMusic(worldIn, pos);
            }
            else if (playerIn.isSneaking())
            {
                playerIn.openGui(MXTuneMain.instance, GuiBandAmp.GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    private boolean canPlayOrStopMusic(World worldIn, BlockPos pos)
    {
        TileBandAmp tileBandAmp = this.getTE(worldIn, pos);
        if (tileBandAmp != null)
        {
            if (PlayManager.isActivePlayID(tileBandAmp.getPlayID()))
            {
                PlayManager.stopPlayID(tileBandAmp.getPlayID());
                tileBandAmp.setPlayID(-1);
                return false;
            }
            tileBandAmp.setPlayID(PlayManager.playMusic(worldIn, pos));
        }
        return true;
    }

    /**
     * React to a redstone powered neighbor block
     */
    @Deprecated
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        boolean powered = worldIn.isBlockPowered(pos);
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) te;
            if (tileBandAmp.getPreviousRedStoneState() != powered)
            {
                if (powered)
                {
                    canPlayOrStopMusic(worldIn, pos);
                    tileBandAmp.setPowered(state, worldIn, pos, blockIn, fromPos);
                }
                tileBandAmp.setPreviousRedStoneState(powered);
            }
        }
    }

    @Deprecated
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    @Deprecated
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {FACING});
    }

    @Deprecated
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) { return state.getValue(FACING).getHorizontalIndex(); }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileBandAmp(state.getValue(FACING));
    }

    @Nullable
    public TileBandAmp getTileEntity(IBlockAccess world, BlockPos pos) {
        return (TileBandAmp) world.getTileEntity(pos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileBandAmp tile = (TileBandAmp) worldIn.getTileEntity(pos);
        if (tile != null)
        {
            for (int slot = 0; slot < tile.getInventory().getSlots(); slot++)
            {
                spawnEntityItem(worldIn, tile.getInventory().getStackInSlot(slot), pos);
            }
            tile.invalidate();
        }
        super.breakBlock(worldIn, pos, state);
    }
//    @Deprecated
//    @Override
//    public boolean isFullCube(IBlockState state) { return false; }
//
//    @Deprecated
//    @Override
//    public boolean isOpaqueCube(IBlockState state) { return false; }
}
