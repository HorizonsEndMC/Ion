package net.starlegacy.feature.gas.collectionfactors;

import org.bukkit.Location;

public class AtmosphereHeightFactor extends CollectionFactor {

    private double maxAtmosphereHeight;
    private double minAtmosphereHeight;

    public AtmosphereHeightFactor(double minAtmosphereHeight, double maxAtmosphereHeight) {
        this.minAtmosphereHeight = minAtmosphereHeight;
        this.maxAtmosphereHeight = maxAtmosphereHeight;
    }

    @Override
    public boolean factor(Location location) {
        return !new SpaceOnlyFactor().factor(location) && location.getY() >= minAtmosphereHeight && location.getY() <=
                maxAtmosphereHeight;
    }

}
