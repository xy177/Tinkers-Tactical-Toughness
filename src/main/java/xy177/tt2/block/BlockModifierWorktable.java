package xy177.tt2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.TinkerRegistry;
import xy177.tt2.TT2;
import xy177.tt2.init.TT2Blocks;
import xy177.tt2.tile.TileModifierWorktable;

import javax.annotation.Nullable;

public class BlockModifierWorktable extends Block implements ITileEntityProvider {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockModifierWorktable() {
        super(Material.ROCK);
        setTranslationKey("tt2.modifier_worktable");
        setHardness(3.0F);
        setResistance(8.0F);
        setCreativeTab(TinkerRegistry.tabGeneral);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(TT2.instance, TT2Blocks.GUI_MODIFIER_WORKTABLE, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileModifierWorktable) {
            TileModifierWorktable inv = (TileModifierWorktable) tile;
            for (int i = 0; i < TileModifierWorktable.SLOT_OUTPUT; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    world.spawnEntity(new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D,
                        pos.getZ() + 0.5D, stack.copy()));
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta);
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileModifierWorktable();
    }
}
