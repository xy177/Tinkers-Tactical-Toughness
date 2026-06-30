package xy177.tt2.risky.asm;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import slimeknights.tconstruct.library.client.texture.MetalTextureTexture;
import slimeknights.tconstruct.library.client.texture.TextureColoredTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class NewTinkerTexture {
    public static Field framesTextureData;
    public static Field width, height;
    public static Field iconWidth, iconHeight;
    public static Field animateMeta;
    public static Field backupTextureLocation;
    public static Method copyFrom;
    public static Method processData;
    public static boolean fail_token = false;

    static {
        try {
            framesTextureData = TextureAtlasSprite.class.getDeclaredField("field_110976_a");
            framesTextureData.setAccessible(true);
            width = TextureAtlasSprite.class.getDeclaredField("field_110973_g");
            width.setAccessible(true);
            height = TextureAtlasSprite.class.getDeclaredField("field_110983_h");
            height.setAccessible(true);
            iconWidth = TextureAtlasSprite.class.getDeclaredField("field_130223_c");
            iconWidth.setAccessible(true);
            iconHeight = TextureAtlasSprite.class.getDeclaredField("field_130224_d");
            iconHeight.setAccessible(true);
            copyFrom = TextureAtlasSprite.class.getDeclaredMethod("func_94217_a", TextureAtlasSprite.class);
            copyFrom.setAccessible(true);
            animateMeta = TextureAtlasSprite.class.getDeclaredField("field_110982_k");
            animateMeta.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException | SecurityException e) {
            System.out.println("Initialize failed,Tinker Construct texture won't show correctly...");
            e.printStackTrace();
            fail_token = true;
        }
    }

    @SuppressWarnings({"unused", "SameReturnValue"})
    public static boolean load(Object texture, IResourceManager manager, ResourceLocation location,
                               Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        if (fail_token) return false;
        if (backupTextureLocation == null) {
            try {
                Class<?> clazz = Class.forName("slimeknights.tconstruct.library.client.texture.AbstractColoredTexture");
                if (clazz.getDeclaredFields().length != 1) {
                    System.out.println(
                        "Class loading invalid,AbstractColoredTexture only have 1 field in source,actual " +
                        clazz.getDeclaredFields().length + " in game"
                    );
                    fail_token = true;
                    return false;
                }
                backupTextureLocation = clazz.getDeclaredFields()[0];
                backupTextureLocation.setAccessible(true);
                processData = clazz.getDeclaredMethod("processData", int[].class);
                if (processData == null) {
                    System.out.println(
                        "Class loading invalid,AbstractColoredTexture haven't processData(int[]) in game."
                    );
                    fail_token = true;
                    return false;
                }
                processData.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                System.out.println("Lazy Initialize failed,Tinker Construct texture won't show correctly...");
                e.printStackTrace();
                fail_token = true;
            }
        }
        try {
            framesTextureData.set(texture, Lists.newArrayList());
            width.set(texture, 0);
            height.set(texture, 0);
            iconWidth.set(texture, 0);
            iconHeight.set(texture, 0);
            ResourceLocation backUp = (ResourceLocation) backupTextureLocation.get(texture);
            if (backUp == null) return false;

            if (backUp.toString().contains("models/armor/")) {
                return loadArmorTexture(texture, manager, backUp);
            }

            TextureAtlasSprite baseTexture = textureGetter.apply(backUp);
            if (baseTexture != null && baseTexture.getFrameCount() > 0) {
                copyFrom.invoke(texture, baseTexture);
                @SuppressWarnings("unchecked")
                List<int[][]> tinkerTextureData = ((List<int[][]>) framesTextureData.get(texture));
                for (int i = 0; i < baseTexture.getFrameCount(); i++) {
                    int[][] original = baseTexture.getFrameTextureData(i);
                    int[][] data = new int[original.length][];
                    data[0] = Arrays.copyOf(original[0], original[0].length);
                    processData.invoke(texture, ((Object) data[0]));
                    tinkerTextureData.add(data);
                }
                animateMeta.set(texture, animateMeta.get(baseTexture));
                return false;
            } else {
                width.set(texture, 1);
                height.set(texture, 1);
                iconWidth.set(texture, 1);
                iconHeight.set(texture, 1);
                return false;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println("Failed");
            e.printStackTrace();
            fail_token = true;
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean loadArmorTexture(Object texture, IResourceManager manager, ResourceLocation baseLocation)
        throws IllegalAccessException, InvocationTargetException {
        ResourceLocation file = getScoutArmorSource(baseLocation);

        try (IResource resource = manager.getResource(file)) {
            BufferedImage image = ImageIO.read(resource.getInputStream());
            if (image == null) {
                setTransparentTexture(texture);
                return false;
            }

            width.set(texture, image.getWidth());
            height.set(texture, image.getHeight());
            iconWidth.set(texture, image.getWidth());
            iconHeight.set(texture, image.getHeight());

            int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
            int[] rawPixels = Arrays.copyOf(pixels, pixels.length);
            applyScoutArmorMask(baseLocation.getPath(), pixels, image.getWidth(), image.getHeight());
            processData.invoke(texture, (Object) pixels);
            applyScoutArmorRawPixels(baseLocation.getPath(), rawPixels, pixels, image.getWidth(), image.getHeight());

            int[][] frameData = new int[getMipLevelCount(image.getWidth(), image.getHeight())][];
            frameData[0] = pixels;

            List<int[][]> tinkerTextureData = (List<int[][]>) framesTextureData.get(texture);
            tinkerTextureData.add(frameData);
            return false;
        } catch (Exception e) {
            System.out.println("Failed to load armor texture directly: " + file);
            e.printStackTrace();
            setTransparentTexture(texture);
            return false;
        }
    }

    public static ResourceLocation getScoutArmorSource(ResourceLocation location) {
        if (isScoutArmorLayerPath(location.getPath())) {
            return new ResourceLocation(location.getNamespace(), "textures/models/armor/scout/armor_full.png");
        }
        return new ResourceLocation(location.getNamespace(), "textures/" + location.getPath() + ".png");
    }

    public static boolean isScoutArmorLayerPath(String path) {
        return path.startsWith("models/armor/scout/armor_full_");
    }

    public static void applyScoutArmorMask(String path, int[] pixels, int width, int height) {
        int[][] uvs = getScoutArmorUvs(path);
        if (uvs == null) {
            return;
        }

        boolean[] visible = new boolean[pixels.length];
        for (int[] uv : uvs) {
            int x1 = Math.max(0, Math.min(width, Math.min(uv[0], uv[2])));
            int y1 = Math.max(0, Math.min(height, Math.min(uv[1], uv[3])));
            int x2 = Math.max(0, Math.min(width, Math.max(uv[0], uv[2])));
            int y2 = Math.max(0, Math.min(height, Math.max(uv[1], uv[3])));
            for (int y = y1; y < y2; y++) {
                int offset = y * width;
                for (int x = x1; x < x2; x++) {
                    visible[offset + x] = true;
                }
            }
        }

        for (int i = 0; i < pixels.length; i++) {
            if (!visible[i]) {
                pixels[i] = 0;
            }
        }
    }

    private static int[][] getScoutArmorUvs(String path) {
        if (isScoutArmorLayer(path, "leg_core")) {
            return SCOUT_LEG_CORE_UVS;
        }
        if (isScoutArmorLayer(path, "leg_plate1")) {
            return SCOUT_LEG_PLATE1_UVS;
        }
        if (isScoutArmorLayer(path, "leg_plate2")) {
            return SCOUT_LEG_PLATE2_UVS;
        }
        if (isScoutArmorLayer(path, "core") || isScoutArmorLayer(path, "core_broken")) {
            return SCOUT_CORE_UVS;
        }
        if (isScoutArmorLayer(path, "plate1")) {
            return SCOUT_PLATE1_UVS;
        }
        if (isScoutArmorLayer(path, "plate2")) {
            return SCOUT_PLATE2_UVS;
        }
        return null;
    }

    private static boolean isScoutArmorLayer(String path, String layer) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        String prefix = "armor_full_" + layer;
        return name.equals(prefix) || name.startsWith(prefix + "_");
    }

    @SuppressWarnings("unchecked")
    private static void setTransparentTexture(Object texture) {
        try {
            width.set(texture, 1);
            height.set(texture, 1);
            iconWidth.set(texture, 1);
            iconHeight.set(texture, 1);

            List<int[][]> textureData = (List<int[][]>) framesTextureData.get(texture);
            if (textureData == null) {
                textureData = Lists.newArrayList();
                framesTextureData.set(texture, textureData);
            } else {
                textureData.clear();
            }

            int[][] frameData = new int[1][];
            frameData[0] = new int[] {0};
            textureData.add(frameData);
        } catch (IllegalAccessException ignored) {
        }
    }

    public static int getMipLevelCount(int width, int height) {
        int size = Math.max(width, height);
        int levels = 1;
        while (size > 1) {
            size >>= 1;
            levels++;
        }
        return levels;
    }

    public static Field texture2;
    public static Field addTexture;

    @SuppressWarnings({"unused"})
    public static void processData(Object texture, int[] data) {
        if (fail_token) return;
        if (texture2 == null || addTexture == null) {
            try {
                Class<?> clazz = Class.forName("slimeknights.tconstruct.library.client.texture.MetalTextureTexture");
                texture2 = clazz.getDeclaredField("texture2");
                texture2.setAccessible(true);
                Class<?> texturedClazz = Class.forName("slimeknights.tconstruct.library.client.texture.TextureColoredTexture");
                addTexture = texturedClazz.getDeclaredField("addTexture");
                addTexture.setAccessible(true);
            } catch (ClassNotFoundException | SecurityException | NoSuchFieldException e) {
                System.out.println("Lazy Initialize MetalTextureTexture failed,Tinker Construct texture won't show correctly...");
                fail_token = true;
            }
        }
        try {
            if (isScoutArmorTexture(texture)) {
                applyScoutArmorOverlay(texture, data);
                return;
            }
            TextureColoredTexture got = (TextureColoredTexture) texture2.get(texture);
            processData.invoke(got, (Object) data);
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println("Failed");
            e.printStackTrace();
            fail_token = true;
        }
    }

    private static void applyScoutArmorOverlay(Object texture, int[] data) throws IllegalAccessException {
        TextureColoredTexture overlayTexture = (TextureColoredTexture) texture2.get(texture);
        if (overlayTexture == null) {
            return;
        }

        TextureAtlasSprite overlaySprite = (TextureAtlasSprite) addTexture.get(overlayTexture);
        if (overlaySprite == null || overlaySprite.getFrameCount() <= 0) {
            return;
        }

        int[][] frame = overlaySprite.getFrameTextureData(0);
        if (frame == null || frame.length == 0 || frame[0] == null) {
            return;
        }

        int baseWidth = (Integer) iconWidth.get(texture);
        int baseHeight = (Integer) iconHeight.get(texture);
        int overlayWidth = overlaySprite.getIconWidth();
        int overlayHeight = overlaySprite.getIconHeight();
        if (baseWidth <= 0 || baseHeight <= 0 || overlayWidth <= 0 || overlayHeight <= 0) {
            return;
        }

        int[] overlayPixels = frame[0];
        for (int i = 0; i < data.length; i++) {
            int pixel = data[i];
            int alpha = (pixel >>> 24) & 0xFF;
            if (alpha == 0) {
                continue;
            }

            int x = i % baseWidth;
            int y = i / baseWidth;
            int mappedX = Math.min(overlayWidth - 1, x * overlayWidth / baseWidth);
            int mappedY = Math.min(overlayHeight - 1, y * overlayHeight / baseHeight);
            int overlayPixel = overlayPixels[mappedY * overlayWidth + mappedX];

            int red = mult(mult((overlayPixel >>> 16) & 0xFF, (pixel >>> 16) & 0xFF), (pixel >>> 16) & 0xFF);
            int green = mult(mult((overlayPixel >>> 8) & 0xFF, (pixel >>> 8) & 0xFF), (pixel >>> 8) & 0xFF);
            int blue = mult(mult(overlayPixel & 0xFF, pixel & 0xFF), pixel & 0xFF);
            data[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

    }

    public static void applyScoutArmorRawPixels(String path, int[] rawPixels, int[] data, int width, int height) {
        if (!isScoutArmorLayer(path, "plate1")) {
            return;
        }

        for (int[] uv : SCOUT_RAW_UVS) {
            int x1 = Math.max(0, Math.min(width, Math.min(uv[0], uv[2])));
            int y1 = Math.max(0, Math.min(height, Math.min(uv[1], uv[3])));
            int x2 = Math.max(0, Math.min(width, Math.max(uv[0], uv[2])));
            int y2 = Math.max(0, Math.min(height, Math.max(uv[1], uv[3])));
            for (int y = y1; y < y2; y++) {
                int offset = y * width;
                for (int x = x1; x < x2; x++) {
                    int pixel = rawPixels[offset + x];
                    if (((pixel >>> 24) & 0xFF) != 0) {
                        data[offset + x] = pixel;
                    }
                }
            }
        }
    }

    private static int mult(int first, int second) {
        return (int) (first * (second / 255.0f));
    }

    private static boolean isScoutArmorTexture(Object texture) {
        if (backupTextureLocation == null) {
            return false;
        }
        try {
            ResourceLocation location = (ResourceLocation) backupTextureLocation.get(texture);
            return location != null && location.toString().contains("tt2:models/armor/scout/");
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private static final int[][] SCOUT_CORE_UVS = {
        {2, 76, 6, 80}, {0, 76, 2, 80}, {8, 76, 12, 80}, {6, 76, 8, 80},
        {2, 74, 6, 76}, {6, 74, 10, 76}, {66, 76, 70, 80}, {64, 76, 66, 80},
        {72, 76, 76, 80}, {70, 76, 72, 80}, {66, 74, 70, 76}, {70, 74, 74, 76},
        {10, 10, 20, 12}, {0, 10, 10, 12}, {30, 10, 40, 12}, {20, 10, 30, 12},
        {10, 0, 20, 10}, {20, 0, 30, 10}, {38, 34, 48, 40}, {32, 34, 38, 40},
        {54, 34, 64, 40}, {48, 34, 54, 40},
        {38, 28, 48, 34}, {48, 28, 58, 34}, {27, 59, 37, 66}, {24, 59, 27, 66},
        {40, 59, 50, 66}, {37, 59, 40, 66}, {27, 56, 37, 59}, {37, 56, 47, 59},
        {62, 46, 68, 51}, {56, 46, 62, 51}, {74, 46, 80, 51}, {68, 46, 74, 51},
        {62, 40, 68, 46}, {68, 40, 74, 46}, {70, 38, 76, 40}, {64, 38, 70, 40},
        {82, 38, 88, 40}, {76, 38, 82, 40}, {70, 32, 76, 38}, {76, 32, 82, 38},
        {70, 71, 74, 74}, {66, 71, 70, 74}, {78, 71, 82, 74}, {74, 71, 78, 74},
        {70, 67, 74, 71}, {74, 67, 78, 71}, {52, 76, 56, 79}, {48, 76, 52, 79},
        {60, 76, 64, 79}, {56, 76, 60, 79}, {52, 72, 56, 76}, {56, 72, 60, 76}
    };

    private static final int[][] SCOUT_PLATE1_UVS = {
        {40, 20, 48, 28}, {32, 20, 40, 28}, {56, 20, 64, 28}, {48, 20, 56, 28},
        {40, 12, 48, 20}, {48, 12, 56, 20}, {57, 52, 61, 56}, {56, 52, 57, 56},
        {62, 52, 66, 56}, {61, 52, 62, 56}, {57, 51, 61, 52}, {61, 51, 65, 52},
        {77, 75, 81, 79}, {76, 75, 77, 79}, {82, 75, 86, 79}, {81, 75, 82, 79},
        {77, 74, 81, 75}, {81, 74, 85, 75}, {36, 44, 44, 56}, {32, 44, 36, 56},
        {48, 44, 56, 56}, {44, 44, 48, 56}, {36, 40, 44, 44}, {44, 40, 52, 44}
    };

    private static final int[][] SCOUT_PLATE2_UVS = {
        {8, 38, 16, 46}, {0, 38, 8, 46}, {24, 38, 32, 46}, {16, 38, 24, 46},
        {8, 30, 16, 38}, {16, 30, 24, 38}, {6, 18, 16, 30}, {0, 18, 6, 30},
        {22, 18, 32, 30}, {16, 18, 22, 30}, {6, 12, 16, 18}, {16, 12, 26, 18},
        {54, 60, 58, 72}, {50, 60, 54, 72}, {62, 60, 66, 72}, {58, 60, 62, 72},
        {54, 56, 58, 60}, {58, 56, 62, 60}, {46, 6, 52, 12}, {40, 6, 46, 12},
        {58, 6, 64, 12}, {52, 6, 58, 12}, {46, 0, 52, 6}, {52, 0, 58, 6},
        {4, 62, 8, 74}, {0, 62, 4, 74}, {12, 62, 16, 74}, {8, 62, 12, 74},
        {4, 58, 8, 62}, {8, 58, 12, 62}, {6, 52, 12, 58}, {0, 52, 6, 58},
        {18, 52, 24, 58}, {12, 52, 18, 58}, {6, 46, 12, 52}, {12, 46, 18, 52},
        {70, 55, 74, 59}, {66, 55, 70, 59}, {78, 55, 82, 59}, {74, 55, 78, 59},
        {70, 51, 74, 55}, {74, 51, 78, 55}, {70, 63, 74, 67}, {66, 63, 70, 67},
        {78, 63, 82, 67}, {74, 63, 78, 67}, {70, 59, 74, 63}, {74, 59, 78, 63}
    };

    private static final int[][] SCOUT_LEG_CORE_UVS = {
        {68, 20, 72, 32}, {64, 20, 68, 32}, {76, 20, 80, 32}, {72, 20, 76, 32},
        {68, 16, 72, 20}, {72, 16, 76, 20}, {20, 70, 24, 82}, {16, 70, 20, 82},
        {28, 70, 32, 82}, {24, 70, 28, 82}, {20, 66, 24, 70}, {24, 66, 28, 70},
        {9, 92, 12, 95}, {8, 92, 9, 95}, {13, 92, 16, 95}, {12, 92, 13, 95},
        {9, 91, 12, 92}, {12, 91, 15, 92}
    };

    private static final int[][] SCOUT_LEG_PLATE1_UVS = {
        {6, 103, 16, 105}, {0, 103, 6, 105}, {22, 103, 32, 105}, {16, 103, 22, 105},
        {6, 97, 16, 103}, {16, 97, 26, 103}
    };

    private static final int[][] SCOUT_LEG_PLATE2_UVS = {
        {36, 70, 40, 82}, {32, 70, 36, 82}, {44, 70, 48, 82}, {40, 70, 44, 82},
        {36, 66, 40, 70}, {40, 66, 44, 70}, {68, 4, 72, 16}, {64, 4, 68, 16},
        {76, 4, 80, 16}, {72, 4, 76, 16}, {68, 0, 72, 4}, {72, 0, 76, 4}
    };

    private static final int[][] SCOUT_RAW_UVS = {
        {24, 46, 28, 54}
    };
}
