package de.guntram.mcmod.advancementinfo;

import de.guntram.mcmod.advancementinfo.accessors.AdvancementScreenAccessor;
import de.guntram.mcmod.advancementinfo.accessors.AdvancementWidgetAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class AdvancementInfo implements ClientModInitializer {

    static final String MODID = "advancementinfo";
    static final String VERSION = "@VERSION@";
    public static final Logger LOGGER = LogManager.getLogger();

    static public AdvancementWidget mouseOver, mouseClicked;
    static public List<AdvancementStep> cachedClickList;
    static public int cachedClickListLineCount;
    public static boolean showAll;
    public static ModConfig config;

    public static List<AdvancementStep> getSteps(AdvancementWidgetAccessor widget) {
        List<AdvancementStep> result = new ArrayList<>();
        AdvancementProgress progress = widget.getProgress();
        if (progress != null) {
            addStep(result, progress, progress.getRemainingCriteria(), false);
            addStep(result, progress, progress.getCompletedCriteria(), true);
        }
        return result;
    }

    private static void addStep(List<AdvancementStep> result, AdvancementProgress progress, Iterable<String> criteria, boolean obtained) {
        final String[] prefixes =
                new String[] {"item.minecraft", "block.minecraft", "entity.minecraft", "container", "effect.minecraft", "biome.minecraft"};
        ArrayList<String> sorted = new ArrayList<>();
        for (String s : criteria) {
            sorted.add(s);
        }
        ArrayList<String> details = null;
        Collections.sort(sorted);
        for (String s : sorted) {
            String translation = null;
            String key = s;
            if (key.startsWith("minecraft:")) {
                key = key.substring(10);
            }
            if (key.startsWith("textures/entity/")) {
                String entityAppearance = key.substring(16);
                int dotPos;
                if ((dotPos = entityAppearance.indexOf(".")) > 0) {
                    entityAppearance = entityAppearance.substring(0, dotPos);
                }
                translation = entityAppearance;
            }
            if (translation == null) {
                for (String prefix : prefixes) {
                    if (I18n.exists(prefix + "." + key)) {
                        translation = I18n.get(prefix + "." + key);
                        break;
                    }
                }
            }
            if (translation == null) {
                translation = key;
            }
            result.add(new AdvancementStep(translation, obtained, details));
        }
    }

    public static void setMatchingFrom(AdvancementsScreen screen, String text) {
        List<AdvancementStep> result = new ArrayList<>();
        ClientAdvancements advancementHandler = ((AdvancementScreenAccessor) screen).getAdvancementHandler();
        int lineCount = 0;

        text = text.toLowerCase();
        for (AdvancementNode node : advancementHandler.getTree().nodes()) {
            Advancement adv = node.advancement();
            Identifier id = node.holder().id();

            if (id.getPath().startsWith("recipes/")) {
                continue;
            }
            if (adv.display().isEmpty()) {
                LOGGER.debug("! {} Has no display", id);
                continue;
            }
            var display = adv.display().get();
            if (display.getTitle() == null) {
                LOGGER.debug("! {} Has no title", id);
                continue;
            }
            if (display.getDescription() == null) {
                LOGGER.debug("! {} Has no description", id);
                continue;
            }
            String title = display.getTitle().getString();
            String desc = display.getDescription().getString();
            LOGGER.debug("- {} {}: {} ", id, title, desc);
            if (title.toLowerCase().contains(text) || desc.toLowerCase().contains(text)) {
                ArrayList<String> details = new ArrayList<>();
                details.add(desc);
                AdvancementWidget widget = screen.getAdvancementWidget(node);
                if (widget == null) {
                    LOGGER.info("no widget found for advancement {} title {} description {}", id, title, desc);
                    continue;
                }
                boolean done = ((AdvancementWidgetAccessor) widget).getProgress().isDone();
                result.add(new AdvancementStep(title, done, details));
                lineCount += 2;
            }
        }
        cachedClickList = result;
        cachedClickListLineCount = lineCount;
        mouseOver = null;
    }

    @Override
    public void onInitializeClient() {
        Configurator.setLevel(LOGGER.getName(), Level.ALL);
        showAll = false;
        if (FabricLoader.getInstance().isModLoaded("cloth-config")
                || FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            ConfigHolder<ModConfig> configHolder = AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
            configHolder.registerSaveListener((holder, modConfig) -> {
                modConfig.validate();
                return InteractionResult.PASS;
            });
            config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        } else {
            config = new ModConfig();
        }

        LOGGER.info("AdvancementInfo initialized");
    }
}
