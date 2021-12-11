package net.starlegacy.feature.gas.collectionfactors;

import org.bukkit.Location;

public class HyperspaceOnlyFactor extends CollectionFactor {

    @Override
    public boolean factor(Location location) {
        return location.getWorld().getName().contains("Hyperspace");
    }
}
