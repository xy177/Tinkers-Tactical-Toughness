package xy177.tt2.client.book.content;

import c4.conarm.common.ConstructsRegistry;
import c4.conarm.lib.armor.ArmorCore;
import c4.conarm.lib.armor.ArmorPart;
import net.minecraft.item.ItemStack;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementText;
import slimeknights.mantle.util.ItemStackList;
import slimeknights.tconstruct.library.book.TinkerPage;
import slimeknights.tconstruct.library.book.content.ContentTool;
import slimeknights.tconstruct.library.book.elements.ElementTinkerItem;
import slimeknights.tconstruct.library.materials.Material;
import xy177.tt2.init.TT2Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TT2ContentScoutArmor extends TinkerPage {

    public static final String ID = "tt2_scout_armor";

    public String title;
    public TextData[] text = new TextData[0];
    public String[] properties = new String[0];

    @Override
    public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
        addTitle(list, title);

        int x = 5;
        int textY = 16;
        int textHeight = GuiBook.PAGE_HEIGHT / 3 - 10;
        list.add(new ElementText(x, textY, GuiBook.PAGE_WIDTH - x * 2, textHeight, text));

        ImageData slotImage = ContentTool.IMG_SLOTS;
        int slotsX = GuiBook.PAGE_WIDTH - slotImage.width - 8;
        int slotsY = GuiBook.PAGE_HEIGHT - slotImage.height - 16;
        int centerX = slotsX + (slotImage.width - 16) / 2;
        int centerY = slotsY + 28;
        textY = slotsY - 6;

        if (properties.length > 0) {
            TextData propertyTitle = new TextData("属性:");
            propertyTitle.underlined = true;
            list.add(new ElementText(x, textY, 86 - x, GuiBook.PAGE_HEIGHT - textHeight - 20,
                new TextData[]{propertyTitle}));

            List<TextData> propertyText = new ArrayList<>();
            for (String property : properties) {
                propertyText.add(new TextData("•"));
                propertyText.add(new TextData(property));
                propertyText.add(new TextData("\n"));
            }

            textY += 10;
            list.add(new ElementText(x, textY, GuiBook.PAGE_WIDTH / 2 + 5,
                GuiBook.PAGE_HEIGHT - textHeight - 20, propertyText));
        }

        int[] itemOffsetX = {-21, -25, 0, 25, 21};
        int[] itemOffsetY = {22, -4, -25, -4, 22};

        list.add(new ElementImage(
            slotsX + (slotImage.width - ContentTool.IMG_TABLE.width) / 2,
            slotsY + 28, -1, -1, ContentTool.IMG_TABLE));
        list.add(new ElementImage(slotsX, slotsY, -1, -1, slotImage, book.appearance.slotColor));

        ItemStackList demoArmor = getDemoArmor();
        ElementTinkerItem armorDisplay = new ElementTinkerItem(centerX, centerY, 1f, demoArmor);
        armorDisplay.noTooltip = true;
        list.add(armorDisplay);
        list.add(new ElementImage(centerX - 3, centerY - 3, -1, -1, ContentTool.IMG_SLOT_1, 0xFFFFFF));

        Material coreMaterial = TT2Items.SCOUT_HELMET.getMaterialForPartForGuiRendering(0);
        ItemStackList coreStacks = ItemStackList.withSize(4);
        List<ArmorPart> coreParts = Arrays.asList(
            ConstructsRegistry.helmetCore,
            ConstructsRegistry.chestCore,
            ConstructsRegistry.leggingsCore,
            ConstructsRegistry.bootsCore
        );
        for (int i = 0; i < coreParts.size(); i++) {
            coreStacks.set(i, coreParts.get(i).getItemstackWithMaterial(coreMaterial));
        }

        ElementTinkerItem coreElement = new ElementTinkerItem(
            centerX + itemOffsetX[0],
            centerY + itemOffsetY[0],
            1f,
            coreStacks
        );
        coreElement.noTooltip = true;
        list.add(coreElement);

        ItemStack plate1 = ConstructsRegistry.armorPlate.getItemstackWithMaterial(
            TT2Items.SCOUT_HELMET.getMaterialForPartForGuiRendering(1));
        ElementTinkerItem plate1Element = new ElementTinkerItem(
            centerX + itemOffsetX[1],
            centerY + itemOffsetY[1],
            1f,
            plate1
        );
        plate1Element.noTooltip = true;
        list.add(plate1Element);

        ItemStack plate2 = ConstructsRegistry.armorPlate.getItemstackWithMaterial(
            TT2Items.SCOUT_HELMET.getMaterialForPartForGuiRendering(2));
        ElementTinkerItem plate2Element = new ElementTinkerItem(
            centerX + itemOffsetX[2],
            centerY + itemOffsetY[2],
            1f,
            plate2
        );
        plate2Element.noTooltip = true;
        list.add(plate2Element);
    }

    private ItemStackList getDemoArmor() {
        List<ArmorCore> armor = Arrays.asList(
            TT2Items.SCOUT_HELMET,
            TT2Items.SCOUT_CHESTPLATE,
            TT2Items.SCOUT_LEGGINGS,
            TT2Items.SCOUT_BOOTS
        );
        ItemStackList list = ItemStackList.withSize(armor.size());
        for (int i = 0; i < armor.size(); i++) {
            ArmorCore piece = armor.get(i);
            if (piece != null) {
                list.set(i, piece.buildItemForRenderingInGui());
            }
        }
        return list;
    }
}
