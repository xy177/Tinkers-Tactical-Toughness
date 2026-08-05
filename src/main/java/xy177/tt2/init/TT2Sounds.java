package xy177.tt2.init;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.utils.TagUtil;
import xy177.tt2.TT2;

import java.util.Random;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public final class TT2Sounds {

    public static final SoundEvent SPEAR_ATTACK = create("item.spear.attack");
    public static final SoundEvent SPEAR_HIT = create("item.spear.hit");
    public static final SoundEvent SPEAR_USE = create("item.spear.use");
    public static final SoundEvent SPEAR_LUNGE_1 = create("item.spear.lunge_1");
    public static final SoundEvent SPEAR_LUNGE_2 = create("item.spear.lunge_2");
    public static final SoundEvent SPEAR_LUNGE_3 = create("item.spear.lunge_3");
    public static final SoundEvent SPEAR_WOOD_ATTACK = create("item.spear_wood.attack");
    public static final SoundEvent SPEAR_WOOD_HIT = create("item.spear_wood.hit");
    public static final SoundEvent SPEAR_WOOD_USE = create("item.spear_wood.use");

    private TT2Sounds() {
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
            SPEAR_ATTACK,
            SPEAR_HIT,
            SPEAR_USE,
            SPEAR_LUNGE_1,
            SPEAR_LUNGE_2,
            SPEAR_LUNGE_3,
            SPEAR_WOOD_ATTACK,
            SPEAR_WOOD_HIT,
            SPEAR_WOOD_USE
        );
    }

    public static SoundEvent spearAttack(ItemStack stack) {
        return hasWoodHead(stack) ? SPEAR_WOOD_ATTACK : SPEAR_ATTACK;
    }

    public static SoundEvent spearHit(ItemStack stack) {
        return hasWoodHead(stack) ? SPEAR_WOOD_HIT : SPEAR_HIT;
    }

    public static SoundEvent spearUse(ItemStack stack) {
        return hasWoodHead(stack) ? SPEAR_WOOD_USE : SPEAR_USE;
    }

    public static SoundEvent randomSpearLunge(Random random) {
        switch (random.nextInt(3)) {
            case 0:
                return SPEAR_LUNGE_1;
            case 1:
                return SPEAR_LUNGE_2;
            default:
                return SPEAR_LUNGE_3;
        }
    }

    private static SoundEvent create(String name) {
        ResourceLocation id = new ResourceLocation(TT2.MOD_ID, name);
        SoundEvent sound = new SoundEvent(id);
        sound.setRegistryName(id);
        return sound;
    }

    private static boolean hasWoodHead(ItemStack stack) {
        try {
            NBTTagList materials = TagUtil.getBaseMaterialsTagList(stack);
            return materials.tagCount() > 0 && "wood".equals(materials.getStringTagAt(0));
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
