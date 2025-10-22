package de.guntram.mcmod.advancementinfo.mixin;

import de.guntram.mcmod.advancementinfo.AdvancementInfo;
import de.guntram.mcmod.advancementinfo.accessors.AdvancementWidgetAccessor;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementWidget.class)
public class AdvancementWidgetMixin implements AdvancementWidgetAccessor {

    @Shadow private AdvancementProgress progress;

    @Inject(method="drawTooltip", at=@At("HEAD"))
    public void rememberTooltip(DrawContext context, int i, int j, float f, int y, int k, CallbackInfo ci) {
        AdvancementInfo.mouseOver = (AdvancementWidget)(Object)this;
    }

    @Override
    public AdvancementProgress getProgress() {
        return this.progress;
    }
}
