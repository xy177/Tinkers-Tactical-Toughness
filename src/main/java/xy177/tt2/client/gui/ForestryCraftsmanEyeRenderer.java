package xy177.tt2.client.gui;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.api.arboriculture.EnumFruitFamily;
import forestry.api.arboriculture.EnumGermlingType;
import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.arboriculture.IAlleleFruit;
import forestry.api.arboriculture.ITree;
import forestry.api.arboriculture.TreeManager;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.EnumTolerance;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IAlleleInteger;
import forestry.api.genetics.IAlleleSpecies;
import forestry.api.genetics.IAlleleTolerance;
import forestry.api.genetics.IAlyzerPlugin;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IChromosomeType;
import forestry.api.genetics.IClassification;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IFruitFamily;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpeciesRoot;
import forestry.api.lepidopterology.ButterflyManager;
import forestry.api.lepidopterology.EnumButterflyChromosome;
import forestry.api.lepidopterology.IButterfly;
import forestry.arboriculture.genetics.alleles.AlleleFruits;
import forestry.core.config.Config;
import forestry.core.genetics.GenericRatings;
import forestry.core.genetics.alleles.AlleleBoolean;
import forestry.core.genetics.mutations.EnumMutateChance;
import forestry.core.render.ColourProperties;
import forestry.core.utils.ItemTooltipUtil;
import forestry.core.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

/** Forestry-only renderer, reflectively loaded after Forestry is confirmed present. */
public final class ForestryCraftsmanEyeRenderer implements CraftsmanEyePageRenderer {

    private static final int COLUMN_0 = 12;
    private static final int COLUMN_1 = 90;
    private static final int COLUMN_2 = 155;

    @Override
    public int getColor(String key, int fallback) {
        try {
            return ColourProperties.INSTANCE.get(key);
        } catch (RuntimeException failure) {
            return fallback;
        }
    }

