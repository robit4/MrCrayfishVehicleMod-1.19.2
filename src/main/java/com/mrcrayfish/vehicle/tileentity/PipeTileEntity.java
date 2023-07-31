package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class PipeTileEntity extends TileEntitySynced
{
    protected Set<BlockPos> pumps = new HashSet<>();
    protected boolean[] disabledConnections = new boolean[Direction.values().length];

    public PipeTileEntity()
    {
        super(ModTileEntities.FLUID_PIPE.get());
    }

    public PipeTileEntity(TileEntityType<?> tileEntityType)
    {
        super(tileEntityType);
    }

    public void addPump(BlockPos pos)
    {
        this.pumps.add(pos);
    }

    public void removePump(BlockPos pos)
    {
        this.pumps.remove(pos);
    }

    public Set<BlockPos> getPumps()
    {
        return this.pumps;
    }

    public boolean[] getDisabledConnections()
    {
        return this.disabledConnections;
    }

    public void setConnectionState(Direction direction, boolean state)
    {
        this.disabledConnections[direction.get3DDataValue()] = state;
        this.syncDisabledConnections();
    }

    public boolean isConnectionDisabled(Direction direction)
    {
        return this.disabledConnections[direction.get3DDataValue()];
    }

    public void syncDisabledConnections()
    {
        if(this.level != null && !this.level.isClientSide())
        {
            CompoundTag compound = new CompoundTag();
            this.writeConnections(compound);
            TileEntityUtil.sendUpdatePacket(this, super.save(compound));
        }
    }

    @Override
    public void load(BlockState state, CompoundTag compound)
    {
        super.load(state, compound);
        if(compound.contains("DisabledConnections", Tag.TAG_BYTE_ARRAY))
        {
            byte[] connections = compound.getByteArray("DisabledConnections");
            for(int i = 0; i < connections.length; i++)
            {
                this.disabledConnections[i] = connections[i] == (byte) 1;
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound)
    {
        this.writeConnections(compound);
        return super.save(compound);
    }

    private void writeConnections(CompoundTag compound)
    {
        byte[] connections = new byte[this.disabledConnections.length];
        for(int i = 0; i < connections.length; i++)
        {
            connections[i] = (byte) (this.disabledConnections[i] ? 1 : 0);
        }
        compound.putByteArray("DisabledConnections", connections);
    }
}
