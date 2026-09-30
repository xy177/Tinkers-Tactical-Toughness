package xy177.tt2.api.consumable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class ConsumableUseEvent extends Event {
    public final ItemStack tool;
    public final String identifier;
    public final int amount;

    protected ConsumableUseEvent(ItemStack tool, String identifier, int amount) {
        this.tool = tool;
        this.identifier = identifier;
        this.amount = amount;
    }

    @Cancelable public static final class Before extends ConsumableUseEvent {
        public Before(ItemStack tool, String id, int amount) { super(tool, id, amount); }
    }
    public static final class Consumed extends ConsumableUseEvent {
        public Consumed(ItemStack tool, String id, int amount) { super(tool, id, amount); }
    }
    public static final class Exhausted extends ConsumableUseEvent {
        public Exhausted(ItemStack tool, String id, int amount) { super(tool, id, amount); }
    }
    public static final class Restored extends ConsumableUseEvent {
        public Restored(ItemStack tool, String id, int amount) { super(tool, id, amount); }
    }
}
