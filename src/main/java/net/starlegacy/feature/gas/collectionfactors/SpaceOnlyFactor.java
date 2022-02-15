package net.starlegacy.feature.gas.collectionfactors;

import net.starlegacy.feature.space.SpaceWorlds;
import org.bukkit.Location;

public class SpaceOnlyFactor extends CollectionFactor {

    @Override
    public boolean factor(Location location) {
        return SpaceWorlds.INSTANCE.contains(location.getWorld());
    }
}
