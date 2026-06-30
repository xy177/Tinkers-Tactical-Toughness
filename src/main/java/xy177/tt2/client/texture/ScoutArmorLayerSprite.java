package xy177.tt2.client.texture;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import xy177.tt2.risky.asm.NewTinkerTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

public class ScoutArmorLayerSprite extends TextureAtlasSprite {

    private final ResourceLocation baseLocation;

    public ScoutArmorLayerSprite(ResourceLocation baseLocation) {
        super(baseLocation.toString());
        this.baseLocation = baseLocation;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return NewTinkerTexture.isScoutArmorLayerPath(baseLocation.getPath());
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location,
                        Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        ResourceLocation file = NewTinkerTexture.getScoutArmorSource(baseLocation);

        try (IResource resource = manager.getResource(file)) {
            BufferedImage image = ImageIO.read(resource.getInputStream());
            if (image == null) {
                setTransparentFrame();
                return false;
            }

            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
            int[] rawPixels = Arrays.copyOf(pixels, pixels.length);

            NewTinkerTexture.applyScoutArmorMask(baseLocation.getPath(), pixels, width, height);
            NewTinkerTexture.applyScoutArmorRawPixels(baseLocation.getPath(), rawPixels, pixels, width, height);

            setIconWidth(width);
            setIconHeight(height);
            int[][] frameData = new int[NewTinkerTexture.getMipLevelCount(width, height)][];
            frameData[0] = pixels;
            framesTextureData = Lists.newArrayList();
            framesTextureData.add(frameData);
        } catch (IOException e) {
            setTransparentFrame();
        }

        return false;
    }

    private void setTransparentFrame() {
        setIconWidth(1);
        setIconHeight(1);
        framesTextureData = Lists.newArrayList();
        framesTextureData.add(new int[][] {new int[] {0}});
    }
}
