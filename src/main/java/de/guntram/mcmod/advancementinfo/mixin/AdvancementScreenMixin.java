package de.guntram.mcmod.advancementinfo.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import de.guntram.mcmod.advancementinfo.AdvancementInfo;
import static de.guntram.mcmod.advancementinfo.AdvancementInfo.config;
import de.guntram.mcmod.advancementinfo.AdvancementStep;
import de.guntram.mcmod.advancementinfo.IteratorReceiver;
import de.guntram.mcmod.advancementinfo.accessors.AdvancementScreenAccessor;
import de.guntram.mcmod.advancementinfo.accessors.AdvancementWidgetAccessor;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementsScreen.class)
public class AdvancementScreenMixin implements AdvancementScreenAccessor {

    private int advancementinfo$scrollPos;
    private int advancementinfo$currentInfoWidth;
    private EditBox advancementinfo$search;

    private static final Identifier advancementinfo$WINDOW_TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/advancements/window.png");
    private static final Component advancementinfo$ADVANCEMENTS_TEXT = Component.translatable("gui.advancements");

    @Shadow
    @Final
    private ClientAdvancements advancements;

    @Shadow
    private AdvancementTab selectedTab;

    @Shadow
    private Map<AdvancementHolder, AdvancementTab> tabs;

    private Screen advancementinfo$screen() {
        return (Screen)(Object)this;
    }

    private int advancementinfo$windowW() {
        return advancementinfo$screen().width - config.marginX * 2;
    }

    private int advancementinfo$windowH() {
        return advancementinfo$screen().height - config.marginY * 2;
    }

    private int advancementinfo$infoW() {
        return config.infoWidth.calculate(advancementinfo$screen().width);
    }

    private int advancementinfo$innerW() {
        return advancementinfo$screen().width - config.marginX * 2 - 18 - advancementinfo$infoW();
    }

    private int advancementinfo$innerH() {
        return advancementinfo$screen().height - config.marginY * 2 - 27;
    }

