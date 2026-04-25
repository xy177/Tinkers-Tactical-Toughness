package xy177.tt2.client.book;

import c4.conarm.lib.book.content.ContentArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.tconstruct.library.book.sectiontransformer.SectionTransformer;
import xy177.tt2.client.book.content.TT2ContentScoutArmor;
import xy177.tt2.config.TT2Config;

@SideOnly(Side.CLIENT)
public class TT2ArmorySectionTransformer extends SectionTransformer {

    public TT2ArmorySectionTransformer() {
        super("armory");
    }

    @Override
    public void transform(BookData book, SectionData section) {
        if (!TT2Config.enableScoutArmor && !TT2Config.enableDefenseDamage) {
            return;
        }

        int insertIndex = 0;

        if (TT2Config.enableDefenseDamage) {
            PageData defenseDamagePage = new PageData(true);
            defenseDamagePage.source = section.source;
            defenseDamagePage.parent = section;
            defenseDamagePage.name = "tt2_defense_damage";
            defenseDamagePage.type = "text";
            defenseDamagePage.data = "armory/tt2_defense_damage.json";
            defenseDamagePage.load();
            section.pages.add(insertIndex, defenseDamagePage);
            insertIndex++;
        }

        if (TT2Config.enableScoutArmor) {
            PageData scoutPage = new PageData(true);
            scoutPage.source = section.source;
            scoutPage.parent = section;
            scoutPage.name = "scout_armor";
            scoutPage.type = TT2ContentScoutArmor.ID;
            scoutPage.data = "armory/scout_armor.json";
            scoutPage.load();

            for (int i = 0; i < section.pages.size(); i++) {
                if ("armory".equals(section.pages.get(i).name)) {
                    insertIndex = i + 1;
                    break;
                }
            }
            section.pages.add(insertIndex, scoutPage);
        }
    }
}
