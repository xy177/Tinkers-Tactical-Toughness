package xy177.tt2.api.consumable;

import slimeknights.tconstruct.library.modifiers.ModifierTrait;

public abstract class ConsumableModifier extends ModifierTrait {
    protected ConsumableModifier(String id, int color, int maxLevel, int countPerLevel, int maximumUses) {
        super(id, color, maxLevel, countPerLevel);
        ConsumableUses.register(getIdentifier(), maximumUses, ConsumableUses.Kind.MODIFIER);
    }
}