    @ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 252))
    private int advancementinfo$extractWindowW(int original) {
        return advancementinfo$windowW();
    }

    @ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 140))
    private int advancementinfo$extractWindowH(int original) {
        return advancementinfo$windowH();
    }

    @ModifyConstant(method = "mouseClicked", constant = @Constant(intValue = 252))
    private int advancementinfo$clickWindowW(int original) {
        return advancementinfo$windowW();
    }

    @ModifyConstant(method = "mouseClicked", constant = @Constant(intValue = 140))
    private int advancementinfo$clickWindowH(int original) {
        return advancementinfo$windowH();
    }

    @ModifyConstant(method = "extractInside", constant = @Constant(intValue = 234))
    private int advancementinfo$insideW(int original) {
        return advancementinfo$innerW();
    }

    @ModifyConstant(method = "extractInside", constant = @Constant(intValue = 113))
    private int advancementinfo$insideH(int original) {
        return advancementinfo$innerH();
    }

    @ModifyConstant(method = "extractInside", constant = @Constant(intValue = 117))
    private int advancementinfo$insideMidX(int original) {
        return advancementinfo$innerW() / 2;
    }

    @Inject(
            method = "extractRenderState",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;extractWindow(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V"))
    private void advancementinfo$rightPanelFill(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        advancementinfo$currentInfoWidth = advancementinfo$infoW();
        if (advancementinfo$currentInfoWidth == 0) {
            return;
        }
        graphics.fill(
                advancementinfo$screen().width - config.marginX - advancementinfo$currentInfoWidth + 4,
                config.marginY + 4,
                advancementinfo$screen().width - config.marginX - 4,
                advancementinfo$screen().height - config.marginY - 4,
                0xffc0c0c0);
    }

    @Inject(method = "extractWindow", at = @At("HEAD"), cancellable = true)
    private void advancementinfo$extractWindow(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, CallbackInfo ci) {
        advancementinfo$currentInfoWidth = advancementinfo$infoW();
        int iw = advancementinfo$currentInfoWidth;
        int wW = advancementinfo$windowW();
        int wH = advancementinfo$windowH();
        int screenW = wW;
        int screenH = wH;
        int actualW = advancementinfo$screen().width - config.marginX - iw - x;
        int actualH = advancementinfo$screen().height - config.marginY - y;
        int halfW = screenW / 2;
        int halfH = screenH / 2;
        int clipXh = (int) (Math.max(0, screenW - actualW) / 2.0 + 0.5);
        int clipXl = (int) (Math.max(0, screenW - actualW) / 2.0);
        int clipYh = (int) (Math.max(0, screenH - actualH) / 2.0 + 0.5);
        int clipYl = (int) (Math.max(0, screenH - actualH) / 2.0);

        int rightQuadX = advancementinfo$screen().width - config.marginX - halfW - iw + clipXh;
        int bottomQuadY = advancementinfo$screen().height - config.marginY - halfH + clipYh;

        advancementinfo$drawTexture(graphics, x, y, 0, 0, halfW - clipXl, halfH - clipYl);
        advancementinfo$drawTexture(graphics, rightQuadX, y, halfW + clipXh, 0, halfW - clipXh, halfH - clipYl);
        advancementinfo$drawTexture(graphics, x, bottomQuadY, 0, halfH + clipYh, halfW - clipXl, halfH - clipYh);
        advancementinfo$drawTexture(graphics, rightQuadX, bottomQuadY, halfW + clipXh, halfH + clipYh, halfW - clipXh, halfH - clipYh);

        advancementinfo$iterate(x + halfW - clipXl, rightQuadX, 200, (pos, len) -> {
            advancementinfo$drawTexture(graphics, pos, y, 15, 0, len, halfH);
            advancementinfo$drawTexture(graphics, pos, bottomQuadY, 15, halfH + clipYh, len, halfH - clipYh);
        });
        advancementinfo$iterate(y + halfH - clipYl, bottomQuadY, 100, (pos, len) -> {
            advancementinfo$drawTexture(graphics, x, pos, 0, 25, halfW, len);
            advancementinfo$drawTexture(graphics, rightQuadX, pos, halfW + clipXh, 25, halfW - clipXh, len);
        });

        if (iw == 0) {
            if (tabs.size() > 1) {
                for (AdvancementTab tab : tabs.values()) {
                    tab.extractTab(graphics, x, y, mouseX, mouseY, tab == selectedTab);
                }
                for (AdvancementTab tab : tabs.values()) {
                    tab.extractIcon(graphics, x, y);
                }
            }

            graphics.text(
                    advancementinfo$screen().font,
                    selectedTab != null ? selectedTab.getTitle() : advancementinfo$ADVANCEMENTS_TEXT,
                    x + 8,
                    y + 6,
                    CommonColors.DARK_GRAY,
                    false);
            ci.cancel();
            return;
        }

        int infoWl = (int) (iw / 2.0);
        int infoWh = (int) (iw / 2.0 + 0.5);
        advancementinfo$drawTexture(graphics, advancementinfo$screen().width - config.marginX - iw, y, 0, 0, infoWh, halfH);
        advancementinfo$drawTexture(graphics, advancementinfo$screen().width - config.marginX - infoWl, y, 252 - infoWl, 0, infoWl, halfH);
        advancementinfo$drawTexture(graphics, advancementinfo$screen().width - config.marginX - iw, bottomQuadY, 0, halfH, infoWh, halfH);
        advancementinfo$drawTexture(graphics, advancementinfo$screen().width - config.marginX - infoWl, bottomQuadY, 252 - infoWl, halfH, infoWl, halfH);

        advancementinfo$iterate(halfH + config.marginY, bottomQuadY, 100, (pos, len) -> {
            advancementinfo$drawTexture(graphics, advancementinfo$screen().width - config.marginX - iw, pos, 0, 25, iw / 2, len);
            advancementinfo$drawTexture(graphics, advancementinfo$screen().width - config.marginX - iw / 2, pos, 252 - iw / 2, 25, iw / 2, len);
        });

        if (tabs.size() > 1) {
            for (AdvancementTab tab : tabs.values()) {
                tab.extractTab(graphics, x, y, mouseX, mouseY, tab == selectedTab);
            }
            for (AdvancementTab tab : tabs.values()) {
                tab.extractIcon(graphics, x, y);
            }
        }

        graphics.text(
                advancementinfo$screen().font,
                selectedTab != null ? selectedTab.getTitle() : advancementinfo$ADVANCEMENTS_TEXT,
                x + 8,
                y + 6,
                CommonColors.DARK_GRAY,
                false);

        if (advancementinfo$search != null) {
            if (AdvancementInfo.mouseClicked != null) {
                advancementinfo$renderCriteria(graphics, AdvancementInfo.mouseClicked);
            } else if (AdvancementInfo.mouseOver != null || AdvancementInfo.cachedClickList != null) {
                advancementinfo$renderCriteria(graphics, AdvancementInfo.mouseOver);
            }
        }

        ci.cancel();
    }

    private void advancementinfo$iterate(int start, int end, int maxstep, IteratorReceiver func) {
        if (start >= end) {
            return;
        }
        int size;
        for (int i = start; i < end; i += maxstep) {
            size = maxstep;
            if (i + size > end) {
                size = end - i;
                if (size <= 0) {
                    return;
                }
            }
            func.accept(i, size);
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void advancementinfo$initSearch(CallbackInfo ci) {
        advancementinfo$currentInfoWidth = advancementinfo$infoW();
        this.advancementinfo$search =
                new EditBox(
                        advancementinfo$screen().font,
                        advancementinfo$screen().width - config.marginX - advancementinfo$currentInfoWidth + 9,
                        config.marginY + 18,
                        Math.max(1, advancementinfo$currentInfoWidth - 18),
                        17,
                        CommonComponents.EMPTY);
        ((AdvancementsScreen) (Object) this).addWidget(this.advancementinfo$search);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void advancementinfo$renderSearchOnTop(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (advancementinfo$search != null) {
            advancementinfo$search.extractRenderState(graphics, mouseX, mouseY, delta);
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"), cancellable = true)
    private void advancementinfo$rememberClicked(MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (advancementinfo$search == null) {
            return;
        }
        double mx = event.x();
        double my = event.y();

        if (advancementinfo$search.isMouseOver(mx, my)) {
            return;
        }
        if (mx >= advancementinfo$screen().width - config.marginX - advancementinfo$currentInfoWidth) {
            return;
        }

        advancementinfo$scrollPos = 0;
        if (AdvancementInfo.mouseOver != null) {
            AdvancementInfo.mouseClicked = AdvancementInfo.mouseOver;
            AdvancementInfo.cachedClickList = AdvancementInfo.getSteps((AdvancementWidgetAccessor) AdvancementInfo.mouseOver);
            AdvancementInfo.cachedClickListLineCount = AdvancementInfo.cachedClickList.size();
        } else {
            AdvancementInfo.mouseClicked = null;
            AdvancementInfo.cachedClickList = null;
            AdvancementInfo.cachedClickListLineCount = 0;
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void advancementinfo$scrollCriteria(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (advancementinfo$search == null) {
            return;
        }
        int lineCapacity =
                (advancementinfo$screen().height - 2 * config.marginY - 45) / advancementinfo$screen().font.lineHeight - 1;
        if (scrollY > 0 && advancementinfo$scrollPos > 0) {
            advancementinfo$scrollPos--;
            cir.setReturnValue(true);
            cir.cancel();
        } else if (scrollY < 0
                && AdvancementInfo.cachedClickList != null
                && advancementinfo$scrollPos < AdvancementInfo.cachedClickListLineCount - lineCapacity) {
            advancementinfo$scrollPos++;
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void advancementinfo$searchKeys(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (advancementinfo$search == null || !advancementinfo$search.isFocused()) {
            return;
        }
        if (event.key() == InputConstants.KEY_RETURN) {
            AdvancementInfo.setMatchingFrom((AdvancementsScreen) (Object) this, advancementinfo$search.getValue());
        }
        advancementinfo$search.keyPressed(event);
        if (!event.isEscape()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    private void advancementinfo$renderCriteria(GuiGraphicsExtractor graphics, AdvancementWidget widget) {
        if (advancementinfo$search == null) {
            return;
        }
        int y = advancementinfo$search.getY() + advancementinfo$search.getHeight() + 4;
        int skip;
        List<AdvancementStep> list;
        if (widget == AdvancementInfo.mouseClicked) {
            list = AdvancementInfo.cachedClickList;
            skip = advancementinfo$scrollPos;
        } else {
            list = AdvancementInfo.getSteps((AdvancementWidgetAccessor) widget);
            skip = 0;
        }
        if (list == null) {
            return;
        }
        int iw = advancementinfo$currentInfoWidth;
        for (AdvancementStep entry : list) {
            if (skip-- <= 0) {
                int rx = advancementinfo$screen().width - config.marginX - iw + 12;
                int color = entry.getObtained() ? AdvancementInfo.config.colorHave : AdvancementInfo.config.colorHaveNot;
                color = color | 0xFF000000;
                String text = advancementinfo$screen().font.plainSubstrByWidth(entry.getName(), iw - 24);
                graphics.text(advancementinfo$screen().font, text, rx, y, color, false);
                y += advancementinfo$screen().font.lineHeight;
                if (y > advancementinfo$screen().height - config.marginY - advancementinfo$screen().font.lineHeight * 2) {
                    return;
                }
            }

            if (entry.getDetails() != null) {
                for (String detail : entry.getDetails()) {
                    if (skip-- <= 0) {
                        graphics.text(
                                advancementinfo$screen().font,
                                advancementinfo$screen().font.plainSubstrByWidth(detail, iw - 34),
                                advancementinfo$screen().width - config.marginX - iw + 22,
                                y,
                                CommonColors.BLACK,
                                false);
                        y += advancementinfo$screen().font.lineHeight;
                        if (y > advancementinfo$screen().height - config.marginY - advancementinfo$screen().font.lineHeight * 2) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void advancementinfo$drawTexture(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int regionW, int regionH) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                advancementinfo$WINDOW_TEXTURE,
                x,
                y,
                (float) u,
                (float) v,
                regionW,
                regionH,
                256,
                256);
    }

    @Override
    public ClientAdvancements getAdvancementHandler() {
        return advancements;
    }
}
