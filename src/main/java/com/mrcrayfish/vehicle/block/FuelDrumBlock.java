package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.tileentity.FuelDrumTileEntity;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class FuelDrumBlock extends Block
{
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    private static final VoxelShape[] SHAPE = {
            Block.box(0, 1, 1, 16, 15, 15),
            Block.box(1, 0, 1, 15, 16, 15),
            Block.box(1, 1, 0, 15, 15, 16)
    };

    public FuelDrumBlock()
    {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(1.0F));
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(INVERTED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) //CollisionContext context is normally the next param, but idk wtf this class is actually supposed to override in 1.17.1
    {
        return SHAPE[state.getValue(AXIS).ordinal()];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) //CollisionContext context is normally the next param, but idk wtf this class is actually supposed to override in 1.17.1
    {
        return SHAPE[state.getValue(AXIS).ordinal()];
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter reader, List<Component> list, TooltipFlag advanced)
    {
        if(Screen.hasShiftDown())
        {
            list.addAll(RenderUtil.lines(new TranslatableContents(ModBlocks.FUEL_DRUM.get().getDescriptionId() + ".info"), 150));
        }
        else
        {
            CompoundTag tag = stack.getTag();
            if(tag != null && tag.contains("BlockEntityTag", Tag.TAG_COMPOUND))
            {
                CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
                if(blockEntityTag.contains("FluidName", Tag.TAG_STRING))
                {
                    String fluidName = blockEntityTag.getString("FluidName");
                    Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
                    int amount = blockEntityTag.getInt("Amount");
                    if(fluid != null && amount > 0)
                    {
                        list.add(new TranslatableContents(fluid.getAttributes().getTranslationKey())).withStyle(ChatFormatting.BLUE));
                        list.add(MutableComponent.create(new LiteralContents(amount + " / " + this.getCapacity() + "mb")).withStyle(ChatFormatting.GRAY));
                    }
                }
            }
            list.add(new TranslatableContents("vehicle.info_help")).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player playerEntity, InteractionHand hand, BlockHitResult result)
    {
        if(!world.isClientSide())
        {
            if(FluidUtil.interactWithFluidHandler(playerEntity, hand, world, pos, result.getDirection()))
            {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation)
    {
        switch(rotation)
        {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch(state.getValue(AXIS))
                {
                    case X:
                        return state.setValue(AXIS, Direction.Axis.Z);
                    case Z:
                        return state.setValue(AXIS, Direction.Axis.X);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(AXIS);
        builder.add(INVERTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        boolean inverted = context.getClickedFace().getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis()).setValue(INVERTED, inverted);
    }

    @Override
    public boolean hasBlockEntity()
    {
        return true;
    }

    public int getCapacity()
    {
        return Config.SERVER.fuelDrumCapacity.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FuelDrumTileEntity();
    }
}