    @Override
    public boolean drawPage(GuiCraftsmanEye gui, ItemStack specimen, int page) {
        ISpeciesRoot root = AlleleManager.alleleRegistry.getSpeciesRoot(specimen);
        if (root == null) {
            return false;
        }
        IIndividual individual = root.getMember(specimen);
        if (individual == null || !individual.isAnalyzed()) {
            return false;
        }
        switch (page) {
            case 1:
            case 2:
            case 3:
                return drawSpeciesPage(gui, specimen, individual, page);
            case 4:
                drawMutations(gui, individual);
                return true;
            case 5:
                drawClassification(gui, individual);
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<String> getHints(ItemStack specimen) {
        ISpeciesRoot root = AlleleManager.alleleRegistry.getSpeciesRoot(specimen);
        if (root == null || root.getAlyzerPlugin() == null) {
            return Collections.emptyList();
        }
        List<String> hints = root.getAlyzerPlugin().getHints();
        return hints == null ? Collections.emptyList() : hints;
    }

    @Override
    public List<String> getItemTooltip(ItemStack stack) {
        return ItemTooltipUtil.getInformation(stack);
    }

    @Override
    public boolean areHintsEnabled() {
        return Config.enableHints;
    }

    @Override
    public int getLedgerAnimationSpeed() {
        return Config.guiTabSpeed;
    }

    private boolean drawSpeciesPage(GuiCraftsmanEye gui, ItemStack specimen,
                                    IIndividual individual, int page) {
        if (individual instanceof IBee) {
            drawBeePage(gui, specimen, page);
            return true;
        }
        if (individual instanceof ITree) {
            drawTreePage(gui, specimen, page);
            return true;
        }
        if (individual instanceof IButterfly) {
            drawButterflyPage(gui, specimen, page);
            return true;
        }
        // Forestry's extension API accepts GuiScreen, but analyzer plugins normally require GuiAlyzer.
        // Returning false lets the TT2-owned GUI show its overview instead of a falsely successful blank page.
        return false;
    }

    private void drawBeePage(GuiCraftsmanEye gui, ItemStack specimen, int page) {
        if (page == 1) {
            drawBeePageOne(gui, specimen);
        } else if (page == 2) {
            drawBeePageTwo(gui, specimen);
        } else {
            drawBeePageThree(gui, specimen);
        }
    }

    private void drawTreePage(GuiCraftsmanEye gui, ItemStack specimen, int page) {
        if (page == 1) {
            drawTreePageOne(gui, specimen);
        } else if (page == 2) {
            drawTreePageTwo(gui, specimen);
        } else {
            drawTreePageThree(gui, specimen);
        }
    }

    private void drawButterflyPage(GuiCraftsmanEye gui, ItemStack specimen, int page) {
        if (page == 1) {
            drawButterflyPageOne(gui, specimen);
        } else if (page == 2) {
            drawButterflyPageTwo(gui, specimen);
        } else {
            drawButterflyPageThree(gui, specimen);
        }
    }

    private int screenColor() {
        return getColor("gui.screen", 0xFFFFFF);
    }

    private int colorCoding(boolean dominant) {
        return getColor(dominant ? "gui.beealyzer.dominant" : "gui.beealyzer.recessive",
            dominant ? 0xFFFF57 : 0xA0A0A0);
    }

    private void drawLine(GuiCraftsmanEye gui, String text, int x, IIndividual individual,
                          IChromosomeType chromosome, boolean inactive) {
        IAllele allele = inactive
            ? individual.getGenome().getInactiveAllele(chromosome)
            : individual.getGenome().getActiveAllele(chromosome);
        gui.getTextLayout().drawLine(text, x, colorCoding(allele.isDominant()));
    }

    private void drawSplitLine(GuiCraftsmanEye gui, String text, int x, int maxWidth,
                               IIndividual individual, IChromosomeType chromosome, boolean inactive) {
        IAllele allele = inactive
            ? individual.getGenome().getInactiveAllele(chromosome)
            : individual.getGenome().getActiveAllele(chromosome);
        gui.getTextLayout().drawSplitLine(text, x, maxWidth, colorCoding(allele.isDominant()));
    }

    private void drawRow(GuiCraftsmanEye gui, String label, String active, String inactive,
                         IIndividual individual, IChromosomeType chromosome) {
        gui.getTextLayout().drawRow(label, active, inactive, screenColor(),
            colorCoding(individual.getGenome().getActiveAllele(chromosome).isDominant()),
            colorCoding(individual.getGenome().getInactiveAllele(chromosome).isDominant()));
    }

    private void drawChromosomeRow(GuiCraftsmanEye gui, String label,
                                   IIndividual individual, IChromosomeType chromosome) {
        IAllele active = individual.getGenome().getActiveAllele(chromosome);
        IAllele inactive = individual.getGenome().getInactiveAllele(chromosome);
        gui.getTextLayout().drawRow(label, active.getAlleleName(), inactive.getAlleleName(),
            screenColor(), colorCoding(active.isDominant()), colorCoding(inactive.isDominant()));
    }

    private void drawSpeciesRow(GuiCraftsmanEye gui, String label, IIndividual individual,
                                IChromosomeType chromosome, @Nullable String customPrimary,
                                @Nullable String customSecondary) {
        CraftsmanEyeLayout layout = gui.getTextLayout();
        IAlleleSpecies primary = individual.getGenome().getPrimary();
        IAlleleSpecies secondary = individual.getGenome().getSecondary();
        layout.drawLine(label, layout.column0);
        int columnWidth = layout.column2 - layout.column1 - 2;
        Map<String, ItemStack> icons = chromosome.getSpeciesRoot().getAlyzerPlugin().getIconStacks();
        gui.renderPageIcon(icons.get(primary.getUID()), layout.column1 + columnWidth - 20, 10);
        gui.renderPageIcon(icons.get(secondary.getUID()), layout.column2 + columnWidth - 20, 10);
        String primaryName = customPrimary == null ? primary.getAlleleName() : customPrimary;
        String secondaryName = customSecondary == null ? secondary.getAlleleName() : customSecondary;
        drawSplitLine(gui, primaryName, layout.column1, columnWidth, individual, chromosome, false);
        drawSplitLine(gui, secondaryName, layout.column2, columnWidth, individual, chromosome, true);
        layout.newLine();
    }

    @Nullable
    private static String customName(String key) {
        return I18n.hasKey(key) ? I18n.format(key) : null;
    }

    private void drawFertilityInfo(GuiCraftsmanEye gui, int fertility, int x,
                                   int textColor, int textureOffset) {
        String text = fertility + " x";
        int stringWidth = gui.getPageFont().getStringWidth(text);
        gui.drawAtlasRegion(x + stringWidth + 2, gui.getTextLayout().getLineY() - 1,
            60, 240 + textureOffset, 12, 8);
        gui.getTextLayout().drawLine(text, x, textColor);
    }

    private void drawToleranceInfo(GuiCraftsmanEye gui, IAlleleTolerance toleranceAllele, int x) {
        int textColor = colorCoding(toleranceAllele.isDominant());
        EnumTolerance tolerance = toleranceAllele.getValue();
        String text = "(" + toleranceAllele.getAlleleName() + ")";
        int textureX;
        switch (tolerance) {
            case BOTH_1:
            case BOTH_2:
            case BOTH_3:
            case BOTH_4:
            case BOTH_5:
                textureX = 30;
                break;
            case DOWN_1:
            case DOWN_2:
            case DOWN_3:
            case DOWN_4:
            case DOWN_5:
                textureX = 0;
                break;
            case UP_1:
            case UP_2:
            case UP_3:
            case UP_4:
            case UP_5:
                textureX = 15;
                break;
            default:
                textureX = 45;
                text = "(0)";
        }
        gui.drawAtlasRegion(x - 2, gui.getTextLayout().getLineY() - 1,
            textureX, 247, 15, 9);
        gui.getTextLayout().drawLine(text, x + 14, textColor);
    }

    private void drawMutations(GuiCraftsmanEye gui, IIndividual individual) {
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        layout.drawLine(I18n.format("for.gui.beealyzer.mutations") + ":", COLUMN_0);
        layout.newLine();
        IGenome genome = individual.getGenome();
        ISpeciesRoot root = genome.getSpeciesRoot();
        IAlleleSpecies species = genome.getPrimary();
        EntityPlayer player = Minecraft.getMinecraft().player;
        IBreedingTracker tracker = root.getBreedingTracker(player.world, player.getGameProfile());
        int x = 0;
        for (IMutation mutation : root.getCombinations(species)) {
            if (tracker.isDiscovered(mutation)) {
                drawMutationInfo(gui, mutation, species, COLUMN_0 + x, tracker);
            } else if (!mutation.isSecret()) {
                drawUnknownMutation(gui, mutation, COLUMN_0 + x, tracker);
            } else {
                continue;
            }
            x += 50;
            if (x >= 200) {
                x = 0;
                layout.newLine(16);
            }
        }
    }

    private void drawMutationInfo(GuiCraftsmanEye gui, IMutation mutation, IAllele species,
                                  int x, IBreedingTracker tracker) {
        Map<String, ItemStack> icons = mutation.getRoot().getAlyzerPlugin().getIconStacks();
        IAllele partner = mutation.getPartner(species);
        gui.renderPageItem(icons.get(partner.getUID()), x, gui.getTextLayout().getLineY());
        drawProbabilityArrow(gui, mutation, x + 18, gui.getTextLayout().getLineY() + 4, tracker);
        IAllele result = mutation.getTemplate()[0];
        gui.renderPageItem(icons.get(result.getUID()), x + 33, gui.getTextLayout().getLineY());
    }

    private void drawUnknownMutation(GuiCraftsmanEye gui, IMutation mutation,
                                     int x, IBreedingTracker tracker) {
        int y = gui.getTextLayout().getLineY();
        gui.drawAtlasRegion(x, y, 78, 240, 16, 16);
        drawProbabilityArrow(gui, mutation, x + 18, y + 4, tracker);
        gui.drawAtlasRegion(x + 32, y, 78, 240, 16, 16);
    }

    private void drawProbabilityArrow(GuiCraftsmanEye gui, IMutation mutation,
                                      int x, int y, IBreedingTracker tracker) {
        int textureX;
        switch (EnumMutateChance.rateChance(mutation.getBaseChance())) {
            case HIGHEST:
                textureX = 100;
                break;
            case HIGHER:
                textureX = 115;
                break;
            case HIGH:
                textureX = 130;
                break;
            case NORMAL:
                textureX = 145;
                break;
            case LOW:
                textureX = 160;
                break;
            default:
                textureX = 175;
        }
        gui.drawAtlasRegion(x, y, textureX, 247, 15, 9);
        if (tracker.isResearched(mutation)) {
            gui.getPageFont().drawString("+", gui.getGuiLeftValue() + x + 9,
                gui.getGuiTopValue() + y + 1, 0);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void drawClassification(GuiCraftsmanEye gui, IIndividual individual) {
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(screenColor());
        layout.drawLine(I18n.format("for.gui.alyzer.classification") + ":", COLUMN_0);
        layout.newLine();
        Stack<IClassification> hierarchy = new Stack<>();
        for (IClassification classification = individual.getGenome().getPrimary().getBranch();
             classification != null; classification = classification.getParent()) {
            if (!classification.getScientific().isEmpty()) {
                hierarchy.push(classification);
            }
        }
        boolean overcrowded = hierarchy.size() > 5;
        int x = COLUMN_0;
        IClassification group = null;
        while (!hierarchy.isEmpty()) {
            group = hierarchy.pop();
            if (!overcrowded || !group.getLevel().isDroppable()) {
                layout.drawLine(group.getScientific(), x, group.getLevel().getColour());
                layout.drawLine(group.getLevel().name(), 170, group.getLevel().getColour());
                layout.newLineCompressed();
                x += 12;
            }
        }
        String binomial = individual.getGenome().getPrimary().getBinomial();
        if (group != null && group.getLevel() == IClassification.EnumClassLevel.GENUS) {
            binomial = group.getScientific().substring(0, 1) + ". "
                + binomial.toLowerCase(Locale.ENGLISH);
        }
        layout.drawLine(binomial, x, 15445637);
        layout.drawLine("SPECIES", 170, 15445637);
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.alyzer.authority") + ": "
            + individual.getGenome().getPrimary().getAuthority(), COLUMN_0);
        if (AlleleManager.alleleRegistry.isBlacklisted(individual.getIdent())) {
            String extinct = ">> " + I18n.format("for.gui.alyzer.extinct").toUpperCase(Locale.ENGLISH) + " <<";
            gui.getPageFont().drawStringWithShadow(extinct,
                gui.getGuiLeftValue() + 200 - gui.getPageFont().getStringWidth(extinct),
                gui.getGuiTopValue() + layout.getLineY(), colorCoding(true));
        }
        layout.newLine();
        String description = individual.getGenome().getPrimary().getDescription();
        if (!StringUtils.isBlank(description) && !description.startsWith("for.description.")) {
            String[] tokens = description.split("\\|");
            layout.drawSplitLine(tokens[0], COLUMN_0, 200, 6710886);
            if (tokens.length > 1) {
                String signature = "- " + tokens[1];
                gui.getPageFont().drawStringWithShadow(signature,
                    gui.getGuiLeftValue() + 210 - gui.getPageFont().getStringWidth(signature),
                    gui.getGuiTopValue() + 131, 10079282);
            }
        } else {
            layout.drawSplitLine(I18n.format("for.gui.alyzer.nodescription"),
                COLUMN_0, 200, 6710886);
        }
    }

    private void drawBeePageOne(GuiCraftsmanEye gui, ItemStack specimen) {
        IBee bee = BeeManager.beeRoot.getMember(specimen);
        if (bee == null) {
            return;
        }
        EnumBeeType type = BeeManager.beeRoot.getType(specimen);
        if (type == null) {
            return;
        }

        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        layout.drawLine(I18n.format("for.gui.active"), COLUMN_1);
        layout.drawLine(I18n.format("for.gui.inactive"), COLUMN_2);
        layout.newLine();
        layout.newLine();
        String customPrimaryKey = "for.bees.custom.beealyzer." + type.getName() + "."
            + bee.getGenome().getPrimary().getUnlocalizedName().replace("for.bees.species.", "");
        String customSecondaryKey = "for.bees.custom.beealyzer." + type.getName() + "."
            + bee.getGenome().getSecondary().getUnlocalizedName().replace("for.bees.species.", "");
        drawSpeciesRow(gui, I18n.format("for.gui.species"), bee, EnumBeeChromosome.SPECIES,
            customName(customPrimaryKey), customName(customSecondaryKey));
        layout.newLine();
        drawChromosomeRow(gui, I18n.format("for.gui.lifespan"), bee, EnumBeeChromosome.LIFESPAN);
        layout.newLine();
        drawChromosomeRow(gui, I18n.format("for.gui.speed"), bee, EnumBeeChromosome.SPEED);
        layout.newLine();
        drawChromosomeRow(gui, I18n.format("for.gui.pollination"), bee, EnumBeeChromosome.FLOWERING);
        layout.newLine();
        drawChromosomeRow(gui, I18n.format("for.gui.flowers"), bee, EnumBeeChromosome.FLOWER_PROVIDER);
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.fertility"), COLUMN_0);
        IAlleleInteger primaryFertility = (IAlleleInteger) bee.getGenome()
            .getActiveAllele(EnumBeeChromosome.FERTILITY);
        IAlleleInteger secondaryFertility = (IAlleleInteger) bee.getGenome()
            .getInactiveAllele(EnumBeeChromosome.FERTILITY);
        drawFertilityInfo(gui, primaryFertility.getValue(), COLUMN_1,
            colorCoding(primaryFertility.isDominant()), 0);
        drawFertilityInfo(gui, secondaryFertility.getValue(), COLUMN_2,
            colorCoding(secondaryFertility.isDominant()), 0);
        layout.newLine();
        drawChromosomeRow(gui, I18n.format("for.gui.area"), bee, EnumBeeChromosome.TERRITORY);
        layout.newLine();
        drawChromosomeRow(gui, I18n.format("for.gui.effect"), bee, EnumBeeChromosome.EFFECT);
        layout.newLine();
    }

    private void drawBeePageTwo(GuiCraftsmanEye gui, ItemStack specimen) {
        IBee bee = BeeManager.beeRoot.getMember(specimen);
        if (bee == null) {
            return;
        }
        EnumBeeType type = BeeManager.beeRoot.getType(specimen);
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        layout.drawLine(I18n.format("for.gui.active"), COLUMN_1);
        layout.drawLine(I18n.format("for.gui.inactive"), COLUMN_2);
        layout.newLine();
        drawRow(gui, I18n.format("for.gui.climate"),
            AlleleManager.climateHelper.toDisplay(bee.getGenome().getPrimary().getTemperature()),
            AlleleManager.climateHelper.toDisplay(bee.getGenome().getSecondary().getTemperature()),
            bee, EnumBeeChromosome.SPECIES);
        layout.newLine();
        IAlleleTolerance activeTemperature = (IAlleleTolerance) bee.getGenome()
            .getActiveAllele(EnumBeeChromosome.TEMPERATURE_TOLERANCE);
        IAlleleTolerance inactiveTemperature = (IAlleleTolerance) bee.getGenome()
            .getInactiveAllele(EnumBeeChromosome.TEMPERATURE_TOLERANCE);
        layout.drawLine("  " + I18n.format("for.gui.tolerance"), COLUMN_0);
        drawToleranceInfo(gui, activeTemperature, COLUMN_1);
        drawToleranceInfo(gui, inactiveTemperature, COLUMN_2);
        layout.newLine(16);
        drawRow(gui, I18n.format("for.gui.humidity"),
            AlleleManager.climateHelper.toDisplay(bee.getGenome().getPrimary().getHumidity()),
            AlleleManager.climateHelper.toDisplay(bee.getGenome().getSecondary().getHumidity()),
            bee, EnumBeeChromosome.SPECIES);
        layout.newLine();
        IAlleleTolerance activeHumidity = (IAlleleTolerance) bee.getGenome()
            .getActiveAllele(EnumBeeChromosome.HUMIDITY_TOLERANCE);
        IAlleleTolerance inactiveHumidity = (IAlleleTolerance) bee.getGenome()
            .getInactiveAllele(EnumBeeChromosome.HUMIDITY_TOLERANCE);
        layout.drawLine("  " + I18n.format("for.gui.tolerance"), COLUMN_0);
        drawToleranceInfo(gui, activeHumidity, COLUMN_1);
        drawToleranceInfo(gui, inactiveHumidity, COLUMN_2);
        layout.newLine(16);

        String yes = I18n.format("for.yes");
        String no = I18n.format("for.no");
        String activeDiurnal;
        String activeNocturnal;
        if (bee.getGenome().getNeverSleeps()) {
            activeDiurnal = yes;
            activeNocturnal = yes;
        } else {
            activeNocturnal = bee.getGenome().getPrimary().isNocturnal() ? yes : no;
            activeDiurnal = bee.getGenome().getPrimary().isNocturnal() ? no : yes;
        }
        String inactiveDiurnal;
        String inactiveNocturnal;
        if (((AlleleBoolean) bee.getGenome().getInactiveAllele(EnumBeeChromosome.NEVER_SLEEPS)).getValue()) {
            inactiveDiurnal = yes;
            inactiveNocturnal = yes;
        } else {
            inactiveNocturnal = bee.getGenome().getSecondary().isNocturnal() ? yes : no;
            inactiveDiurnal = bee.getGenome().getSecondary().isNocturnal() ? no : yes;
        }
        layout.drawLine(I18n.format("for.gui.diurnal"), COLUMN_0);
        layout.drawLine(activeDiurnal, COLUMN_1, colorCoding(false));
        layout.drawLine(inactiveDiurnal, COLUMN_2, colorCoding(false));
        layout.newLineCompressed();
        layout.drawLine(I18n.format("for.gui.nocturnal"), COLUMN_0);
        layout.drawLine(activeNocturnal, COLUMN_1, colorCoding(false));
        layout.drawLine(inactiveNocturnal, COLUMN_2, colorCoding(false));
        layout.newLineCompressed();
        String active = StringUtil.readableBoolean(bee.getGenome().getToleratesRain(), yes, no);
        String inactive = StringUtil.readableBoolean(
            ((AlleleBoolean) bee.getGenome().getInactiveAllele(EnumBeeChromosome.TOLERATES_RAIN)).getValue(),
            yes, no);
        drawRow(gui, I18n.format("for.gui.flyer"), active, inactive,
            bee, EnumBeeChromosome.TOLERATES_RAIN);
        layout.newLineCompressed();
        active = StringUtil.readableBoolean(bee.getGenome().getCaveDwelling(), yes, no);
        inactive = StringUtil.readableBoolean(
            ((AlleleBoolean) bee.getGenome().getInactiveAllele(EnumBeeChromosome.CAVE_DWELLING)).getValue(),
            yes, no);
        drawRow(gui, I18n.format("for.gui.cave"), active, inactive,
            bee, EnumBeeChromosome.CAVE_DWELLING);
        layout.newLine();
        if (type == EnumBeeType.PRINCESS || type == EnumBeeType.QUEEN) {
            String key = bee.isNatural() ? "for.bees.stock.pristine" : "for.bees.stock.ignoble";
            layout.drawCenteredLine(I18n.format(key), 8, 208,
                getColor("gui.beealyzer.binomial", 0x14D50B));
        }
        if (bee.getGeneration() >= 0) {
            layout.newLineCompressed();
            layout.drawCenteredLine(I18n.format("for.gui.beealyzer.generations", bee.getGeneration()),
                8, 208, getColor("gui.beealyzer.binomial", 0x14D50B));
        }
    }

    private void drawBeePageThree(GuiCraftsmanEye gui, ItemStack specimen) {
        IBee bee = BeeManager.beeRoot.getMember(specimen);
        if (bee == null) {
            return;
        }
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        layout.drawLine(I18n.format("for.gui.beealyzer.produce") + ":", COLUMN_0);
        layout.newLine();
        int x = COLUMN_0;
        for (ItemStack stack : bee.getProduceList()) {
            gui.renderPageItem(stack, x, layout.getLineY());
            x += 18;
            if (x > 148) {
                x = COLUMN_0;
                layout.newLine();
            }
        }
        layout.newLine();
        layout.newLine();
        layout.newLine();
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.beealyzer.specialty") + ":", COLUMN_0);
        layout.newLine();
        x = COLUMN_0;
        for (ItemStack stack : bee.getSpecialtyList()) {
            gui.renderPageItem(stack, x, layout.getLineY());
            x += 18;
            if (x > 148) {
                x = COLUMN_0;
                layout.newLine();
            }
        }
    }

    private void drawTreePageOne(GuiCraftsmanEye gui, ItemStack specimen) {
        ITree tree = TreeManager.treeRoot.getMember(specimen);
        if (tree == null) {
            return;
        }
        EnumGermlingType type = TreeManager.treeRoot.getType(specimen);
        if (type == null) {
            return;
        }
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        layout.drawLine(I18n.format("for.gui.active"), COLUMN_1);
        layout.drawLine(I18n.format("for.gui.inactive"), COLUMN_2);
        layout.newLine();
        layout.newLine();
        String customPrimaryKey = "trees.custom.treealyzer." + type.getName() + "."
            + tree.getGenome().getPrimary().getUnlocalizedName().replace("for.trees.species.", "");
        String customSecondaryKey = "trees.custom.treealyzer." + type.getName() + "."
            + tree.getGenome().getSecondary().getUnlocalizedName().replace("for.trees.species.", "");
        drawSpeciesRow(gui, I18n.format("for.gui.species"), tree, EnumTreeChromosome.SPECIES,
            customName(customPrimaryKey), customName(customSecondaryKey));
        layout.newLine();
        drawChromosomeRow(gui, I18n.format("for.gui.saplings"), tree, EnumTreeChromosome.FERTILITY);
        layout.newLineCompressed();
        drawChromosomeRow(gui, I18n.format("for.gui.maturity"), tree, EnumTreeChromosome.MATURATION);
        layout.newLineCompressed();
        drawChromosomeRow(gui, I18n.format("for.gui.height"), tree, EnumTreeChromosome.HEIGHT);
        layout.newLineCompressed();
        IAlleleInteger activeGirth = (IAlleleInteger) tree.getGenome()
            .getActiveAllele(EnumTreeChromosome.GIRTH);
        IAlleleInteger inactiveGirth = (IAlleleInteger) tree.getGenome()
            .getInactiveAllele(EnumTreeChromosome.GIRTH);
        layout.drawLine(I18n.format("for.gui.girth"), COLUMN_0);
        drawLine(gui, String.format("%sx%s", activeGirth.getValue(), activeGirth.getValue()),
            COLUMN_1, tree, EnumTreeChromosome.GIRTH, false);
        drawLine(gui, String.format("%sx%s", inactiveGirth.getValue(), inactiveGirth.getValue()),
            COLUMN_2, tree, EnumTreeChromosome.GIRTH, true);
        layout.newLineCompressed();
        drawChromosomeRow(gui, I18n.format("for.gui.yield"), tree, EnumTreeChromosome.YIELD);
        layout.newLineCompressed();
        drawChromosomeRow(gui, I18n.format("for.gui.sappiness"), tree, EnumTreeChromosome.SAPPINESS);
        layout.newLineCompressed();
        drawChromosomeRow(gui, I18n.format("for.gui.effect"), tree, EnumTreeChromosome.EFFECT);
    }

    private void drawTreePageTwo(GuiCraftsmanEye gui, ItemStack specimen) {
        ITree tree = TreeManager.treeRoot.getMember(specimen);
        if (tree == null) {
            return;
        }
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        int primarySpeciesColor = colorCoding(tree.getGenome().getPrimary().isDominant());
        int secondarySpeciesColor = colorCoding(tree.getGenome().getSecondary().isDominant());
        layout.drawLine(I18n.format("for.gui.active"), COLUMN_1);
        layout.drawLine(I18n.format("for.gui.inactive"), COLUMN_2);
        layout.newLine();
        layout.newLine();
        String yes = I18n.format("for.yes");
        String no = I18n.format("for.no");
        String activeFireproof = StringUtil.readableBoolean(tree.getGenome().getFireproof(), yes, no);
        String inactiveFireproof = StringUtil.readableBoolean(
            ((AlleleBoolean) tree.getGenome().getInactiveAllele(EnumTreeChromosome.FIREPROOF)).getValue(),
            yes, no);
        drawRow(gui, I18n.format("for.gui.fireproof"), activeFireproof, inactiveFireproof,
            tree, EnumTreeChromosome.FIREPROOF);
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.native"), COLUMN_0);
        layout.drawLine(I18n.format("for.gui."
                + tree.getGenome().getPrimary().getPlantType().toString().toLowerCase(Locale.ENGLISH)),
            COLUMN_1, primarySpeciesColor);
        layout.drawLine(I18n.format("for.gui."
                + tree.getGenome().getSecondary().getPlantType().toString().toLowerCase(Locale.ENGLISH)),
            COLUMN_2, secondarySpeciesColor);
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.supports"), COLUMN_0);
        List<IFruitFamily> primaryFamilies = new ArrayList<>(
            tree.getGenome().getPrimary().getSuitableFruit());
        List<IFruitFamily> secondaryFamilies = new ArrayList<>(
            tree.getGenome().getSecondary().getSuitableFruit());
        int familyRows = Math.max(primaryFamilies.size(), secondaryFamilies.size());
        for (int i = 0; i < familyRows; i++) {
            if (i > 0) {
                layout.newLineCompressed();
            }
            if (primaryFamilies.size() > i) {
                layout.drawLine(primaryFamilies.get(i).getName(), COLUMN_1, primarySpeciesColor);
            }
            if (secondaryFamilies.size() > i) {
                layout.drawLine(secondaryFamilies.get(i).getName(), COLUMN_2, secondarySpeciesColor);
            }
        }
        layout.newLine();
        int primaryFruitColor = colorCoding(tree.getGenome()
            .getActiveAllele(EnumTreeChromosome.FRUITS).isDominant());
        int secondaryFruitColor = colorCoding(tree.getGenome()
            .getInactiveAllele(EnumTreeChromosome.FRUITS).isDominant());
        layout.drawLine(I18n.format("for.gui.fruits"), COLUMN_0);
        IAllele primaryFruitAllele = tree.getGenome().getActiveAllele(EnumTreeChromosome.FRUITS);
        String strike = !tree.canBearFruit() && primaryFruitAllele != AlleleFruits.fruitNone
            ? TextFormatting.STRIKETHROUGH.toString() : "";
        layout.drawLine(strike + tree.getGenome().getFruitProvider().getDescription(),
            COLUMN_1, primaryFruitColor);
        IAlleleFruit secondaryFruit = (IAlleleFruit) tree.getGenome()
            .getInactiveAllele(EnumTreeChromosome.FRUITS);
        strike = !tree.getGenome().getSecondary().getSuitableFruit()
            .contains(secondaryFruit.getProvider().getFamily())
            && secondaryFruit != AlleleFruits.fruitNone
            ? TextFormatting.STRIKETHROUGH.toString() : "";
        layout.drawLine(strike + secondaryFruit.getProvider().getDescription(),
            COLUMN_2, secondaryFruitColor);
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.family"), COLUMN_0);
        IFruitFamily primaryFamily = tree.getGenome().getFruitProvider().getFamily();
        IFruitFamily secondaryFamily = secondaryFruit.getProvider().getFamily();
        if (primaryFamily != null && !primaryFamily.getUID().equals(EnumFruitFamily.NONE.getUID())) {
            layout.drawLine(primaryFamily.getName(), COLUMN_1, primaryFruitColor);
        }
        if (secondaryFamily != null && !secondaryFamily.getUID().equals(EnumFruitFamily.NONE.getUID())) {
            layout.drawLine(secondaryFamily.getName(), COLUMN_2, secondaryFruitColor);
        }
    }

    private void drawTreePageThree(GuiCraftsmanEye gui, ItemStack specimen) {
        ITree tree = TreeManager.treeRoot.getMember(specimen);
        if (tree == null) {
            return;
        }
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        layout.drawLine(I18n.format("for.gui.beealyzer.produce") + ":", COLUMN_0);
        layout.newLine();
        int x = COLUMN_0;
        for (ItemStack stack : tree.getProducts().keySet()) {
            gui.renderPageItem(stack, x, layout.getLineY());
            x += 18;
            if (x > 148) {
                x = COLUMN_0;
                layout.newLine();
            }
        }
        layout.newLine();
        layout.newLine();
        layout.newLine();
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.beealyzer.specialty") + ":", COLUMN_0);
        layout.newLine();
        x = COLUMN_0;
        for (ItemStack stack : tree.getSpecialties().keySet()) {
            gui.renderPageDecoration(stack, x, layout.getLineY());
            x += 18;
            if (x > 148) {
                x = COLUMN_0;
                layout.newLine();
            }
        }
    }

    private void drawButterflyPageOne(GuiCraftsmanEye gui, ItemStack specimen) {
        IButterfly butterfly = ButterflyManager.butterflyRoot.getMember(specimen);
        if (butterfly == null) {
            return;
        }
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        layout.drawLine(I18n.format("for.gui.active"), COLUMN_1);
        layout.drawLine(I18n.format("for.gui.inactive"), COLUMN_2);
        layout.newLine();
        layout.newLine();
        drawSpeciesRow(gui, I18n.format("for.gui.species"), butterfly,
            EnumButterflyChromosome.SPECIES, null, null);
        layout.newLine();
        drawRow(gui, I18n.format("for.gui.size"),
            butterfly.getGenome().getActiveAllele(EnumButterflyChromosome.SIZE).getAlleleName(),
            butterfly.getGenome().getInactiveAllele(EnumButterflyChromosome.SIZE).getAlleleName(),
            butterfly, EnumButterflyChromosome.SPEED);
        layout.newLine();
        drawRow(gui, I18n.format("for.gui.lifespan"),
            butterfly.getGenome().getActiveAllele(EnumButterflyChromosome.LIFESPAN).getAlleleName(),
            butterfly.getGenome().getInactiveAllele(EnumButterflyChromosome.LIFESPAN).getAlleleName(),
            butterfly, EnumButterflyChromosome.LIFESPAN);
        layout.newLine();
        drawRow(gui, I18n.format("for.gui.speed"),
            butterfly.getGenome().getActiveAllele(EnumButterflyChromosome.SPEED).getAlleleName(),
            butterfly.getGenome().getInactiveAllele(EnumButterflyChromosome.SPEED).getAlleleName(),
            butterfly, EnumButterflyChromosome.SPEED);
        layout.newLine();
        drawRow(gui, I18n.format("for.gui.metabolism"),
            GenericRatings.rateMetabolism(butterfly.getGenome().getMetabolism()),
            GenericRatings.rateMetabolism(((IAlleleInteger) butterfly.getGenome()
                .getInactiveAllele(EnumButterflyChromosome.METABOLISM)).getValue()),
            butterfly, EnumButterflyChromosome.METABOLISM);
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.fertility"), COLUMN_0);
        drawFertilityInfo(gui, butterfly.getGenome().getFertility(), COLUMN_1,
            colorCoding(butterfly.getGenome().getActiveAllele(EnumButterflyChromosome.FERTILITY)
                .isDominant()), 8);
        drawFertilityInfo(gui, ((IAlleleInteger) butterfly.getGenome()
                .getInactiveAllele(EnumButterflyChromosome.FERTILITY)).getValue(), COLUMN_2,
            colorCoding(butterfly.getGenome().getInactiveAllele(EnumButterflyChromosome.FERTILITY)
                .isDominant()), 8);
        layout.newLine();
        drawRow(gui, I18n.format("for.gui.flowers"),
            butterfly.getGenome().getFlowerProvider().getDescription(),
            ((IAlleleFlowers) butterfly.getGenome()
                .getInactiveAllele(EnumButterflyChromosome.FLOWER_PROVIDER)).getProvider().getDescription(),
            butterfly, EnumButterflyChromosome.FLOWER_PROVIDER);
        layout.newLine();
        drawRow(gui, I18n.format("for.gui.effect"), butterfly.getGenome().getEffect().getAlleleName(),
            butterfly.getGenome().getInactiveAllele(EnumButterflyChromosome.EFFECT).getAlleleName(),
            butterfly, EnumButterflyChromosome.EFFECT);
        layout.newLine();
    }

    private void drawButterflyPageTwo(GuiCraftsmanEye gui, ItemStack specimen) {
        IButterfly butterfly = ButterflyManager.butterflyRoot.getMember(specimen);
        if (butterfly == null) {
            return;
        }
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        layout.drawLine(I18n.format("for.gui.active"), COLUMN_1);
        layout.drawLine(I18n.format("for.gui.inactive"), COLUMN_2);
        layout.newLine();
        layout.newLine();
        drawRow(gui, I18n.format("for.gui.climate"),
            AlleleManager.climateHelper.toDisplay(butterfly.getGenome().getPrimary().getTemperature()),
            AlleleManager.climateHelper.toDisplay(butterfly.getGenome().getPrimary().getTemperature()),
            butterfly, EnumButterflyChromosome.SPECIES);
        layout.newLine();
        IAlleleTolerance activeTemperature = (IAlleleTolerance) butterfly.getGenome()
            .getActiveAllele(EnumButterflyChromosome.TEMPERATURE_TOLERANCE);
        IAlleleTolerance inactiveTemperature = (IAlleleTolerance) butterfly.getGenome()
            .getInactiveAllele(EnumButterflyChromosome.TEMPERATURE_TOLERANCE);
        layout.drawLine("  " + I18n.format("for.gui.tolerance"), COLUMN_0);
        drawToleranceInfo(gui, activeTemperature, COLUMN_1);
        drawToleranceInfo(gui, inactiveTemperature, COLUMN_2);
        layout.newLine();
        drawRow(gui, I18n.format("for.gui.humidity"),
            AlleleManager.climateHelper.toDisplay(butterfly.getGenome().getPrimary().getHumidity()),
            AlleleManager.climateHelper.toDisplay(butterfly.getGenome().getPrimary().getHumidity()),
            butterfly, EnumButterflyChromosome.SPECIES);
        layout.newLine();
        IAlleleTolerance activeHumidity = (IAlleleTolerance) butterfly.getGenome()
            .getActiveAllele(EnumButterflyChromosome.HUMIDITY_TOLERANCE);
        IAlleleTolerance inactiveHumidity = (IAlleleTolerance) butterfly.getGenome()
            .getInactiveAllele(EnumButterflyChromosome.HUMIDITY_TOLERANCE);
        layout.drawLine("  " + I18n.format("for.gui.tolerance"), COLUMN_0);
        drawToleranceInfo(gui, activeHumidity, COLUMN_1);
        drawToleranceInfo(gui, inactiveHumidity, COLUMN_2);
        layout.newLine();
        layout.newLine();

        String yes = I18n.format("for.yes");
        String no = I18n.format("for.no");
        String activeDiurnal;
        String activeNocturnal;
        if (butterfly.getGenome().getNocturnal()) {
            activeDiurnal = yes;
            activeNocturnal = yes;
        } else {
            activeNocturnal = butterfly.getGenome().getPrimary().isNocturnal() ? yes : no;
            activeDiurnal = butterfly.getGenome().getPrimary().isNocturnal() ? no : yes;
        }
        String inactiveDiurnal;
        String inactiveNocturnal;
        if (((AlleleBoolean) butterfly.getGenome()
                .getInactiveAllele(EnumButterflyChromosome.NOCTURNAL)).getValue()) {
            inactiveDiurnal = yes;
            inactiveNocturnal = yes;
        } else {
            inactiveNocturnal = butterfly.getGenome().getSecondary().isNocturnal() ? yes : no;
            inactiveDiurnal = butterfly.getGenome().getSecondary().isNocturnal() ? no : yes;
        }
        layout.drawLine(I18n.format("for.gui.diurnal"), COLUMN_0);
        layout.drawLine(activeDiurnal, COLUMN_1, colorCoding(false));
        layout.drawLine(inactiveDiurnal, COLUMN_2, colorCoding(false));
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.nocturnal"), COLUMN_0);
        layout.drawLine(activeNocturnal, COLUMN_1, colorCoding(false));
        layout.drawLine(inactiveNocturnal, COLUMN_2, colorCoding(false));
        layout.newLine();
        String active = StringUtil.readableBoolean(butterfly.getGenome().getTolerantFlyer(), yes, no);
        String inactive = StringUtil.readableBoolean(((AlleleBoolean) butterfly.getGenome()
            .getInactiveAllele(EnumButterflyChromosome.TOLERANT_FLYER)).getValue(), yes, no);
        drawRow(gui, I18n.format("for.gui.flyer"), active, inactive,
            butterfly, EnumButterflyChromosome.TOLERANT_FLYER);
        layout.newLine();
        active = StringUtil.readableBoolean(butterfly.getGenome().getFireResist(), yes, no);
        inactive = StringUtil.readableBoolean(((AlleleBoolean) butterfly.getGenome()
            .getInactiveAllele(EnumButterflyChromosome.FIRE_RESIST)).getValue(), yes, no);
        drawRow(gui, I18n.format("for.gui.fireresist"), active, inactive,
            butterfly, EnumButterflyChromosome.FIRE_RESIST);
    }

    private void drawButterflyPageThree(GuiCraftsmanEye gui, ItemStack specimen) {
        IButterfly butterfly = ButterflyManager.butterflyRoot.getMember(specimen);
        if (butterfly == null) {
            return;
        }
        CraftsmanEyeLayout layout = gui.getTextLayout();
        layout.startPage(COLUMN_0, COLUMN_1, COLUMN_2, screenColor());
        layout.drawLine(I18n.format("for.gui.loot.butterfly") + ":", COLUMN_0);
        layout.newLine();
        int x = COLUMN_0;
        for (ItemStack stack : butterfly.getGenome().getPrimary().getButterflyLoot().keySet()) {
            gui.renderPageDecoration(stack, x, layout.getLineY());
            x += 18;
            if (x > 148) {
                x = COLUMN_0;
                layout.newLine();
            }
        }
        layout.newLine();
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.loot.caterpillar") + ":", COLUMN_0);
        layout.newLine();
        x = COLUMN_0;
        for (ItemStack stack : butterfly.getGenome().getPrimary().getCaterpillarLoot().keySet()) {
            gui.renderPageDecoration(stack, x, layout.getLineY());
            x += 18;
            if (x > 148) {
                x = COLUMN_0;
                layout.newLine();
            }
        }
        layout.newLine();
        layout.newLine();
        layout.drawLine(I18n.format("for.gui.loot.cocoon") + ":", COLUMN_0);
        layout.newLine();
        x = COLUMN_0;
        for (ItemStack stack : butterfly.getGenome().getCocoon().getCocoonLoot().keySet()) {
            gui.renderPageDecoration(stack, x, layout.getLineY());
            x += 18;
            if (x > 148) {
                x = COLUMN_0;
                layout.newLine();
            }
        }
    }
}
