package de.guntram.mcmod.advancementinfo.mixin;

import de.guntram.mcmod.advancementinfo.AdvancementInfo;
import static de.guntram.mcmod.advancementinfo.AdvancementInfo.config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementTab.class)
public class AdvancementTabMixin {

    @Shadow @Final
    private AdvancementsScreen screen;

    @Shadow
    private double originX;

    @Shadow
    private double originY;

    @Shadow
    private boolean initialized;

    @Shadow
    private int minPanX;

    @Shadow
    private int minPanY;

    @Shadow
    private int maxPanX;

    @Shadow
    private int maxPanY;

    private int currentInfoWidth;
    private int lastScreenWidth = 0;
    private int lastScreenHeight = 0;

    @Inject(method="render", at = @At("HEAD"))
    private void updateLayout(DrawContext context, int x, int y, CallbackInfo ci) {
        if(screen != null) {
            currentInfoWidth = config.infoWidth.calculate(screen.width);

            if (lastScreenWidth != screen.width || lastScreenHeight != screen.height) {
                lastScreenWidth = screen.width;
                lastScreenHeight = screen.height;

                int centerX = (screen.width - config.marginX*2 - currentInfoWidth - 18) / 2;
                int centerY = (screen.height - config.marginY*2 - 27) / 2;

                this.originX = centerX - (this.maxPanX + this.minPanX) / 2;
                this.originY = centerY - (this.maxPanY + this.minPanY) / 2;
            }
        }
    }

    @ModifyConstant(method="render", constant=@Constant(intValue = 234), require = 1)
    private int getAdvTreeXSize(int orig) { return screen.width - config.marginX*2 - 2*9 - currentInfoWidth; }

    @ModifyConstant(method="render", constant=@Constant(intValue = 113), require = 1)
    private int getAdvTreeYSize(int orig) { return screen.height - config.marginY*2 - 3*9; }

    @ModifyConstant(method="render", constant=@Constant(intValue = 117), require = 1)
    private int getAdvTreeXOrig(int orig) { return screen.width/2 - config.marginX - currentInfoWidth/2; }

    @ModifyConstant(method="render", constant=@Constant(intValue = 56), require = 1)
    private int getAdvTreeYOrig(int orig) { return screen.height/2 - config.marginY; }
    
    @ModifyConstant(method="move", constant=@Constant(intValue = 234), require = 2)
    private int getMoveXCenter(int orig) { return screen.width - config.marginX*2 - 2*9 - currentInfoWidth; }

    @ModifyConstant(method="move", constant=@Constant(intValue = 113), require = 2)
    private int getMoveYCenter(int orig) { return screen.height - config.marginY*2 - 3*9; }

    @ModifyConstant(method="render", constant=@Constant(intValue = 15), require = 1)
    private int getXTextureRepeats(int orig) { return (screen.width-config.marginX*2 - currentInfoWidth) / 16 + 1; }

    @ModifyConstant(method="render", constant=@Constant(intValue = 8), require = 1)
    private int getYTextureRepeats(int orig) { return (screen.height-config.marginY*2) / 16 + 1; }

    @ModifyConstant(method="drawWidgetTooltip", constant=@Constant(intValue = 234), require = 2)
    private int getTooltipXSize(int orig) { return screen.width - config.marginX*2 - 2*9 - currentInfoWidth; }

    @ModifyConstant(method="drawWidgetTooltip", constant=@Constant(intValue = 113), require = 2)
    private int getTooltipYSize(int orig) { return screen.height - config.marginY*2 - 3*9; }

    @Inject(method="drawWidgetTooltip", at=@At("HEAD"))
    private void forgetMouseOver(DrawContext context, int i, int j, int y, int k, CallbackInfo ci) {
        AdvancementInfo.mouseOver = null;
    }
}
