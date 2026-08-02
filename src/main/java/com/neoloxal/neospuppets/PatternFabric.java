package com.neoloxal.neospuppets;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

public class PatternFabric extends Item {
    public PatternFabric(Properties properties) {
        super(properties);
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        stack.set(NeosPuppets.SKIN_COMPONENT, new NeosPuppets.skinRecord("0699057e-febf-47a0-9b16-552a5b64dd92", "Neoloxal"));
        super.verifyComponentsAfterLoad(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.neospuppets.pattern_fabric", stack.get(NeosPuppets.SKIN_COMPONENT).skinName()));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
