package xy177.tt2.client.texture;

import c4.conarm.common.armor.modifiers.ModPolished;
import c4.conarm.lib.client.IArmorMaterialTexture;
import c4.conarm.lib.modifiers.IArmorModelModifier;
import c4.conarm.lib.tinkering.TinkersArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.modifiers.ModIncognito;
import xy177.tt2.TT2;
import xy177.tt2.risky.asm.NewTinkerTexture;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public final class ScoutTmtArmorTextures implements IResourceManagerReloadListener {

    public static final ScoutTmtArmorTextures INSTANCE = new ScoutTmtArmorTextures();

    private static final int MAX_CACHE_SIZE = 256;
    private static final ResourceLocation SOURCE_TEXTURE = new ResourceLocation(
        TT2.MOD_ID,
        "textures/models/armor/scout/armor_full.png"
    );
    private static final ResourceLocation POLISHED_TEMPLATE = new ResourceLocation(
        "conarm",
        "models/modifiers/mod_polished_armor"
    );

    private static final Method GET_RESOURCE_MANAGER = findMethod(
        Minecraft.class,
        new Class<?>[0],
        "getResourceManager",
        "func_110442_L"
    );
    private static final Method GET_TEXTURE_MAP = findMethod(
        Minecraft.class,
        new Class<?>[0],
        "getTextureMapBlocks",
        "func_147117_R"
    );
    private static final Method GET_TEXTURE_MANAGER = findMethod(
        Minecraft.class,
        new Class<?>[0],
        "getTextureManager",
        "func_110434_K"
    );
    private static final Method GET_ATLAS_SPRITE = findMethod(
        TextureMap.class,
        new Class<?>[] {String.class},
        "getAtlasSprite",
        "func_110572_b"
    );
    private static final Method GET_MISSING_SPRITE = findMethod(
        TextureMap.class,
        new Class<?>[0],
        "getMissingSprite",
        "func_174944_f"
    );
    private static final Method GET_SPRITE_NAME = findMethod(
        TextureAtlasSprite.class,
        new Class<?>[0],
        "getIconName",
        "func_94215_i"
    );
    private static final Method MAKE_PNG_SIZE_INFO = findMethod(
        PngSizeInfo.class,
        new Class<?>[] {IResource.class},
        "makeFromResource",
        "func_188532_a"
    );
    private static final Method GET_RESOURCE_METADATA = findMethod(
        IResource.class,
        new Class<?>[] {String.class},
        "getMetadata",
        "func_110526_a"
    );
    private static final Method LOAD_SPRITE = findMethod(
        TextureAtlasSprite.class,
        new Class<?>[] {PngSizeInfo.class, boolean.class},
        "loadSprite",
        "func_188538_a"
    );
    private static final Method LOAD_SPRITE_FRAMES = findMethod(
        TextureAtlasSprite.class,
        new Class<?>[] {IResource.class, int.class},
        "loadSpriteFrames",
        "func_188539_a"
    );
    private static final Method LOAD_TEXTURE = findMethod(
        TextureManager.class,
        new Class<?>[] {ResourceLocation.class, ITextureObject.class},
        "loadTexture",
        "func_110579_a"
    );
    private static final Method DELETE_TEXTURE = findMethod(
        TextureManager.class,
        new Class<?>[] {ResourceLocation.class},
        "deleteTexture",
        "func_147645_c"
    );
    private static final Method GET_GL_TEXTURE_ID = findMethod(
        AbstractTexture.class,
        new Class<?>[0],
        "getGlTextureId",
        "func_110552_b"
    );
    private static final Method ALLOCATE_TEXTURE = findMethod(
        TextureUtil.class,
        new Class<?>[] {int.class, int.class, int.class},
        "allocateTexture",
        "func_110991_a"
    );
    private static final Method UPLOAD_TEXTURE = findMethod(
        TextureUtil.class,
        new Class<?>[] {int.class, int[].class, int.class, int.class},
        "uploadTexture",
        "func_110988_a"
    );

    private final LinkedHashMap<CacheKey, CacheEntry> cache = new LinkedHashMap<>(16, 0.75f, true);
    private final Set<ResourceLocation> ownedTextures = new LinkedHashSet<>();
    private long nextTextureId;
    private boolean reloadListenerRegistered;
    private boolean failureLogged;

    private ScoutTmtArmorTextures() {
    }

    public synchronized String getTexture(ItemStack stack, TinkersArmor armor) {
        CacheKey key = new CacheKey(stack);
        CacheEntry cached = cache.get(key);
        if (cached != null) {
            return cached.location.toString();
        }

        try {
            ResourceLocation created = compose(stack.copy(), armor);
            cache.put(key, new CacheEntry(created, true));
            trimCache();
            return created.toString();
        } catch (Exception e) {
            if (!failureLogged) {
                TT2.logger.error("Failed to build the Too Many Tinkers scout armor texture", e);
                failureLogged = true;
            }
            cache.put(key, CacheEntry.FALLBACK);
            trimCache();
            return SOURCE_TEXTURE.toString();
        }
    }

    public synchronized void registerReloadListener() {
        if (reloadListenerRegistered) {
            return;
        }
        try {
            IResourceManager manager = getResourceManager();
            if (manager instanceof IReloadableResourceManager) {
                ((IReloadableResourceManager) manager).registerReloadListener(this);
                reloadListenerRegistered = true;
            }
        } catch (ReflectiveOperationException e) {
            TT2.logger.error("Unable to register the TMT scout armor resource listener", e);
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        clear();
    }

    public synchronized void clear() {
        cache.clear();
        for (ResourceLocation location : new ArrayList<>(ownedTextures)) {
            release(location);
        }
        failureLogged = false;
    }

    private ResourceLocation compose(ItemStack stack, TinkersArmor armor) throws Exception {
        Context context = new Context(getResourceManager(), getTextureMap());
        List<BufferedImage> layers = new ArrayList<>();
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(stack));

        for (int i = 0; i < materials.size(); i++) {
            String type = getArmorPartType(i, materials.size());
            ResourceLocation base = new ResourceLocation(armor.getArmorModelTexture(stack, type));
            BufferedImage layer = createMaterialImage(context, base, materials.get(i));
            if (layer != null) {
                layers.add(layer);
            }
        }

        if (layers.isEmpty()) {
            layers.add(copyImage(context.source));
        }
        appendModifierImages(context, stack, layers);

        int width = context.source.getWidth();
        int height = context.source.getHeight();
        for (BufferedImage layer : layers) {
            width = Math.max(width, layer.getWidth());
            height = Math.max(height, layer.getHeight());
        }
        if (width <= 0 || height <= 0) {
            throw new IOException("Invalid scout armor texture dimensions");
        }

        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = combined.createGraphics();
        try {
            for (BufferedImage layer : layers) {
                graphics.drawImage(layer, 0, 0, null);
            }
        } finally {
            graphics.dispose();
        }

        int[] pixels = new int[width * height];
        combined.getRGB(0, 0, width, height, pixels, 0, width);
        ResourceLocation location = new ResourceLocation(
            TT2.MOD_ID,
            "dynamic/scout_tmt_" + (++nextTextureId)
        );
        TextureManager manager = getTextureManager();
        OneShotTexture texture = new OneShotTexture(pixels, width, height);
        boolean loaded;
        try {
            loaded = (Boolean) LOAD_TEXTURE.invoke(manager, location, texture);
        } catch (Exception e) {
            texture.discard();
            throw e;
        }
        if (!loaded) {
            texture.discard();
            throw new IOException("Minecraft rejected the generated scout armor texture");
        }
        ownedTextures.add(location);
        if (texture.uploadFailure != null) {
            release(location);
            throw texture.uploadFailure;
        }
        return location;
    }

    private BufferedImage createMaterialImage(Context context, ResourceLocation base, Material material)
        throws Exception {
        ResourceLocation output = new ResourceLocation(base.toString() + "_" + material.identifier);
        BufferedImage direct = context.loadAtlasOrResource(output);
        if (direct != null) {
            return copyImage(direct);
        }

        MaterialRenderInfo renderInfo = material.renderInfo;
        if (renderInfo == null) {
            return null;
        }

        ResourceLocation effectiveBase = base;
        String suffix = renderInfo.getTextureSuffix();
        if (suffix != null) {
            ResourceLocation suffixed = new ResourceLocation(base.toString() + "_" + suffix);
            if (context.loadAtlasOrResource(suffixed) != null) {
                effectiveBase = suffixed;
            }
        }

        if (renderInfo.useVertexColoring()) {
            BufferedImage image = context.loadBaseLayer(effectiveBase);
            if (image == null) {
                return null;
            }
            applyVertexColor(image, renderInfo.getVertexColor());
            restoreRawPixels(context, base, image);
            return image;
        }

        TextureAtlasSprite sprite = renderInfo.getTexture(effectiveBase, output.toString());
        if (sprite == null) {
            return null;
        }
        if (!hasFrame(sprite)) {
            String spriteName = (String) GET_SPRITE_NAME.invoke(sprite);
            ResourceLocation file = textureFile(
                spriteName == null || spriteName.isEmpty() ? output : new ResourceLocation(spriteName)
            );
            if (!sprite.hasCustomLoader(context.resources, file)) {
                return null;
            }
            Function<ResourceLocation, TextureAtlasSprite> resolver = dependency -> context.resolve(dependency);
            sprite.load(context.resources, file, resolver);
        }

        BufferedImage image = imageFromSprite(sprite);
        if (image != null) {
            restoreRawPixels(context, base, image);
        }
        return image;
    }

    private void appendModifierImages(Context context, ItemStack stack, List<BufferedImage> layers)
        throws Exception {
        List<IModifier> modifiers = TinkerUtil.getModifiers(stack);
        for (IModifier modifier : modifiers) {
            if (modifier instanceof ModIncognito) {
                return;
            }
        }

        for (IModifier modifier : modifiers) {
            String textureName = null;
            if (modifier instanceof IArmorMaterialTexture) {
                textureName = ((IArmorMaterialTexture) modifier).getBaseTexture();
            } else if (modifier instanceof IArmorModelModifier) {
                textureName = ((IArmorModelModifier) modifier).getModelTextureLocation()
                    + "_" + modifier.getIdentifier();
            }
            if (textureName == null) {
                continue;
            }

            ResourceLocation location = new ResourceLocation(textureName);
            BufferedImage image = context.loadAtlasOrResource(location);
            if (image == null && modifier instanceof ModPolished) {
                image = createMaterialImage(context, POLISHED_TEMPLATE, ((ModPolished) modifier).material);
            }
            if (image != null) {
                layers.add(image);
            }
        }
    }

    private static String getArmorPartType(int index, int materialCount) {
        switch (index) {
            case 0:
                return c4.conarm.lib.materials.ArmorMaterialType.CORE;
            case 1:
                return c4.conarm.lib.materials.ArmorMaterialType.PLATES;
            case 2:
                return materialCount > 3
                    ? c4.conarm.lib.materials.ArmorMaterialType.PLATES
                    : c4.conarm.lib.materials.ArmorMaterialType.TRIM;
            default:
                return c4.conarm.lib.materials.ArmorMaterialType.TRIM;
        }
    }

    private static void applyVertexColor(BufferedImage image, int color) {
        int alpha = color >>> 24;
        if (alpha == 0) {
            alpha = 255;
        }
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;

        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int outAlpha = ((pixel >>> 24) & 0xFF) * alpha / 255;
            int outRed = ((pixel >>> 16) & 0xFF) * red / 255;
            int outGreen = ((pixel >>> 8) & 0xFF) * green / 255;
            int outBlue = (pixel & 0xFF) * blue / 255;
            pixels[i] = (outAlpha << 24) | (outRed << 16) | (outGreen << 8) | outBlue;
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);
    }

    private static void restoreRawPixels(Context context, ResourceLocation base, BufferedImage image) {
        if (image.getWidth() != context.source.getWidth() || image.getHeight() != context.source.getHeight()) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        NewTinkerTexture.applyScoutArmorRawPixels(
            resourcePath(base),
            context.sourcePixels,
            pixels,
            width,
            height
        );
        image.setRGB(0, 0, width, height, pixels, 0, width);
    }

    private static BufferedImage imageFromSprite(TextureAtlasSprite sprite) {
        if (sprite == null) {
            return null;
        }
        int width = NewTinkerTexture.getSpriteWidth(sprite);
        int height = NewTinkerTexture.getSpriteHeight(sprite);
        int[][] frame = NewTinkerTexture.getSpriteFrame(sprite, 0);
        if (width <= 0 || height <= 0 || NewTinkerTexture.getSpriteFrameCount(sprite) <= 0
            || frame == null || frame.length == 0 || frame[0] == null
            || frame[0].length < width * height) {
            return null;
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        image.setRGB(0, 0, width, height, frame[0], 0, width);
        return image;
    }

    private static boolean hasFrame(TextureAtlasSprite sprite) {
        return sprite != null
            && NewTinkerTexture.getSpriteWidth(sprite) > 0
            && NewTinkerTexture.getSpriteHeight(sprite) > 0
            && NewTinkerTexture.getSpriteFrameCount(sprite) > 0
            && NewTinkerTexture.getSpriteFrame(sprite, 0) != null;
    }

    private static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = copy.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return copy;
    }

    private synchronized void trimCache() {
        while (cache.size() > MAX_CACHE_SIZE) {
            Map.Entry<CacheKey, CacheEntry> eldest = cache.entrySet().iterator().next();
            CacheEntry entry = eldest.getValue();
            cache.remove(eldest.getKey());
            if (entry.ownedDynamic) {
                release(entry.location);
            }
        }
    }

    private void release(ResourceLocation location) {
        try {
            DELETE_TEXTURE.invoke(getTextureManager(), location);
            ownedTextures.remove(location);
        } catch (ReflectiveOperationException e) {
            TT2.logger.error("Unable to release a cached TMT scout armor texture", e);
        }
    }

    private static IResourceManager getResourceManager() throws ReflectiveOperationException {
        return (IResourceManager) GET_RESOURCE_MANAGER.invoke(Minecraft.getMinecraft());
    }

    private static TextureMap getTextureMap() throws ReflectiveOperationException {
        return (TextureMap) GET_TEXTURE_MAP.invoke(Minecraft.getMinecraft());
    }

    private static TextureManager getTextureManager() throws ReflectiveOperationException {
        return (TextureManager) GET_TEXTURE_MANAGER.invoke(Minecraft.getMinecraft());
    }

    private static Method findMethod(Class<?> owner, Class<?>[] parameterTypes, String... names) {
        for (String name : names) {
            try {
                Method method = owner.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new ExceptionInInitializerError(
            owner.getName() + " does not contain any of " + Arrays.toString(names)
        );
    }

    private static ResourceLocation textureFile(ResourceLocation sprite) {
        String value = sprite.toString();
        int separator = value.indexOf(':');
        String namespace = separator < 0 ? "minecraft" : value.substring(0, separator);
        String path = separator < 0 ? value : value.substring(separator + 1);
        if (!path.startsWith("textures/")) {
            path = "textures/" + path;
        }
        if (!path.endsWith(".png")) {
            path += ".png";
        }
        return new ResourceLocation(namespace, path);
    }

    private static String resourcePath(ResourceLocation location) {
        String value = location.toString();
        int separator = value.indexOf(':');
        return separator < 0 ? value : value.substring(separator + 1);
    }

    private static final class Context {

        private final IResourceManager resources;
        private final TextureMap atlas;
        private final BufferedImage source;
        private final int[] sourcePixels;
        private final Map<String, BufferedImage> resourceImages = new LinkedHashMap<>();

        private Context(IResourceManager resources, TextureMap atlas) throws Exception {
            this.resources = resources;
            this.atlas = atlas;
            this.source = loadImage(resources, SOURCE_TEXTURE);
            if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
                throw new IOException("Missing scout armor source texture: " + SOURCE_TEXTURE);
            }
            this.sourcePixels = new int[source.getWidth() * source.getHeight()];
            source.getRGB(0, 0, source.getWidth(), source.getHeight(), sourcePixels, 0, source.getWidth());
        }

        private BufferedImage loadBaseLayer(ResourceLocation base) throws Exception {
            BufferedImage uploaded = imageFromSprite(getUploadedSprite(atlas, base));
            if (uploaded != null) {
                return uploaded;
            }
            BufferedImage physical = loadSpriteResource(base);
            if (physical != null) {
                return copyImage(physical);
            }
            if (!NewTinkerTexture.isScoutArmorLayerPath(resourcePath(base))) {
                return null;
            }
            BufferedImage layer = copyImage(source);
            int width = layer.getWidth();
            int height = layer.getHeight();
            int[] pixels = new int[width * height];
            layer.getRGB(0, 0, width, height, pixels, 0, width);
            NewTinkerTexture.applyScoutArmorMask(resourcePath(base), pixels, width, height);
            layer.setRGB(0, 0, width, height, pixels, 0, width);
            return layer;
        }

        private BufferedImage loadAtlasOrResource(ResourceLocation location) throws Exception {
            TextureAtlasSprite sprite = getUploadedSprite(atlas, location);
            BufferedImage image = imageFromSprite(sprite);
            return image != null ? image : loadSpriteResource(location);
        }

        private TextureAtlasSprite resolve(ResourceLocation location) {
            try {
                TextureAtlasSprite uploaded = getUploadedSprite(atlas, location);
                if (hasFrame(uploaded)) {
                    return uploaded;
                }
                return loadResourceSprite(resources, location);
            } catch (Exception e) {
                return null;
            }
        }

        private BufferedImage loadSpriteResource(ResourceLocation sprite) throws Exception {
            ResourceLocation file = textureFile(sprite);
            String key = file.toString();
            if (resourceImages.containsKey(key)) {
                return resourceImages.get(key);
            }
            BufferedImage image = imageFromSprite(loadResourceSprite(resources, sprite));
            resourceImages.put(key, image);
            return image;
        }
    }

    private static TextureAtlasSprite getUploadedSprite(TextureMap atlas, ResourceLocation location)
        throws InvocationTargetException, IllegalAccessException {
        TextureAtlasSprite sprite = (TextureAtlasSprite) GET_ATLAS_SPRITE.invoke(atlas, location.toString());
        TextureAtlasSprite missing = (TextureAtlasSprite) GET_MISSING_SPRITE.invoke(atlas);
        return sprite == missing ? null : sprite;
    }

    private static BufferedImage loadImage(IResourceManager manager, ResourceLocation file) throws Exception {
        IResource resource = openResource(manager, file);
        if (resource == null) {
            return null;
        }
        try (IResource closeable = resource) {
            InputStream stream = NewTinkerTexture.openResourceStream(resource);
            return stream == null ? null : ImageIO.read(stream);
        } catch (IOException e) {
            return null;
        }
    }

    private static TextureAtlasSprite loadResourceSprite(IResourceManager manager, ResourceLocation sprite)
        throws Exception {
        ResourceLocation file = textureFile(sprite);
        IResource sizeResource = openResource(manager, file);
        if (sizeResource == null) {
            return null;
        }

        PngSizeInfo sizeInfo = (PngSizeInfo) MAKE_PNG_SIZE_INFO.invoke(null, sizeResource);

        boolean animated;
        IResource metadataResource = openResource(manager, file);
        if (metadataResource == null) {
            return null;
        }
        try (IResource closeable = metadataResource) {
            animated = GET_RESOURCE_METADATA.invoke(metadataResource, "animation") != null;
        }

        TextureAtlasSprite result = new MemorySprite(sprite);
        LOAD_SPRITE.invoke(result, sizeInfo, animated);

        IResource frameResource = openResource(manager, file);
        if (frameResource == null) {
            return null;
        }
        try (IResource closeable = frameResource) {
            LOAD_SPRITE_FRAMES.invoke(result, frameResource, 1);
        }
        return result;
    }

    private static IResource openResource(IResourceManager manager, ResourceLocation file) throws Exception {
        try {
            return NewTinkerTexture.openResource(manager, file);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IOException) {
                return null;
            }
            throw e;
        }
    }

    private static final class MemorySprite extends TextureAtlasSprite {

        private MemorySprite(ResourceLocation location) {
            super(location.toString());
        }
    }

    private static final class OneShotTexture extends AbstractTexture {

        private int[] pixels;
        private final int width;
        private final int height;
        private IOException uploadFailure;

        private OneShotTexture(int[] pixels, int width, int height) {
            this.pixels = pixels;
            this.width = width;
            this.height = height;
        }

        @Override
        public void loadTexture(IResourceManager resourceManager) throws IOException {
            if (pixels == null) {
                return;
            }
            try {
                int textureId = (Integer) GET_GL_TEXTURE_ID.invoke(this);
                ALLOCATE_TEXTURE.invoke(null, textureId, width, height);
                UPLOAD_TEXTURE.invoke(null, textureId, pixels, width, height);
            } catch (ReflectiveOperationException e) {
                uploadFailure = new IOException("Unable to upload the TMT scout armor texture", e);
            } finally {
                pixels = null;
            }
        }

        private void discard() {
            pixels = null;
            deleteGlTexture();
        }
    }

    private static final class CacheEntry {

        private static final CacheEntry FALLBACK = new CacheEntry(SOURCE_TEXTURE, false);

        private final ResourceLocation location;
        private final boolean ownedDynamic;

        private CacheEntry(ResourceLocation location, boolean ownedDynamic) {
            this.location = location;
            this.ownedDynamic = ownedDynamic;
        }
    }

    private static final class CacheKey {

        private final String item;
        private final List<String> materials;
        private final boolean broken;
        private final List<String> modifiers;

        private CacheKey(ItemStack stack) {
            ResourceLocation registryName = stack.getItem().getRegistryName();
            this.item = registryName == null ? stack.getItem().getClass().getName() : registryName.toString();
            this.materials = new ArrayList<>();
            for (Material material : TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(stack))) {
                materials.add(material.identifier);
            }
            this.broken = ToolHelper.isBroken(stack);
            this.modifiers = new ArrayList<>();
            for (IModifier modifier : TinkerUtil.getModifiers(stack)) {
                modifiers.add(modifier.getIdentifier());
            }
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CacheKey)) {
                return false;
            }
            CacheKey key = (CacheKey) other;
            return broken == key.broken
                && item.equals(key.item)
                && materials.equals(key.materials)
                && modifiers.equals(key.modifiers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, materials, broken, modifiers);
        }
    }
}
