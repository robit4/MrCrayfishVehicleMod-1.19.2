package com.mrcrayfish.vehicle.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.vehicle.inventory.container.StorageContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class StorageScreen extends ContainerScreen<StorageContainer>
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final PlayerInventory playerInventory;
    private final int inventoryRows;

    public StorageScreen(StorageContainer container, PlayerInventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.passEvents = false;
        this.inventoryRows = container.getStorageInventory().getContainerSize() / 9;
        this.imageHeight = 114 + this.inventoryRows * 18;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
    {
        this.minecraft.font.draw(matrixStack, this.getTitle().getString(), 8, 6, 4210752);
        this.minecraft.font.draw(matrixStack, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 96 + 2, 4210752);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(CHEST_GUI_TEXTURE);
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, startX, startY, 0, 0, this.imageWidth, this.inventoryRows * 18 + 17);
        this.blit(matrixStack, startX, startY + this.inventoryRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}
