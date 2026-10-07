package de.guntram.mcmod.advancementinfo.mixin;

import de.guntram.mcmod.advancementinfo.AdvancementInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AdvancementTabType.class)
public class AdvancementTabTypeMixin {

    @ModifyConstant(method = "getX", constant = @Constant(intValue = 248))
    private int advancementinfo$rightTabX(int original) {
        var screen = Minecraft.getInstance().gui.screen();
        if (screen == null) {
            return original;
        }
        return screen.width - AdvancementInfo.config.marginX * 2 - 4;
    }

    @ModifyConstant(method = "getY", constant = @Constant(intValue = 136))
    private int advancementinfo$belowTabY(int original) {
        var screen = Minecraft.getInstance().gui.screen();
        if (screen == null) {
            return original;
        }
        return screen.height - AdvancementInfo.config.marginY * 2 - 4;
    }
}
