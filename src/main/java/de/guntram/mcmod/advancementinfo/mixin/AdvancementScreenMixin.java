package de.guntram.mcmod.advancementinfo.mixin;

import de.guntram.mcmod.advancementinfo.AdvancementInfo;
import static de.guntram.mcmod.advancementinfo.AdvancementInfo.config;
import de.guntram.mcmod.advancementinfo.AdvancementStep;
import de.guntram.mcmod.advancementinfo.IteratorReceiver;
import de.guntram.mcmod.advancementinfo.accessors.AdvancementScreenAccessor;
import de.guntram.mcmod.advancementinfo.accessors.AdvancementWidgetAccessor;
import java.util.List;
import java.util.Map;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementScreenMixin extends Screen implements AdvancementScreenAccessor {

    public AdvancementScreenMixin() { super(null); }

    private int scrollPos;
    private int currentInfoWidth = config.infoWidth.calculate(width);
    private TextFieldWidget search;
    @Shadow @Final private ClientAdvancementManager advancementHandler;
    @Shadow private AdvancementTab selectedTab;
    @Shadow private Map<PlacedAdvancement, AdvancementTab> tabs;

    private static final Identifier WINDOW_TEXTURE = Identifier.ofVanilla("textures/gui/advancements/window.png");
    private static final net.minecraft.text.Text ADVANCEMENTS_TEXT = net.minecraft.text.Text.translatable("gui.advancements");

    @ModifyConstant(method="render", constant=@Constant(intValue = 252), require=1)
    private int getRenderLeft(int orig) { return width - config.marginX*2; }

    @ModifyConstant(method="render", constant=@Constant(intValue = 140), require=1)
    private int getRenderTop(int orig) { return height - config.marginY*2; }

    @ModifyConstant(method="mouseClicked", constant=@Constant(intValue = 252), require=1)
    private int getMouseLeft(int orig) { return width - config.marginX*2; }

    @ModifyConstant(method="mouseClicked", constant=@Constant(intValue = 140), require=1)
    private int getMouseTop(int orig) { return height - config.marginY*2; }

    @ModifyConstant(method="drawAdvancementTree", constant=@Constant(intValue = 234), require = 1)
    private int getAdvTreeXSize(int orig) { return width - config.marginX*2 - 2*9 - currentInfoWidth; }

    @ModifyConstant(method="drawAdvancementTree", constant=@Constant(intValue = 113), require = 1)
    private int getAdvTreeYSize(int orig) { return height - config.marginY*2 - 3*9; }

    @Inject(method="init", at=@At("RETURN"))
    private void initSearchField(CallbackInfo ci) {
        currentInfoWidth = config.infoWidth.calculate(width);
        this.search = new TextFieldWidget(textRenderer, width-config.marginX-currentInfoWidth+9, config.marginY+18, currentInfoWidth-18, 17, ScreenTexts.EMPTY);
    }

    @Inject(method="render",
            at=@At(value="INVOKE",
                    target="net/minecraft/client/gui/screen/advancement/AdvancementsScreen.drawWindow(Lnet/minecraft/client/gui/DrawContext;II)V"))
    public void renderRightFrameBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        currentInfoWidth = config.infoWidth.calculate(width);
        if(currentInfoWidth == 0) return;
        context.fill(
                width-config.marginX-currentInfoWidth+4, config.marginY+4,
                width-config.marginX-4, height-config.marginY-4, 0xffc0c0c0);
    }

    @Inject(method="drawWindow", at=@At("HEAD"), cancellable = true)
    public void renderFrames(DrawContext context, int x, int y, CallbackInfo ci) {
        currentInfoWidth = config.infoWidth.calculate(width);
        int iw = currentInfoWidth;

        int screenW = 252;
        int screenH = 140;
        int actualW = width - config.marginX - iw - x;
        int actualH = width - config.marginY - y;
        int halfW = screenW/2;
        int halfH = screenH/2;
        int clipXh = (int) (Math.max(0, screenW-actualW)/2.+0.5);
        int clipXl = (int) (Math.max(0, screenW-actualW)/2.);
        int clipYh = (int) (Math.max(0, screenH-actualH)/2.+0.5);
        int clipYl = (int) (Math.max(0, screenH-actualH)/2.);

        int rightQuadX = width - config.marginX - halfW - iw + clipXh;
        int bottomQuadY = height - config.marginY - halfH + clipYh;

        drawTexture(context, x, y, 0, 0, halfW-clipXl, halfH-clipYl);
        drawTexture(context, rightQuadX, y, halfW+clipXh, 0, halfW-clipXh, halfH-clipYl);
        drawTexture(context, x, bottomQuadY, 0, halfH+clipYh, halfW-clipXl, halfH-clipYh);
        drawTexture(context, rightQuadX, bottomQuadY, halfW+clipXh, halfH+clipYh, halfW-clipXh, halfH-clipYh);

        iterate(x+halfW-clipXl, rightQuadX, 200, (pos, len) -> {
            drawTexture(context, pos, y, 15, 0, len, halfH);
            drawTexture(context, pos, bottomQuadY, 15, halfH+clipYh, len, halfH-clipYh);
        });
        iterate(y+halfH-clipYl, bottomQuadY, 100, (pos, len) -> {
            drawTexture(context, x, pos, 0, 25, halfW, len);
            drawTexture(context, rightQuadX, pos, halfW+clipXh, 25, halfW-clipXh, len);
        });

        if(currentInfoWidth == 0) {
            ci.cancel();
            return;
        }

        int infoWl = (int) (iw/2.);
        int infoWh = (int) (iw/2.+0.5);
        drawTexture(context, width-config.marginX - iw    , y,0, 0, infoWh, halfH);
        drawTexture(context, width-config.marginX - infoWl, y, screenW-infoWl, 0, infoWl, halfH);
        drawTexture(context, width-config.marginX - iw    , bottomQuadY, 0, halfH, infoWh, halfH);
        drawTexture(context, width-config.marginX - infoWl, bottomQuadY, screenW-infoWl, halfH, infoWl, halfH);

        iterate(halfH+config.marginY, bottomQuadY, 100, (pos, len) -> {
            drawTexture(context, width-config.marginX - iw, pos,0, 25, iw/2, len);
            drawTexture(context, width-config.marginX - iw/2, pos, screenW-iw/2, 25, iw/2, len);
        });

        if (tabs.size() > 1) {
            for (AdvancementTab tab : tabs.values()) {
                tab.drawBackground(context, x, y, tab == selectedTab);
            }
            for (AdvancementTab tab : tabs.values()) {
                tab.drawIcon(context, x, y);
            }
        }

        context.drawText(textRenderer, selectedTab != null ? selectedTab.getTitle() : ADVANCEMENTS_TEXT, x + 8, y + 6, Colors.DARK_GRAY, false);

        if (search != null) {
            if (AdvancementInfo.mouseClicked != null) {
                renderCriteria(context, AdvancementInfo.mouseClicked);
            } else if (AdvancementInfo.mouseOver != null || AdvancementInfo.cachedClickList != null) {
                renderCriteria(context, AdvancementInfo.mouseOver);
            }
        }

        ci.cancel();
    }

    private void iterate(int start, int end, int maxstep, IteratorReceiver func) {
        if(start >= end) return;
        int size;
        for (int i=start; i<end; i+=maxstep) {
            size=maxstep;
            if (i+size > end) {
                size = end - i;
                if(size <= 0) return;
            }
            func.accept(i, size);
        }
    }

    @Inject(method="mouseClicked", at=@At("RETURN"), cancellable = true)
    public void rememberClickedWidget(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        double x = click.x();
        double y = click.y();

        if (search.isMouseOver(x, y)) {
            return;
        }
        if (x >= width - config.marginX - currentInfoWidth) {
            return;
        }

        scrollPos = 0;
        if (AdvancementInfo.mouseOver != null) {
            AdvancementInfo.mouseClicked = AdvancementInfo.mouseOver;
            AdvancementInfo.cachedClickList = AdvancementInfo.getSteps((AdvancementWidgetAccessor) AdvancementInfo.mouseClicked);
            AdvancementInfo.cachedClickListLineCount = AdvancementInfo.cachedClickList.size();
        } else {
            AdvancementInfo.mouseClicked = null;
            AdvancementInfo.cachedClickList = null;
            AdvancementInfo.cachedClickListLineCount = 0;
        }
    }

    @Inject(method="mouseScrolled", at=@At("HEAD"), cancellable = true)
    public void handleScrollWheel(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (verticalAmount > 0 && scrollPos > 0) {
            scrollPos--;
            cir.setReturnValue(true);
        } else if (verticalAmount < 0 && AdvancementInfo.cachedClickList != null
                && scrollPos < AdvancementInfo.cachedClickListLineCount - ((height-2*config.marginY-45)/textRenderer.fontHeight - 1)) {
            scrollPos++;
            cir.setReturnValue(true);
        }
    }

    @Inject(method="keyPressed", at=@At("HEAD"), cancellable = true)
    public void redirectKeysToSearch(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (search.isActive()) {
            if (input.key() == GLFW.GLFW_KEY_ENTER) {
                AdvancementInfo.setMatchingFrom((AdvancementsScreen)(Object)this, search.getText());
            }
            search.keyPressed(input);
            if (input.key() != GLFW.GLFW_KEY_ESCAPE) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    private void renderCriteria(DrawContext context, AdvancementWidget widget) {
        if (search == null) return;
        int y = search.getY() + search.getHeight() + 4;
        int skip;
        List<AdvancementStep> list;
        if (widget == AdvancementInfo.mouseClicked) {
            list = AdvancementInfo.cachedClickList;
            skip = scrollPos;
        } else {
            list = AdvancementInfo.getSteps((AdvancementWidgetAccessor) widget);
            skip = 0;
        }
        if (list == null) {
            return;
        }
        for (AdvancementStep entry: list) {
            if (skip-- <= 0) {
                int x = width-config.marginX-currentInfoWidth+12;
                int color = entry.getObtained() ? AdvancementInfo.config.colorHave : AdvancementInfo.config.colorHaveNot;
                color = color | 0xFF000000;
                String text = textRenderer.trimToWidth(entry.getName(), currentInfoWidth-24);
                context.drawText(textRenderer, text, x, y, color, false);
                y+=textRenderer.fontHeight;
                if (y > height - config.marginY - textRenderer.fontHeight*2) {
                    return;
                }
            }

            if (entry.getDetails() != null) {
                for (String detail: entry.getDetails()) {
                    if (skip-- <= 0) {
                        context.drawText(textRenderer,
                                textRenderer.trimToWidth(detail, currentInfoWidth-34),
                                width-config.marginX-currentInfoWidth+22, y,
                                Colors.BLACK, false);
                        y+=textRenderer.fontHeight;
                        if (y > height - config.marginY - textRenderer.fontHeight*2) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void drawTexture(DrawContext context, int x, int y, int u, int v, int width, int height) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE,
                x, y, (float)u, (float)v, width, height, 256, 256);
    }

    @Override
    public ClientAdvancementManager getAdvancementHandler() {
        return advancementHandler;
    }
}
