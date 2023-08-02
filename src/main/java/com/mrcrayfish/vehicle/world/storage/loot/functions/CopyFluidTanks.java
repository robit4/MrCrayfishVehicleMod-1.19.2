package com.mrcrayfish.vehicle.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.init.ModLootFunctions;
import com.mrcrayfish.vehicle.tileentity.IFluidTankWriter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.FluidHandlerBlockEntity;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
public class CopyFluidTanks extends LootFunction
{
    private CopyFluidTanks(ILootCondition[] conditionsIn)
    {
        super(conditionsIn);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context)
    {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if(state != null && stack.getItem() == state.getBlock().asItem())
        {
            BlockEntity tileEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            if(tileEntity != null)
            {
                CompoundTag tileEntityTag = new CompoundTag();
                if(tileEntity instanceof FluidHandlerBlockEntity)
                {
                    LazyOptional<IFluidHandler> handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    handler.ifPresent(h ->
                    {
                        FluidTank tank = (FluidTank) h;
                        if(!tank.isEmpty())
                        {
                            tank.writeToNBT(tileEntityTag);
                        }
                    });
                }
                else if(tileEntity instanceof IFluidTankWriter)
                {
                    IFluidTankWriter writer = (IFluidTankWriter) tileEntity;
                    if(!writer.areTanksEmpty())
                    {
                        writer.writeTanks(tileEntityTag);
                    }
                }

                if(!tileEntityTag.isEmpty())
                {
                    CompoundTag compound = stack.getTag();
                    if(compound == null)
                    {
                        compound = new CompoundTag();
                    }
                    compound.put("BlockEntityTag", tileEntityTag);
                    stack.setTag(compound);
                }
            }
        }
        return stack;
    }

    @Override
    public LootFunctionType getType()
    {
        return ModLootFunctions.COPY_FLUID_TANKS;
    }

    public static CopyFluidTanks.Builder copyFluidTanks()
    {
        return new CopyFluidTanks.Builder();
    }

    public static class Builder extends LootFunction.Builder<CopyFluidTanks.Builder>
    {
        private Builder() {}

        protected CopyFluidTanks.Builder getThis()
        {
            return this;
        }

        public ILootFunction build()
        {
            return new CopyFluidTanks(this.getConditions());
        }
    }

    public static class Serializer extends LootFunction.Serializer<CopyFluidTanks>
    {
        @Override
        public CopyFluidTanks deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn)
        {
            return new CopyFluidTanks(conditionsIn);
        }
    }
}
