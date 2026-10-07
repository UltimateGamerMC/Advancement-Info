package de.guntram.mcmod.advancementinfo.mixin;

import de.guntram.mcmod.advancementinfo.AdvancementInfo;
import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
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

    @Shadow
    @Final
    private AdvancementsScreen screen;

    @Shadow
    private double scrollX;

    @Shadow
    private double scrollY;

    @Shadow
    private int minX;

    @Shadow
    private int minY;

    @Shadow
    private int maxX;

    @Shadow
    private int maxY;

    @Shadow
    private boolean centered;

    @Shadow
    @Final
    private Identifier background;

    @Shadow
    private AdvancementWidget hovered;

    @Shadow
    private AdvancementWidget root;

    @Shadow
    private float fade;

    @Shadow
    @Final
    private Map<AdvancementHolder, AdvancementWidget> widgets;

    private int advancementinfo$innerW() {
        int iw = AdvancementInfo.config.infoWidth.calculate(screen.width);
        return screen.width - AdvancementInfo.config.marginX * 2 - 18 - iw;
    }

    private int advancementinfo$innerH() {
        return screen.height - AdvancementInfo.config.marginY * 2 - 27;
    }

    @Inject(method = "extractContents", at = @At("HEAD"), cancellable = true)
    private void advancementinfo$extractContents(GuiGraphicsExtractor graphics, int windowLeft, int windowTop, CallbackInfo ci) {
        int innerW = advancementinfo$innerW();
        int innerH = advancementinfo$innerH();
        if (!this.centered) {
            this.scrollX = innerW / 2.0 - (this.maxX + this.minX) / 2.0;
            this.scrollY = innerH / 2.0 - (this.maxY + this.minY) / 2.0;
            this.centered = true;
        }

        graphics.enableScissor(windowLeft, windowTop, windowLeft + innerW, windowTop + innerH);
        graphics.pose().pushMatrix();
        graphics.pose().translate(windowLeft, windowTop);
                int intScrollX = Mth.floor(this.scrollX);
        int intScrollY = Mth.floor(this.scrollY);
        int left = intScrollX % 16;
        int top = intScrollY % 16;

        int maxTileX = (innerW + 16 - 1) / 16 + 1;
        int maxTileY = (innerH + 16 - 1) / 16 + 1;
        for (int x = -1; x <= maxTileX; x++) {
            for (int y = -1; y <= maxTileY; y++) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, background, left + 16 * x, top + 16 * y, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }

        this.root.extractConnectivity(graphics, intScrollX, intScrollY, true);
        this.root.extractConnectivity(graphics, intScrollX, intScrollY, false);
        this.root.extractRenderState(graphics, intScrollX, intScrollY);
        graphics.pose().popMatrix();
        graphics.disableScissor();
        ci.cancel();
    }

    @Inject(method = "extractTooltips", at = @At("HEAD"), cancellable = true)
    private void advancementinfo$extractTooltips(GuiGraphicsExtractor graphics, int xo, int yo, CallbackInfo ci) {
        int innerW = advancementinfo$innerW();
        int innerH = advancementinfo$innerH();
        graphics.fill(0, 0, innerW, innerH, Mth.floor(this.fade * 255.0F) << 24);
        AdvancementInfo.mouseOver = this.hovered;
        if (this.hovered != null) {
            int intScrollX = Mth.floor(this.scrollX);
            int intScrollY = Mth.floor(this.scrollY);
            this.hovered.extractHover(graphics, intScrollX, intScrollY, this.fade, xo, yo, screen.width - AdvancementInfo.config.marginX - AdvancementInfo.config.infoWidth.calculate(screen.width));
        }
        ci.cancel();
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 234))
    private int advancementinfo$tickHoverW(int original) {
        return advancementinfo$innerW();
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 113))
    private int advancementinfo$tickHoverH(int original) {
        return advancementinfo$innerH();
    }

    @ModifyConstant(method = "scroll", constant = @Constant(intValue = 234))
    private int advancementinfo$scrollClampW(int original) {
        return advancementinfo$innerW();
    }

    @ModifyConstant(method = "scroll", constant = @Constant(intValue = 113))
    private int advancementinfo$scrollClampH(int original) {
        return advancementinfo$innerH();
    }

    @ModifyConstant(method = "canScrollHorizontally", constant = @Constant(intValue = 234))
    private int advancementinfo$canScrollX(int original) {
        return advancementinfo$innerW();
    }

    @ModifyConstant(method = "canScrollVertically", constant = @Constant(intValue = 113))
    private int advancementinfo$canScrollY(int original) {
        return advancementinfo$innerH();
    }
}
