package xy177.tt2.risky.asm;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import slimeknights.tconstruct.library.client.texture.MetalTextureTexture;
import slimeknights.tconstruct.library.client.texture.TextureColoredTexture;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class NewTinkerTexture {
    public static Field framesTextureData;
    public static Field width, height;
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
            ResourceLocation backUp = (ResourceLocation) backupTextureLocation.get(texture);
            if (backUp == null) return false;
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
                return false;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println("Failed");
            e.printStackTrace();
            fail_token = true;
            return false;
        }
    }

    public static Field texture2;

    @SuppressWarnings({"unused"})
    public static void processData(Object texture, int[] data) {
        if (fail_token) return;
        if (texture2 == null) {
            try {
                Class<?> clazz = Class.forName("slimeknights.tconstruct.library.client.texture.MetalTextureTexture");
                texture2 = clazz.getDeclaredField("texture2");
                texture2.setAccessible(true);
            } catch (ClassNotFoundException | SecurityException | NoSuchFieldException e) {
                System.out.println("Lazy Initialize MetalTextureTexture failed,Tinker Construct texture won't show correctly...");
                fail_token = true;
            }
        }
        try {
            TextureColoredTexture got = (TextureColoredTexture) texture2.get(texture);
            processData.invoke(got, (Object) data);
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println("Failed");
            e.printStackTrace();
            fail_token = true;
        }
    }
}
