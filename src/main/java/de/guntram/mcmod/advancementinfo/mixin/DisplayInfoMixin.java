package de.guntram.mcmod.advancementinfo.mixin;

import de.guntram.mcmod.advancementinfo.AdvancementInfo;
import net.minecraft.advancements.DisplayInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DisplayInfo.class)
public class DisplayInfoMixin {

    @Shadow
    @Final
    private boolean hidden;

    @Inject(method = "hidden", at = @At("HEAD"), cancellable = true)
    private void advancementinfo$isHidden(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(hidden && !AdvancementInfo.showAll);
        cir.cancel();
    }
}
