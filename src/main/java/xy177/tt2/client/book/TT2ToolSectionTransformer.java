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
import slimeknights.tconstruct.library.book.content.ContentListing;
import slimeknights.tconstruct.library.book.content.ContentTool;
import slimeknights.tconstruct.library.book.sectiontransformer.SectionTransformer;
import slimeknights.tconstruct.library.tools.ToolCore;
import xy177.tt2.TT2;
import xy177.tt2.init.TT2Items;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@SideOnly(Side.CLIENT)
public class TT2ToolSectionTransformer extends SectionTransformer {

    public TT2ToolSectionTransformer() {
        super("tools");
    }

    @Override
    public void transform(BookData book, SectionData section) {
        ContentListing listing = findListing(section);

        appendToolPage(section, listing, TT2Items.SWIFT_SHIELD, "swift_shield");
        appendToolPage(section, listing, TT2Items.HEAVY_SHIELD, "heavy_shield");
        appendToolPage(section, listing, TT2Items.NUNCHAKU, "nunchaku");
        appendToolPage(section, listing, TT2Items.DOPPELHANDER, "doppelhander");
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

    private void appendToolPage(SectionData section, @Nullable ContentListing listing,
                                @Nullable ToolCore tool, String pageName) {
        if (tool == null) {
            return;
        }

        ContentTool content = loadToolContent(pageName);
        if (content == null) {
            return;
        }

        PageData page = new PageData(true);
        page.source = section.source;
        page.parent = section;
        page.name = pageName;
        page.type = ContentTool.ID;
        page.content = content;
        page.load();

        section.pages.add(page);

        if (listing != null) {
            listing.addEntry(tool.getLocalizedName(), page);
        }
    }

    @Nullable
    private ContentTool loadToolContent(String pageName) {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        String language = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();

        ContentTool content = readToolContent(resourceManager, language, pageName, false);
        if (content == null && !"en_us".equals(language)) {
            content = readToolContent(resourceManager, "en_us", pageName, true);
        }
        return content;
    }

    @Nullable
    private ContentTool readToolContent(IResourceManager resourceManager, String language, String pageName,
                                        boolean logFailure) {
        ResourceLocation location = new ResourceLocation(TT2.MOD_ID, "book/" + language + "/tools/" + pageName + ".json");

        try (IResource resource = resourceManager.getResource(location);
             Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return BookLoader.GSON.fromJson(reader, ContentTool.class);
        } catch (IOException | JsonParseException e) {
            if (logFailure) {
                TT2.logger.warn("Failed to load TT2 tool book page {}", location, e);
            }
            return null;
        }
    }
}
