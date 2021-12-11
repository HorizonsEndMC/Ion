package net.starlegacy.feature.gas.collectionfactors;

import org.bukkit.Location;

public class SkyLightFactor extends CollectionFactor {
    private int minimum;
    private int maximum;

    public SkyLightFactor(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public boolean factor(Location location) {
        return location.getBlock().getLightFromSky() <= maximum && location.getBlock().getLightFromSky() >= minimum;
    }
}
