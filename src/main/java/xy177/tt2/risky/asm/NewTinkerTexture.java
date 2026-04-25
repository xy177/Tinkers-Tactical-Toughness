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
        ResourceLocation file = new ResourceLocation(
            baseLocation.getNamespace(),
            "textures/" + baseLocation.getPath() + ".png"
        );

        try (IResource resource = manager.getResource(file)) {
            BufferedImage image = ImageIO.read(resource.getInputStream());
            if (image == null) {
                width.set(texture, 1);
                height.set(texture, 1);
                iconWidth.set(texture, 1);
                iconHeight.set(texture, 1);
                return false;
            }

            width.set(texture, image.getWidth());
            height.set(texture, image.getHeight());
            iconWidth.set(texture, image.getWidth());
            iconHeight.set(texture, image.getHeight());

            int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
            processData.invoke(texture, (Object) pixels);

            int[][] frameData = new int[getMipLevelCount(image.getWidth(), image.getHeight())][];
            frameData[0] = pixels;

            List<int[][]> tinkerTextureData = (List<int[][]>) framesTextureData.get(texture);
            tinkerTextureData.add(frameData);
            return false;
        } catch (Exception e) {
            System.out.println("Failed to load armor texture directly: " + file);
            e.printStackTrace();
            width.set(texture, 1);
            height.set(texture, 1);
            iconWidth.set(texture, 1);
            iconHeight.set(texture, 1);
            return false;
        }
    }

    private static int getMipLevelCount(int width, int height) {
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
}
