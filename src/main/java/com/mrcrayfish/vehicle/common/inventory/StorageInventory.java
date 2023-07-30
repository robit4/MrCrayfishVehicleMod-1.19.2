package com.mrcrayfish.vehicle.common.inventory;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class StorageInventory extends SimpleContainer
{
    private final WeakReference<Entity> entityRef;
    private final Component displayName;
    private final Predicate<ItemStack> itemPredicate;

    public StorageInventory(Entity entity, Component displayName, int rows)
    {
        super(rows * 9);
        this.entityRef = new WeakReference<>(entity);
        this.displayName = displayName;
        this.itemPredicate = stack -> true;
    }

    public StorageInventory(Entity entity, Component displayName, int rows, Predicate<ItemStack> itemPredicate)
    {
        super(rows * 9);
        this.entityRef = new WeakReference<>(entity);
        this.displayName = displayName;
        this.itemPredicate = itemPredicate;
    }

    @Nullable
    public Entity getEntity()
    {
        return this.entityRef.get();
    }

    public Component getDisplayName()
    {
        return this.displayName;
    }

    public boolean isStorageItem(ItemStack stack)
    {
        return this.itemPredicate.test(stack);
    }

    public ListNBT createTag()
    {
        ListNBT tagList = new ListNBT();
        for(int i = 0; i < this.getContainerSize(); i++)
        {
            ItemStack stack = this.getItem(i);
            if(!stack.isEmpty())
            {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte("Slot", (byte) i);
                stack.save(slotTag);
                tagList.add(slotTag);
            }
        }
        return tagList;
    }

    @Override
    public void fromTag(ListNBT tagList)
    {
        this.clearContent();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag slotTag = tagList.getCompound(i);
            byte slot = slotTag.getByte("Slot");
            if(slot >= 0 && slot < this.getContainerSize())
            {
                this.setItem(slot, ItemStack.of(slotTag));
            }
        }
    }

    @Override
    public boolean stillValid(Player player)
    {
        Entity entity = this.entityRef.get();
        return entity != null && entity.isAlive();
    }
}
