package de.guntram.mcmod.advancementinfo.mixin;

import de.guntram.mcmod.advancementinfo.AdvancementInfo;
import de.guntram.mcmod.advancementinfo.accessors.AdvancementWidgetAccessor;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementWidget.class)
public class AdvancementWidgetMixin implements AdvancementWidgetAccessor {

    @Shadow
    @Final
    private AdvancementTab tab;

    @Shadow
    private AdvancementProgress progress;

    @ModifyConstant(method = "extractHover", constant = @Constant(intValue = 113))
    private int advancementinfo$hoverInnerH(int original) {
        return tab.getScreen().height - AdvancementInfo.config.marginY * 2 - 27;
    }

    @Inject(method = "extractHover", at = @At("HEAD"))
    private void advancementinfo$rememberHover(
            GuiGraphicsExtractor graphics,
            int xo,
            int yo,
            float fade,
            int screenxo,
            int screenyo,
            CallbackInfo ci
    ) {
        AdvancementInfo.mouseOver = (AdvancementWidget) (Object) this;
    }

    @Override
    public AdvancementProgress getProgress() {
        return this.progress;
    }
}
