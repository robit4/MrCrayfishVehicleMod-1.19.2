package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.block.FluidMixerBlock;
import com.mrcrayfish.vehicle.crafting.FluidExtractorRecipe;
import com.mrcrayfish.vehicle.crafting.ModRecipeTypes;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.inventory.container.FluidExtractorContainer;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.core.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.INameable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorTileEntity extends TileFluidHandlerSynced implements Container, BaseEntityBlock, MenuProvider, INameable
{
    private NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);

    private static final int SLOT_FUEL_SOURCE = 0;
    public static final int SLOT_FLUID_SOURCE = 1;

    private FluidExtractorRecipe currentRecipe = null;
    private int remainingFuel;
    private int fuelMaxProgress;
    private int extractionProgress;
    private int capacity;
    private boolean extracting;

    private String customName;

    protected final IIntArray fluidExtractorData = new IIntArray()
    {
        public int get(int index)
        {
            switch(index)
            {
                case 0:
                    return extractionProgress;
                case 1:
                    return remainingFuel;
                case 2:
                    return fuelMaxProgress;
                case 3:
                    return tank.getFluid().getFluid().builtInRegistryHolder().key().location().hashCode();
                case 4:
                    return tank.getFluidAmount();
            }
            return 0;
        }

        public void set(int index, int value)
        {
            switch(index)
            {
                case 0:
                    extractionProgress = value;
                    break;
                case 1:
                    remainingFuel = value;
                    break;
                case 2:
                    fuelMaxProgress = value;
                    break;
                case 3:
                    updateFluid(tank, value);
                    break;
                case 4:
                    if(!tank.isEmpty() || tank.getFluid().getRawFluid() != Fluids.EMPTY)
                    {
                        tank.getFluid().setAmount(value);
                    }
                    break;
            }

        }

        public int getCount()
        {
            return 5;
        }
    };

    public FluidExtractorTileEntity()
    {
        super(ModTileEntities.FLUID_EXTRACTOR.get(), Config.SERVER.extractorCapacity.get(), stack -> true);
        this.capacity = Config.SERVER.extractorCapacity.get();
    }

    @Override
    public void tick()
    {
        if(this.level != null && !this.level.isClientSide())
        {
            ItemStack source = this.getItem(SLOT_FLUID_SOURCE);
            ItemStack fuel = this.getItem(SLOT_FUEL_SOURCE);

            if(this.currentRecipe == null && !source.isEmpty())
            {
                this.currentRecipe = this.getRecipe().orElse(null);
            }
            else if(source.isEmpty())
            {
                this.currentRecipe = null;
                this.extractionProgress = 0;
            }

            this.updateFuel(source, fuel);

            if(this.remainingFuel > 0 && this.canFillWithFluid(source))
            {
                this.setExtracting(true);

                if(this.extractionProgress++ == Config.SERVER.extractorExtractTime.get())
                {
                    this.tank.fill(this.currentRecipe.getResult().createStack(), IFluidHandler.FluidAction.EXECUTE);
                    this.extractionProgress = 0;
                    this.shrinkItem(SLOT_FLUID_SOURCE);
                    this.currentRecipe = null;
                }
            }
            else
            {
                this.extractionProgress = 0;
                this.setExtracting(false);
            }

            if(this.remainingFuel > 0)
            {
                this.remainingFuel--;
                this.updateFuel(source, fuel);

                // Updates the enabled state of the fluid extractor
                if(this.remainingFuel == 0)
                {
                    this.setExtracting(false);
                }
            }
        }
    }

    private void updateFuel(ItemStack source, ItemStack fuel)
    {
        if(!fuel.isEmpty() && this.remainingFuel == 0 && this.canFillWithFluid(source))
        {
            this.fuelMaxProgress = ForgeHooks.getBurnTime(fuel);
            this.remainingFuel = this.fuelMaxProgress;
            this.shrinkItem(SLOT_FUEL_SOURCE);
        }
    }

    private boolean canFillWithFluid(ItemStack stack)
    {
        return this.currentRecipe != null && this.currentRecipe.getIngredient().getItem() == stack.getItem() && this.tank.getFluidAmount() < this.tank.getCapacity() && (this.tank.isEmpty() || this.tank.getFluid().getFluid() == this.currentRecipe.getResult().getFluid()) && (this.tank.getFluidAmount() + this.currentRecipe.getResult().getAmount()) <= this.tank.getCapacity();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canExtract()
    {
        ItemStack ingredient = this.getItem(SLOT_FLUID_SOURCE);
        if(!ingredient.isEmpty())
        {
            if(this.currentRecipe == null)
            {
                this.currentRecipe = this.getRecipe().orElse(null);
            }
        }
        else
        {
            this.currentRecipe = null;
        }
        return this.canFillWithFluid(ingredient) && this.remainingFuel >= 0;
    }

    @OnlyIn(Dist.CLIENT)
    public FluidExtractorRecipe getCurrentRecipe()
    {
        return currentRecipe;
    }

    public FluidStack getFluidStackTank()
    {
        return this.tank.getFluid();
    }

    public int getCapacity()
    {
        return capacity;
    }

    @Override
    public int getContainerSize()
    {
        return 2;
    }

    @Override
    public boolean isEmpty()
    {
        for(ItemStack stack : this.inventory)
        {
            if(!stack.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index)
    {
        return this.inventory.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count)
    {
        ItemStack stack = ItemStackHelper.removeItem(this.inventory, index, count);
        if(!stack.isEmpty())
        {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index)
    {
        return ItemStackHelper.takeItem(this.inventory, index);
    }

    @Override
    public void setItem(int index, ItemStack stack)
    {
        this.inventory.set(index, stack);
        if(stack.getCount() > this.getMaxStackSize())
        {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player)
    {
        return this.level.getBlockEntity(this.worldPosition) == this && player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack)
    {
        if(index == 0)
        {
            return ForgeHooks.getBurnTime(stack) > 0;
        }
        else if(index == 1)
        {
            return this.isValidIngredient(stack);
        }
        return false;
    }

    @Override
    public void clearContent()
    {
        this.inventory.clear();
    }

    public int getExtractionProgress()
    {
        return this.fluidExtractorData.get(0);
    }

    public int getRemainingFuel()
    {
        return this.fluidExtractorData.get(1);
    }

    public int getFuelMaxProgress()
    {
        return this.fluidExtractorData.get(2);
    }

    public int getFluidLevel()
    {
        return this.fluidExtractorData.get(4);
    }

    @Override
    public void load(BlockState state, CompoundTag compound)
    {
        super.load(state, compound);
        if(compound.contains("ExtractionProgress", Tag.TAG_INT))
        {
            this.extractionProgress = compound.getInt("ExtractionProgress");
        }
        if(compound.contains("RemainingFuel", Tag.TAG_INT))
        {
            this.remainingFuel = compound.getInt("RemainingFuel");
        }
        if(compound.contains("FuelMaxProgress", Tag.TAG_INT))
        {
            this.fuelMaxProgress = compound.getInt("FuelMaxProgress");
        }
        if(compound.contains("Items", Tag.TAG_LIST))
        {
            this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(compound, this.inventory);
        }
        if(compound.contains("CustomName", Tag.TAG_STRING))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt("ExtractionProgress", this.extractionProgress);
        compound.putInt("RemainingFuel", this.remainingFuel);
        compound.putInt("FuelMaxProgress", this.fuelMaxProgress);

        ItemStackHelper.saveAllItems(compound, this.inventory);

        if(this.hasCustomName())
        {
            compound.putString("CustomName", this.customName);
        }

        return compound;
    }

    @Override
    public Component getName()
    {
        return this.getDisplayName();
    }


    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Override
    public Component getDisplayName()
    {
        return this.hasCustomName() ? MutableComponent.create(new LiteralContents(this.customName) : Component.translatable("container.fluid_extractor");
    }

    private void shrinkItem(int index)
    {
        ItemStack stack = this.getItem(index);
        stack.shrink(1);
        if(stack.isEmpty())
        {
            this.setItem(index, ItemStack.EMPTY);
        }
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, Inventory playerInventory, Player playerEntity)
    {
        return new FluidExtractorContainer(windowId, playerInventory, this);
    }

    public IIntArray getFluidExtractorData()
    {
        return fluidExtractorData;
    }

    public void updateFluid(FluidTank tank, int fluidHash)
    {
        Optional<Fluid> optional = ForgeRegistries.FLUIDS.getValues().stream().filter(fluid -> fluid.builtInRegistryHolder().key().location().hashCode() == fluidHash).findFirst();
        optional.ifPresent(fluid -> tank.setFluid(new FluidStack(fluid, tank.getFluidAmount())));
    }

    public Optional<FluidExtractorRecipe> getRecipe()
    {
        return this.level.getRecipeManager().getRecipeFor(ModRecipeTypes.FLUID_EXTRACTOR, this, this.level);
    }

    public boolean isValidIngredient(ItemStack ingredient)
    {
        List<FluidExtractorRecipe> recipes = this.level.getRecipeManager().getRecipes().stream().filter(recipe -> recipe.getType() == ModRecipeTypes.FLUID_EXTRACTOR).map(recipe -> (FluidExtractorRecipe) recipe).collect(Collectors.toList());
        return recipes.stream().anyMatch(recipe -> InventoryUtil.areItemStacksEqualIgnoreCount(ingredient, recipe.getIngredient()));
    }

    private final net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional.of(this::createUnSidedHandler);

    @Nonnull
    protected net.minecraftforge.items.IItemHandler createUnSidedHandler()
    {
        return new net.minecraftforge.items.wrapper.InvWrapper(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (!this.remove && cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
            return this.itemHandler.cast();
        return super.getCapability(cap, side);
    }

    private void setExtracting(boolean state)
    {
        if(this.extracting != state)
        {
            this.extracting = state;
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FluidMixerBlock.ENABLED, state), Constants.BlockFlags.DEFAULT);
        }
    }
}