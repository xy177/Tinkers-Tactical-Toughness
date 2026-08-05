package xy177.tt2.client.book;

import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.book.content.ContentListing;
import slimeknights.tconstruct.library.book.content.ContentModifier;
import slimeknights.tconstruct.library.book.sectiontransformer.SectionTransformer;
import slimeknights.tconstruct.library.modifiers.IModifier;
import xy177.tt2.TT2;
import xy177.tt2.init.TT2Items;
import xy177.tt2.modifiers.ModSpearLunge;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@SideOnly(Side.CLIENT)
public class TT2ModifierSectionTransformer extends SectionTransformer {

    public TT2ModifierSectionTransformer() {
        super("modifiers");
    }

    @Override
    public void transform(BookData book, SectionData section) {
        IModifier modifier = TinkerRegistry.getModifier(ModSpearLunge.ID);
        if (modifier == null || TT2Items.SPEAR == null) {
            return;
        }

        ContentModifier content = loadModifierContent();
        if (content == null) {
            return;
        }

        PageData page = new PageData(true);
        page.source = section.source;
        page.parent = section;
        page.name = ModSpearLunge.ID;
        page.type = ContentModifier.ID;
        page.content = content;
        page.load();
        section.pages.add(page);

        ContentListing listing = findListing(section);
        if (listing != null) {
            listing.addEntry(modifier.getLocalizedName(), page);
        }
    }

    @Nullable
    private ContentListing findListing(SectionData section) {
        for (PageData page : section.pages) {
            if (page.content instanceof ContentListing) {
                return (ContentListing) page.content;
            }
        }
        return null;
    }

    @Nullable
    private ContentModifier loadModifierContent() {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        String language = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();

        ContentModifier content = readModifierContent(resourceManager, language, false);
        if (content == null && !"en_us".equals(language)) {
            content = readModifierContent(resourceManager, "en_us", true);
        }
        return content;
    }

    @Nullable
    private ContentModifier readModifierContent(IResourceManager resourceManager, String language,
                                                boolean logFailure) {
        ResourceLocation location = new ResourceLocation(
            TT2.MOD_ID,
            "book/" + language + "/modifiers/tt2_lunge.json"
        );

        try (IResource resource = resourceManager.getResource(location);
             Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return BookLoader.GSON.fromJson(reader, ContentModifier.class);
        } catch (IOException | JsonParseException e) {
            if (logFailure) {
                TT2.logger.warn("Failed to load TT2 modifier book page {}", location, e);
            }
            return null;
        }
    }
}
