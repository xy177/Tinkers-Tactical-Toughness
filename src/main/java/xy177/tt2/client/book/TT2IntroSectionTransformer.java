package xy177.tt2.client.book;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.tconstruct.library.book.sectiontransformer.SectionTransformer;
import xy177.tt2.config.TT2Config;

@SideOnly(Side.CLIENT)
public class TT2IntroSectionTransformer extends SectionTransformer {

    public TT2IntroSectionTransformer() {
        super("intro");
    }

    @Override
    public void transform(BookData book, SectionData section) {
        if (!TT2Config.enableDefenseDamage) {
            return;
        }

        PageData page = new PageData(true);
        page.source = section.source;
        page.parent = section;
        page.name = "tt2_defense_damage";
        page.type = "text";
        page.data = "intro/tt2_defense_damage.json";
        page.load();
        section.pages.add(page);
    }
}
