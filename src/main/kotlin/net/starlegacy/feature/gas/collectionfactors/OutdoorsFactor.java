package net.starlegacy.feature.gas.collectionfactors;

import net.starlegacy.util.LegacyBlockUtils;
import org.bukkit.Location;

public class OutdoorsFactor extends CollectionFactor {
    @Override
    public boolean factor(Location location) {
        return !LegacyBlockUtils.INSTANCE.isInside(location, 2);
    }
}
