package com.mrcrayfish.vehicle.fluid;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModFluids;
import com.mrcrayfish.vehicle.init.ModItems;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

/**
 * Author: MrCrayfish
 */
public abstract class BlazeJuice extends ForgeFlowingFluid
{
    public BlazeJuice()
    {
        super(new Properties(() -> ModFluids.BLAZE_JUICE.get(), () -> ModFluids.FLOWING_BLAZE_JUICE.get(), FluidAttributes.builder(new ResourceLocation(Reference.MOD_ID, "block/blaze_juice_still"), new ResourceLocation(Reference.MOD_ID, "block/blaze_juice_flowing")).viscosity(800).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)).block(() -> ModBlocks.BLAZE_JUICE.get()));
    }

    @Override
    public Item getBucket()
    {
        return ModItems.BLAZE_JUICE_BUCKET.get();
    }

    public static class Source extends BlazeJuice
    {
        @Override
        public boolean isSource(FluidState state)
        {
            return true;
        }

        @Override
        public int getAmount(FluidState state)
        {
            return 8;
        }
    }

    public static class Flowing extends BlazeJuice
    {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
        {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state)
        {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state)
        {
            return false;
        }
    }
}
