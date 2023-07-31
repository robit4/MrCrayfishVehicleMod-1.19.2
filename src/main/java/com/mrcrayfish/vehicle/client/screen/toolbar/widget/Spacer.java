package com.mrcrayfish.vehicle.client.screen.toolbar.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.network.chat.MutableComponent;

/**
 * Author: MrCrayfish
 */
public class Spacer extends Widget
{
    public Spacer(int widthIn)
    {
        super(0, 0, widthIn, 20, TextComponent.EMPTY);
    }

    public static Spacer of(int width)
    {
        return new Spacer(width);
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        AbstractGui.fill(matrixStack, this.x + this.width / 2, this.y, this.x + this.width / 2 + 1, this.y + this.height, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return false;
    }
}
