package xy177.tt2.api.consumable;

import slimeknights.tconstruct.library.traits.AbstractTrait;

/** Register with TinkerRegistry and attach to a material as with any AbstractTrait. */
public abstract class ConsumableTrait extends AbstractTrait {
    protected ConsumableTrait(String id, int color, int maximumUses) {
        super(id, color);
        ConsumableUses.register(getIdentifier(), maximumUses, ConsumableUses.Kind.MATERIAL);
    }
}
